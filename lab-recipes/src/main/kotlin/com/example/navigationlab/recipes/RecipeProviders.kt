package com.example.navigationlab.recipes

import android.content.Context
import android.content.Intent
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.recipes.hosts.RecipeAppStateHostActivity
import com.example.navigationlab.recipes.hosts.RecipeBasicHostActivity
import com.example.navigationlab.recipes.hosts.RecipeDeepLinkHostActivity
import com.example.navigationlab.recipes.hosts.RecipeInteropHostActivity
import com.example.navigationlab.recipes.hosts.RecipeMigrationHostActivity
import com.example.navigationlab.recipes.hosts.RecipeResultsHostActivity

object RecipeBasicProvider {

    val scenarios: List<LabScenario> = R_BASIC_SCENARIOS

    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeBasicHostActivity.createIntent(context, caseId, runMode)
}

object RecipeInteropProvider {

    val scenarios: List<LabScenario> = R_INTEROP_SCENARIOS

    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeInteropHostActivity.createIntent(context, caseId, runMode)
}

object RecipeMigrationProvider {

    val scenarios: List<LabScenario> = R_MIGRATION_SCENARIOS

    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeMigrationHostActivity.createIntent(context, caseId, runMode)
}

object RecipeResultsProvider {

    val scenarios: List<LabScenario> = R_RESULTS_SCENARIOS

    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeResultsHostActivity.createIntent(context, caseId, runMode)
}

object RecipeAppStateProvider {

    val scenarios: List<LabScenario> = R_APP_STATE_SCENARIOS

    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeAppStateHostActivity.createIntent(context, caseId, runMode)
}

object RecipeDeepLinkProvider {

    val scenarios: List<LabScenario> = R_DEEP_LINK_SCENARIOS

    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeDeepLinkHostActivity.createIntent(context, caseId, runMode)
}
