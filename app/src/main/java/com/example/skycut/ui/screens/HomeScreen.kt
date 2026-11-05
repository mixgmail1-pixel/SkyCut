package com.example.skycut.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.skycut.model.AspectRatio
import com.example.skycut.model.SkyCutProject
import com.example.skycut.viewmodel.HomeViewModel
import com.example.ui.theme.AccentPurpleFX
import com.example.ui.theme.AccentRedCut
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenProject: (String) -> Unit,
    onOpenAiTools: () -> Unit,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Projects, 1 = Templates

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.skycut_icon),
                            contentDescription = "SkyCut Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SkyCut",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = SkyBluePrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SkyBluePrimary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "PRO",
                                        color = SkyBluePrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "4K Multi-Track Video Studio",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenAiTools,
                        modifier = Modifier.testTag("home_ai_tools_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Studio", tint = SkyBluePrimary)
                    }
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("home_theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF090E17)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewProjectDialog = true },
                containerColor = SkyBluePrimary,
                contentColor = Color(0xFF090E17),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Cut", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("home_fab_new_project")
            )
        },
        containerColor = Color(0xFF090E17)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.skycut_hero),
                    contentDescription = "SkyCut Hero",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF090E17).copy(alpha = 0.85f), Color.Transparent)
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "SkyCut Video Studio",
                        color = SkyBluePrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Create Without Limits",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Real 4K multi-track timeline & MP4 engine",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Quick Access Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onOpenAiTools,
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, SkyBluePrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f).testTag("quick_ai_captions_button")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ClosedCaption, contentDescription = null, tint = SkyBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Auto Captions", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("AI Subtitles", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        }
                    }
                }

                Surface(
                    onClick = {
                        val proj = viewModel.createFromTemplate("Travel")
                        onOpenProject(proj.id)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.weight(1f).testTag("quick_template_button")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DashboardCustomize, contentDescription = null, tint = Color(0xFFFBBF24))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Templates", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Vlog & Reels", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Bar: Drafts & Projects vs Trending Templates
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = SkyBluePrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Recent Drafts (${projects.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Templates", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // Drafts List
                if (projects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No projects yet", color = Color(0xFF94A3B8), fontSize = 14.sp)
                            Text("Tap New Cut below to start editing", color = Color(0xFF64748B), fontSize = 12.sp)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        projects.forEach { project ->
                            ProjectCardItem(
                                project = project,
                                onClick = { onOpenProject(project.id) },
                                onDuplicate = { viewModel.duplicateProject(project) },
                                onDelete = { viewModel.deleteProject(project.id) }
                            )
                        }
                    }
                }
            } else {
                // Templates Cards
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TemplateCard(
                        title = "Pacific Coast Sunset Vlog",
                        category = "Travel & Nature • 9:16 Shorts",
                        imageRes = R.drawable.template_travel,
                        onClick = {
                            val p = viewModel.createFromTemplate("Travel")
                            onOpenProject(p.id)
                        }
                    )
                    TemplateCard(
                        title = "Neo Tokyo Cyberpunk Run",
                        category = "Action & Beats • 9:16 TikTok",
                        imageRes = R.drawable.template_urban,
                        onClick = {
                            val p = viewModel.createFromTemplate("Cyberpunk")
                            onOpenProject(p.id)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        NewProjectDialog(
            onConfirm = { title, aspect ->
                showNewProjectDialog = false
                val newProj = viewModel.createNewProject(title, aspect)
                onOpenProject(newProj.id)
            },
            onDismiss = { showNewProjectDialog = false }
        )
    }
}

@Composable
private fun ProjectCardItem(
    project: SkyCutProject,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(project.updatedAt) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(project.updatedAt))
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF131D2E),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_item_${project.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail / Icon box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                val sampleRes = project.mainVideoClips.firstOrNull()?.thumbnailRes
                if (sampleRes != null) {
                    Image(
                        painter = painterResource(id = sampleRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = project.aspectRatio.label,
                            color = SkyBluePrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${project.totalDurationMs / 1000}s • $dateStr",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            // Quick actions
            IconButton(onClick = onDuplicate) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRedCut, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun TemplateCard(
    title: String,
    category: String,
    imageRes: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF131D2E),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(category, color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
                ) {
                    Text("Use", color = Color(0xFF090E17), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun NewProjectDialog(
    onConfirm: (String, AspectRatio) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("New Project") }
    var selectedAspect by remember { mutableStateOf(AspectRatio.RATIO_9_16) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131D2E),
        titleContentColor = Color.White,
        title = { Text("Create New Project", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SkyBluePrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("new_project_title_input")
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text("Aspect Ratio", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AspectRatio.values().forEach { aspect ->
                        val isSel = aspect == selectedAspect
                        Surface(
                            onClick = { selectedAspect = aspect },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) SkyBluePrimary else Color(0xFF1E293B),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = aspect.label.takeWhile { it != ' ' },
                                color = if (isSel) Color(0xFF090E17) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title.ifBlank { "Untitled" }, selectedAspect) },
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                modifier = Modifier.testTag("confirm_create_project_button")
            ) {
                Text("Create Cut", color = Color(0xFF090E17), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}
