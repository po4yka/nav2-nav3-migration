package com.example.navigationlab.host.nav3

/** Navigation keys for T3 topology (Nav3 NavDisplay). */
sealed interface Nav3Key {
    data object Home : Nav3Key
    data object ScreenA : Nav3Key
    data object ScreenB : Nav3Key
    data object ScreenC : Nav3Key
    data object DialogModal : Nav3Key
    data object SheetModal : Nav3Key
    data object PopupOverlay : Nav3Key
}
