package com.example.navigationlab.testkit

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.host.nav2.hosts.Nav2ToNav3InteropActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class T7ModalInteropTest {

    @Test
    fun b13_parentDialogOverLeaf_preservesLeafStack() {
        val scenario = launchCase(CaseFamily.B, 13)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav2ToNav3InteropActivity.ROUTE_NAV3_LEAF)
                activity.openLeafDialogModal()
                val leafDepthBeforeParentDialog = activity.nav3LeafBackStackDepth

                activity.openParentDialog()
                assertTrue(activity.isParentDialogVisible)
                assertTrue(activity.dismissParentDialog())

                assertEquals(leafDepthBeforeParentDialog, activity.nav3LeafBackStackDepth)
                assertTrue(activity.isLeafDialogVisible)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun b14_leafModalFlow_returnsToParentWithoutCorruption() {
        val scenario = launchCase(CaseFamily.B, 14)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav2ToNav3InteropActivity.ROUTE_NAV3_LEAF)
                val parentDepth = activity.nav2BackStackDepth

                activity.openLeafDialogModal()
                assertTrue(activity.isLeafDialogVisible)
                assertTrue(activity.dismissLeafModal())

                activity.openLeafSheetModal()
                assertTrue(activity.isLeafSheetVisible)
                assertTrue(activity.dismissLeafModal())

                assertEquals(parentDepth, activity.nav2BackStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun e09_backOrder_parentDialogThenChildModalThenParentRoute() {
        val scenario = launchCase(CaseFamily.E, 9)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav2ToNav3InteropActivity.ROUTE_NAV3_LEAF)
                activity.openLeafDialogModal()
                activity.openParentDialog()

                assertTrue(activity.isParentDialogVisible)
                assertTrue(activity.dismissParentDialog())

                assertTrue(activity.isLeafDialogVisible)
                assertTrue(activity.dismissLeafModal())

                assertEquals(Nav2ToNav3InteropActivity.ROUTE_NAV3_LEAF, activity.currentNav2Route)
                assertTrue(activity.popNav2Back())
            }
        } finally {
            scenario.close()
        }
    }

    private fun launchCase(family: CaseFamily, number: Int): ActivityScenario<Nav2ToNav3InteropActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav2ToNav3InteropActivity.createIntent(
            context = context,
            caseId = LabCaseId(family, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }
}
