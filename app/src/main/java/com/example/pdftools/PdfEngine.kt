package com.example.pdftools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.Environment
import android.os.ParcelFileDescriptor
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

class PdfEngine(private val context: Context, private val outputDirectory: File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "PDF Tools")) {
    init { PDFBoxResourceLoader.init(context.applicationContext) }

    suspend fun process(
        tool: Tool, inputs: List<File>, name: String, ranges: String,
        compression: Compression, png: Boolean, splitEach: Boolean = false, progress: (String) -> Unit
    ): JobResult {
        require(inputs.isNotEmpty()) { "Select a file first." }
        require(inputs.all { it.isFile && it.canRead() }) { "A selected file is missing or no longer accessible." }
        require(tool != Tool.MERGE || inputs.size >= 2) { "Select at least two PDFs to merge." }
        if (tool == Tool.SPLIT && splitEach) return splitToPdfs(inputs.first(), name, ranges, progress)
        val extension = tool.extension
        val outputName = safeOutputName(name, extension)
        val temporary = File.createTempFile("pdf-job-", ".$extension", context.cacheDir)
        var message = "Your file is ready."
        var text: String? = null
        try {
            when (tool) {
                Tool.MERGE -> {
                    progress("Combining ${inputs.size} PDFs…")
                    val merger = PDFMergerUtility()
                    inputs.forEach(merger::addSource)
                    merger.destinationFileName = temporary.absolutePath
                    merger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly())
                    message = "${inputs.size} PDFs combined in your chosen order."
                }
                Tool.SPLIT -> {
                    PDDocument.load(inputs.first(), MemoryUsageSetting.setupTempFileOnly()).use { source ->
                        val pages = parsePages(ranges, source.numberOfPages)
                        PDDocument(MemoryUsageSetting.setupTempFileOnly()).use { out ->
                            pages.forEachIndexed { index, page ->
                                currentCoroutineContext().ensureActive()
                                progress("Extracting page ${index + 1} of ${pages.size}…")
                                val original = source.getPage(page)
                                out.importPage(original).resources = original.resources
                            }
                            out.save(temporary)
                        }
                        message = "${pages.size} pages extracted into a new PDF."
                    }
                }
                Tool.COMPRESS -> {
                    PDDocument(MemoryUsageSetting.setupTempFileOnly()).use { out ->
                        renderPages(inputs.first(), "", compression.dpi) { bitmap, width, height, index, count ->
                            progress("Compressing page ${index + 1} of $count…")
                            addBitmap(out, bitmap, width.toFloat(), height.toFloat(), compression.quality)
                        }
                        out.save(temporary)
                    }
                    val originalSize = inputs.first().length()
                    if (temporary.length() >= originalSize) {
                        inputs.first().copyTo(temporary, overwrite = true)
                        message = "Already compact. Saved an unchanged copy; size could not be reduced."
                    } else {
                        val percent = ((1.0 - temporary.length().toDouble() / originalSize) * 100).roundToInt()
                        message = "Size reduced by $percent%. Pages are image-based; text and interactive features are flattened."
                    }
                }
                Tool.PDF_TO_IMAGE -> {
                    ZipOutputStream(temporary.outputStream().buffered()).use { zip ->
                        renderPages(inputs.first(), ranges, 144) { bitmap, _, _, index, count ->
                            progress("Exporting image ${index + 1} of $count…")
                            zip.putNextEntry(ZipEntry("page-${index + 1}.${if (png) "png" else "jpg"}"))
                            check(bitmap.compress(if (png) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG, 90, zip))
                            zip.closeEntry()
                        }
                    }
                    message = "Page images saved together in a ZIP archive."
                }
                Tool.IMAGE_TO_PDF -> {
                    PDDocument(MemoryUsageSetting.setupTempFileOnly()).use { out ->
                        inputs.forEachIndexed { index, file ->
                            currentCoroutineContext().ensureActive()
                            progress("Adding image ${index + 1} of ${inputs.size}…")
                            val bitmap = readBitmap(file)
                            try {
                                val page = if (bitmap.width > bitmap.height) PDRectangle(842f, 595f) else PDRectangle.A4
                                addBitmap(out, bitmap, page.width, page.height, .9f)
                            } finally { bitmap.recycle() }
                        }
                        out.save(temporary)
                    }
                    message = "${inputs.size} images combined into a PDF."
                }
                Tool.OCR -> {
                    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    val builder = StringBuilder()
                    try {
                        if (inputs.first().extension.equals("pdf", true)) {
                            renderPages(inputs.first(), ranges, 180) { bitmap, _, _, index, count ->
                                progress("Recognizing page ${index + 1} of $count…")
                                val recognized = recognizer.process(InputImage.fromBitmap(bitmap, 0)).await()
                                builder.append("Page ${index + 1}\n").append(recognized.text).append("\n\n")
                            }
                        } else {
                            val bitmap = readBitmap(inputs.first())
                            try {
                                progress("Recognizing text…")
                                builder.append(recognizer.process(InputImage.fromBitmap(bitmap, 0)).await().text)
                            } finally { bitmap.recycle() }
                        }
                    } finally { recognizer.close() }
                    text = builder.toString().trim()
                    require(text.isNotBlank() && text.lines().any { it.isNotBlank() && !it.matches(Regex("Page \\d+")) }) {
                        "No text found. Try a clearer scan. This version recognizes Latin-script text."
                    }
                    temporary.writeText(text)
                    message = "Recognized text saved as a UTF-8 text file."
                }
            }
            currentCoroutineContext().ensureActive()
            progress("Saving…")
            val directory = outputDirectory
            check(directory.isDirectory || directory.mkdirs()) { "Unable to create Documents/PDF Tools. Check file access." }
            // Atomically reserve a new name, never overwrite an input or a previous result.
            var destination = File(directory, outputName)
            var suffix = 1
            while (!destination.createNewFile()) {
                val stem = outputName.substringBeforeLast('.')
                destination = File(directory, "$stem (${suffix++}).$extension")
            }
            try { temporary.inputStream().use { input -> destination.outputStream().use(input::copyTo) } }
            catch (error: Exception) { destination.delete(); throw error }
            return JobResult(destination.absolutePath, message, text)
        } finally { temporary.delete() }
    }

    private suspend fun splitToPdfs(input: File, name: String, ranges: String, progress: (String) -> Unit): JobResult {
        val stem = safeOutputName(name, "pdf").removeSuffix(".pdf")
        val staged = mutableListOf<Pair<File, Int>>()
        val saved = mutableListOf<File>()
        try {
            PDDocument.load(input, MemoryUsageSetting.setupTempFileOnly()).use { source ->
                val pages = parsePages(ranges, source.numberOfPages)
                pages.forEachIndexed { index, page ->
                    currentCoroutineContext().ensureActive()
                    progress("Splitting page ${index + 1} of ${pages.size}…")
                    val temporary = File.createTempFile("split-page-", ".pdf", context.cacheDir)
                    staged.add(temporary to page)
                    PDDocument(MemoryUsageSetting.setupTempFileOnly()).use { out ->
                        val original = source.getPage(page)
                        out.importPage(original).resources = original.resources
                        out.save(temporary)
                    }
                }
            }
            check(outputDirectory.isDirectory || outputDirectory.mkdirs()) { "Unable to create the output folder. Check file access." }
            staged.forEach { (temporary, page) ->
                currentCoroutineContext().ensureActive()
                var destination = File(outputDirectory, "$stem-page-${page + 1}.pdf")
                var suffix = 1
                while (!destination.createNewFile()) destination = File(outputDirectory, "$stem-page-${page + 1} (${suffix++}).pdf")
                saved.add(destination)
                temporary.inputStream().use { inputStream -> destination.outputStream().use(inputStream::copyTo) }
            }
            return JobResult(saved.first().absolutePath, "${saved.size} PDF files created.", paths = saved.map { it.absolutePath })
        } catch (failure: Throwable) {
            saved.forEach { it.delete() }
            throw failure
        } finally { staged.forEach { it.first.delete() } }
    }

    private fun addBitmap(document: PDDocument, bitmap: Bitmap, width: Float, height: Float, quality: Float) {
        val page = PDPage(PDRectangle(width, height))
        document.addPage(page)
        val image = JPEGFactory.createFromImage(document, bitmap, quality)
        val scale = minOf(width / bitmap.width, height / bitmap.height)
        val w = bitmap.width * scale
        val h = bitmap.height * scale
        PDPageContentStream(document, page).use { canvas ->
            canvas.drawImage(image, (width - w) / 2, (height - h) / 2, w, h)
        }
    }

    private suspend fun renderPages(
        file: File, ranges: String, dpi: Int,
        consume: suspend (Bitmap, Int, Int, Int, Int) -> Unit
    ) {
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                val pages = parsePages(ranges, renderer.pageCount)
                pages.forEachIndexed { index, number ->
                    currentCoroutineContext().ensureActive()
                    renderer.openPage(number).use { page ->
                        val scale = minOf(dpi / 72f, 2400f / max(page.width, page.height))
                        val bitmap = Bitmap.createBitmap(max(1, (page.width * scale).roundToInt()), max(1, (page.height * scale).roundToInt()), Bitmap.Config.ARGB_8888)
                        try {
                            bitmap.eraseColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                            consume(bitmap, page.width, page.height, index, pages.size)
                        } finally { bitmap.recycle() }
                    }
                }
            }
        }
    }

    private fun readBitmap(file: File): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Cannot read image ${file.name}." }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > 2400) sample *= 2
        val original = BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: error("Cannot decode image ${file.name}.")
        val orientation = runCatching { ExifInterface(file).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1) }.getOrDefault(1)
        val matrix = Matrix()
        when (orientation) {
            2 -> matrix.setScale(-1f, 1f)
            3 -> matrix.setRotate(180f)
            4 -> matrix.setScale(1f, -1f)
            5 -> { matrix.setRotate(90f); matrix.postScale(-1f, 1f) }
            6 -> matrix.setRotate(90f)
            7 -> { matrix.setRotate(-90f); matrix.postScale(-1f, 1f) }
            8 -> matrix.setRotate(-90f)
        }
        val rotated = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
        if (rotated !== original) original.recycle()
        if (rotated.hasAlpha()) {
            val opaque = Bitmap.createBitmap(rotated.width, rotated.height, Bitmap.Config.ARGB_8888)
            opaque.eraseColor(Color.WHITE)
            android.graphics.Canvas(opaque).drawBitmap(rotated, 0f, 0f, null)
            rotated.recycle()
            opaque.setHasAlpha(false)
            return opaque
        }
        return rotated
    }
}

