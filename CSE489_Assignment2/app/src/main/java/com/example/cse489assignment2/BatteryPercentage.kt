package com.example.cse489assignment2

/** Converts ACTION_BATTERY_CHANGED level/scale extras to a percentage; null if invalid. */
object BatteryPercentage {
    fun from(level: Int, scale: Int): Int? {
        if (level < 0 || scale <= 0) return null
        return (level.toDouble() * 100.0 / scale.toDouble()).toInt().coerceIn(0, 100)
    }
}
