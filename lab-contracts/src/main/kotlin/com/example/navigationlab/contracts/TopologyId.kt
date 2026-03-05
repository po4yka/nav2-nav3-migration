package com.example.navigationlab.contracts

/** Host topology identifiers T1-T8. */
enum class TopologyId(val description: String) {
    T1("Activity(XML) -> FragmentContainerView -> Fragments"),
    T2("Activity(XML) -> ComposeView -> Nav2 NavHost"),
    T3("Activity(XML) -> ComposeView -> Nav3 NavDisplay"),
    T4("Activity(XML) -> ComposeView + overlay FrameLayout (dual containers)"),
    T5("Nav3 root -> LegacyIslandEntry -> AndroidViewBinding(FragmentContainerView)"),
    T6("Fragment host -> ComposeView -> internal Nav2"),
    T7("Nav2 route -> Nav3 leaf screen"),
    T8("Nav3 key -> Nav2 leaf graph");
}
