package com.example.navigationlab.recipes.helpers

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Debug overlay showing navigation state for educational purposes.
 * Displays back stack depth and current route name.
 */
@Composable
fun NavStateIndicator(
    backStackSize: Int,
    currentRoute: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp,
    ) {
        Text(
            text = "Depth: $backStackSize | $currentRoute",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
