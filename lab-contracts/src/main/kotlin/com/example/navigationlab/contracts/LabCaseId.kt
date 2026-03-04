package com.example.navigationlab.contracts

/** Unique identifier for a lab test case (e.g., "A01", "B03"). */
data class LabCaseId(val family: CaseFamily, val number: Int) {
    val code: String get() = "${family.prefix}${number.toString().padStart(2, '0')}"
    override fun toString(): String = code

    companion object {
        fun parse(code: String): LabCaseId {
            require(code.length >= 3) { "Invalid case code: $code" }
            val family = CaseFamily.fromPrefix(code.first().toString())
            val number = code.drop(1).toInt()
            return LabCaseId(family, number)
        }
    }
}
