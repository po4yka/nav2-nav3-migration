package com.example.navigationlab.host.nav3.hosts

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * Minimal stub fragment for the T5 fragment island.
 * Uses programmatic views (no XML layout or viewBinding needed).
 * Displays a colored box with a centered label.
 */
class IslandStubFragment : Fragment() {

    /** Debug label used by instrumentation tests to validate restore/order semantics. */
    val debugLabel: String?
        get() = arguments?.getString(ARG_LABEL)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val label = arguments?.getString(ARG_LABEL) ?: "Fragment"
        val color = arguments?.getInt(ARG_COLOR, Color.GRAY) ?: Color.GRAY

        return FrameLayout(requireContext()).apply {
            setBackgroundColor(color)
            addView(
                TextView(context).apply {
                    text = label
                    setTextColor(Color.WHITE)
                    textSize = 24f
                    gravity = Gravity.CENTER
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                },
            )
        }
    }

    companion object {
        private const val ARG_LABEL = "label"
        private const val ARG_COLOR = "color"

        fun newInstance(label: String, color: Int): IslandStubFragment =
            IslandStubFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_LABEL, label)
                    putInt(ARG_COLOR, color)
                }
            }
    }
}
