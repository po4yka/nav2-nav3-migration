package com.example.navigationlab.engine

import android.os.SystemClock
import android.util.Log
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabTraceEvent
import com.example.navigationlab.contracts.TraceEventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Read-only view of the trace store, exposing only observation capabilities.
 *
 * External consumers (e.g., UI layers) should depend on this interface
 * rather than the full [LabTraceStore], which additionally exposes
 * mutation methods ([LabTraceStore.record], [LabTraceStore.clear],
 * [LabTraceStore.startCase]).
 */
interface ReadableTraceStore {
    /** Monotonically increasing version counter; collect this to react to new events. */
    val eventVersion: StateFlow<Long>

    /** Return an immutable snapshot of the current trace events. */
    fun snapshot(): List<LabTraceEvent>

    /** Export the current snapshot as a JSON string. */
    fun exportSnapshotJson(pretty: Boolean = true): String
}

/**
 * In-memory store for trace events captured during lab scenario execution.
 * Observable via [eventVersion] StateFlow for live UI updates.
 *
 * External code should prefer the [ReadableTraceStore] interface for
 * read-only access. Mutation methods are intended for use within the
 * `lab-engine` module only.
 */
class LabTraceStore(
    private val maxEvents: Int = DEFAULT_MAX_EVENTS,
) : ReadableTraceStore {
    init {
        require(maxEvents > 0) { "maxEvents must be > 0" }
    }

    private val _eventVersion = MutableStateFlow(0L)
    override val eventVersion: StateFlow<Long> = _eventVersion.asStateFlow()
    private val ringBuffer = ArrayDeque<LabTraceEvent>(maxEvents)
    private var droppedEventCount: Int = 0

    /** Currently active case, if any. */
    private var activeCaseId: LabCaseId? = null

    fun startCase(caseId: LabCaseId) {
        activeCaseId = caseId
        resetBuffer()
        record(TraceEventType.STEP_MARKER, "Started case ${caseId.code}")
    }

    fun record(type: TraceEventType, description: String, metadata: Map<String, String> = emptyMap()) {
        val event = LabTraceEvent(
            timestampMs = SystemClock.elapsedRealtime(),
            type = type,
            description = description,
            metadata = metadata,
        )
        append(event)

        // Always mirror to logcat
        val tag = "LabTrace"
        val msg = "[${type.name}] $description${if (metadata.isNotEmpty()) " $metadata" else ""}"
        if (type == TraceEventType.INVARIANT && metadata["passed"] == "false") {
            Log.e(tag, msg)
        } else {
            Log.d(tag, msg)
        }
    }

    fun clear() {
        activeCaseId = null
        resetBuffer()
    }

    override fun snapshot(): List<LabTraceEvent> = ringBuffer.toList()

    override fun exportSnapshotJson(pretty: Boolean): String {
        val payload = JSONObject().apply {
            put("caseId", activeCaseId?.code ?: JSONObject.NULL)
            put("eventCount", ringBuffer.size)
            put("droppedEventCount", droppedEventCount)
            put("generatedAtElapsedRealtimeMs", SystemClock.elapsedRealtime())
            put(
                "events",
                JSONArray().apply {
                    snapshot().forEach { event ->
                        put(
                            JSONObject().apply {
                                put("timestampMs", event.timestampMs)
                                put("type", event.type.name)
                                put("description", event.description)
                                put("metadata", event.metadata.toSortedJson())
                            },
                        )
                    }
                },
            )
        }
        return if (pretty) payload.toString(2) else payload.toString()
    }

    private fun append(event: LabTraceEvent) {
        if (ringBuffer.size == maxEvents) {
            ringBuffer.removeFirst()
            droppedEventCount += 1
        }
        ringBuffer.addLast(event)
        publishUpdate()
    }

    private fun resetBuffer() {
        ringBuffer.clear()
        droppedEventCount = 0
        publishUpdate()
    }

    private fun publishUpdate() {
        _eventVersion.value = _eventVersion.value + 1
    }

    private companion object {
        const val DEFAULT_MAX_EVENTS: Int = 2_000
    }
}

private fun Map<String, String>.toSortedJson(): JSONObject = JSONObject().apply {
    entries.sortedBy { it.key }.forEach { (key, value) ->
        put(key, value)
    }
}
