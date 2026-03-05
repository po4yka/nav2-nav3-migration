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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay

/**
 * Fragment hosting Nav3 NavDisplay with a modal entry for B08 scenario.
 * The Nav3 "modal" is a key that renders an overlay-styled composable
 * (Nav3 has no built-in dialog support like Nav2's dialog() builder).
 *
 * Used for B08 (Fragment screen launches Nav3 modal entry and returns result).
 */
class ComposeNav3Fragment : Fragment() {

    /** Nav3 back stack exposed for scenario step executors. */
    val backStack = mutableStateListOf<Any>(Nav3ModalKey.Home)

    /** Last result returned by the Nav3 modal entry (B08). */
    var lastModalResult: String? = null
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
                    NavDisplay(
                        backStack = backStack,
                        onBack = {
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            }
                        },
                        entryProvider = { key ->
                            when (key) {
                                is Nav3ModalKey.Home -> NavEntry(key) {
                                    Nav3InternalStubScreen("Home", COLORS[0])
                                }
                                is Nav3ModalKey.ScreenA -> NavEntry(key) {
                                    Nav3InternalStubScreen("Screen A", COLORS[1])
                                }
                                is Nav3ModalKey.ResultModal -> NavEntry(key) {
                                    Nav3ModalStubContent(
                                        onConfirm = {
                                            lastModalResult = "confirmed"
                                            if (backStack.size > 1) {
                                                backStack.removeLastOrNull()
                                            }
                                        },
                                    )
                                }
                                else -> NavEntry(key) {
                                    Nav3InternalStubScreen("Unknown", COLORS[4])
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    /** Navigate to a Nav3 key. */
    fun navigateTo(key: Any) {
        backStack.add(key)
    }

    /** Pop the Nav3 back stack. */
    fun popBack(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeLastOrNull()
        return true
    }

    companion object {
        val COLORS = listOf(
            Color(0xFF6200EE), // Purple
            Color(0xFF03DAC5), // Teal
            Color(0xFFBB86FC), // Light purple
            Color(0xFF018786), // Dark teal
            Color(0xFFCF6679), // Pink
        )

        fun newInstance(): ComposeNav3Fragment = ComposeNav3Fragment()
    }
}

/** Navigation keys for Nav3 inside the fragment (B08). */
sealed interface Nav3ModalKey {
    data object Home : Nav3ModalKey
    data object ScreenA : Nav3ModalKey
    data object ResultModal : Nav3ModalKey
}

/** Stub screen for internal Nav3 destinations within the fragment. */
@Composable
private fun Nav3InternalStubScreen(label: String, color: Color) {
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

/** Modal-styled stub content for the Nav3 modal entry (B08). */
@Composable
private fun Nav3ModalStubContent(onConfirm: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Nav3 Modal",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onConfirm) {
                    Text("Confirm & Return Result")
                }
            }
        }
    }
}
