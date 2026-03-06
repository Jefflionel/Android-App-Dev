package com.example.gradecalculator.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.gradecalculator.model.StudentGrade
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.io.OutputStream

object ExcelHandler {

    fun readStudentsFromUri(
        context: Context,
        uri: Uri,
        onRowParsed: (String, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        runCatching {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Cannot open file")
            inputStream.use { stream ->
                val workbook = WorkbookFactory.create(stream)
                val sheet = workbook.getSheetAt(0)
                sheet.drop(1).forEach { row ->
                    val nameCell = row.getCell(0)
                    val markCell = row.getCell(1)
                    if (nameCell != null && markCell != null) {
                        val name = nameCell.stringCellValue.trim()
                        val mark = when (markCell.cellType) {
                            CellType.NUMERIC -> markCell.numericCellValue
                            CellType.STRING  -> markCell.stringCellValue.toDoubleOrNull() ?: -1.0
                            else             -> -1.0
                        }
                        if (name.isNotEmpty() && GradeCalculator.isValidMark(mark)) {
                            onRowParsed(name, mark)
                        }
                    }
                }
                workbook.close()
            }
        }.onFailure { e -> onError(e.message ?: "Unknown error reading file") }
    }

    fun exportToExcel(
        context: Context,
        students: List<StudentGrade>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        runCatching {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Grades")
            sheet.createRow(0).apply {
                createCell(0).setCellValue("Name")
                createCell(1).setCellValue("Mark")
                createCell(2).setCellValue("Grade")
            }
            students.mapIndexed { index, student ->
                sheet.createRow(index + 1).apply {
                    createCell(0).setCellValue(student.name)
                    createCell(1).setCellValue(student.mark)
                    createCell(2).setCellValue(student.grade)
                }
            }
            val fileName = "GradeResults_${System.currentTimeMillis()}.xlsx"
            val outputStream: OutputStream =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE,
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val outUri = context.contentResolver
                        .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        ?: throw IllegalStateException("Cannot create output file")
                    context.contentResolver.openOutputStream(outUri)
                        ?: throw IllegalStateException("Cannot open output stream")
                } else {
                    java.io.File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        fileName
                    ).outputStream()
                }
            outputStream.use { workbook.write(it) }
            workbook.close()
            onSuccess(fileName)
        }.onFailure { e -> onError(e.message ?: "Export failed") }
    }
}