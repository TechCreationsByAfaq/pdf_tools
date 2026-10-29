@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pdftools

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

private data class PagePreview(val bitmap: Bitmap, val count: Int)

@Composable
fun PdfViewerScreen(path: String, onBack: () -> Unit) {
    var count by remember(path) { mutableIntStateOf(0) }
    val pager = rememberPagerState(pageCount = { count.coerceAtLeast(1) })
    val page = pager.currentPage
    val scope = rememberCoroutineScope()
    var fullscreen by rememberSaveable(path) { mutableStateOf(false) }
    val view = LocalView.current
    val activity = remember(view) {
        generateSequence(view.context) { (it as? android.content.ContextWrapper)?.baseContext }
            .filterIsInstance<android.app.Activity>().firstOrNull()
    }
    DisposableEffect(activity, fullscreen) {
        val controller = activity?.let { WindowCompat.getInsetsController(it.window, view) }
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (fullscreen) controller?.hide(WindowInsetsCompat.Type.systemBars())
        else controller?.show(WindowInsetsCompat.Type.systemBars())
        onDispose { controller?.show(WindowInsetsCompat.Type.systemBars()) }
    }
    BackHandler(fullscreen) { fullscreen = false }
    var preview by remember(path) { mutableStateOf<Bitmap?>(null) }
    var renderedPage by remember(path) { mutableIntStateOf(-1) }
    var error by remember(path) { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var zoom by rememberSaveable(path) { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(path, page) {
        loading = true; error = null; zoom = 1f; panX = 0f; panY = 0f
        try {
            val result = withContext(Dispatchers.IO) {
                ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        require(renderer.pageCount > 0) { "This document has no pages." }
                        renderer.openPage(page.coerceIn(0, renderer.pageCount - 1)).use { current ->
                            val scale = 1600f / maxOf(current.width, current.height)
                            val bitmap = Bitmap.createBitmap(maxOf(1, (current.width * scale).roundToInt()), maxOf(1, (current.height * scale).roundToInt()), Bitmap.Config.ARGB_8888)
                            try {
                                bitmap.eraseColor(AndroidColor.WHITE)
                                current.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                PagePreview(bitmap, renderer.pageCount)
                            } catch (failure: Exception) { bitmap.recycle(); throw failure }
                        }
                    }
                }
            }
            preview = result.bitmap; count = result.count; renderedPage = page
        } catch (failure: CancellationException) { throw failure }
        catch (failure: Exception) { error = "Cannot display this PDF. It may be encrypted, damaged, or no longer accessible." }
        finally { loading = false }
    }
    Column(Modifier.fillMaxSize().background(if (fullscreen) Color.Black else CanvasColor)
        .then(if (fullscreen) Modifier else Modifier.safeDrawingPadding())) {
        if (!fullscreen) Row(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(AppIcons.ArrowBack, "Back", tint = Blue) }
            Text(File(path).name, Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            IconButton(onClick = { fullscreen = true }) { Icon(AppIcons.ZoomOutMap, "Enter fullscreen", tint = Blue) }
        }
        Box(Modifier.weight(1f).fillMaxWidth().background(if (fullscreen) Color.Black else Color(0xFFE8EDF5)), contentAlignment = Alignment.Center) {
            if (loading) CircularProgressIndicator()
            else if (error != null) Text(error!!, Modifier.padding(24.dp), color = Muted, textAlign = TextAlign.Center)
            HorizontalPager(state = pager, userScrollEnabled = zoom <= 1f && !loading && error == null,
                modifier = Modifier.fillMaxSize()) { index ->
                if (!loading && error == null && renderedPage == index) preview?.let { bitmap ->
                    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(0.dp))) {
                        Image(bitmap.asImageBitmap(), "PDF page ${index + 1}", Modifier.fillMaxSize()
                            .pointerInput(path, index) {
                                detectTapGestures(onDoubleTap = {
                                    zoom = if (zoom > 1f) 1f else 2f; panX = 0f; panY = 0f
                                })
                            }
                            .pointerInput(path, index) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    do {
                                        val event = awaitPointerEvent()
                                        if (event.changes.count { it.pressed } > 1 || zoom > 1f) {
                                            zoom = (zoom * event.calculateZoom()).coerceIn(1f, 4f)
                                            val pan = event.calculatePan()
                                            panX = (panX + pan.x).coerceIn(-size.width * (zoom - 1) / 2, size.width * (zoom - 1) / 2)
                                            panY = (panY + pan.y).coerceIn(-size.height * (zoom - 1) / 2, size.height * (zoom - 1) / 2)
                                            event.changes.forEach { it.consume() }
                                        }
                                    } while (event.changes.any { it.pressed })
                                }
                            }.graphicsLayer(scaleX = zoom, scaleY = zoom, translationX = panX, translationY = panY))
                    }
                }
            }
            if (fullscreen) IconButton(onClick = { fullscreen = false }, modifier = Modifier.align(Alignment.TopEnd).safeDrawingPadding()
                .padding(8.dp).background(Color.Black.copy(alpha = .55f), RoundedCornerShape(24.dp))) {
                Icon(AppIcons.Close, "Exit fullscreen", tint = Color.White)
            }
        }
        if (!fullscreen) Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            IconButton(onClick = { scope.launch { pager.animateScrollToPage(page - 1) } }, enabled = page > 0 && !loading) { Icon(AppIcons.ArrowBack, "Previous page") }
            Text(if (count > 0) "${page + 1} / $count" else "—", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 12.dp))
            IconButton(onClick = { zoom = 1f; panX = 0f; panY = 0f }, enabled = zoom > 1f) { Icon(AppIcons.ZoomOutMap, "Fit page") }
            IconButton(onClick = { scope.launch { pager.animateScrollToPage(page + 1) } }, enabled = page < count - 1 && !loading) { Icon(AppIcons.ArrowForward, "Next page") }
        }
    }
}

