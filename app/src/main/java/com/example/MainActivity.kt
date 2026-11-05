package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.skycut.ui.components.AiToolsDialog
import com.example.skycut.ui.screens.EditorScreen
import com.example.skycut.ui.screens.HomeScreen
import com.example.skycut.viewmodel.EditorViewModel
import com.example.skycut.viewmodel.HomeViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val editorViewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            var currentProjectId by remember { mutableStateOf<String?>(null) }
            var showAiToolsDialog by remember { mutableStateOf(false) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isDarkTheme) Color(0xFF090E17) else Color(0xFFF8FAFC)
                ) {
                    val activeProjectId = currentProjectId
                    if (activeProjectId != null) {
                        EditorScreen(
                            viewModel = editorViewModel,
                            projectId = activeProjectId,
                            onNavigateBack = { currentProjectId = null }
                        )
                    } else {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onOpenProject = { projectId -> currentProjectId = projectId },
                            onOpenAiTools = { showAiToolsDialog = true },
                            onToggleTheme = { isDarkTheme = !isDarkTheme },
                            isDarkTheme = isDarkTheme
                        )
                    }

                    if (showAiToolsDialog) {
                        AiToolsDialog(
                            onSelectScriptToVideo = { topic ->
                                val project = homeViewModel.createNewProject("AI: $topic")
                                currentProjectId = project.id
                                showAiToolsDialog = false
                            },
                            onTriggerAutoCaptions = {
                                val project = homeViewModel.createFromTemplate("Travel")
                                currentProjectId = project.id
                                showAiToolsDialog = false
                            },
                            onTriggerSmartCut = {
                                val project = homeViewModel.createFromTemplate("Cyberpunk")
                                currentProjectId = project.id
                                showAiToolsDialog = false
                            },
                            onTriggerBeatSync = {
                                val project = homeViewModel.createFromTemplate("Cyberpunk")
                                currentProjectId = project.id
                                showAiToolsDialog = false
                            },
                            onDismiss = { showAiToolsDialog = false }
                        )
                    }
                }
            }
        }
    }
}
