package com.example.navigationlab.recipes

import android.content.Context
import android.content.Intent
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabHostProvider
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

object RecipeBasicProvider : LabHostProvider {

    override val scenarios: List<LabScenario> = R_BASIC_SCENARIOS

    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeBasicHostActivity.createIntent(context, caseId, runMode)
}

object RecipeInteropProvider : LabHostProvider {

    override val scenarios: List<LabScenario> = R_INTEROP_SCENARIOS

    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeInteropHostActivity.createIntent(context, caseId, runMode)
}

object RecipeMigrationProvider : LabHostProvider {

    override val scenarios: List<LabScenario> = R_MIGRATION_SCENARIOS

    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeMigrationHostActivity.createIntent(context, caseId, runMode)
}

object RecipeResultsProvider : LabHostProvider {

    override val scenarios: List<LabScenario> = R_RESULTS_SCENARIOS

    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeResultsHostActivity.createIntent(context, caseId, runMode)
}

object RecipeAppStateProvider : LabHostProvider {

    override val scenarios: List<LabScenario> = R_APP_STATE_SCENARIOS

    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeAppStateHostActivity.createIntent(context, caseId, runMode)
}

object RecipeDeepLinkProvider : LabHostProvider {

    override val scenarios: List<LabScenario> = R_DEEP_LINK_SCENARIOS

    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
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

object RecipeTransitionProvider : LabHostProvider {

    override val scenarios: List<LabScenario> = R_TRANSITION_SCENARIOS

    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeTransitionHostActivity.createIntent(context, caseId, runMode)
}

object RecipeAdaptiveProvider : LabHostProvider {

    override val scenarios: List<LabScenario> = R_ADAPTIVE_SCENARIOS

    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeAdaptiveHostActivity.createIntent(context, caseId, runMode)
}

object RecipeConditionalProvider : LabHostProvider {

    override val scenarios: List<LabScenario> = R_CONDITIONAL_SCENARIOS

    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
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

object RecipeModalMatrixProvider : LabHostProvider {

    override val scenarios: List<LabScenario> = R_MODAL_MATRIX_SCENARIOS

    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        RecipeModalMatrixHostActivity.createIntent(context, caseId, runMode)
}
