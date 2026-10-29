package com.example.pdftools

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.json.JSONArray
import java.io.File

class DocumentStore(private val context: Context) {
    private val preferences = context.getSharedPreferences("library", Context.MODE_PRIVATE)

    suspend fun scan(): List<LocalDocument> {
        val result = mutableListOf<LocalDocument>()
        val visited = hashSetOf<String>()
        // Shared storage only. Other apps' private Android/data directories are deliberately excluded.
        val roots = mutableSetOf(Environment.getExternalStorageDirectory())
        context.getExternalFilesDirs(null).filterNotNull().forEach {
            val marker = "${File.separator}Android${File.separator}"
            if (marker in it.absolutePath) roots.add(File(it.absolutePath.substringBefore(marker)))
        }
        suspend fun visit(dir: File) {
            currentCoroutineContext().ensureActive()
            if (!visited.add(runCatching { dir.canonicalPath }.getOrDefault(dir.absolutePath))) return
            val children = dir.listFiles() ?: return
            for (file in children) {
                currentCoroutineContext().ensureActive()
                if (file.isHidden || file.name == "Android") continue
                if (file.isDirectory) visit(file)
                else if (file.extension.lowercase() in setOf("pdf", "jpg", "jpeg", "png", "webp")) {
                    result.add(LocalDocument.from(file))
                }
            }
        }
        roots.forEach { visit(it) }
        return result.distinctBy { it.path }.sortedByDescending { it.modified }
    }

    fun history(): List<LocalDocument> {
        val paths = runCatching { JSONArray(preferences.getString("history", "[]")) }.getOrDefault(JSONArray())
        return (0 until paths.length()).map { File(paths.getString(it)) }
            .filter { it.isFile }.map(LocalDocument::from)
    }

    fun record(file: File) {
        val paths = (listOf(file.absolutePath) + history().map { it.path }).distinct().take(100)
        preferences.edit().putString("history", JSONArray(paths).toString()).apply()
    }

}

