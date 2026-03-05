package com.example.navigationlab.host.fragment.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.navigationlab.host.fragment.databinding.FragmentLabStubBinding

/**
 * Minimal fake screen fragment: colored box with a route label.
 * Used across all fragment-based topologies as a stand-in for real screens.
 */
class LabStubFragment : Fragment() {

    private var _binding: FragmentLabStubBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLabStubBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val label = arguments?.getString(ARG_LABEL) ?: "Unknown"
        val color = arguments?.getInt(ARG_COLOR, Color.DKGRAY) ?: Color.DKGRAY

        binding.tvRouteLabel.text = label
        binding.stubRoot.setBackgroundColor(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_LABEL = "label"
        private const val ARG_COLOR = "color"

        fun newInstance(label: String, color: Int): LabStubFragment =
            LabStubFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_LABEL, label)
                    putInt(ARG_COLOR, color)
                }
            }
    }
}
