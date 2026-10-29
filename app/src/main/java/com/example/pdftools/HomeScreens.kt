@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pdftools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun Tool.icon(): ImageVector = when (this) {
    Tool.MERGE -> AppIcons.NoteAdd
    Tool.SPLIT -> AppIcons.ContentCut
    Tool.COMPRESS -> AppIcons.Compress
    Tool.OCR -> AppIcons.DocumentScanner
    Tool.PDF_TO_IMAGE -> AppIcons.Image
    Tool.IMAGE_TO_PDF -> AppIcons.PictureAsPdf
}

fun Tool.accent(): Color = when (this) {
    Tool.MERGE -> Blue
    Tool.SPLIT -> Color(0xFF7C3AED)
    Tool.COMPRESS -> Color(0xFF009F91)
    Tool.OCR -> Color(0xFFCB8700)
    Tool.PDF_TO_IMAGE -> Color(0xFF0789EA)
    Tool.IMAGE_TO_PDF -> Color(0xFFED404C)
}

@Composable
fun AppHeader(title: String, onBack: (() -> Unit)? = null, action: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) IconButton(onClick = onBack, modifier = Modifier.padding(end = 8.dp)
            .background(Color(0xFFE8F1FF), CircleShape)) { Icon(AppIcons.ArrowBack, "Back", tint = Blue) }
        else Icon(AppIcons.Description, null, Modifier.padding(8.dp).size(32.dp), tint = Blue)
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        action()
    }
}

@Composable
fun PrimaryButton(label: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick, modifier.fillMaxWidth().heightIn(min = 54.dp).clip(RoundedCornerShape(18.dp))
        .background(if (enabled) OceanGradient else Brush.linearGradient(listOf(Color(0xFFD8E3F4), Color(0xFFD8E3F4)))),
        enabled = enabled, shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent,
            disabledContentColor = Muted),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) { delay(1000); onDone() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(color = Blue, shape = RoundedCornerShape(28.dp), shadowElevation = 8.dp) {
                Icon(AppIcons.Description, null, Modifier.padding(25.dp).size(62.dp), tint = Color.White)
            }
            Spacer(Modifier.height(28.dp))
            Text("PDF Tools", style = MaterialTheme.typography.headlineLarge)
            Text("Every PDF. Simplified.", color = Muted, modifier = Modifier.padding(top = 8.dp))
        }
        Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            Text("Preparing your workspace", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val headings = listOf("Make PDFs work for you", "Turn scans into text", "From PDFs to pictures")
    val descriptions = listOf("Merge files, split pages, and compress PDFs in a few simple steps.",
        "Extract text from scanned PDFs and images with OCR.",
        "Convert PDF pages into images, or combine your images into a PDF.")
    Column(Modifier.fillMaxSize()) {
        AppHeader("PDF Tools") { if (page < 2) TextButton(onClick = onDone) { Text("Skip") } }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            OnboardingArt(page, Modifier.sizeIn(maxWidth = 300.dp, maxHeight = 300.dp))
            Spacer(Modifier.height(36.dp))
            Text(headings[page], style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Text(descriptions[page], style = MaterialTheme.typography.bodyLarge, color = Muted, textAlign = TextAlign.Center)
        }
        Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.Center) {
            repeat(3) { dot -> Box(Modifier.padding(5.dp).size(if (dot == page) 30.dp else 9.dp, 9.dp)
                .clip(CircleShape).background(if (dot == page) Blue else Color(0xFFC8DCFF))) }
        }
        PrimaryButton(if (page == 2) "Get started" else "Continue", Modifier.padding(horizontal = 22.dp)) {
            if (page == 2) onDone() else page++
        }
        Text("${page + 1} of 3", color = Muted, textAlign = TextAlign.Center, fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(18.dp))
    }
}

@Composable
fun HomeScreen(
    state: AppState, tab: String, access: Boolean, onTab: (String) -> Unit,
    onTool: (Tool) -> Unit, onSettings: () -> Unit, onOpen: (LocalDocument) -> Unit,
    onRefresh: () -> Unit, onRename: (LocalDocument, String) -> Unit, onDelete: (LocalDocument) -> Unit,
    onViewAll: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var menu by remember { mutableStateOf<LocalDocument?>(null) }
    var rename by remember { mutableStateOf<LocalDocument?>(null) }
    var delete by remember { mutableStateOf<LocalDocument?>(null) }
    var newName by rememberSaveable { mutableStateOf("") }
    val pdfs = state.documents.filter { it.isPdf }
    Scaffold(containerColor = CanvasColor, contentWindowInsets = WindowInsets(0, 0, 0, 0), bottomBar = {
        NavigationBar(containerColor = Color.White, tonalElevation = 0.dp, windowInsets = WindowInsets(0, 0, 0, 0)) {
            listOf("Home" to AppIcons.Home, "Files" to AppIcons.FolderOpen, "History" to AppIcons.History).forEach { (label, icon) ->
                NavigationBarItem(selected = tab == label, onClick = { onTab(label) }, icon = { Icon(icon, null) }, label = { Text(label) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Blue, selectedTextColor = Blue,
                        indicatorColor = Color(0xFFEAF2FF), unselectedIconColor = Muted, unselectedTextColor = Muted))
            }
        }
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 20.dp)) {
            item { AppHeader(if (tab == "Home") "PDF Tools" else tab) {
                IconButton(onClick = onRefresh) { Icon(AppIcons.Refresh, "Refresh files", tint = Muted) }
                IconButton(onClick = onSettings, modifier = Modifier.background(Color(0xFFE8F1FF), RoundedCornerShape(14.dp))) {
                    Icon(AppIcons.Settings, "Settings", Modifier.size(23.dp), tint = Blue)
                }
            } }
            if (tab == "Home") {
                item { SummaryCard(pdfs, access, state.scanning) }
                item {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Tool.entries.chunked(3).forEach { tools ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                tools.forEach { tool -> ToolTile(tool, Modifier.weight(1f)) { onTool(tool) } }
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Recent", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        TextButton(onClick = onViewAll) { Text("View all") }
                    }
                }
            } else if (tab == "Files") {
                item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Search PDF files") }, leadingIcon = { Icon(AppIcons.Search, null) },
                    singleLine = true, shape = RoundedCornerShape(16.dp)) }
            }
            val visible = when (tab) {
                "Home" -> pdfs.take(5)
                "Files" -> pdfs.filter { it.name.contains(query, true) }
                else -> state.history
            }
            if (state.scanning) item { LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) }
            if (visible.isEmpty()) item {
                EmptyState(if (!access && tab != "History") "Your documents will appear here" else if (tab == "History") "No completed tasks yet" else "No PDFs found",
                    if (!access && tab != "History") "Tap a tool to allow file access." else if (tab == "History") "Merged and converted results appear in History." else "Add PDFs to shared storage, then refresh.")
            }
            items(visible, key = { it.path }) { document ->
                Box(Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    DocumentRow(document, onOpen = { onOpen(document) }, onMenu = { menu = document })
                    DropdownMenu(expanded = menu?.path == document.path, onDismissRequest = { menu = null }) {
                        DropdownMenuItem(text = { Text("Open") }, onClick = { menu = null; onOpen(document) })
                        if (tab != "History") {
                            DropdownMenuItem(text = { Text("Rename") }, onClick = { menu = null; rename = document; newName = document.name })
                            DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) }, onClick = { menu = null; delete = document })
                        }
                    }
                }
            }
        }
    }
    rename?.let { document -> AlertDialog(onDismissRequest = { rename = null }, title = { Text("Rename file") },
        text = { OutlinedTextField(newName, { newName = it }, label = { Text("Filename") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onRename(document, newName); rename = null }) { Text("Rename") } },
        dismissButton = { TextButton(onClick = { rename = null }) { Text("Cancel") } }) }
    delete?.let { document -> AlertDialog(onDismissRequest = { delete = null }, title = { Text("Delete file?") },
        text = { Text("${document.name} will be permanently deleted from your device.") },
        confirmButton = { TextButton(onClick = { onDelete(document); delete = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { delete = null }) { Text("Cancel") } }) }
}

@Composable
private fun SummaryCard(pdfs: List<LocalDocument>, access: Boolean, scanning: Boolean) {
    val today = LocalDate.now()
    val added = pdfs.count { Instant.ofEpochMilli(it.modified).atZone(ZoneId.systemDefault()).toLocalDate() == today }
    OceanCard(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Hello there!", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text("Your PDF overview", color = Color.White.copy(alpha = .8f), fontSize = 13.sp, modifier = Modifier.padding(top = 3.dp))
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth()) {
                val values = listOf(if (access) pdfs.size.toString() else "—", if (access) formatBytes(pdfs.sumOf { it.bytes }) else "—", if (access) added.toString() else "—")
                val icons = listOf(AppIcons.Description, AppIcons.Storage, AppIcons.Schedule)
                val labels = listOf("PDF files", "Total size", "Added today")
                values.forEachIndexed { index, value ->
                    if (index > 0) Box(Modifier.padding(top = 4.dp).width(1.dp).height(70.dp).background(Color.White.copy(alpha = .25f)))
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icons[index], null, Modifier.size(25.dp), tint = Color.White)
                        Text(if (scanning) "…" else value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White,
                            maxLines = 1, modifier = Modifier.padding(top = 7.dp))
                        Text(labels[index], color = Color.White.copy(alpha = .82f), fontSize = 11.sp)
                    }
                }
            }
    }
}

@Composable
private fun ToolTile(tool: Tool, modifier: Modifier, onClick: () -> Unit) {
    val accent = tool.accent()
    Surface(onClick = onClick, modifier = modifier.heightIn(min = 134.dp), shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Line), color = Color.White, shadowElevation = 1.dp) {
        Column(Modifier.background(Brush.linearGradient(listOf(Color.White, accent.copy(alpha = .045f)))).padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Surface(shape = RoundedCornerShape(18.dp), color = accent.copy(alpha = .08f)) {
                Icon(tool.icon(), null, Modifier.padding(13.dp).size(32.dp), tint = accent)
            }
            Spacer(Modifier.height(10.dp))
            Text(tool.title, fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                minLines = 2, maxLines = 2)
        }
    }
}

@Composable
fun DocumentRow(document: LocalDocument, onOpen: () -> Unit, onMenu: (() -> Unit)? = null, selected: Boolean = false) {
    Surface(onClick = onOpen, shape = RoundedCornerShape(20.dp), color = if (selected) Color(0xFFE8F1FF) else Color.White,
        border = BorderStroke(1.dp, if (selected) Blue else Line)) {
        Row(Modifier.fillMaxWidth().padding(start = 13.dp, top = 12.dp, bottom = 12.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (document.isPdf) AppIcons.PictureAsPdf else AppIcons.InsertDriveFile, null,
                Modifier.size(32.dp), tint = if (document.isPdf) Color(0xFFE44951) else Blue)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(document.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(formatBytes(document.bytes), color = Muted, fontSize = 12.sp)
            }
            if (onMenu != null) IconButton(onClick = onMenu) { Icon(AppIcons.MoreVert, "File actions", tint = Muted) }
            else Checkbox(checked = selected, onCheckedChange = { onOpen() })
        }
    }
}

@Composable
fun EmptyState(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(AppIcons.FolderOpen, null, Modifier.size(42.dp), tint = Color(0xFFAAC4EA))
        Text(title, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp))
        Text(body, textAlign = TextAlign.Center, color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
fun FileBrowserScreen(state: AppState, tool: Tool, onBack: () -> Unit, onRefresh: () -> Unit, onDone: (List<LocalDocument>) -> Unit) {
    var query by rememberSaveable(tool) { mutableStateOf("") }
    var selected by rememberSaveable(tool) { mutableStateOf(state.selected.map { it.path }) }
    val visible = state.documents.filter { tool.accepts(it.file) && it.name.contains(query, true) }
    Column(Modifier.fillMaxSize()) {
        AppHeader(tool.title, onBack) {
            IconButton(onClick = onRefresh) { Icon(AppIcons.Refresh, "Refresh") }
        }
        Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) { ToolIntro(tool, selection = true) }
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search files") }, leadingIcon = { Icon(AppIcons.Search, null) },
            shape = RoundedCornerShape(16.dp), singleLine = true)
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            SectionLabel(if (tool == Tool.IMAGE_TO_PDF) "Images" else "Available files", Modifier.weight(1f))
            Text("${selected.size} selected", color = Blue, style = MaterialTheme.typography.labelMedium)
        }
        if (state.scanning) LinearProgressIndicator(Modifier.fillMaxWidth())
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            if (visible.isEmpty()) item { EmptyState("No matching files", "Try another search or refresh your library.") }
            items(visible, key = { it.path }) { document ->
                DocumentRow(document, selected = document.path in selected, onOpen = {
                    selected = if (tool.multiple) {
                        if (document.path in selected) selected - document.path else selected + document.path
                    } else if (document.path in selected) emptyList() else listOf(document.path)
                })
            }
        }
        ActionFooter { PrimaryButton("Continue${if (selected.isNotEmpty()) " (${selected.size})" else ""}",
            enabled = selected.isNotEmpty() && (tool != Tool.MERGE || selected.size >= 2)) {
            val files = selected.mapNotNull { path -> state.documents.find { it.path == path } }
            onDone(files)
        } }
    }
}

