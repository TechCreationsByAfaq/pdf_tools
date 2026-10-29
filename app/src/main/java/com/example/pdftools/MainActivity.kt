package com.example.pdftools

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(0xFFF5F8FF.toInt(), 0xFFF5F8FF.toInt())
        )
        setContent { PdfTheme { PdfApp() } }
    }
}

fun hasFileAccess(context: android.content.Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager()
    else listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

@Composable
fun PdfApp(vm: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var exitConfirmation by rememberSaveable { mutableStateOf(false) }
    val uiScope = rememberCoroutineScope()
    val preferences = remember { context.getSharedPreferences("experience", 0) }
    val state by vm.state.collectAsStateWithLifecycle()
    var route by rememberSaveable { mutableStateOf("splash") }
    var tab by rememberSaveable { mutableStateOf("Home") }
    var toolName by rememberSaveable { mutableStateOf(Tool.MERGE.name) }
    var pending by rememberSaveable { mutableStateOf<String?>(null) }
    var permissionDialog by rememberSaveable { mutableStateOf(false) }
    var access by remember { mutableStateOf(hasFileAccess(context)) }
    var viewerPath by rememberSaveable { mutableStateOf("") }
    var viewerReturn by rememberSaveable { mutableStateOf("home") }
    val tool = Tool.valueOf(toolName)

    fun resumeRequest() {
        access = hasFileAccess(context)
        if (access) {
            vm.refresh()
            when (val request = pending) {
                "Files" -> { route = "home"; tab = "Files" }
                null -> Unit
                else -> { toolName = request; vm.select(emptyList()); route = "browser" }
            }
        } else {
            vm.revokeAccess()
            vm.fail("File access was not granted. You can try again when you are ready.")
        }
        pending = null
    }

    val permissionSettings = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resumeRequest() }
    val legacyPermissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resumeRequest() }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                access = hasFileAccess(context)
                if (access) vm.refresh() else vm.revokeAccess()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun requestFiles(request: String) {
        if (hasFileAccess(context)) {
            access = true
            vm.refresh()
            if (request == "Files") { tab = "Files"; route = "home" }
            else { toolName = request; vm.select(emptyList()); route = "browser" }
        } else { pending = request; permissionDialog = true }
    }

    fun openDocument(document: LocalDocument, from: String = "home"): Boolean {
        if (document.isPdf) { viewerPath = document.path; viewerReturn = from; route = "viewer"; return true }
        else {
            // Only app-produced results are shared through our narrowly scoped provider.
            return runCatching {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", document.file)
                val mime = when (document.file.extension.lowercase()) { "txt" -> "text/plain"; "zip" -> "application/zip"; else -> "application/octet-stream" }
                context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
            }.onFailure { vm.fail("No compatible app is available to open this file.") }.isSuccess
        }
    }

    BackHandler(enabled = route == "home" && !state.busy) {
        if (tab != "Home") tab = "Home" else exitConfirmation = true
    }
    BackHandler(enabled = route !in setOf("home", "splash", "onboarding") && !state.busy) {
        route = when (route) { "viewer" -> viewerReturn; "privacy" -> "settings"; "browser" -> "home"; else -> "home" }
    }
    BackHandler(enabled = state.busy) { /* Keep the foreground operation alive until its result is saved. */ }

    Surface(color = CanvasColor, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().then(if (route == "viewer") Modifier else Modifier.safeDrawingPadding())) {
            when (route) {
                "splash" -> SplashScreen {
                    route = if (preferences.getBoolean("onboarded", false)) "home" else "onboarding"
                }
                "onboarding" -> OnboardingScreen {
                    preferences.edit().putBoolean("onboarded", true).apply()
                    route = "home"
                }
                "home" -> HomeScreen(
                    state, tab, access, onTab = { if (it == "Files" && !access) requestFiles("Files") else tab = it },
                    onTool = { requestFiles(it.name) }, onSettings = { route = "settings" },
                    onOpen = { openDocument(it) }, onRefresh = { if (access) vm.refresh() else requestFiles("Files") },
                    onRename = vm::rename, onDelete = vm::delete,
                    onViewAll = { if (access) tab = "Files" else requestFiles("Files") }
                )
                "browser" -> FileBrowserScreen(state, tool, onBack = { route = "home" }, onRefresh = vm::refresh) {
                    vm.select(it); route = "tool"
                }
                "tool" -> ToolScreen(state, tool, onBack = { route = "home" }, onAdd = {
                    if (hasFileAccess(context)) route = "browser" else requestFiles(tool.name)
                }, onRemove = vm::remove, onMove = vm::move, onRun = { name, pages, compression, png, splitEach ->
                    if (!hasFileAccess(context)) {
                        vm.fail("File access has been turned off. Enable it in Settings, then select your files again.")
                        route = "home"
                    } else vm.run(tool, name, pages, compression, png, splitEach)
                })
                "viewer" -> PdfViewerScreen(viewerPath, onBack = { route = viewerReturn })
                "privacy" -> PrivacyPolicyScreen { route = "settings" }
                "settings" -> SettingsScreen(onBack = { route = "home" },
                    onHistory = { tab = "History"; route = "home" }, onPrivacy = { route = "privacy" }, onShare = {
                        uiScope.launch {
                            runCatching {
                                val apk = withContext(Dispatchers.IO) {
                                    val directory = File(context.cacheDir, "app-share")
                                    check(directory.isDirectory || directory.mkdirs())
                                    File(context.applicationInfo.sourceDir).copyTo(File(directory, "PDF-Tools.apk"), overwrite = true)
                                }
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", apk)
                                val intent = Intent(Intent.ACTION_SEND).setType("application/vnd.android.package-archive")
                                    .putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                context.startActivity(Intent.createChooser(intent, "Share PDF Tools"))
                            }.onFailure { vm.fail("Unable to share the app package.") }
                        }
                    })
            }
        }
    }

    if (route == "tool") state.result?.let { result ->
        ResultSheet(result, onDismiss = vm::clearResult, onOpen = { path ->
            if (openDocument(LocalDocument.from(File(path)), "home")) {
                vm.clearResult()
                tab = "Home"
                if (route != "viewer") route = "home"
            }
        })
    }

    if (exitConfirmation) ExitDialog(
        onDismissRequest = { exitConfirmation = false },
        onExit = { exitConfirmation = false; activity?.finish() }
    )

    if (permissionDialog) AlertDialog(
        onDismissRequest = { permissionDialog = false; pending = null },
        title = { Text("Allow file access") },
        text = { Text("PDF Tools needs access to shared storage to find your documents, show file totals, and save merged or converted files. On the next screen, enable ‘Allow access to manage all files’. Android still protects other apps’ private folders.") },
        confirmButton = { TextButton(onClick = {
            permissionDialog = false
            if (Build.VERSION.SDK_INT >= 30) {
                try {
                    permissionSettings.launch(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${context.packageName}")))
                } catch (_: ActivityNotFoundException) {
                    runCatching { permissionSettings.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)) }
                        .onFailure { pending = null; vm.fail("Open All files access in Android Settings to grant access.") }
                }
            } else legacyPermissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }) { Text("Continue") } },
        dismissButton = { TextButton(onClick = { permissionDialog = false; pending = null }) { Text("Not now") } }
    )
    state.error?.let { error -> AlertDialog(
        onDismissRequest = vm::clearError, title = { Text("Couldn't complete that") }, text = { Text(error) },
        confirmButton = { TextButton(onClick = vm::clearError) { Text("OK") } }
    ) }
}

