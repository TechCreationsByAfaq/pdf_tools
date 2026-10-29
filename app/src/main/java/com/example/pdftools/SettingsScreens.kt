package com.example.pdftools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onBack: () -> Unit, onHistory: () -> Unit, onPrivacy: () -> Unit, onShare: () -> Unit) {
    var reviewNotice by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val version = remember { context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty() }
    Column(Modifier.fillMaxSize()) {
        AppHeader("Settings", onBack)
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SettingsRow("History", onHistory)
            SettingsRow("Privacy policy", onPrivacy)
            SettingsRow("Share app", onShare)
            SettingsRow("Review app") { reviewNotice = true }
            Text("App version $version", color = Muted, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 14.dp))
        }
    }
    if (reviewNotice) AlertDialog(onDismissRequest = { reviewNotice = false }, title = { Text("Review app") },
        text = { Text("Reviews will be available once PDF Tools is published on Google Play.") },
        confirmButton = { TextButton(onClick = { reviewNotice = false }) { Text("OK") } })
}

@Composable
private fun SettingsRow(title: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = androidx.compose.ui.graphics.Color.White,
        shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Line)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f))
            Icon(AppIcons.ArrowForward, null, Modifier.size(18.dp), tint = Muted)
        }
    }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AppHeader("Privacy policy", onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Effective 30 October 2026", color = Muted, style = MaterialTheme.typography.bodySmall)
            PolicySection("Your documents stay on your device", "PDF Tools merges, splits, compresses, converts and reads documents locally. OCR also runs on your device using a bundled text recognition model. This version does not upload your documents, images or recognized text to our servers.")
            PolicySection("File access", "With your permission, the app scans accessible shared storage for supported documents and images, displays their names, sizes and dates, and reads the files you select. Results are saved in Documents / PDF Tools. Android protects other apps’ private folders. You can revoke access in Android Settings → Apps → Special app access → All files access.")
            PolicySection("History and preferences", "Recent output paths and app preferences are stored locally in the app’s private storage. No account is required. This version has no advertising, analytics or tracking SDKs and does not request internet access. Android may collect system diagnostics under your device settings.")
            PolicySection("Sharing and opening files", "Files are passed to another app only when you choose to open or share them. The selected app receives temporary permission to read that file and handles it under its own privacy policy. Share app opens Android’s sharing menu with a copy of the installed app package; it does not include your documents or history.")
            PolicySection("Deleting data", "You can delete generated files through the file list or your device’s file manager. Clearing PDF Tools’ app data or uninstalling it removes its private history and preferences. Exported files in shared storage remain until you delete them. Automatic Android backup is disabled for this app.")
            PolicySection("Changes to this policy", "This policy describes the current version. If a future release adds accounts, online processing, analytics or advertising, the policy will be updated to explain those changes before you use them.")
        }
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(body, color = Muted, style = MaterialTheme.typography.bodyMedium)
    }
}
