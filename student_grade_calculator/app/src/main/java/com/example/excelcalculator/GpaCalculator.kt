package com.example.excelcalculator

object GpaCalculator {

    fun calculateGpa(average: Double): String {
        return when {
            average >= 80 -> "A"
            average >= 60 -> "B"
            average >= 50 -> "C"
            average >= 40 -> "D"
            else -> "F"
        }
    }

    fun getGpaColor(gpa: String): Int {
        return when (gpa) {
            "A", "B", "C" -> 0xFF2E7D32.toInt()  // Green
            "D", "F" -> 0xFFD32F2F.toInt()       // Red
            else -> 0xFF757575.toInt()           // Grey
        }
    }
}