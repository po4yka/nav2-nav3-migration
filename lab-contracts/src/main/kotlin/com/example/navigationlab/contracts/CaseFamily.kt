package com.example.navigationlab.contracts

/** Case family groupings A-H. */
enum class CaseFamily(val prefix: String, val title: String) {
    A("A", "Container and host ownership"),
    B("B", "Nav2/Nav3 interoperability"),
    C("C", "XML <-> Compose screen connection"),
    D("D", "Dialog/bottom-sheet/overlay semantics"),
    E("E", "Back handling and nested stacks"),
    F("F", "Deeplink and fallback behavior"),
    G("G", "State restore and argument stability"),
    H("H", "Transaction safety and race conditions"),
    R("R", "Nav3 recipe reference implementations");

    companion object {
        fun fromPrefix(prefix: String): CaseFamily =
            entries.first { it.prefix == prefix }
    }
}
