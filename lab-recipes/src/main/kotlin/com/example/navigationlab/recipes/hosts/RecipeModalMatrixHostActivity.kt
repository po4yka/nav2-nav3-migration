package com.example.navigationlab.recipes.hosts

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button as AndroidButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.contracts.parseRunModeOrDefault
import com.example.navigationlab.recipes.R
import com.example.navigationlab.recipes.helpers.MyCustomFragment
import com.example.navigationlab.recipes.keys.ModalNav3Detail
import com.example.navigationlab.recipes.keys.ModalNav3Dialog
import com.example.navigationlab.recipes.keys.ModalNav3Home
import com.example.navigationlab.recipes.keys.ModalNav3Island
import com.example.navigationlab.recipes.keys.ModalNav3LeafNav2
import com.example.navigationlab.recipes.keys.ModalNav3ParentDialog
import com.example.navigationlab.recipes.keys.ModalNav3ParentPopup
import com.example.navigationlab.recipes.keys.ModalNav3Popup
import com.example.navigationlab.recipes.keys.ModalNav3Sheet

/**
 * Consolidated host for modal interoperability recipe cases R20-R25.
 */
class RecipeModalMatrixHostActivity : AppCompatActivity() {

    private lateinit var caseCode: String

    private lateinit var nav2ParentController: NavHostController
    private var nav2ParentDepthValue: Int = 1

    private var nav2LeafController: NavHostController? = null
    private var nav2LeafDepthValue: Int = 0
    private var pendingLeafRestoreRoute: String? = null
    private var rememberedLeafModalRoute: String? = null

    val nav3ParentBackStack = mutableStateListOf<Any>(ModalNav3Home)
    val nav3LeafBackStack = mutableStateListOf<Any>(ModalNav3Home)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_host)

        caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }
        val runMode = parseRunModeOrDefault(intent.getStringExtra(EXTRA_RUN_MODE))
        if (caseCode == CASE_R25) {
            restoreR25ParentBackStack(savedInstanceState)
            pendingLeafRestoreRoute = savedInstanceState?.getString(STATE_PENDING_LEAF_MODAL_ROUTE)
            rememberedLeafModalRoute = pendingLeafRestoreRoute
        }

        findViewById<TextView>(R.id.tvTopologyLabel).text = getString(
            R.string.topology_label_with_case_mode,
            getString(R.string.topology_recipe_modal_matrix),
            caseCode,
            runMode,
        )

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                when (caseCode) {
                    CASE_R20 -> Nav2ReferenceContent()
                    CASE_R21 -> Nav3ReferenceContent()
                    CASE_R22 -> Nav2ToNav3ReferenceContent()
                    CASE_R23, CASE_R25 -> Nav3ToNav2ReferenceContent()
                    CASE_R24 -> LegacyIslandReferenceContent()
                    else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Unsupported case: $caseCode")
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (caseCode != CASE_R25) return
        outState.putStringArrayList(
            STATE_R25_PARENT_BACK_STACK_KEYS,
            ArrayList(nav3ParentBackStack.mapNotNull(::r25ParentKeyToToken)),
        )
        val route = currentLeafRoute ?: rememberedLeafModalRoute
        if (isLeafModalRoute(route)) {
            outState.putString(STATE_PENDING_LEAF_MODAL_ROUTE, route)
        }
    }

    /** Parent-level helper for building a non-root stack before modal operations. */
    fun openParentDetail() {
        when (caseCode) {
            CASE_R20, CASE_R22 -> navigateNav2Parent(PARENT_ROUTE_DETAIL)
            CASE_R21, CASE_R23, CASE_R24, CASE_R25 -> pushNav3Parent(ModalNav3Detail)
        }
    }

    /** Navigate into the interop leaf/island entry for relevant recipe cases. */
    fun openInteropLeaf() {
        when (caseCode) {
            CASE_R22 -> navigateNav2Parent(PARENT_ROUTE_NAV3_LEAF)
            CASE_R23, CASE_R25 -> pushNav3Parent(ModalNav3LeafNav2)
            CASE_R24 -> pushNav3Parent(ModalNav3Island)
        }
    }

    /** Open a dialog modal in the currently active layer for this case. */
    fun openDialogModal() {
        when (caseCode) {
            CASE_R20 -> navigateNav2Parent(PARENT_ROUTE_DIALOG)
            CASE_R21 -> pushNav3Parent(ModalNav3Dialog)
            CASE_R22 -> {
                if (currentParentRoute == PARENT_ROUTE_NAV3_LEAF) {
                    pushNav3Leaf(ModalNav3Dialog)
                } else {
                    navigateNav2Parent(PARENT_ROUTE_DIALOG)
                }
            }
            CASE_R23, CASE_R25 -> navigateNav2Leaf(LEAF_ROUTE_DIALOG)
            CASE_R24 -> openIslandDialogModal()
        }
    }

    /** Open a sheet modal in the currently active layer for this case. */
    fun openSheetModal() {
        when (caseCode) {
            CASE_R20 -> navigateNav2Parent(PARENT_ROUTE_SHEET)
            CASE_R21 -> pushNav3Parent(ModalNav3Sheet)
            CASE_R22 -> pushNav3Leaf(ModalNav3Sheet)
            CASE_R23, CASE_R25 -> navigateNav2Leaf(LEAF_ROUTE_SHEET)
        }
    }

    /** Open a fullscreen dialog where supported (Nav2-only variants). */
    fun openFullScreenDialogModal() {
        when (caseCode) {
            CASE_R20 -> navigateNav2Parent(PARENT_ROUTE_FULL_SCREEN_DIALOG)
            CASE_R23, CASE_R25 -> navigateNav2Leaf(LEAF_ROUTE_FULL_SCREEN_DIALOG)
        }
    }

    /** Open parent modal layer explicitly for interop reference cases. */
    fun openParentDialogModal() {
        when (caseCode) {
            CASE_R22 -> navigateNav2Parent(PARENT_ROUTE_PARENT_DIALOG)
            CASE_R23, CASE_R24, CASE_R25 -> pushNav3Parent(ModalNav3ParentDialog)
            CASE_R21 -> pushNav3Parent(ModalNav3Dialog)
            CASE_R20 -> navigateNav2Parent(PARENT_ROUTE_DIALOG)
        }
    }

    /** Open parent popup overlay where available. */
    fun openParentPopupOverlay() {
        when (caseCode) {
            CASE_R21 -> pushNav3Parent(ModalNav3Popup)
            CASE_R23, CASE_R24, CASE_R25 -> pushNav3Parent(ModalNav3ParentPopup)
        }
    }

    /** Attach a legacy fragment to the island container in R24. */
    fun attachIslandFragment(fragment: Fragment = MyCustomFragment()): Boolean {
        val containerId = R.id.recipeLegacyFragmentContainer
        if (findViewById<FragmentContainerView?>(containerId) == null) return false
        supportFragmentManager.beginTransaction()
            .replace(containerId, fragment, TAG_ISLAND_FRAGMENT)
            .addToBackStack("recipe_island")
            .commit()
        return true
    }

    /** Open legacy island modal dialog in R24. */
    fun openIslandDialogModal() {
        if (caseCode != CASE_R24 || !isLegacyIslandVisible || isIslandDialogVisible) return
        RecipeIslandDialogFragment.newInstance(getString(R.string.recipe_island_dialog_label))
            .show(supportFragmentManager, TAG_ISLAND_DIALOG)
    }

    /** Dismiss the top-most modal/overlay according to case-specific unwind order. */
    fun dismissTopLayer(): Boolean {
        return when (caseCode) {
            CASE_R20 -> dismissNav2ParentModal()
            CASE_R21 -> dismissNav3ParentModal()
            CASE_R22 -> dismissR22ModalChain()
            CASE_R23, CASE_R25 -> dismissR23ModalChain()
            CASE_R24 -> dismissR24ModalChain()
            else -> false
        }
    }

    val currentParentRoute: String?
        get() = if (::nav2ParentController.isInitialized) {
            nav2ParentController.currentBackStackEntry?.destination?.route
        } else {
            null
        }

    val currentLeafRoute: String?
        get() = nav2LeafController?.currentBackStackEntry?.destination?.route

    val nav2ParentDepth: Int
        get() = nav2ParentDepthValue

    val nav3ParentDepth: Int
        get() = nav3ParentBackStack.size

    val nav3LeafDepth: Int
        get() = nav3LeafBackStack.size

    val nav2LeafDepth: Int
        get() = nav2LeafDepthValue

    val isParentDialogVisible: Boolean
        get() = when (caseCode) {
            CASE_R20, CASE_R22 ->
                currentParentRoute == PARENT_ROUTE_DIALOG || currentParentRoute == PARENT_ROUTE_PARENT_DIALOG
            CASE_R21, CASE_R23, CASE_R24, CASE_R25 ->
                nav3ParentBackStack.lastOrNull() is ModalNav3Dialog || nav3ParentBackStack.lastOrNull() is ModalNav3ParentDialog
            else -> false
        }

    val isParentPopupVisible: Boolean
        get() = nav3ParentBackStack.lastOrNull() is ModalNav3Popup ||
            nav3ParentBackStack.lastOrNull() is ModalNav3ParentPopup

    val isSheetVisible: Boolean
        get() = when (caseCode) {
            CASE_R20 -> currentParentRoute == PARENT_ROUTE_SHEET
            CASE_R21 -> nav3ParentBackStack.lastOrNull() is ModalNav3Sheet
            CASE_R22 -> nav3LeafBackStack.lastOrNull() is ModalNav3Sheet
            CASE_R23, CASE_R25 -> currentLeafRoute == LEAF_ROUTE_SHEET
            else -> false
        }

    val isDialogVisible: Boolean
        get() = when (caseCode) {
            CASE_R20 -> currentParentRoute == PARENT_ROUTE_DIALOG
            CASE_R21 -> nav3ParentBackStack.lastOrNull() is ModalNav3Dialog
            CASE_R22 -> nav3LeafBackStack.lastOrNull() is ModalNav3Dialog
            CASE_R23, CASE_R25 -> currentLeafRoute == LEAF_ROUTE_DIALOG
            CASE_R24 -> isIslandDialogVisible
            else -> false
        }

    val isFullScreenDialogVisible: Boolean
        get() = when (caseCode) {
            CASE_R20 -> currentParentRoute == PARENT_ROUTE_FULL_SCREEN_DIALOG
            CASE_R23, CASE_R25 -> currentLeafRoute == LEAF_ROUTE_FULL_SCREEN_DIALOG
            else -> false
        }

    val isLegacyIslandVisible: Boolean
        get() = nav3ParentBackStack.lastOrNull() is ModalNav3Island

    val isIslandDialogVisible: Boolean
        get() = (supportFragmentManager.findFragmentByTag(TAG_ISLAND_DIALOG) as? DialogFragment)
            ?.dialog
            ?.isShowing == true

    @Composable
    private fun Nav2ReferenceContent() {
        val controller = rememberNavController()
        nav2ParentController = controller
        nav2ParentDepthValue = 1
        NavHost(navController = controller, startDestination = PARENT_ROUTE_HOME) {
            composable(PARENT_ROUTE_HOME) { StubScreen("Nav2 Home", Color(0xFFA8E6CF)) }
            composable(PARENT_ROUTE_DETAIL) { StubScreen("Nav2 Detail", Color(0xFFA8D8EA)) }
            dialog(PARENT_ROUTE_DIALOG) { DialogLayer("Nav2 Dialog") { popNav2ParentBack() } }
            dialog(PARENT_ROUTE_SHEET) { SheetLayer("Nav2 Sheet") { popNav2ParentBack() } }
            dialog(PARENT_ROUTE_FULL_SCREEN_DIALOG) {
                FullScreenLayer("Nav2 Fullscreen Dialog") { popNav2ParentBack() }
            }
        }
    }

    @Composable
    private fun Nav3ReferenceContent() {
        nav3ParentBackStack.ensureSingleRoot(ModalNav3Home)
        NavDisplay(
            backStack = nav3ParentBackStack,
            onBack = {
                if (!dismissNav3ParentModal() && nav3ParentBackStack.size > 1) {
                    popNav3ParentBack()
                } else if (nav3ParentBackStack.size <= 1) {
                    finish()
                }
            },
            entryProvider = { key ->
                when (key) {
                    is ModalNav3Home -> NavEntry(key) { StubScreen("Nav3 Home", Color(0xFFFFF9C4)) }
                    is ModalNav3Detail -> NavEntry(key) { StubScreen("Nav3 Detail", Color(0xFFFFE0B2)) }
                    is ModalNav3Dialog -> NavEntry(key) { DialogLayer("Nav3 Dialog") { popNav3ParentBack() } }
                    is ModalNav3Sheet -> NavEntry(key) { SheetLayer("Nav3 Sheet") { popNav3ParentBack() } }
                    is ModalNav3Popup -> NavEntry(key) { PopupLayer("Nav3 Popup") { popNav3ParentBack() } }
                    else -> NavEntry(key) { StubScreen("Nav3 Unknown", Color(0xFFE0BBE4)) }
                }
            },
        )
    }

    @Composable
    private fun Nav2ToNav3ReferenceContent() {
        val controller = rememberNavController()
        nav2ParentController = controller
        nav2ParentDepthValue = 1
        nav3LeafBackStack.ensureSingleRoot(ModalNav3Home)

        NavHost(navController = controller, startDestination = PARENT_ROUTE_HOME) {
            composable(PARENT_ROUTE_HOME) { StubScreen("Nav2 Parent Home", Color(0xFFA8E6CF)) }
            composable(PARENT_ROUTE_DETAIL) { StubScreen("Nav2 Parent Detail", Color(0xFFA8D8EA)) }
            dialog(PARENT_ROUTE_PARENT_DIALOG) { DialogLayer("Nav2 Parent Dialog") { popNav2ParentBack() } }
            composable(PARENT_ROUTE_NAV3_LEAF) {
                NavDisplay(
                    backStack = nav3LeafBackStack,
                    onBack = {
                        if (nav3LeafBackStack.size > 1) {
                            popNav3LeafBack()
                        } else {
                            popNav2ParentBack()
                        }
                    },
                    entryProvider = { key ->
                        when (key) {
                            is ModalNav3Home -> NavEntry(key) { StubScreen("Nav3 Leaf Home", Color(0xFFFFF9C4)) }
                            is ModalNav3Detail -> NavEntry(key) { StubScreen("Nav3 Leaf Detail", Color(0xFFFFE0B2)) }
                            is ModalNav3Dialog -> NavEntry(key) { DialogLayer("Nav3 Leaf Dialog") { popNav3LeafBack() } }
                            is ModalNav3Sheet -> NavEntry(key) { SheetLayer("Nav3 Leaf Sheet") { popNav3LeafBack() } }
                            else -> NavEntry(key) { StubScreen("Leaf Unknown", Color(0xFFE0BBE4)) }
                        }
                    },
                )
            }
        }
    }

    @Composable
    private fun Nav3ToNav2ReferenceContent() {
        nav3ParentBackStack.ensureSingleRoot(ModalNav3Home)
        NavDisplay(
            backStack = nav3ParentBackStack,
            onBack = {
                if (!dismissR23ModalChain() && nav3ParentBackStack.size > 1) {
                    popNav3ParentBack()
                } else if (nav3ParentBackStack.size <= 1) {
                    finish()
                }
            },
            entryProvider = { key ->
                when (key) {
                    is ModalNav3Home -> NavEntry(key) { StubScreen("Nav3 Parent Home", Color(0xFFFFF9C4)) }
                    is ModalNav3Detail -> NavEntry(key) { StubScreen("Nav3 Parent Detail", Color(0xFFFFE0B2)) }
                    is ModalNav3ParentDialog -> NavEntry(key) {
                        DialogLayer("Nav3 Parent Dialog") { popNav3ParentBack() }
                    }
                    is ModalNav3ParentPopup -> NavEntry(key) {
                        PopupLayer("Nav3 Parent Popup") { popNav3ParentBack() }
                    }
                    is ModalNav3LeafNav2 -> NavEntry(key) {
                        val controller = rememberNavController()
                        nav2LeafController = controller
                        nav2LeafDepthValue = 1
                        LaunchedEffect(controller, pendingLeafRestoreRoute) {
                            val restoreRoute = pendingLeafRestoreRoute ?: return@LaunchedEffect
                            controller.navigate(restoreRoute)
                            nav2LeafDepthValue += 1
                            rememberedLeafModalRoute = restoreRoute
                            pendingLeafRestoreRoute = null
                        }
                        NavHost(navController = controller, startDestination = LEAF_ROUTE_HOME) {
                            composable(LEAF_ROUTE_HOME) { StubScreen("Nav2 Leaf Home", Color(0xFFA8E6CF)) }
                            composable(LEAF_ROUTE_DETAIL) { StubScreen("Nav2 Leaf Detail", Color(0xFFA8D8EA)) }
                            dialog(LEAF_ROUTE_DIALOG) { DialogLayer("Nav2 Leaf Dialog") { popNav2LeafBack() } }
                            dialog(LEAF_ROUTE_SHEET) { SheetLayer("Nav2 Leaf Sheet") { popNav2LeafBack() } }
                            dialog(LEAF_ROUTE_FULL_SCREEN_DIALOG) {
                                FullScreenLayer("Nav2 Leaf Fullscreen") { popNav2LeafBack() }
                            }
                        }
                    }
                    else -> NavEntry(key) { StubScreen("Parent Unknown", Color(0xFFE0BBE4)) }
                }
            },
        )
    }

    @Composable
    private fun LegacyIslandReferenceContent() {
        nav3ParentBackStack.ensureSingleRoot(ModalNav3Home)
        NavDisplay(
            backStack = nav3ParentBackStack,
            onBack = {
                if (!dismissR24ModalChain() && nav3ParentBackStack.size > 1) {
                    popNav3ParentBack()
                } else if (nav3ParentBackStack.size <= 1) {
                    finish()
                }
            },
            entryProvider = { key ->
                when (key) {
                    is ModalNav3Home -> NavEntry(key) { StubScreen("Island Home", Color(0xFFFFF9C4)) }
                    is ModalNav3Detail -> NavEntry(key) { StubScreen("Island Detail", Color(0xFFFFE0B2)) }
                    is ModalNav3ParentDialog -> NavEntry(key) {
                        DialogLayer("Island Parent Dialog") { popNav3ParentBack() }
                    }
                    is ModalNav3ParentPopup -> NavEntry(key) {
                        PopupLayer("Island Parent Popup") { popNav3ParentBack() }
                    }
                    is ModalNav3Island -> NavEntry(key) {
                        AndroidView(
                            factory = { ctx ->
                                FragmentContainerView(ctx).apply { id = R.id.recipeLegacyFragmentContainer }
                            },
                        )
                    }
                    else -> NavEntry(key) { StubScreen("Island Unknown", Color(0xFFE0BBE4)) }
                }
            },
        )
    }

    private fun navigateNav2Parent(route: String) {
        if (!::nav2ParentController.isInitialized) return
        nav2ParentController.navigate(route)
        nav2ParentDepthValue += 1
        NavLogger.push(TAG, route, nav2ParentDepthValue)
    }

    private fun popNav2ParentBack(): Boolean {
        if (!::nav2ParentController.isInitialized) return false
        val from = nav2ParentController.currentBackStackEntry?.destination?.route ?: "?"
        val popped = nav2ParentController.popBackStack()
        if (popped && nav2ParentDepthValue > 1) {
            nav2ParentDepthValue -= 1
            NavLogger.pop(TAG, from, nav2ParentDepthValue)
        }
        return popped
    }

    private fun navigateNav2Leaf(route: String) {
        val controller = nav2LeafController ?: return
        controller.navigate(route)
        nav2LeafDepthValue += 1
        if (isLeafModalRoute(route)) {
            rememberedLeafModalRoute = route
        }
        NavLogger.push(TAG, route, nav2LeafDepthValue)
    }

    private fun popNav2LeafBack(): Boolean {
        val controller = nav2LeafController ?: return false
        val from = controller.currentBackStackEntry?.destination?.route ?: "?"
        val popped = controller.popBackStack()
        if (popped && nav2LeafDepthValue > 1) {
            nav2LeafDepthValue -= 1
            rememberedLeafModalRoute = if (isLeafModalRoute(controller.currentBackStackEntry?.destination?.route)) {
                controller.currentBackStackEntry?.destination?.route
            } else {
                null
            }
            NavLogger.pop(TAG, from, nav2LeafDepthValue)
        }
        return popped
    }

    private fun pushNav3Parent(key: Any) {
        nav3ParentBackStack.add(key)
        NavLogger.push(TAG, key::class.simpleName ?: "?", nav3ParentBackStack.size)
    }

    private fun popNav3ParentBack(): Boolean {
        if (nav3ParentBackStack.size <= 1) return false
        val from = nav3ParentBackStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
        val popped = nav3ParentBackStack.removeLastOrNull()
        if (popped is ModalNav3LeafNav2) {
            nav2LeafController = null
            nav2LeafDepthValue = 0
            rememberedLeafModalRoute = null
        }
        NavLogger.pop(TAG, from, nav3ParentBackStack.size)
        return true
    }

    private fun pushNav3Leaf(key: Any) {
        nav3LeafBackStack.add(key)
        NavLogger.push(TAG, key::class.simpleName ?: "?", nav3LeafBackStack.size)
    }

    private fun popNav3LeafBack(): Boolean {
        if (nav3LeafBackStack.size <= 1) return false
        val from = nav3LeafBackStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
        nav3LeafBackStack.removeLastOrNull()
        NavLogger.pop(TAG, from, nav3LeafBackStack.size)
        return true
    }

    private fun dismissNav2ParentModal(): Boolean =
        when (currentParentRoute) {
            PARENT_ROUTE_DIALOG,
            PARENT_ROUTE_SHEET,
            PARENT_ROUTE_FULL_SCREEN_DIALOG,
            PARENT_ROUTE_PARENT_DIALOG,
            -> popNav2ParentBack()
            else -> false
        }

    private fun dismissNav3ParentModal(): Boolean =
        when (nav3ParentBackStack.lastOrNull()) {
            is ModalNav3Dialog,
            is ModalNav3Sheet,
            is ModalNav3Popup,
            is ModalNav3ParentDialog,
            is ModalNav3ParentPopup,
            -> popNav3ParentBack()
            else -> false
        }

    private fun dismissR22ModalChain(): Boolean {
        if (dismissNav2ParentModal()) return true
        return when (nav3LeafBackStack.lastOrNull()) {
            is ModalNav3Dialog,
            is ModalNav3Sheet,
            -> popNav3LeafBack()
            else -> false
        }
    }

    private fun dismissR23ModalChain(): Boolean {
        if (dismissNav3ParentModal()) return true
        return when (currentLeafRoute) {
            LEAF_ROUTE_DIALOG,
            LEAF_ROUTE_SHEET,
            LEAF_ROUTE_FULL_SCREEN_DIALOG,
            -> popNav2LeafBack()
            else -> false
        }
    }

    private fun dismissR24ModalChain(): Boolean {
        if (dismissNav3ParentModal()) return true
        val dialog = supportFragmentManager.findFragmentByTag(TAG_ISLAND_DIALOG) as? DialogFragment
        if (dialog?.dialog?.isShowing == true) {
            if (supportFragmentManager.isStateSaved) return false
            dialog.dismiss()
            return true
        }
        return false
    }

    private fun isLeafModalRoute(route: String?): Boolean =
        route == LEAF_ROUTE_DIALOG || route == LEAF_ROUTE_SHEET || route == LEAF_ROUTE_FULL_SCREEN_DIALOG

    private fun restoreR25ParentBackStack(savedState: Bundle?) {
        val tokens = savedState?.getStringArrayList(STATE_R25_PARENT_BACK_STACK_KEYS) ?: return
        if (tokens.isEmpty()) return
        val restored = tokens.mapNotNull(::r25TokenToParentKey)
        if (restored.isEmpty()) return
        nav3ParentBackStack.clear()
        nav3ParentBackStack.addAll(restored)
    }

    private fun r25ParentKeyToToken(key: Any): String? = when (key) {
        is ModalNav3Home -> TOKEN_HOME
        is ModalNav3Detail -> TOKEN_DETAIL
        is ModalNav3LeafNav2 -> TOKEN_LEAF
        is ModalNav3ParentDialog -> TOKEN_PARENT_DIALOG
        is ModalNav3ParentPopup -> TOKEN_PARENT_POPUP
        else -> null
    }

    private fun r25TokenToParentKey(token: String): Any? = when (token) {
        TOKEN_HOME -> ModalNav3Home
        TOKEN_DETAIL -> ModalNav3Detail
        TOKEN_LEAF -> ModalNav3LeafNav2
        TOKEN_PARENT_DIALOG -> ModalNav3ParentDialog
        TOKEN_PARENT_POPUP -> ModalNav3ParentPopup
        else -> null
    }

    companion object {
        private const val TAG = "RecipeModalMatrixHost"
        private const val STATE_PENDING_LEAF_MODAL_ROUTE = "pending_leaf_modal_route"
        private const val STATE_R25_PARENT_BACK_STACK_KEYS = "state_r25_parent_back_stack_keys"

        private const val CASE_R20 = "R20"
        private const val CASE_R21 = "R21"
        private const val CASE_R22 = "R22"
        private const val CASE_R23 = "R23"
        private const val CASE_R24 = "R24"
        private const val CASE_R25 = "R25"

        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        private const val PARENT_ROUTE_HOME = "parent_home"
        private const val PARENT_ROUTE_DETAIL = "parent_detail"
        private const val PARENT_ROUTE_DIALOG = "parent_dialog"
        private const val PARENT_ROUTE_SHEET = "parent_sheet"
        private const val PARENT_ROUTE_FULL_SCREEN_DIALOG = "parent_full_screen_dialog"
        private const val PARENT_ROUTE_NAV3_LEAF = "parent_nav3_leaf"
        private const val PARENT_ROUTE_PARENT_DIALOG = "parent_parent_dialog"

        private const val LEAF_ROUTE_HOME = "leaf_home"
        private const val LEAF_ROUTE_DETAIL = "leaf_detail"
        private const val LEAF_ROUTE_DIALOG = "leaf_dialog"
        private const val LEAF_ROUTE_SHEET = "leaf_sheet"
        private const val LEAF_ROUTE_FULL_SCREEN_DIALOG = "leaf_full_screen_dialog"

        private const val TAG_ISLAND_FRAGMENT = "recipe_island_fragment"
        private const val TAG_ISLAND_DIALOG = "recipe_island_dialog"

        private const val TOKEN_HOME = "home"
        private const val TOKEN_DETAIL = "detail"
        private const val TOKEN_LEAF = "leaf"
        private const val TOKEN_PARENT_DIALOG = "parent_dialog"
        private const val TOKEN_PARENT_POPUP = "parent_popup"

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, RecipeModalMatrixHostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}

@Composable
private fun StubScreen(label: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
private fun DialogLayer(title: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(24.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) { Text("Dismiss") }
            }
        }
    }
}

@Composable
private fun SheetLayer(title: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .padding(24.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) { Text("Dismiss Sheet") }
            }
        }
    }
}

@Composable
private fun FullScreenLayer(title: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.95f), shape = RoundedCornerShape(20.dp))
                .padding(24.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = title, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}

@Composable
private fun PopupLayer(title: String, onDismiss: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.TopEnd)
                .background(Color.White, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}

private fun MutableList<Any>.ensureSingleRoot(root: Any) {
    if (isEmpty() || first() != root) {
        clear()
        add(root)
    }
}

class RecipeIslandDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val label = requireArguments().getString(ARG_LABEL)
            ?: getString(R.string.recipe_island_dialog_label)
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
                AndroidButton(context).apply {
                    text = getString(R.string.action_dismiss)
                    setOnClickListener { dismiss() }
                },
            )
        }
        return AlertDialog.Builder(requireContext()).setView(content).create()
    }

    companion object {
        private const val ARG_LABEL = "label"

        fun newInstance(label: String): RecipeIslandDialogFragment =
            RecipeIslandDialogFragment().apply {
                arguments = bundleOf(ARG_LABEL to label)
            }
    }
}
