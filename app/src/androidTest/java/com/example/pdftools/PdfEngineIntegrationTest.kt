package com.example.pdftools

import androidx.test.platform.app.InstrumentationRegistry
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class PdfEngineIntegrationTest {
    @Test fun mergeExtractAndSeparateKeepTheExpectedPages() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        PDFBoxResourceLoader.init(context)
        val outputDirectory = File(context.cacheDir, "engine-test-${System.nanoTime()}")
        val engine = PdfEngine(context, outputDirectory)
        val a = File(context.cacheDir, "fixture-a.pdf")
        val b = File(context.cacheDir, "fixture-b.pdf")
        val created = mutableListOf<File>()
        try {
            PDDocument().use { doc ->
                doc.addPage(PDPage(PDRectangle(200f, 300f)))
                doc.addPage(PDPage(PDRectangle(400f, 500f)))
                doc.save(a)
            }
            PDDocument().use { doc -> doc.addPage(PDPage(PDRectangle.A4)); doc.save(b) }
            val merged = engine.process(Tool.MERGE, listOf(a, b), "integration-merge", "", Compression.BALANCED, true) {}
            created.add(File(merged.path))
            PDDocument.load(File(merged.path)).use { doc ->
                assertEquals(3, doc.numberOfPages)
                assertEquals(200f, doc.getPage(0).mediaBox.width, 0f)
                assertEquals(400f, doc.getPage(1).mediaBox.width, 0f)
            }
            val extracted = engine.process(Tool.SPLIT, listOf(File(merged.path)), "integration-extract", "2,1", Compression.BALANCED, true) {}
            created.add(File(extracted.path))
            PDDocument.load(File(extracted.path)).use { doc ->
                assertEquals(2, doc.numberOfPages)
                assertEquals(400f, doc.getPage(0).mediaBox.width, 0f)
                assertEquals(200f, doc.getPage(1).mediaBox.width, 0f)
            }
            val separated = engine.process(Tool.SPLIT, listOf(a), "integration-pages", "", Compression.BALANCED, true, true) {}
            created.addAll(separated.paths.map(::File))
            assertEquals(2, separated.paths.size)
            separated.paths.forEach { assertTrue(it.endsWith(".pdf")) }
            PDDocument.load(File(separated.paths[1])).use { doc ->
                assertEquals(1, doc.numberOfPages)
                assertEquals(400f, doc.getPage(0).mediaBox.width, 0f)
            }
            assertTrue("Original input must remain", a.isFile && b.isFile)
        } finally { a.delete(); b.delete(); created.forEach { it.delete() }; outputDirectory.delete() }
    }
}
