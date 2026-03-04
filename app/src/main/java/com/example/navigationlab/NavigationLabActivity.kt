package com.example.navigationlab

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.navigationlab.engine.NavigationLabEngine
import com.example.navigationlab.engine.casebrowser.CaseBrowserScreen
import com.example.navigationlab.launch.CaseHostLauncher
import org.koin.android.ext.android.inject

class NavigationLabActivity : ComponentActivity() {

    private val engine: NavigationLabEngine by inject()
    private val caseHostLauncher: CaseHostLauncher by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CaseBrowserScreen(
                        scenarios = engine.scenarios,
                        onCaseSelected = { caseId, runMode ->
                            Log.i(TAG, "Case selected: ${caseId.code}, mode: $runMode")
                            val launched = caseHostLauncher.launch(this, caseId, runMode)
                            if (!launched) {
                                Log.e(TAG, "No host launcher registered for case ${caseId.code}")
                            }
                        },
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "NavigationLab"
    }
}
