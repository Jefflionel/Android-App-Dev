package com.example.gradecalculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultsScreen(
        result: ProcessingResult,
        viewModel: GradeCalculatorViewModel,
        onNavigateToImport: () -> Unit,
        onClose: () -> Unit
) {
    val context = LocalContext.current

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
                text = "RESULTS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
        )

        // Success Message
        Text(
                text = "✅ Grade calculation complete!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 16.dp)
        )

        // Statistics Section
        Text(
                text = "📈 STATISTICS",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
                text = "Total students: ${result.statistics.totalStudents}",
                fontSize = 12.sp,
                modifier = Modifier.padding(4.dp),
                color = Color.DarkGray
        )

        Text(
                text = "Average score: ${result.statistics.averageScore}",
                fontSize = 12.sp,
                modifier = Modifier.padding(4.dp),
                color = Color.DarkGray
        )

        Text(
                text = "Pass rate: ${result.statistics.passRate.toInt()}%",
                fontSize = 12.sp,
                modifier = Modifier.padding(4.dp, bottom = 16.dp),
                color = Color.DarkGray
        )

        // Grade Breakdown Section
        Text(
                text = "📊 GRADE BREAKDOWN",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
        )

        result.statistics.gradeBreakdown.forEach { (grade, count) ->
            val gradeThresholds =
                    when (grade) {
                        "A" -> "80+"
                        "B" -> "60-79"
                        "C" -> "50-59"
                        "D" -> "40-49"
                        "F" -> "<40"
                        else -> ""
                    }

            Text(
                    text = "$grade ($gradeThresholds): $count students",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(4.dp),
                    color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Export Section
        Text(
                text = "💾 EXPORT",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
                text = "Format: ${result.exportFormat.name} (.${result.exportFormat.extension})",
                fontSize = 12.sp,
                modifier = Modifier.padding(4.dp, bottom = 12.dp),
                color = Color.DarkGray
        )

        Button(
                onClick = {
                    val exportedFile = viewModel.exportFile()
                    if (exportedFile != null) {
                        // Show success message and share option
                        val exportService = ExportService(context)
                        val shareIntent = exportService.shareFile(exportedFile)
                        context.startActivity(shareIntent)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors =
                        ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF2196F3),
                                contentColor = Color.White
                        )
        ) { Text("SAVE FILE", fontWeight = FontWeight.Bold) }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                    onClick = {
                        viewModel.newFile()
                        onNavigateToImport()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF2196F3),
                                    contentColor = Color.White
                            )
            ) { Text("NEW FILE", fontWeight = FontWeight.Bold) }

            Button(
                    onClick = onClose,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor = Color.LightGray,
                                    contentColor = Color.Black
                            )
            ) { Text("CLOSE", fontWeight = FontWeight.Bold) }
        }
    }
}
