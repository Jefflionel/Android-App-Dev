import org.apache.poi.ss.usermodel.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

fun main() {
    val scanner = Scanner(System.`in`)

    print("Please enter the path to your Excel file: ")
    val filePath = scanner.nextLine().trim()

    try {
        FileInputStream(filePath).use { fis ->
            WorkbookFactory.create(fis).use { workbook ->
                val sheet = workbook.getSheetAt(0)

                val headerRow = sheet.getRow(0)
                    ?: run {
                        println("Error: Sheet is empty.")
                        return
                    }

                val (nameCol, scoreCol, gradeCol) = findColumnIndices(headerRow)

                if (nameCol == -1 || scoreCol == -1 || gradeCol == -1) {
                    println("Error: File must contain 'Name', 'Score', and 'Grade' columns")
                    return
                }

                val (rowCount, processedCount) = processRows(
                    sheet,
                    nameCol,
                    scoreCol,
                    gradeCol,
                    parseScore = ::parseScoreCell,
                    gradeFromScore = gradeCalculator()
                )

                val outputPath = buildOutputPath(filePath)

                FileOutputStream(outputPath).use { fos ->
                    workbook.write(fos)
                }

                println("\nProcessing complete!")
                println("Total rows processed: $processedCount out of $rowCount")
                println("Graded file saved as: $outputPath")
            }
        }
    } catch (e: Exception) {
        println("Error processing file: ${e.message}")
        e.printStackTrace()
    }
}

/** Find column indices using a functional style */
private fun findColumnIndices(headerRow: Row): Triple<Int, Int, Int> {
    val lower = Locale.getDefault()
    val indices = (0 until headerRow.lastCellNum.toInt())
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

/**
 * Process rows using higher-order functions:
 * - parseScore: (Cell) -> Double?
 * - gradeFromScore: (Double?) -> String
 *
 * Returns Pair(rowCount, processedCount)
 */
private fun processRows(
    sheet: Sheet,
    nameCol: Int,
    scoreCol: Int,
    gradeCol: Int,
    parseScore: (Cell) -> Double?,
    gradeFromScore: (Double?) -> String
): Pair<Int, Int> {
    var rowCount = 0
    var processedCount = 0

    // iterate rows functionally but keep counters for exact behavior
    (1..sheet.lastRowNum).forEach { i ->
        val row = sheet.getRow(i) ?: return@forEach
        rowCount++

        val nameCell = row.getCell(nameCol)
        val scoreCell = row.getCell(scoreCol)

        if (nameCell == null || scoreCell == null) return@forEach

        // preserve original behavior: read name (not used further, but kept)
        val name = nameCell.stringCellValue

        val scoreValue = parseScore(scoreCell)

        val grade = gradeFromScore(scoreValue)

        val gradeCell = row.getCell(gradeCol) ?: row.createCell(gradeCol)
        gradeCell.setCellValue(grade)

        processedCount++
    }

    return Pair(rowCount, processedCount)
}

/** Parse score cell (kept as a function to pass as lambda) */
private fun parseScoreCell(cell: Cell): Double? {
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

/**
 * Return a grade calculator as a lambda.
 * This keeps the same thresholds and logic but packages it as a higher-order function.
 */
private fun gradeCalculator(): (Double?) -> String = { score ->
    if (score == null) {
        "Invalid Score"
    } else {
        if (score >= 80.0) {
            "A"
        } else if (score >= 60.0) {
            "B"
        } else if (score >= 50.0) {
            "C"
        } else if (score >= 40.0) {
            "D"
        } else {
            "F"
        }
    }
}

/** Build output path (unchanged logic) */
private fun buildOutputPath(inputPath: String): String {
    return if (inputPath.lowercase(Locale.getDefault()).endsWith(".xlsx")) {
        inputPath.replace(".xlsx", "_graded.xlsx")
    } else {
        inputPath + "_graded.xlsx"
    }
}
