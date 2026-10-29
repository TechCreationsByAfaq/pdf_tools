@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pdftools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun ToolScreen(
    state: AppState, tool: Tool, onBack: () -> Unit, onAdd: () -> Unit,
    onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit,
    onRun: (String, String, Compression, Boolean, Boolean) -> Unit
) {
    var name by rememberSaveable(tool) { mutableStateOf(when (tool) {
        Tool.MERGE -> "Merged document.pdf"; Tool.SPLIT -> "Extracted pages.pdf"
        Tool.COMPRESS -> "Compressed document.pdf"; Tool.OCR -> "Recognized text.txt"
        Tool.PDF_TO_IMAGE -> "Page images.zip"; Tool.IMAGE_TO_PDF -> "Images.pdf"
    }) }
    var pages by rememberSaveable(tool) { mutableStateOf("") }
    var compressionName by rememberSaveable(tool) { mutableStateOf(Compression.BALANCED.name) }
    var png by rememberSaveable(tool) { mutableStateOf(true) }
    var splitEach by rememberSaveable(tool) { mutableStateOf(false) }
    var chooseSplitPages by rememberSaveable(tool) { mutableStateOf(true) }
    val compression = Compression.valueOf(compressionName)
    val pdfInput = state.selected.firstOrNull()?.isPdf == true
    val compact = tool == Tool.SPLIT || tool == Tool.PDF_TO_IMAGE
    Column(Modifier.fillMaxSize()) {
        AppHeader(tool.title, onBack = { if (!state.busy) onBack() })
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!compact) item { ToolIntro(tool) }
            if (compact && state.selected.isNotEmpty()) item {
                val document = state.selected.first()
                Surface(color = Color.White, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Line)) {
                    Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(AppIcons.PictureAsPdf, null, Modifier.size(26.dp), tint = Blue)
                        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                            Text(document.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(formatBytes(document.bytes), color = Muted, fontSize = 12.sp)
                        }
                        TextButton(onClick = onAdd, enabled = !state.busy) { Text("Change") }
                    }
                }
            }
            if (!compact || state.selected.isEmpty()) item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionLabel("${state.selected.size} ${if (state.selected.size == 1) "file" else "files"} selected", Modifier.weight(1f))
                    TextButton(onClick = onAdd, enabled = !state.busy) {
                        Icon(AppIcons.Add, null, Modifier.size(18.dp))
                        Text(if (tool.multiple) "Add files" else "Change file")
                    }
                }
            }
            if (!compact) itemsIndexed(state.selected, key = { _, document -> document.path }) { index, document ->
                SelectedFileRow(document, index, state.selected.size, !state.busy, tool.multiple, onRemove, onMove)
            }
            if (state.selected.isEmpty()) item { EmptyState("Select your files", "Choose files to start ${tool.title.lowercase()}.") }
            if (tool == Tool.SPLIT) item {
                WhitePanel {
                    Text("Create", fontWeight = FontWeight.SemiBold)
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        SegmentedButton(selected = !splitEach, onClick = {
                            splitEach = false
                            if (name.endsWith(".zip", true)) name = name.dropLast(4) + ".pdf"
                        }, shape = SegmentedButtonDefaults.itemShape(0, 2), enabled = !state.busy) { Text("One PDF", fontSize = 13.sp) }
                        SegmentedButton(selected = splitEach, onClick = {
                            splitEach = true
                            if (name.endsWith(".zip", true)) name = name.dropLast(4) + ".pdf"
                        }, shape = SegmentedButtonDefaults.itemShape(1, 2), enabled = !state.busy) { Text("PDF per page", fontSize = 13.sp) }
                    }
                    Text(if (splitEach) "Each page is saved as a PDF file." else "Selected pages together in one PDF.", color = Muted, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Pages", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        FilterChip(!chooseSplitPages, onClick = { chooseSplitPages = false }, label = { Text("All") }, enabled = !state.busy)
                        FilterChip(chooseSplitPages, onClick = { chooseSplitPages = true }, label = { Text("Custom") }, enabled = !state.busy)
                    }
                    if (chooseSplitPages) {
                        OutlinedTextField(pages, { pages = it }, Modifier.fillMaxWidth(), enabled = !state.busy,
                            label = { Text("Pages, e.g. 1-3, 5, 8") },
                            singleLine = true, shape = RoundedCornerShape(12.dp))
                    }
                    OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), enabled = !state.busy,
                        label = { Text("Save as") }, singleLine = true, shape = RoundedCornerShape(12.dp))
                    Text("Documents / PDF Tools", color = Muted, fontSize = 12.sp)
                }
            }
            if (pdfInput && tool == Tool.OCR) item {
                WhitePanel {
                    Text("Pages", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(pages, { pages = it }, Modifier.fillMaxWidth(), enabled = !state.busy,
                        placeholder = { Text("All pages, or 1-3, 5, 8") }, singleLine = true, shape = RoundedCornerShape(12.dp))
                    Text("Leave blank to process every page.", color = Muted, fontSize = 12.sp)
                }
            }
            if (tool == Tool.COMPRESS) item {
                WhitePanel {
                    Text("Compression", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Compression.entries.forEach { option ->
                            FilterChip(selected = compression == option, onClick = { compressionName = option.name }, enabled = !state.busy, label = { Text(option.label, fontSize = 12.sp) })
                        }
                    }
                    Text("Creates image-based pages. Searchable text, links, forms, signatures, and annotations may be flattened. Your original stays unchanged. Size reduction varies.", color = Muted, fontSize = 12.sp)
                }
            }
            if (tool == Tool.OCR) item {
                Text("Recognizes Latin-script text and saves a .txt file. Results depend on scan quality.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(4.dp))
            }
            if (tool == Tool.PDF_TO_IMAGE) item {
                WhitePanel {
                    Text("Image format", fontWeight = FontWeight.SemiBold)
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        SegmentedButton(selected = png, onClick = { png = true }, shape = SegmentedButtonDefaults.itemShape(0, 2), enabled = !state.busy) { Text("PNG") }
                        SegmentedButton(selected = !png, onClick = { png = false }, shape = SegmentedButtonDefaults.itemShape(1, 2), enabled = !state.busy) { Text("JPG") }
                    }
                    OutlinedTextField(pages, { pages = it }, Modifier.fillMaxWidth(), enabled = !state.busy,
                        label = { Text("Pages") }, placeholder = { Text("All pages, or 1-3, 5, 8") }, singleLine = true,
                        supportingText = { Text("Leave blank for all pages.") }, shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), enabled = !state.busy,
                        label = { Text("Save as") }, singleLine = true, shape = RoundedCornerShape(12.dp))
                    Text("Images saved as a ZIP in Documents / PDF Tools.", color = Muted, fontSize = 12.sp)
                }
            }
            if (!compact) item {
                WhitePanel {
                    Text("Output filename", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), enabled = !state.busy, singleLine = true,
                        shape = RoundedCornerShape(12.dp))
                    Text("Saved in Documents / PDF Tools", color = Muted, fontSize = 12.sp)
                }
            }
        }
        ActionFooter {
        if (state.busy) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text(state.progress, color = Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(8.dp))
        }
        PrimaryButton(if (state.busy) "Processing…" else if (tool == Tool.SPLIT) { if (splitEach) "Create separate PDFs" else "Create PDF" } else tool.action,
            enabled = !state.busy && state.selected.isNotEmpty() && (tool != Tool.MERGE || state.selected.size >= 2) && name.isNotBlank()
                && (tool != Tool.SPLIT || !chooseSplitPages || pages.isNotBlank())) {
            onRun(name, if (tool == Tool.SPLIT && !chooseSplitPages) "" else pages, compression, png, splitEach)
        }
        }
    }
}

@Composable
private fun SelectedFileRow(document: LocalDocument, index: Int, count: Int, enabled: Boolean, reorder: Boolean,
    onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit) {
    val currentIndex by rememberUpdatedState(index)
    val move by rememberUpdatedState(onMove)
    val threshold = with(LocalDensity.current) { 60.dp.toPx() }
    var actions by remember { mutableStateOf(false) }
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, Line)) {
            Row(Modifier.fillMaxWidth().padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (reorder) Text("${index + 1}", color = Blue, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Color(0xFFEAF2FF), RoundedCornerShape(10.dp)).padding(8.dp))
                Icon(if (document.isPdf) AppIcons.PictureAsPdf else AppIcons.Image, null,
                    Modifier.padding(start = if (reorder) 8.dp else 0.dp).size(29.dp), tint = if (document.isPdf) Color(0xFFE44951) else Blue)
                Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text(document.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(formatBytes(document.bytes), color = Muted, fontSize = 12.sp)
                }
                Box {
                    IconButton(onClick = { actions = true }, enabled = enabled) { Icon(AppIcons.MoreVert, "File actions", tint = Muted) }
                    DropdownMenu(expanded = actions, onDismissRequest = { actions = false }) {
                        if (reorder) {
                            DropdownMenuItem(text = { Text("Move up") }, enabled = index > 0, onClick = { actions = false; onMove(index, -1) })
                            DropdownMenuItem(text = { Text("Move down") }, enabled = index < count - 1, onClick = { actions = false; onMove(index, 1) })
                        }
                        DropdownMenuItem(text = { Text("Remove", color = MaterialTheme.colorScheme.error) }, onClick = { actions = false; onRemove(index) })
                    }
                }
                if (reorder) Box(Modifier.size(48.dp)
                    .pointerInput(enabled) {
                        if (enabled) {
                            var distance = 0f
                            detectDragGesturesAfterLongPress(onDragStart = { distance = 0f }, onDragEnd = { distance = 0f }, onDragCancel = { distance = 0f }) { change, amount ->
                                change.consume(); distance += amount.y
                                if (distance > threshold) { move(currentIndex, 1); distance = 0f }
                                else if (distance < -threshold) { move(currentIndex, -1); distance = 0f }
                            }
                        }
                    }, contentAlignment = Alignment.Center) { Icon(AppIcons.DragIndicator, "Long press and drag to reorder", tint = Muted) }
            }
    }
}

