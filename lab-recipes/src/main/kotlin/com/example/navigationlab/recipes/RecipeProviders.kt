package com.example.navigationlab.recipes

import android.content.Context
import android.content.Intent
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.recipes.hosts.RecipeAdaptiveHostActivity
import com.example.navigationlab.recipes.hosts.RecipeAdvancedDeepLinksActivity
import com.example.navigationlab.recipes.hosts.RecipeAppStateHostActivity
import com.example.navigationlab.recipes.hosts.RecipeBasicHostActivity
import com.example.navigationlab.recipes.hosts.RecipeConditionalHostActivity
import com.example.navigationlab.recipes.hosts.RecipeDeepLinkHostActivity
import com.example.navigationlab.recipes.hosts.RecipeDeepLinksActivity
import com.example.navigationlab.recipes.hosts.RecipeInteropHostActivity
import com.example.navigationlab.recipes.hosts.RecipeMigrationHostActivity
import com.example.navigationlab.recipes.hosts.RecipeModalMatrixHostActivity
import com.example.navigationlab.recipes.hosts.RecipeResultsHostActivity
import com.example.navigationlab.recipes.hosts.RecipeTransitionHostActivity

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
        if (caseId.code == "R13") {
            RecipeDeepLinksActivity.createInAppIntent(
                context = context,
                param = RecipeDeepLinksActivity.DEFAULT_IN_APP_PARAM,
                runMode = runMode,
            )
        } else {
            RecipeDeepLinkHostActivity.createIntent(context, caseId, runMode)
        }
}

object RecipeTransitionProvider {

    val scenarios: List<LabScenario> = R_TRANSITION_SCENARIOS

    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeTransitionHostActivity.createIntent(context, caseId, runMode)
}

object RecipeAdaptiveProvider {

    val scenarios: List<LabScenario> = R_ADAPTIVE_SCENARIOS

    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeAdaptiveHostActivity.createIntent(context, caseId, runMode)
}

object RecipeConditionalProvider {

    val scenarios: List<LabScenario> = R_CONDITIONAL_SCENARIOS

    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        if (caseId.code == "R19") {
            RecipeAdvancedDeepLinksActivity.createInAppIntent(
                context = context,
                name = RecipeAdvancedDeepLinksActivity.DEFAULT_IN_APP_NAME,
                location = RecipeAdvancedDeepLinksActivity.DEFAULT_IN_APP_LOCATION,
                runMode = runMode,
            )
        } else {
            RecipeConditionalHostActivity.createIntent(context, caseId, runMode)
        }
}

object RecipeModalMatrixProvider {

    val scenarios: List<LabScenario> = R_MODAL_MATRIX_SCENARIOS

    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeModalMatrixHostActivity.createIntent(context, caseId, runMode)
}
