package com.example.gradecalculator

import java.io.InputStream
import java.util.*
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory

class ExcelProcessor {

    fun processExcelFile(inputStream: InputStream): List<StudentRecord> {
        return inputStream.use { fis ->
            WorkbookFactory.create(fis).use { workbook ->
                val sheet = workbook.getSheetAt(0)

                val headerRow = sheet.getRow(0) ?: throw IllegalArgumentException("Sheet is empty")

                val (nameCol, scoreCol, gradeCol) = findColumnIndices(headerRow)

                if (nameCol == -1 || scoreCol == -1 || gradeCol == -1) {
                    throw IllegalArgumentException(
                            "File must contain 'Name', 'Score', and 'Grade' columns"
                    )
                }

                val students = mutableListOf<StudentRecord>()
                val gradeCalculator = gradeCalculator()

                (1..sheet.lastRowNum).forEach { i ->
                    val row = sheet.getRow(i) ?: return@forEach
                    val nameCell = row.getCell(nameCol) ?: return@forEach
                    val scoreCell = row.getCell(scoreCol) ?: return@forEach

                    try {
                        val name = nameCell.stringCellValue
                        val scoreValue = parseScoreCell(scoreCell) ?: return@forEach
                        val grade = gradeCalculator(scoreValue)

                        students.add(StudentRecord(name, scoreValue, grade))
                    } catch (e: Exception) {
                        // Skip malformed rows
                    }
                }

                students
            }
        }
    }

    private fun findColumnIndices(headerRow: Row): Triple<Int, Int, Int> {
        val lower = Locale.getDefault()
        val indices =
                (0 until headerRow.lastCellNum.toInt())
                        .mapNotNull { i ->
                            headerRow.getCell(i)?.let { cell ->
                                cell.stringCellValue.lowercase(lower) to i
                            }
                        }
                        .toMap()

        val nameCol = indices["name"] ?: -1
        val scoreCol = indices["score"] ?: -1
        val gradeCol = indices["grade"] ?: -1

        return Triple(nameCol, scoreCol, gradeCol)
    }

    private fun parseScoreCell(cell: org.apache.poi.ss.usermodel.Cell): Double? {
        return when (cell.cellType) {
            CellType.NUMERIC -> cell.numericCellValue
            CellType.STRING -> {
                try {
                    cell.stringCellValue.toDouble()
                } catch (e: NumberFormatException) {
                    null
                }
            }
            else -> null
        }
    }

    private fun gradeCalculator(): (Double) -> String = { score ->
        when {
            score >= 80.0 -> "A"
            score >= 60.0 -> "B"
            score >= 50.0 -> "C"
            score >= 40.0 -> "D"
            else -> "F"
        }
    }

    fun calculateStatistics(students: List<StudentRecord>): GradeStatistics {
        val averageScore =
                if (students.isNotEmpty()) {
                    students.map { it.score }.average()
                } else {
                    0.0
                }

        val passCount = students.count { it.score >= 50.0 }
        val passRate =
                if (students.isNotEmpty()) {
                    (passCount.toDouble() / students.size) * 100
                } else {
                    0.0
                }

        val gradeBreakdown =
                mapOf(
                        "A" to students.count { it.grade == "A" },
                        "B" to students.count { it.grade == "B" },
                        "C" to students.count { it.grade == "C" },
                        "D" to students.count { it.grade == "D" },
                        "F" to students.count { it.grade == "F" }
                )

        return GradeStatistics(
                totalStudents = students.size,
                averageScore = String.format("%.1f", averageScore).toDouble(),
                passRate = String.format("%.0f", passRate).toDouble(),
                gradeBreakdown = gradeBreakdown
        )
    }
}
