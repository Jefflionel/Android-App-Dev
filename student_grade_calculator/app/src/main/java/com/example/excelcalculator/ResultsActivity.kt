package com.example.excelcalculator

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.excelcalculator.databinding.ActivityResultsBinding
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultsBinding
    private lateinit var students: ArrayList<Student>

    private val createExcelLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? -> uri?.let { saveExcelToUri(it) } }

    private val createCsvLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? -> uri?.let { saveCsvToUri(it) } }

    private val createPdfLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? -> uri?.let { savePdfToUri(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        students = intent.getParcelableArrayListExtra("students") ?: arrayListOf()

        setupSummary()

        binding.btnExport.setOnClickListener {
            showExportDialog()
        }
    }

    private fun setupSummary() {
        binding.tvTotalStudents.text = "Total Students: ${students.size}"
        val classAverage = if (students.isNotEmpty()) {
            students.map { it.average }.average()
        } else 0.0
        binding.tvClassAverage.text = "Class Average: %.2f".format(classAverage)
    }

    private fun showExportDialog() {
        val options = arrayOf("Excel (.xlsx)", "CSV (.csv)", "PDF Document (.pdf)")

        AlertDialog.Builder(this)
            .setTitle("Choose Export Format")
            .setItems(options) { _, which ->
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                when (which) {
                    0 -> createExcelLauncher.launch("GPA_Results_$timestamp.xlsx")
                    1 -> createCsvLauncher.launch("GPA_Results_$timestamp.csv")
                    2 -> createPdfLauncher.launch("GPA_Results_$timestamp.pdf")
                }
            }
            .show()
    }

    private fun saveExcelToUri(uri: Uri) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("GPA Results")

            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("Name")
            headerRow.createCell(1).setCellValue("GPA")

            students.forEachIndexed { index, student ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(student.name)
                row.createCell(1).setCellValue(student.gpa)
            }

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()
            Toast.makeText(this, "Excel file saved!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveCsvToUri(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                writer.write("Name,GPA\n")
                students.forEach { student ->
                    val safeName = if (student.name.contains(",")) "\"${student.name}\"" else student.name
                    writer.write("$safeName,${student.gpa}\n")
                }
            }
            Toast.makeText(this, "CSV file saved!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun savePdfToUri(uri: Uri) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint().apply { textSize = 16f }
            val titlePaint = Paint().apply {
                textSize = 24f
                isFakeBoldText = true
            }

            var yPosition = 50f
            canvas.drawText("Class GPA Results", 50f, yPosition, titlePaint)
            yPosition += 40f

            canvas.drawText("Name", 50f, yPosition, paint)
            canvas.drawText("GPA", 400f, yPosition, paint)

            yPosition += 10f
            canvas.drawLine(50f, yPosition, 500f, yPosition, paint)
            yPosition += 20f

            students.forEach { student ->
                if (yPosition > 800f) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 50f
                }
                canvas.drawText(student.name, 50f, yPosition, paint)

                // Color the PDF text based on the grade!
                paint.color = GpaCalculator.getGpaColor(student.gpa)
                canvas.drawText(student.gpa, 400f, yPosition, paint)
                paint.color = android.graphics.Color.BLACK // Reset to black for next name

                yPosition += 25f
            }

            pdfDocument.finishPage(page)

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()
            Toast.makeText(this, "PDF file saved!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}