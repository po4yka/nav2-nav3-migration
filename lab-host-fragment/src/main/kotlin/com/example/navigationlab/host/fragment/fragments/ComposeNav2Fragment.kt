package com.example.navigationlab.host.fragment.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController

/**
 * T6 topology: Fragment host -> ComposeView -> internal Nav2.
 * This Fragment contains a ComposeView with a Nav2 NavHost inside.
 *
 * Used for B06 (Nav2 triggers activity-level fragment transaction) and
 * B07 (Nav2 dialog returns result).
 */
class ComposeNav2Fragment : Fragment() {

    /** Nav2 controller exposed for scenario step executors. */
    var navHostController: NavHostController? = null
        private set

    /** Last result returned by the Nav2 dialog route (B07). */
    var lastDialogResult: String? = null
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    val controller = rememberNavController()
                    navHostController = controller

                    NavHost(
                        navController = controller,
                        startDestination = ROUTE_HOME,
                    ) {
                        composable(ROUTE_HOME) {
                            InternalStubScreen("Home", COLORS[0])
                        }
                        composable(ROUTE_SCREEN_A) {
                            InternalStubScreen("Screen A", COLORS[1])
                        }
                        composable(ROUTE_SCREEN_B) {
                            InternalStubScreen("Screen B", COLORS[2])
                        }
                        dialog(ROUTE_RESULT_DIALOG) {
                            DialogStubContent(
                                onConfirm = {
                                    lastDialogResult = "confirmed"
                                    controller.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set(DIALOG_RESULT_KEY, "confirmed")
                                    controller.popBackStack()
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ROUTE_HOME = "home"
        const val ROUTE_SCREEN_A = "screen_a"
        const val ROUTE_SCREEN_B = "screen_b"
        const val ROUTE_RESULT_DIALOG = "result_dialog"
        const val DIALOG_RESULT_KEY = "dialog_result"

        val COLORS = listOf(
            Color(0xFF6200EE), // Purple
            Color(0xFF03DAC5), // Teal
            Color(0xFFBB86FC), // Light purple
            Color(0xFF018786), // Dark teal
            Color(0xFFCF6679), // Pink
        )

        fun newInstance(): ComposeNav2Fragment = ComposeNav2Fragment()
    }
}

/** Stub screen for internal Nav2 destinations within the fragment. */
@Composable
private fun InternalStubScreen(label: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
        )
    }
}

/** Stub dialog content for the Nav2 dialog route (B07). */
@Composable
private fun DialogStubContent(onConfirm: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Nav2 Dialog",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onConfirm) {
                Text("Confirm & Return Result")
            }
        }
    }
}
