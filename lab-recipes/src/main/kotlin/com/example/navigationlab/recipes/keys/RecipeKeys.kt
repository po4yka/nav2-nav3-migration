package com.example.navigationlab.recipes.keys

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// -- R01: Basic (non-saveable, no NavKey) --
data object BasicRouteA
data class BasicRouteB(val id: String)

// -- R02: BasicSaveable --
@Serializable
data object SaveableRouteA : NavKey

@Serializable
data class SaveableRouteB(val id: String) : NavKey

// -- R03: BasicDsl (reuses R02 keys via DSL syntax) --
@Serializable
data object DslRouteA : NavKey

@Serializable
data class DslRouteB(val id: String) : NavKey

// -- R04: Interop --
@Serializable
data object InteropFragmentRoute : NavKey

@Serializable
data class InteropViewRoute(val id: String) : NavKey

// -- R05: Migration Begin (Nav2 routes, no NavKey needed) --
@Serializable
data object MigBeginBaseRouteA

@Serializable
data object MigBeginRouteA

@Serializable
data object MigBeginRouteA1

@Serializable
data object MigBeginBaseRouteB

@Serializable
data object MigBeginRouteB

@Serializable
data class MigBeginRouteB1(val id: String)

@Serializable
data object MigBeginBaseRouteC

@Serializable
data object MigBeginRouteC

@Serializable
data object MigBeginRouteD

// -- R06: Migration End (Nav3 NavKey routes) --
@Serializable
data object MigEndRouteA : NavKey

@Serializable
data object MigEndRouteA1 : NavKey

@Serializable
data object MigEndRouteB : NavKey

@Serializable
data class MigEndRouteB1(val id: String) : NavKey

@Serializable
data object MigEndRouteC : NavKey

@Serializable
data object MigEndRouteD : NavKey

// -- R07/R08: Results --
@Serializable
data object ResultHome : NavKey

@Serializable
class ResultPersonDetailsForm : NavKey

// -- R09-R12: Multi-tab app keys --
@Serializable
data object TabAlpha : NavKey

@Serializable
data class TabAlphaDetail(val from: String) : NavKey

@Serializable
data class TabAlphaEdit(val from: String) : NavKey

@Serializable
data object TabBeta : NavKey

@Serializable
data object TabBetaDetail : NavKey

@Serializable
data object TabGamma : NavKey

@Serializable
data class TabGammaDetail(val result: String) : NavKey

// -- R13: Deep link keys --
@Serializable
data object DeepLinkHome : NavKey

@Serializable
data class DeepLinkTarget(val param: String) : NavKey

// -- NavBarItem for migration scenarios --
class NavBarItem(
    val icon: ImageVector,
    val description: String,
)

val MIG_BEGIN_TOP_LEVEL_ROUTES = mapOf(
    MigBeginBaseRouteA to NavBarItem(icon = Icons.Default.Home, description = "Route A"),
    MigBeginBaseRouteB to NavBarItem(icon = Icons.Default.Face, description = "Route B"),
    MigBeginBaseRouteC to NavBarItem(icon = Icons.Default.Camera, description = "Route C"),
)

val MIG_END_TOP_LEVEL_ROUTES = mapOf(
    MigEndRouteA to NavBarItem(icon = Icons.Default.Home, description = "Route A"),
    MigEndRouteB to NavBarItem(icon = Icons.Default.Face, description = "Route B"),
    MigEndRouteC to NavBarItem(icon = Icons.Default.Camera, description = "Route C"),
)
