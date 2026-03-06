package com.example.gradecalculator.util

import com.example.gradecalculator.model.StudentGrade

object GradeCalculator {

    private val gradeBoundaries: List<Pair<Double, String>> = listOf(
        70.0 to "A",
        60.0 to "B",
        50.0 to "C",
        40.0 to "D",
        0.0  to "F"
    )

    val calculateGrade: (Double) -> String = { mark ->
        gradeBoundaries.firstOrNull { mark >= it.first }?.second ?: "F"
    }

    val processStudents: (List<Pair<String, Double>>) -> List<StudentGrade> = { entries ->
        entries.map { (name, mark) ->
            StudentGrade(name = name, mark = mark, grade = calculateGrade(mark))
        }
    }

    val isValidMark: (Double) -> Boolean = { it in 0.0..100.0 }

    val buildSummary: (List<StudentGrade>) -> String = { students ->
        if (students.isEmpty()) "No data"
        else {
            val avg = students.map { it.mark }.average()
            val gradeCount = students
                .groupBy { it.grade }
                .entries
                .sortedBy { it.key }
                .fold("") { acc, entry -> "$acc  ${entry.key}: ${entry.value.size}  " }
            "Total: ${students.size} students  |  Avg: ${"%.1f".format(avg)}  |  $gradeCount"
        }
    }
}