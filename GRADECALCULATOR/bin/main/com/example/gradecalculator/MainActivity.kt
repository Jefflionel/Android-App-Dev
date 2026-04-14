package com.example.gradecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) { GradeCalculatorApp(context = this) }
            }
        }
    }
}

@Composable
fun GradeCalculatorApp(context: ComponentActivity) {
    val viewModel = remember { GradeCalculatorViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    var currentScreen by remember { mutableStateOf<String>("import") }

    when (uiState) {
        is UIState.ImportScreen -> {
            ImportScreen(viewModel = viewModel, onNavigateToResults = { currentScreen = "results" })
        }
        is UIState.ResultsScreen -> {
            val resultsState = uiState as UIState.ResultsScreen
            ResultsScreen(
                    result = resultsState.result,
                    viewModel = viewModel,
                    onNavigateToImport = { currentScreen = "import" },
                    onClose = { context.finish() }
            )
        }
        is UIState.Processing -> {
            // Show loading screen or keep current screen
            Text("Processing...")
        }
    }
}
