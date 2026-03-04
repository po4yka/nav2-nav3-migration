package com.example.navigationlab.engine

import android.os.SystemClock
import android.util.Log
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabTraceEvent
import com.example.navigationlab.contracts.TraceEventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory store for trace events captured during lab scenario execution.
 * Observable via [events] StateFlow for live UI updates.
 */
class LabTraceStore {

    private val _events = MutableStateFlow<List<LabTraceEvent>>(emptyList())
    val events: StateFlow<List<LabTraceEvent>> = _events.asStateFlow()

    /** Currently active case, if any. */
    private var activeCaseId: LabCaseId? = null

    fun startCase(caseId: LabCaseId) {
        activeCaseId = caseId
        _events.value = emptyList()
        record(TraceEventType.STEP_MARKER, "Started case ${caseId.code}")
    }

    fun record(type: TraceEventType, description: String, metadata: Map<String, String> = emptyMap()) {
        val event = LabTraceEvent(
            timestampMs = SystemClock.elapsedRealtime(),
            type = type,
            description = description,
            metadata = metadata,
        )
        _events.value = _events.value + event

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
        _events.value = emptyList()
    }

    fun snapshot(): List<LabTraceEvent> = _events.value
}
