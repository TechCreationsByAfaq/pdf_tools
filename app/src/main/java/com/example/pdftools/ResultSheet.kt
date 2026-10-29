@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pdftools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun ResultSheet(result: JobResult, onDismiss: () -> Unit, onOpen: (String) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showText by rememberSaveable(result.path) { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = CanvasColor) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(AppIcons.CheckCircle, null, Modifier.size(30.dp), tint = Blue)
                Column(Modifier.weight(1f)) {
                    Text(if (result.paths.size > 1) "Your files are ready" else "Your file is ready", style = MaterialTheme.typography.titleLarge)
                    Text(result.message, color = Muted, fontSize = 13.sp)
                }
            }
            LazyColumn(Modifier.heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(result.paths, key = { it }) { path ->
                    ResultFileRow(File(path)) { onOpen(path) }
                }
            }
            result.text?.let { text ->
                TextButton(onClick = { showText = !showText }) { Text(if (showText) "Hide text preview" else "Preview recognized text") }
                if (showText) {
                    SelectionContainer {
                        Text(text.take(12000), Modifier.heightIn(max = 180.dp).verticalScroll(rememberScrollState()), fontSize = 14.sp)
                    }
                    if (text.length > 12000) Text("Full text is in the saved file.", color = Muted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ResultFileRow(file: File, onOpen: () -> Unit) {
    Surface(onClick = onOpen, color = Color.White, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Line)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(if (file.extension.equals("pdf", true)) AppIcons.PictureAsPdf else AppIcons.InsertDriveFile,
                null, Modifier.size(32.dp), tint = Blue)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(file.name, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(formatBytes(file.length()), color = Muted, fontSize = 13.sp)
                Text("Tap to open", color = Blue, fontSize = 13.sp)
            }
            Icon(AppIcons.ArrowForward, null, Modifier.size(20.dp), tint = Blue)
        }
    }
}

