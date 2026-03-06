package com.example.navigationlab.host.nav3

import kotlinx.serialization.Serializable

/** Navigation keys for T3 topology (Nav3 NavDisplay). */
@Serializable
sealed interface Nav3Key {
    @Serializable data object Home : Nav3Key
    @Serializable data object ScreenA : Nav3Key
    @Serializable data object ScreenB : Nav3Key
    @Serializable data object ScreenC : Nav3Key
    @Serializable data object DialogModal : Nav3Key
    @Serializable data object SheetModal : Nav3Key
    @Serializable data object PopupOverlay : Nav3Key
}
