import org.apache.poi.ss.usermodel.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

fun main() {
    val scanner = Scanner(System.`in`)

    print("Enter the path to your Excel file: ")
    val filePath = scanner.nextLine().trim()

    try {
        // Open the Excel file
        FileInputStream(filePath).use { fileInputStream ->
            val workbook = WorkbookFactory.create(fileInputStream)
            val sheet = workbook.getSheetAt(0) // Assume first sheet

            // Find column indices
            val headerRow = sheet.getRow(0)
            var nameColIndex = -1
            var scoreColIndex = -1
            var gradeColIndex = -1

            // Find columns by name
            for (i in 0 until headerRow.lastCellNum.toInt()) {
                val cell = headerRow.getCell(i)
                if (cell != null) {
                    val cellValue = cell.stringCellValue.lowercase(Locale.getDefault())
                    when {
                        cellValue == "name" -> nameColIndex = i
                        cellValue == "score" -> scoreColIndex = i
                        cellValue == "grade" -> gradeColIndex = i
                    }
                }
            }

            // Verify required columns exist
            if (nameColIndex == -1 || scoreColIndex == -1 || gradeColIndex == -1) {
                println("Error: File must contain 'Name', 'Score', and 'Grade' columns")
                return
            }

            // Process each row (skip header row)
            var rowCount = 0
            var processedCount = 0

            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                rowCount++

                // Get name and score
                val nameCell = row.getCell(nameColIndex)
                val scoreCell = row.getCell(scoreColIndex)

                if (nameCell == null || scoreCell == null) {
                    continue // Skip rows with missing data
                }

                val name = nameCell.stringCellValue

                // Handle score (could be numeric or string)
                val scoreValue = when (scoreCell.cellType) {
                    CellType.NUMERIC -> scoreCell.numericCellValue
                    CellType.STRING -> {
                        try {
                            scoreCell.stringCellValue.toDouble()
                        } catch (e: NumberFormatException) {
                            null
                        }
                    }
                    else -> null
                }

                // Calculate grade - FIXED
                val grade = if (scoreValue == null) {
                    "Invalid Score"
                } else {
                    // Use if-else chain instead of when with operators
                    if (scoreValue >= 80.0) {
                        "A"
                    } else if (scoreValue >= 60.0) {
                        "B"
                    } else if (scoreValue >= 50.0) {
                        "C"
                    } else if (scoreValue >= 40.0) {
                        "D"
                    } else {
                        "F"
                    }
                }

                // Write grade to grade column
                var gradeCell = row.getCell(gradeColIndex)
                if (gradeCell == null) {
                    gradeCell = row.createCell(gradeColIndex)
                }
                gradeCell.setCellValue(grade)
                processedCount++
            }

            // Save the file
            val outputPath = if (filePath.lowercase(Locale.getDefault()).endsWith(".xlsx")) {
                filePath.replace(".xlsx", "_graded.xlsx")
            } else {
                filePath + "_graded.xlsx"
            }

            FileOutputStream(outputPath).use { fileOut ->
                workbook.write(fileOut)
            }

            println("\nProcessing complete!")
            println("Total rows processed: $processedCount out of $rowCount")
            println("Graded file saved as: $outputPath")

            workbook.close()
        }

    } catch (e: Exception) {
        println("Error processing file: ${e.message}")
        e.printStackTrace()
    }
}