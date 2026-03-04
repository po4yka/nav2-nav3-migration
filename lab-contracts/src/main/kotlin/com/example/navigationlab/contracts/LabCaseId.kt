package com.example.navigationlab.contracts

/** Unique identifier for a lab test case (e.g., "A01", "B03"). */
data class LabCaseId(val family: String, val number: Int) {
    val code: String get() = "$family${number.toString().padStart(2, '0')}"
    override fun toString(): String = code
}
