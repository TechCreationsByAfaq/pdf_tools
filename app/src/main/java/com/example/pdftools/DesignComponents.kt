package com.example.pdftools

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val OceanGradient = Brush.linearGradient(listOf(Color(0xFF3182FF), Color(0xFF1749DD)))

@Composable
fun ExitDialog(onDismissRequest: () -> Unit, onExit: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        Surface(color = Color.White, shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Exit PDF Tools?", fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold)
                Text("Your saved files will stay on your device.", color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismissRequest, modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line)) {
                        Text("Stay", fontSize = 14.sp)
                    }
                    Button(onClick = onExit, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White)) {
                        Text("Exit", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OceanCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp)).background(OceanGradient)) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(Color.White.copy(alpha = .065f), size.width * .48f, Offset(size.width, -size.height * .25f))
            drawCircle(Color.White.copy(alpha = .045f), size.width * .42f, Offset(0f, size.height * 1.3f))
        }
        Column(Modifier.fillMaxWidth().padding(20.dp), content = content)
    }
}

@Composable
fun ToolIntro(tool: Tool, selection: Boolean = false) {
    BlueIntro(if (selection) "Choose your files" else when (tool) {
        Tool.MERGE -> "Combine your PDFs"; Tool.SPLIT -> "Select your pages"
        Tool.COMPRESS -> "Make room for more"; Tool.OCR -> "Turn scans into text"
        Tool.PDF_TO_IMAGE -> "Pages to pictures"; Tool.IMAGE_TO_PDF -> "Pictures to pages"
    }, if (selection) {
        if (tool.multiple) "Select files to get started." else "Select one file to continue."
    } else if (tool.multiple) "Arrange files in your preferred order." else "Customize your output below.", tool.icon())
}

@Composable
fun BlueIntro(title: String, subtitle: String, icon: ImageVector) {
    OceanCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Color.White.copy(alpha = .14f), shape = RoundedCornerShape(16.dp)) {
                Icon(icon, null, Modifier.padding(12.dp).size(30.dp), tint = Color.White)
            }
            Column(Modifier.padding(start = 14.dp).weight(1f)) {
                Text(title, color = Color.White, fontSize = 18.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold)
                Text(subtitle,
                    color = Color.White.copy(alpha = .82f), fontSize = 12.sp, lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun ActionFooter(content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Color.White, shadowElevation = 8.dp) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
fun SectionLabel(title: String, modifier: Modifier = Modifier) {
    Text(title, style = MaterialTheme.typography.titleMedium, modifier = modifier)
}
@Composable
fun WhitePanel(content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Color.White, shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}


