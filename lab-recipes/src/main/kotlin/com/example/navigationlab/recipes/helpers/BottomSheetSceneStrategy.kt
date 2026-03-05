package com.example.navigationlab.recipes.helpers

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import kotlinx.coroutines.launch

/**
 * Custom SceneStrategy that renders entries marked with [bottomSheet] metadata
 * as a ModalBottomSheet overlay.
 */
class BottomSheetSceneStrategy : SceneStrategy<NavKey> {

    override fun SceneStrategyScope<NavKey>.calculateScene(
        entries: List<NavEntry<NavKey>>,
    ): Scene<NavKey>? {
        val lastEntry = entries.lastOrNull() ?: return null
        if (lastEntry.metadata[BOTTOM_SHEET_KEY] != true) return null

        val overlaid = entries.dropLast(1)
        return BottomSheetScene(
            overlaidEntries = overlaid,
            sheetEntry = lastEntry,
            allEntries = entries,
            onBack = onBack,
        )
    }

    companion object {
        private const val BOTTOM_SHEET_KEY = "BottomSheetSceneStrategy.bottomSheet"

        fun bottomSheet(): Map<String, Any> = mapOf(BOTTOM_SHEET_KEY to true)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private class BottomSheetScene(
    override val overlaidEntries: List<NavEntry<NavKey>>,
    private val sheetEntry: NavEntry<NavKey>,
    private val allEntries: List<NavEntry<NavKey>>,
    private val onBack: () -> Unit,
) : OverlayScene<NavKey> {

    override val key: Any = "BottomSheetScene"

    override val entries: List<NavEntry<NavKey>> = allEntries

    override val previousEntries: List<NavEntry<NavKey>> = overlaidEntries

    override val content: @Composable () -> Unit = {
        // Render background entries
        for (entry in overlaidEntries) {
            entry.Content()
        }

        // Render bottom sheet overlay
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()

        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    onBack()
                }
            },
            sheetState = sheetState,
        ) {
            sheetEntry.Content()
        }
    }
}
