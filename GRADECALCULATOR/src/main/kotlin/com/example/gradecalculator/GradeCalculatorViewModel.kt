package com.example.gradecalculator

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GradeCalculatorViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState>(UIState.ImportScreen())
    val uiState: StateFlow<UIState> = _uiState

    private val excelProcessor = ExcelProcessor()
    private val exportService = ExportService(context)

    private var selectedFileUri: Uri? = null
    private var selectedFilePath: String? = null
    private var selectedExportFormat = ExportFormat.XLSX
    private var previewStudents: List<StudentRecord> = emptyList()
    private var allStudents: List<StudentRecord> = emptyList()

    fun selectFile(uri: Uri) {
        selectedFileUri = uri
        selectedFilePath = uri.path

        // Load preview (first 5 rows)
        loadPreview(uri)
    }

    private fun loadPreview(uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream =
                        context.contentResolver.openInputStream(uri)
                                ?: throw Exception("Cannot open file")

                val students = excelProcessor.processExcelFile(inputStream)
                previewStudents = students.take(5)
                allStudents = students

                updateImportScreen()
            } catch (e: Exception) {
                _uiState.value = UIState.ImportScreen(error = "Error: ${e.message}")
            }
        }
    }

    fun setExportFormat(format: ExportFormat) {
        selectedExportFormat = format
    }

    fun processFile() {
        if (selectedFileUri == null || allStudents.isEmpty()) {
            _uiState.value = UIState.ImportScreen(error = "Please select a valid Excel file")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = UIState.Processing()

                val statistics = excelProcessor.calculateStatistics(allStudents)

                val result =
                        ProcessingResult(
                                students = allStudents,
                                statistics = statistics,
                                exportFormat = selectedExportFormat
                        )

                _uiState.value = UIState.ResultsScreen(result = result)
            } catch (e: Exception) {
                _uiState.value = UIState.ImportScreen(error = "Error: ${e.message}")
            }
        }
    }

    fun exportFile(): File? {
        return try {
            val currentState = _uiState.value
            if (currentState !is UIState.ResultsScreen) return null

            val outputFileName = "students_graded.${currentState.result.exportFormat.extension}"
            val currentResult = currentState.result

            val outputFile =
                    getRealPathFromUri(selectedFileUri!!)?.let { realPath ->
                        exportService.exportToFormat(
                                realPath,
                                currentResult.students,
                                currentResult.exportFormat,
                                outputFileName
                        )
                    }

            outputFile
        } catch (e: Exception) {
            null
        }
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex =
                            it.getColumnIndex(android.provider.MediaStore.Images.ImageColumns.DATA)
                    if (columnIndex != -1) {
                        return@use it.getString(columnIndex)
                    }
                }
            }
            uri.path
        } catch (e: Exception) {
            uri.path
        }
    }

    fun newFile() {
        selectedFileUri = null
        selectedFilePath = null
        previewStudents = emptyList()
        allStudents = emptyList()
        selectedExportFormat = ExportFormat.XLSX
        updateImportScreen()
    }

    fun clearFile() {
        selectedFileUri = null
        selectedFilePath = null
        previewStudents = emptyList()
        allStudents = emptyList()
        updateImportScreen()
    }

    private fun updateImportScreen() {
        _uiState.value =
                UIState.ImportScreen(
                        selectedFilePath = selectedFilePath,
                        previewStudents = previewStudents,
                        totalStudents = allStudents.size
                )
    }
}

sealed class UIState {
    data class ImportScreen(
            val selectedFilePath: String? = null,
            val previewStudents: List<StudentRecord> = emptyList(),
            val totalStudents: Int = 0,
            val error: String? = null
    ) : UIState()

    data class ResultsScreen(val result: ProcessingResult) : UIState()

    class Processing : UIState()
}
