package com.example.gradecalculator

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ImportScreen(viewModel: GradeCalculatorViewModel, onNavigateToResults: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val state = (uiState as? UIState.ImportScreen) ?: return

    var selectedFormat by remember { mutableStateOf(ExportFormat.XLSX) }

    val filePickerLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri
                ->
                if (uri != null) {
                    viewModel.selectFile(uri)
                }
            }

    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(Color.White)
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
            verticalArrangement = Arrangement.Top
    ) {
        // Title
        Text(
                text = "STUDENT GRADE CALCULATOR",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
        )

        // File Selection Section
        Text(
                text = "📁 FILE",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors =
                        ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF2196F3),
                                contentColor = Color.White
                        )
        ) { Text("CHOOSE FILE", fontSize = 14.sp, fontWeight = FontWeight.Bold) }

        if (state.selectedFilePath != null) {
            Text(
                    text = "Selected: ${state.selectedFilePath?.split("/")?.lastOrNull() ?: ""}",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Preview Section
        if (state.previewStudents.isNotEmpty()) {
            Text(
                    text = "📊 PREVIEW (first 5 rows)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
            )

            state.previewStudents.forEachIndexed { index, student ->
                Text(
                        text =
                                "${index + 1}. ${student.name} - ${student.score.toInt()} → ${student.grade}",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(4.dp),
                        color = Color.DarkGray
                )
            }

            Text(
                    text =
                            "Showing ${state.previewStudents.size} of ${state.totalStudents} total students",
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Export Format Section
        Text(
                text = "💾 EXPORT FORMAT",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
        )

        ExportFormat.values().forEach { format ->
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                RadioButton(
                        selected = selectedFormat == format,
                        onClick = {
                            selectedFormat = format
                            viewModel.setExportFormat(format)
                        }
                )
                Text(
                        text = "${format.name} (.${format.extension})",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Error Message
        if (state.error != null) {
            Text(
                    text = state.error,
                    fontSize = 12.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Buttons
        Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                    onClick = { viewModel.clearFile() },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor = Color.LightGray,
                                    contentColor = Color.Black
                            )
            ) { Text("CLEAR", fontWeight = FontWeight.Bold) }

            Button(
                    onClick = {
                        viewModel.processFile()
                        onNavigateToResults()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF4CAF50),
                                    contentColor = Color.White
                            ),
                    enabled = state.selectedFilePath != null && state.previewStudents.isNotEmpty()
            ) { Text("PROCESS", fontWeight = FontWeight.Bold) }
        }
    }
}
