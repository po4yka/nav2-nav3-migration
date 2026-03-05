package com.example.navigationlab.host.nav3.hosts

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment

/**
 * DialogFragment used by T5 scenarios to validate legacy island modal layering behavior.
 */
class IslandModalDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val label = requireArguments().getString(ARG_LABEL) ?: "Island Modal"

        val content = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(48, 48, 48, 32)
            addView(
                TextView(context).apply {
                    text = label
                    textSize = 20f
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                },
            )
            addView(
                Button(context).apply {
                    text = "Dismiss"
                    setOnClickListener { dismissAllowingStateLoss() }
                },
            )
        }

        return AlertDialog.Builder(requireContext())
            .setView(content)
            .create()
    }

    companion object {
        private const val ARG_LABEL = "label"

        fun newInstance(label: String): IslandModalDialogFragment =
            IslandModalDialogFragment().apply {
                arguments = bundleOf(ARG_LABEL to label)
            }
    }
}
