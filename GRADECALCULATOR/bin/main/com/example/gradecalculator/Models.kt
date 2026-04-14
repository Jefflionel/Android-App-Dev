package com.example.gradecalculator

data class StudentRecord(val name: String, val score: Double, val grade: String)

data class GradeStatistics(
        val totalStudents: Int,
        val averageScore: Double,
        val passRate: Double,
        val gradeBreakdown: Map<String, Int>
)

data class ProcessingResult(
        val students: List<StudentRecord>,
        val statistics: GradeStatistics,
        val exportFormat: ExportFormat
)

enum class ExportFormat(val extension: String, val mimeType: String) {
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    CSV("csv", "text/csv"),
    PDF("pdf", "application/pdf")
}
