package com.example.navigationlab.host.fragment.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.contracts.parseRunModeOrDefault
import com.example.navigationlab.host.fragment.R
import com.example.navigationlab.host.fragment.databinding.ActivityFragmentHostBinding
import com.example.navigationlab.host.fragment.fragments.LabStubFragment

/**
 * T1 host topology: Activity(XML) -> FragmentContainerView -> Fragments.
 * Receives a case ID via Intent extras and manages fragment navigation
 * within a single FragmentContainerView.
 */
class FragmentHostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFragmentHostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFragmentHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }
        val runMode = parseRunModeOrDefault(intent.getStringExtra(EXTRA_RUN_MODE))

        binding.tvTopologyLabel.text = "T1: Fragment Host - $caseCode [$runMode]"

        if (savedInstanceState == null) {
            showFragment(
                LabStubFragment.newInstance("Home", COLORS[0]),
                addToBackStack = false,
            )
        }
    }

    /**
     * Navigate to a new stub fragment within the container.
     * Host topology modules use this to execute scenario steps.
     */
    fun showFragment(fragment: LabStubFragment, addToBackStack: Boolean = true) {
        val tx = supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
        if (addToBackStack) {
            tx.addToBackStack(null)
        }
        tx.commit()
        NavLogger.push(TAG, fragment::class.simpleName ?: "Fragment", supportFragmentManager.backStackEntryCount + if (addToBackStack) 1 else 0)
    }

    /** Add an overlay fragment on top of the current content without replacing. */
    fun addOverlayFragment(fragment: LabStubFragment) {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, fragment)
            .addToBackStack("overlay")
            .commit()
    }

    /** Current backstack entry count. */
    val backStackDepth: Int
        get() = supportFragmentManager.backStackEntryCount

    companion object {
        private const val TAG = "T1Host"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        /** Predefined colors for fake screens. */
        val COLORS = intArrayOf(
            0xFF6200EE.toInt(), // Purple
            0xFF03DAC5.toInt(), // Teal
            0xFFBB86FC.toInt(), // Light purple
            0xFF018786.toInt(), // Dark teal
            0xFFCF6679.toInt(), // Pink
            0xFF3700B3.toInt(), // Deep purple
        )

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, FragmentHostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}
