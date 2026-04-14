package com.example.gradecalculator

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.StringWriter
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.poi.ss.usermodel.WorkbookFactory

class ExportService(private val context: Context) {

    fun exportToFormat(
            inputFilePath: String,
            students: List<StudentRecord>,
            format: ExportFormat,
            outputFileName: String
    ): File {
        return when (format) {
            ExportFormat.XLSX -> exportToXlsx(inputFilePath, students, outputFileName)
            ExportFormat.CSV -> exportToCsv(students, outputFileName)
            ExportFormat.PDF -> exportToPdf(students, outputFileName)
        }
    }

    private fun exportToXlsx(
            inputFilePath: String,
            students: List<StudentRecord>,
            outputFileName: String
    ): File {
        val outputFile = File(context.getExternalFilesDir(null), outputFileName)

        FileInputStream(inputFilePath).use { fis ->
            WorkbookFactory.create(fis).use { workbook ->
                val sheet = workbook.getSheetAt(0)
                val gradeColIndex = 2 // Assuming Grade is in column 3

                students.forEachIndexed { index, student ->
                    val row = sheet.getRow(index + 1) ?: sheet.createRow(index + 1)
                    val gradeCell = row.getCell(gradeColIndex) ?: row.createCell(gradeColIndex)
                    gradeCell.setCellValue(student.grade)
                }

                FileOutputStream(outputFile).use { fos -> workbook.write(fos) }
            }
        }

        return outputFile
    }

    private fun exportToCsv(students: List<StudentRecord>, outputFileName: String): File {
        val outputFile = File(context.getExternalFilesDir(null), outputFileName)

        StringWriter().use { sw ->
            CSVPrinter(sw, CSVFormat.DEFAULT.withHeader("Name", "Score", "Grade")).use { printer ->
                students.forEach { student ->
                    printer.printRecord(student.name, student.score, student.grade)
                }
            }

            outputFile.writeText(sw.toString())
        }

        return outputFile
    }

    private fun exportToPdf(students: List<StudentRecord>, outputFileName: String): File {
        val outputFile = File(context.getExternalFilesDir(null), outputFileName)

        PdfWriter(outputFile).use { writer ->
            Document(com.itextpdf.kernel.pdf.PdfDocument(writer)).use { doc ->
                doc.add(Paragraph("Student Grades Report"))
                doc.add(Paragraph(" "))

                // Create table
                val table = Table(floatArrayOf(3f, 2f, 1f))
                table.addCell(Cell().add(Paragraph("Name")))
                table.addCell(Cell().add(Paragraph("Score")))
                table.addCell(Cell().add(Paragraph("Grade")))

                students.forEach { student ->
                    table.addCell(Cell().add(Paragraph(student.name)))
                    table.addCell(Cell().add(Paragraph(student.score.toString())))
                    table.addCell(Cell().add(Paragraph(student.grade)))
                }

                doc.add(table)
            }
        }

        return outputFile
    }

    fun shareFile(file: File): Intent {
        val uri: Uri =
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val intent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

        return Intent.createChooser(intent, "Share File")
    }
}
