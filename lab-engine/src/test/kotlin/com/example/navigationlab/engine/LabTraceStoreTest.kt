package com.example.navigationlab.engine

import android.app.Application
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.TraceEventType
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class LabTraceStoreTest {

    @Test
    fun record_whenCapacityExceeded_keepsMostRecentEvents() {
        val store = LabTraceStore(maxEvents = 3)
        store.startCase(LabCaseId(CaseFamily.A, 1))
        store.record(TraceEventType.STEP_MARKER, "step1")
        store.record(TraceEventType.STEP_MARKER, "step2")
        store.record(TraceEventType.STEP_MARKER, "step3")

        val snapshot = store.snapshot()
        assertEquals(3, snapshot.size)
        assertEquals("step1", snapshot.first().description)
        assertEquals("step3", snapshot.last().description)

        val payload = JSONObject(store.exportSnapshotJson(pretty = false))
        assertEquals(1, payload.getInt("droppedEventCount"))
        assertEquals(snapshot.size, payload.getInt("eventCount"))
    }

    @Test
    fun exportSnapshotJson_includesCaseAndMetadata() {
        val caseId = LabCaseId(CaseFamily.B, 2)
        val store = LabTraceStore(maxEvents = 8)
        store.startCase(caseId)
        store.record(
            type = TraceEventType.INVARIANT,
            description = "Invariant check",
            metadata = mapOf("passed" to "true", "scope" to "scenario"),
        )

        val payload = JSONObject(store.exportSnapshotJson(pretty = false))
        assertEquals(caseId.code, payload.getString("caseId"))

        val events = payload.getJSONArray("events")
        assertTrue(events.length() >= 2)

        val lastEvent = events.getJSONObject(events.length() - 1)
        assertEquals("INVARIANT", lastEvent.getString("type"))
        val metadata = lastEvent.getJSONObject("metadata")
        assertEquals("true", metadata.getString("passed"))
        assertEquals("scenario", metadata.getString("scope"))
    }
}
