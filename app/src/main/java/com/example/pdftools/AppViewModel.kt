package com.example.pdftools

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class AppState(
    val documents: List<LocalDocument> = emptyList(),
    val history: List<LocalDocument> = emptyList(),
    val scanning: Boolean = false,
    val selected: List<LocalDocument> = emptyList(),
    val busy: Boolean = false,
    val progress: String = "",
    val result: JobResult? = null,
    val error: String? = null
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val store = DocumentStore(application)
    private val engine = PdfEngine(application)
    private val mutable = MutableStateFlow(AppState(history = store.history()))
    val state = mutable.asStateFlow()
    private var scan: Job? = null

    fun refresh() {
        if (scan?.isActive == true) return
        scan = viewModelScope.launch {
            mutable.update { it.copy(scanning = true) }
            try {
                val files = withContext(Dispatchers.IO) { store.scan() }
                mutable.update { it.copy(documents = files, history = store.history()) }
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) { fail(error.message ?: "Unable to read shared storage.") }
            finally { mutable.update { it.copy(scanning = false) } }
        }
    }

    fun revokeAccess() {
        scan?.cancel()
        mutable.update { it.copy(documents = emptyList(), selected = emptyList()) }
    }

    fun select(files: List<LocalDocument>) = mutable.update { it.copy(selected = files, result = null) }
    fun remove(index: Int) = mutable.update { it.copy(selected = it.selected.filterIndexed { i, _ -> i != index }) }
    fun move(index: Int, direction: Int) = mutable.update { state ->
        val files = state.selected.toMutableList()
        val target = index + direction
        if (target in files.indices) { val file = files.removeAt(index); files.add(target, file) }
        state.copy(selected = files)
    }
    fun clearError() = mutable.update { it.copy(error = null) }
    fun fail(message: String) = mutable.update { it.copy(error = message) }
    fun clearResult() = mutable.update { it.copy(result = null) }

    fun run(tool: Tool, name: String, ranges: String, compression: Compression, png: Boolean, splitEach: Boolean) {
        if (mutable.value.busy) return
        val inputs = mutable.value.selected.map { it.file }
        mutable.update { it.copy(busy = true, progress = "Preparing…", error = null, result = null) }
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    engine.process(tool, inputs, name, ranges, compression, png, splitEach) { progress ->
                        mutable.update { it.copy(progress = progress) }
                    }
                }
                result.paths.asReversed().forEach { store.record(File(it)) }
                mutable.update { it.copy(result = result, history = store.history()) }
                refresh()
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) { fail(error.message ?: "Unable to process this file. It may be encrypted or damaged.") }
            finally { mutable.update { it.copy(busy = false) } }
        }
    }

    fun rename(document: LocalDocument, name: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val target = File(document.file.parentFile, safeOutputName(name, document.file.extension))
                    require(!target.exists()) { "A file with this name already exists." }
                    check(document.file.renameTo(target)) { "Unable to rename this file." }
                }
                refresh()
            } catch (error: Exception) { fail(error.message ?: "Rename failed.") }
        }
    }

    fun delete(document: LocalDocument) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { check(document.file.delete()) { "Unable to delete this file." } }
                refresh()
            } catch (error: Exception) { fail(error.message ?: "Delete failed.") }
        }
    }
}

