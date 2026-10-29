package com.example.pdftools

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Small original vector set. Only the symbols the app actually uses are packaged. */
object AppIcons {
    private fun icon(name: String, mirror: Boolean = false, draw: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = mirror).apply {
            path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathBuilder = draw)
        }.build()
    private fun PathBuilder.document(x: Float = 5f, y: Float = 2f, w: Float = 14f, h: Float = 20f) {
        moveTo(x, y); lineTo(x + w - 5, y); lineTo(x + w, y + 5); lineTo(x + w, y + h)
        lineTo(x, y + h); close(); moveTo(x + w - 5, y); lineTo(x + w - 5, y + 5); lineTo(x + w, y + 5)
    }
    val Description = icon("Document") { document(); moveTo(9f, 12f); lineTo(15f, 12f); moveTo(9f, 16f); lineTo(13f, 16f) }
    val InsertDriveFile = Description
    val PictureAsPdf = icon("PDF") { document(); moveTo(8f, 13f); lineTo(16f, 13f); moveTo(8f, 17f); lineTo(13f, 17f) }
    val NoteAdd = icon("Merge") { document(3f, 2f, 12f, 16f); document(10f, 8f, 11f, 14f); moveTo(13f, 17f); lineTo(18f, 17f); moveTo(15.5f, 14.5f); lineTo(15.5f, 19.5f) }
    val ContentCut = icon("Split") {
        moveTo(9f, 3f); lineTo(4f, 3f); lineTo(4f, 21f); lineTo(9f, 21f)
        moveTo(15f, 3f); lineTo(20f, 3f); lineTo(20f, 21f); lineTo(15f, 21f)
        for (y in 1..21 step 4) { moveTo(12f, y.toFloat()); lineTo(12f, y + 1.5f) }
    }
    val Compress = icon("Compress") {
        moveTo(3f, 3f); lineTo(9f, 9f); moveTo(4f, 9f); lineTo(9f, 9f); lineTo(9f, 4f)
        moveTo(21f, 3f); lineTo(15f, 9f); moveTo(15f, 4f); lineTo(15f, 9f); lineTo(20f, 9f)
        moveTo(3f, 21f); lineTo(9f, 15f); moveTo(4f, 15f); lineTo(9f, 15f); lineTo(9f, 20f)
        moveTo(21f, 21f); lineTo(15f, 15f); moveTo(15f, 20f); lineTo(15f, 15f); lineTo(20f, 15f)
    }
    val DocumentScanner = icon("OCR") {
        moveTo(7f, 2f); lineTo(2f, 2f); lineTo(2f, 7f); moveTo(17f, 2f); lineTo(22f, 2f); lineTo(22f, 7f)
        moveTo(2f, 17f); lineTo(2f, 22f); lineTo(7f, 22f); moveTo(17f, 22f); lineTo(22f, 22f); lineTo(22f, 17f)
        moveTo(7f, 17f); lineTo(12f, 6f); lineTo(17f, 17f); moveTo(9f, 13f); lineTo(15f, 13f)
    }
    val Image = icon("Image") {
        moveTo(3f, 3f); lineTo(21f, 3f); lineTo(21f, 21f); lineTo(3f, 21f); close()
        moveTo(3f, 18f); lineTo(10f, 10f); lineTo(15f, 16f); lineTo(18f, 13f); lineTo(21f, 17f)
        moveTo(16f, 7f); lineTo(16.1f, 7f)
    }
    val ArrowBack = icon("Back", true) { moveTo(20f, 12f); lineTo(4f, 12f); moveTo(11f, 5f); lineTo(4f, 12f); lineTo(11f, 19f) }
    val ArrowForward = icon("Forward", true) { moveTo(4f, 12f); lineTo(20f, 12f); moveTo(13f, 5f); lineTo(20f, 12f); lineTo(13f, 19f) }
    val Home = icon("Home") { moveTo(3f, 10f); lineTo(12f, 3f); lineTo(21f, 10f); lineTo(21f, 21f); lineTo(15f, 21f); lineTo(15f, 14f); lineTo(9f, 14f); lineTo(9f, 21f); lineTo(3f, 21f); close() }
    val FolderOpen = icon("Files") { moveTo(2f, 6f); lineTo(9f, 6f); lineTo(11f, 9f); lineTo(22f, 9f); lineTo(20f, 21f); lineTo(2f, 21f); close(); moveTo(2f, 11f); lineTo(20f, 11f) }
    val Schedule = icon("Clock") { moveTo(21f, 12f); arcToRelative(9f, 9f, 0f, true, true, -18f, 0f); arcToRelative(9f, 9f, 0f, true, true, 18f, 0f); moveTo(12f, 6f); lineTo(12f, 12f); lineTo(16f, 15f) }
    val History = Schedule
    val Storage = icon("Storage") { moveTo(4f, 5f); curveTo(4f, 1f, 20f, 1f, 20f, 5f); curveTo(20f, 9f, 4f, 9f, 4f, 5f); lineTo(4f, 18f); curveTo(4f, 22f, 20f, 22f, 20f, 18f); lineTo(20f, 5f); moveTo(4f, 12f); curveTo(4f, 16f, 20f, 16f, 20f, 12f) }
    val Settings = icon("Settings") {
        var first = true
        repeat(8) { tooth ->
            for ((offset, radius) in listOf(-22.5 to 8.0, -14.0 to 8.0, -10.0 to 10.0, 10.0 to 10.0, 14.0 to 8.0)) {
                val angle = Math.toRadians(tooth * 45.0 + offset)
                val x = (12 + kotlin.math.cos(angle) * radius).toFloat()
                val y = (12 + kotlin.math.sin(angle) * radius).toFloat()
                if (first) { moveTo(x, y); first = false } else lineTo(x, y)
            }
        }
        close()
        moveTo(15.2f, 12f); arcToRelative(3.2f, 3.2f, 0f, true, true, -6.4f, 0f); arcToRelative(3.2f, 3.2f, 0f, true, true, 6.4f, 0f)
    }
    val Search = icon("Search") { moveTo(17f, 10f); arcToRelative(7f, 7f, 0f, true, true, -14f, 0f); arcToRelative(7f, 7f, 0f, true, true, 14f, 0f); moveTo(15f, 15f); lineTo(22f, 22f) }
    val Refresh = icon("Refresh") { moveTo(20f, 7f); curveTo(15f, -1f, 3f, 2f, 3f, 12f); curveTo(3f, 22f, 17f, 25f, 21f, 15f); moveTo(20f, 2f); lineTo(20f, 8f); lineTo(14f, 8f) }
    val Add = icon("Add") { moveTo(4f, 12f); lineTo(20f, 12f); moveTo(12f, 4f); lineTo(12f, 20f) }
    val Close = icon("Close") { moveTo(6f, 6f); lineTo(18f, 18f); moveTo(18f, 6f); lineTo(6f, 18f) }
    val MoreVert = icon("Actions") { for (y in listOf(5f,12f,19f)) { moveTo(12f, y); lineTo(12f, y + .2f) } }
    val DragIndicator = icon("Reorder") { for (x in listOf(9f,15f)) for (y in listOf(5f,12f,19f)) { moveTo(x, y); lineTo(x, y + .2f) } }
    val KeyboardArrowUp = icon("Up") { moveTo(6f, 15f); lineTo(12f, 9f); lineTo(18f, 15f) }
    val KeyboardArrowDown = icon("Down") { moveTo(6f, 9f); lineTo(12f, 15f); lineTo(18f, 9f) }
    val CheckCircle = icon("Complete") { moveTo(22f,12f); arcToRelative(10f,10f,0f,true,true,-20f,0f); arcToRelative(10f,10f,0f,true,true,20f,0f); moveTo(7f,12f); lineTo(10f,15f); lineTo(17f,8f) }
    val ZoomOutMap = icon("Fit page") { moveTo(8f,3f); lineTo(3f,3f); lineTo(3f,8f); moveTo(16f,3f); lineTo(21f,3f); lineTo(21f,8f); moveTo(3f,16f); lineTo(3f,21f); lineTo(8f,21f); moveTo(16f,21f); lineTo(21f,21f); lineTo(21f,16f) }
}


