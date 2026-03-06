package com.example.navigationlab.testkit

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import androidx.navigation3.runtime.NavKey
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.recipes.hosts.RecipeAdaptiveHostActivity
import com.example.navigationlab.recipes.hosts.RecipeAppStateHostActivity
import com.example.navigationlab.recipes.hosts.RecipeBasicHostActivity
import com.example.navigationlab.recipes.hosts.RecipeConditionalHostActivity
import com.example.navigationlab.recipes.hosts.RecipeDeepLinkHostActivity
import com.example.navigationlab.recipes.hosts.RecipeInteropHostActivity
import com.example.navigationlab.recipes.hosts.RecipeResultsHostActivity
import com.example.navigationlab.recipes.hosts.RecipeTransitionHostActivity
import com.example.navigationlab.recipes.keys.TabAlphaDetail
import com.example.navigationlab.recipes.keys.TabAlphaEdit
import com.example.navigationlab.recipes.keys.TabBetaDetail
import com.example.navigationlab.recipes.keys.TabGammaDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeFamiliesBehaviorParityTest {

    @Test
    fun r01_basicNav3MutableStack_navigatesAndPopsDeterministically() {
        val scenario = launchBasicCase(1)
        try {
            assertTrue(waitUntil(scenario) { it.currentRouteName == "BasicRouteA" && it.backStackDepth == 1 })
            scenario.onActivity { activity ->
                activity.navigateToDetail()
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "BasicRouteB" && it.currentRouteId == "123" && it.backStackDepth == 2 })
            scenario.onActivity { activity ->
                assertTrue(activity.popBack())
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "BasicRouteA" && it.backStackDepth == 1 })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r02_basicSaveableNav3_recreateRestoresBackStackAndRoute() {
        val scenario = launchBasicCase(2)
        try {
            scenario.onActivity { activity ->
                activity.navigateToDetail()
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "SaveableRouteB" && it.currentRouteId == "123" })

            scenario.recreate()
            assertTrue(waitUntil(scenario) { it.currentRouteName == "SaveableRouteB" && it.currentRouteId == "123" && it.backStackDepth == 2 })

            scenario.onActivity { activity ->
                assertTrue(activity.popBack())
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "SaveableRouteA" && it.backStackDepth == 1 })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r03_basicDslNav3_entryProviderDslPath_behavesLikeReferenceFlow() {
        val scenario = launchBasicCase(3)
        try {
            assertTrue(waitUntil(scenario) { it.currentRouteName == "DslRouteA" && it.backStackDepth == 1 })
            scenario.onActivity { activity ->
                activity.navigateToDetail()
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "DslRouteB" && it.currentRouteId == "123" && it.backStackDepth == 2 })
            scenario.onActivity { activity ->
                assertTrue(activity.popBack())
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "DslRouteA" && it.backStackDepth == 1 })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r04_interopAndroidFragmentAndAndroidView_unwindWithoutCorruption() {
        val scenario = launchInteropCase()
        try {
            assertTrue(waitUntil(scenario) { it.currentRouteName == "InteropFragmentRoute" && it.backStackDepth == 1 })
            scenario.onActivity { activity ->
                activity.navigateToView()
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "InteropViewRoute" && it.currentViewRouteId == "123" && it.backStackDepth == 2 })
            scenario.onActivity { activity ->
                assertTrue(activity.popBack())
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "InteropFragmentRoute" && it.backStackDepth == 1 })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r07_resultsEventBus_submissionUpdatesHomeState() {
        val scenario = launchResultsCase(7)
        try {
            assertTrue(waitUntil(scenario) { it.currentRouteName == "ResultHome" })
            scenario.onActivity { activity ->
                activity.openPersonDetailsForm()
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "ResultPersonDetailsForm" })
            scenario.onActivity { activity ->
                activity.submitPerson(name = "Alice", favoriteColor = "Blue")
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentRouteName == "ResultHome" &&
                        it.currentPerson?.name == "Alice" &&
                        it.currentPerson?.favoriteColor == "Blue"
                },
            )
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r08_resultsStateStore_submissionPersistsAcrossRecreate() {
        val scenario = launchResultsCase(8)
        try {
            scenario.onActivity { activity ->
                activity.openPersonDetailsForm()
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "ResultPersonDetailsForm" })
            scenario.onActivity { activity ->
                activity.submitPerson(name = "Bob", favoriteColor = "Green")
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "ResultHome" && it.currentPerson?.name == "Bob" })

            scenario.recreate()
            assertTrue(
                waitUntil(scenario) {
                    it.currentRouteName == "ResultHome" &&
                        it.currentPerson?.name == "Bob" &&
                        it.currentPerson?.favoriteColor == "Green"
                },
            )
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r09_multiStackHistory_backFromRootReturnsToLastVisitedTab() {
        val scenario = launchAppStateCase(9)
        try {
            assertTrue(waitUntil(scenario) { it.currentTopLevelLabel == "Alpha" })
            scenario.onActivity { activity ->
                activity.navigate(TabAlphaDetail(from = "Alpha"))
                activity.selectTopLevelBeta()
            }
            assertTrue(waitUntil(scenario) { it.currentTopLevelLabel == "Beta" })

            scenario.onActivity { activity ->
                activity.back()
            }
            assertTrue(waitUntil(scenario) { it.currentTopLevelLabel == "Alpha" && it.currentRoute?.javaClass?.simpleName == "TabAlphaDetail" })

            scenario.onActivity { activity ->
                activity.selectTopLevelGamma()
                activity.selectTopLevelBeta()
            }
            assertTrue(waitUntil(scenario) { it.currentTopLevelLabel == "Beta" })
            scenario.onActivity { activity ->
                activity.back()
            }
            assertTrue(waitUntil(scenario) { it.currentTopLevelLabel == "Gamma" })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r10_bottomBarVisibility_obeysHideAndSameAsParentPolicies() {
        val scenario = launchAppStateCase(10)
        try {
            assertTrue(waitUntil(scenario) { it.isBottomNavigationVisible })
            scenario.onActivity { activity ->
                activity.navigate(TabAlphaDetail(from = "Alpha"))
            }
            assertTrue(waitUntil(scenario) { !it.isBottomNavigationVisible && it.currentRoute?.javaClass?.simpleName == "TabAlphaDetail" })

            scenario.onActivity { activity ->
                activity.back()
            }
            assertTrue(waitUntil(scenario) { it.currentRoute?.javaClass?.simpleName == "TabAlpha" && it.isBottomNavigationVisible })

            scenario.onActivity { activity ->
                activity.selectTopLevelBeta()
                activity.navigate(TabBetaDetail)
            }
            assertTrue(waitUntil(scenario) { it.currentRoute?.javaClass?.simpleName == "TabBetaDetail" && it.isBottomNavigationVisible })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r11_viewModelInNavEntry_preservedAcrossConfigurationChange() {
        val scenario = launchAppStateCase(11)
        try {
            scenario.onActivity { activity ->
                activity.selectTopLevelGamma()
                activity.navigate(TabGammaDetail(result = "test"))
            }
            assertTrue(waitUntil(scenario) { it.currentRoute?.javaClass?.simpleName == "TabGammaDetail" && it.gammaViewModelResult == "test" })

            scenario.recreate()
            assertTrue(waitUntil(scenario) { it.currentRoute?.javaClass?.simpleName == "TabGammaDetail" && it.gammaViewModelResult == "test" })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r12_launchedEffectResultConsumption_clearsStoreAfterUse() {
        val scenario = launchAppStateCase(12)
        try {
            scenario.onActivity { activity ->
                activity.navigate(TabAlphaDetail(from = "Alpha"))
                activity.navigate(TabAlphaEdit(from = "Alpha"))
            }
            assertTrue(waitUntil(scenario) { it.currentRoute?.javaClass?.simpleName == "TabAlphaEdit" })
            scenario.onActivity { activity ->
                activity.submitAlphaEditResult("edited")
            }
            assertTrue(waitUntil(scenario) { it.currentRoute?.javaClass?.simpleName == "TabAlphaDetail" })
            assertTrue(waitUntil(scenario) { it.pendingEditResult == null })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r13_deepLinkTargetIntent_buildsBackStackHomeThenTarget() {
        val scenario = launchR13DeepLinkCase("hello")
        try {
            assertTrue(waitUntil(scenario) { it.currentRouteName == "DeepLinkTarget" && it.currentTargetParam == "hello" && it.backStackDepth == 2 })
            scenario.onActivity { activity ->
                assertTrue(activity.popBack())
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "DeepLinkHome" && it.backStackDepth == 1 })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r14_transitionSpecs_routesRemainDeterministicAcrossForwardAndBack() {
        val scenario = launchTransitionCase(14)
        try {
            scenario.onActivity { activity ->
                activity.openSlideTransition()
                activity.openFadeTransition()
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "TransitionFade" && it.backStackDepth == 3 })
            scenario.onActivity { activity ->
                assertTrue(activity.popBack())
                assertTrue(activity.popBack())
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "TransitionHome" && it.backStackDepth == 1 })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r15_dialogSceneStrategy_behavesAsTopMostEntryAndPopsCleanly() {
        val scenario = launchTransitionCase(15)
        try {
            scenario.onActivity { activity ->
                activity.openDialog()
            }
            assertTrue(waitUntil(scenario) { it.isDialogVisible && it.backStackDepth == 2 })
            scenario.onActivity { activity ->
                assertTrue(activity.popBack())
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "TransitionHome" && it.backStackDepth == 1 })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r16_bottomSheetSceneStrategy_behavesAsTopMostEntryAndPopsCleanly() {
        val scenario = launchTransitionCase(16)
        try {
            scenario.onActivity { activity ->
                activity.openBottomSheet()
            }
            assertTrue(waitUntil(scenario) { it.isBottomSheetVisible && it.backStackDepth == 2 })
            scenario.onActivity { activity ->
                assertTrue(activity.popBack())
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "TransitionHome" && it.backStackDepth == 1 })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r17_adaptiveListDetail_supportsDetailAndExtraPaneFlow() {
        val scenario = launchAdaptiveCase()
        try {
            assertTrue(waitUntil(scenario) { it.currentRouteName == "ItemList" })
            scenario.onActivity { activity ->
                activity.selectItem("item-1")
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "ItemDetail" && it.currentItemId == "item-1" })
            scenario.onActivity { activity ->
                activity.openExtraPane()
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "ItemExtra" && it.currentItemId == "item-1" })
            scenario.onActivity { activity ->
                assertTrue(activity.popBack())
                assertTrue(activity.popBack())
                activity.selectItem("item-2")
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "ItemDetail" && it.currentItemId == "item-2" })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r18_conditionalNavigator_redirectsToLogin_thenResolvesRedirectTarget() {
        val scenario = launchConditionalCase(18)
        try {
            assertTrue(waitUntil(scenario) { it.currentRouteName == "GateHome" && !it.isLoggedIn })
            scenario.onActivity { activity ->
                activity.navigateToProfile()
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "GateLogin" && !it.isLoggedIn })
            scenario.onActivity { activity ->
                activity.login()
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "GateProfile" && it.isLoggedIn })
            scenario.onActivity { activity ->
                activity.logout()
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "GateHome" && !it.isLoggedIn })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r19_advancedDeepLink_createsSyntheticBackStackAndUnwindsToHome() {
        val scenario = launchR19AdvancedDeepCase(name = "Alice", location = "NYC")
        try {
            assertTrue(
                waitUntil(scenario) {
                    it.currentRouteName == "AdvancedDeepTarget" &&
                        it.advancedTargetName == "Alice" &&
                        it.advancedTargetLocation == "NYC" &&
                        it.backStackDepth == 2
                },
            )
            scenario.onActivity { activity ->
                assertTrue(activity.popBack())
            }
            assertTrue(waitUntil(scenario) { it.currentRouteName == "AdvancedDeepHome" && it.backStackDepth == 1 })
            scenario.onActivity { activity ->
                assertFalse(activity.popBack())
            }
        } finally {
            scenario.close()
        }
    }

    private fun launchBasicCase(number: Int): ActivityScenario<RecipeBasicHostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = RecipeBasicHostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.R, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchInteropCase(): ActivityScenario<RecipeInteropHostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = RecipeInteropHostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.R, 4),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchResultsCase(number: Int): ActivityScenario<RecipeResultsHostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = RecipeResultsHostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.R, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchAppStateCase(number: Int): ActivityScenario<RecipeAppStateHostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = RecipeAppStateHostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.R, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchR13DeepLinkCase(param: String): ActivityScenario<RecipeDeepLinkHostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = RecipeDeepLinkHostActivity.createDeepLinkIntent(
            context = context,
            param = param,
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchTransitionCase(number: Int): ActivityScenario<RecipeTransitionHostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = RecipeTransitionHostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.R, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchAdaptiveCase(): ActivityScenario<RecipeAdaptiveHostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = RecipeAdaptiveHostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.R, 17),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchConditionalCase(number: Int): ActivityScenario<RecipeConditionalHostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = RecipeConditionalHostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.R, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchR19AdvancedDeepCase(
        name: String,
        location: String,
    ): ActivityScenario<RecipeConditionalHostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = RecipeConditionalHostActivity.createAdvancedDeepIntent(
            context = context,
            name = name,
            location = location,
        )
        return ActivityScenario.launch(intent)
    }

    private fun <T : Activity> waitUntil(
        scenario: ActivityScenario<T>,
        timeoutMs: Long = 6_000,
        condition: (T) -> Boolean,
    ): Boolean {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (SystemClock.elapsedRealtime() < deadline) {
            var matched = false
            scenario.onActivity { activity ->
                matched = condition(activity)
            }
            if (matched) return true
            SystemClock.sleep(50)
        }
        return false
    }
}
