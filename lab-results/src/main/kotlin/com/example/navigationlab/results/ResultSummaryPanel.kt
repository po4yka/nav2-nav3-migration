package com.example.navigationlab.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.navigationlab.contracts.InvariantResult
import com.example.navigationlab.contracts.LabResult
import com.example.navigationlab.contracts.ResultStatus

/**
 * Panel displaying a completed [LabResult] summary: status, invariant results, and error info.
 * Shown after a scenario finishes execution.
 */
@Composable
fun ResultSummaryPanel(
    result: LabResult,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: case code + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = result.caseId.code,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                StatusBadge(result.status)
            }

            // Error message
            val errorMsg = result.errorMessage
            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMsg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            // Invariant results
            if (result.invariantResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Invariants",
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.height(4.dp))
                result.invariantResults.forEach { invariant ->
                    InvariantRow(invariant)
                }
            }

            // Trace count
            if (result.trace.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${result.trace.size} trace events",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ResultStatus) {
    val (text, bgColor, fgColor) = when (status) {
        ResultStatus.PASS -> Triple("PASS", Color(0xFF2E7D32), Color.White)
        ResultStatus.FAIL -> Triple("FAIL", Color(0xFFC62828), Color.White)
        ResultStatus.SKIPPED -> Triple("SKIP", Color(0xFF757575), Color.White)
        ResultStatus.ERROR -> Triple("ERR", Color(0xFFE65100), Color.White)
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = fgColor,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .semantics { contentDescription = "Status: $text" },
    )
}

@Composable
private fun InvariantRow(invariant: InvariantResult) {
    val statusLabel = if (invariant.passed) "OK" else "FAIL"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .semantics {
                contentDescription = "Invariant: ${invariant.description}, status: $statusLabel"
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = statusLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (invariant.passed) Color(0xFF2E7D32) else Color(0xFFC62828),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.size(32.dp, 16.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = invariant.description,
                style = MaterialTheme.typography.bodySmall,
            )
            val failMsg = invariant.failureMessage
            if (failMsg != null) {
                Text(
                    text = failMsg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
