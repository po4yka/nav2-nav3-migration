package com.example.navigationlab.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.navigationlab.contracts.LabTraceEvent
import com.example.navigationlab.contracts.TraceEventType

/**
 * Inline panel displaying a live timeline of trace events during case execution.
 * Designed to be embedded as a bottom panel or overlay in host topology screens.
 */
@Composable
fun TraceTimelinePanel(
    events: List<LabTraceEvent>,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(events.size) {
        if (events.isNotEmpty()) {
            listState.animateScrollToItem(events.lastIndex)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        tonalElevation = 2.dp,
    ) {
        Column {
            Text(
                text = "Trace Timeline",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
            HorizontalDivider()

            if (events.isEmpty()) {
                Text(
                    text = "No events yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp),
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(events.size, key = { index ->
                        val event = events[index]
                        "${index}_${event.timestampMs}_${event.type}_${event.description}"
                    }) { index ->
                        val event = events[index]
                        TraceEventRow(
                            event = event,
                            baseTimestampMs = events.first().timestampMs,
                        )
                    }
                }
            }
        }
    }
}

/**
 * A single row in the trace timeline, showing event type, relative timestamp, and description.
 */
@Composable
private fun TraceEventRow(
    event: LabTraceEvent,
    baseTimestampMs: Long,
) {
    val relativeMs = event.timestampMs - baseTimestampMs
    val isFailedInvariant = event.type == TraceEventType.INVARIANT &&
        event.metadata["passed"] == "false"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { mod ->
                if (isFailedInvariant) {
                    mod.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                } else {
                    mod
                }
            }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Relative timestamp
        Text(
            text = "+${relativeMs}ms",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )

        // Event type badge
        Text(
            text = event.type.label,
            style = MaterialTheme.typography.labelSmall,
            color = event.type.color,
            modifier = Modifier
                .background(
                    color = event.type.color.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp),
                )
                .padding(horizontal = 4.dp, vertical = 1.dp),
        )

        // Description
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isFailedInvariant) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

/** Short display label for each trace event type. */
private val TraceEventType.label: String
    get() = when (this) {
        TraceEventType.STACK_CHANGE -> "STACK"
        TraceEventType.CONTAINER_CHANGE -> "CNTR"
        TraceEventType.FRAGMENT_TRANSACTION -> "FRAG"
        TraceEventType.BACK_EVENT -> "BACK"
        TraceEventType.DEEPLINK -> "LINK"
        TraceEventType.LIFECYCLE -> "LIFE"
        TraceEventType.INVARIANT -> "INV"
        TraceEventType.STEP_MARKER -> "STEP"
    }

/** Color for each trace event type badge. */
private val TraceEventType.color: Color
    get() = when (this) {
        TraceEventType.STACK_CHANGE -> Color(0xFF1565C0)
        TraceEventType.CONTAINER_CHANGE -> Color(0xFF6A1B9A)
        TraceEventType.FRAGMENT_TRANSACTION -> Color(0xFF00838F)
        TraceEventType.BACK_EVENT -> Color(0xFFE65100)
        TraceEventType.DEEPLINK -> Color(0xFF2E7D32)
        TraceEventType.LIFECYCLE -> Color(0xFF455A64)
        TraceEventType.INVARIANT -> Color(0xFFC62828)
        TraceEventType.STEP_MARKER -> Color(0xFF37474F)
    }
