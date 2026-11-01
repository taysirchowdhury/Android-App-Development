package com.example.cse489assignment2

/** Standalone JVM checks for the same battery percent helper called by BatteryActivity. */
fun main() {
    val cases = listOf(
        Triple(50, 100, 50),
        Triple(45, 90, 50),
        Triple(0, 100, 0),
        Triple(100, 100, 100),
        Triple(9, 20, 45),
        Triple(120, 100, 100),
    )
    for ((level, scale, expected) in cases) {
        check(BatteryPercentage.from(level, scale) == expected) {
            "Battery percentage calculation failed for level=$level, scale=$scale"
        }
    }
    check(BatteryPercentage.from(-1, 100) == null)
    check(BatteryPercentage.from(2, 0) == null)
    println("PASS: 8 battery percentage cases")
}
