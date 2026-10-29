package com.example.pdftools

import java.io.File
import java.text.DecimalFormat

enum class Tool(val title: String, val action: String, val extension: String) {
    MERGE("Merge PDF", "Merge PDFs", "pdf"),
    SPLIT("Split PDF", "Extract pages", "pdf"),
    COMPRESS("Compress PDF", "Compress PDF", "pdf"),
    OCR("OCR", "Extract text", "txt"),
    PDF_TO_IMAGE("PDF to Image", "Convert to images", "zip"),
    IMAGE_TO_PDF("Image to PDF", "Create PDF", "pdf");

    val multiple get() = this == MERGE || this == IMAGE_TO_PDF
    fun accepts(file: File): Boolean = when (this) {
        IMAGE_TO_PDF -> file.extension.lowercase() in setOf("jpg", "jpeg", "png", "webp")
        OCR -> file.extension.lowercase() in setOf("pdf", "jpg", "jpeg", "png", "webp")
        else -> file.extension.equals("pdf", true)
    }
}

data class LocalDocument(val path: String, val name: String, val bytes: Long, val modified: Long) {
    val file get() = File(path)
    val isPdf get() = file.extension.equals("pdf", true)
    companion object {
        fun from(file: File) = LocalDocument(file.absolutePath, file.name, file.length(), file.lastModified())
    }
}

enum class Compression(val label: String, val dpi: Int, val quality: Float) {
    LIGHT("Light", 140, .85f), BALANCED("Balanced", 110, .65f), STRONG("Strong", 85, .45f)
}

data class JobResult(val path: String, val message: String, val text: String? = null, val paths: List<String> = listOf(path))

fun formatBytes(bytes: Long): String {
    val format = DecimalFormat("0.#")
    return when {
        bytes >= 1_073_741_824 -> "${format.format(bytes / 1_073_741_824.0)} GB"
        bytes >= 1_048_576 -> "${format.format(bytes / 1_048_576.0)} MB"
        bytes >= 1024 -> "${format.format(bytes / 1024.0)} KB"
        else -> "$bytes B"
    }
}

/** Returns zero-based pages in explicitly requested order; rejects ambiguous/invalid input. */
fun parsePages(value: String, pageCount: Int): List<Int> {
    require(pageCount > 0) { "This PDF has no pages." }
    if (value.isBlank()) return (0 until pageCount).toList()
    val pages = linkedSetOf<Int>()
    value.split(',').forEach { part ->
        val token = part.trim()
        require(token.matches(Regex("\\d+(\\s*-\\s*\\d+)?"))) { "Use page numbers like 1-3, 5, 8." }
        val bounds = token.split('-').map { it.trim().toIntOrNull() ?: error("Invalid page number.") }
        val start = bounds.first()
        val end = bounds.last()
        require(start in 1..pageCount && end in start..pageCount) { "Pages must be between 1 and $pageCount, in ascending ranges." }
        for (page in start..end) pages.add(page - 1)
    }
    return pages.toList()
}

fun safeOutputName(value: String, extension: String): String {
    val clean = value.trim()
    require(clean.isNotEmpty() && clean.length <= 120) { "Enter an output name of 1–120 characters." }
    require(clean !in setOf(".", "..") && clean.none { it in "/\\:*?\"<>|" || it.code < 32 }) {
        "Use a filename without slashes or special characters."
    }
    return if (clean.endsWith(".$extension", true)) clean else "$clean.$extension"
}

