package com.example.navigationlab.catalog

import android.content.Context
import android.content.Intent
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabHostProvider
import com.example.navigationlab.contracts.LabScenario

typealias IntentFactory = (Context, LabCaseId, String) -> Intent

/** Shared source of truth for all registered scenario providers and host launchers. */
class LabScenarioCatalog(providers: List<LabHostProvider>) {

    val scenarios: List<LabScenario> = providers
        .flatMap { it.scenarios }
        .sortedWith(compareBy({ it.id.family.ordinal }, { it.id.number }))

    val launchByCaseCode: Map<String, IntentFactory> = buildMap {
        providers.forEach { provider ->
            provider.scenarios.forEach { scenario ->
                val previous = put(scenario.id.code, provider::createHostIntent)
                require(previous == null) {
                    "Duplicate host launch mapping for case ${scenario.id.code}"
                }
            }
        }
    }
}
