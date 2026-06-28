package com.steply.app.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.steply.app.ui.screens.components.RemoteCameraQrScanner
import com.steply.app.ui.screens.components.SteplySecondaryButton
import com.steply.app.ui.screens.components.SteplyBottomNavigation
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyDestructiveButton
import com.steply.app.ui.screens.components.SteplyMainTab
import com.steply.app.ui.screens.components.SteplyPrimaryButton
import com.steply.app.ui.screens.components.SteplyScaffold
import com.steply.app.ui.screens.components.SteplyScreenColumn
import com.steply.app.ui.screens.components.WarmNoteSurface

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onToday: () -> Unit,
    onCheck: () -> Unit,
    onHistory: () -> Unit,
    onChangeProfile: () -> Unit,
    onExportSelectedUserData: () -> Unit,
    onExportShared: () -> Unit,
    onRemoteCameraQrScanned: (String) -> Boolean,
    onDeleteSelectedUserData: () -> Unit,
    onDeleteAllLocalData: () -> Unit,
) {
    val context = LocalContext.current
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showQrScanner by remember { mutableStateOf(false) }
    var qrScanMessage by remember { mutableStateOf<String?>(null) }
    val isDeleting = uiState.deleteState is SettingsDeleteState.Deleting
    val isExporting = uiState.exportState is SettingsExportState.Exporting
    val showBottomNavigation = uiState.selectedUser != null
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            showQrScanner = true
            qrScanMessage = null
        } else {
            qrScanMessage = "Camera permission is needed to scan the PC QR code."
        }
    }

    LaunchedEffect(uiState.exportState) {
        val exportState = uiState.exportState
        if (exportState is SettingsExportState.Ready) {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, "Steply Profile Data")
                putExtra(Intent.EXTRA_TEXT, exportState.json)
            }
            val chooser = Intent.createChooser(sendIntent, "Export Current Profile Data")
            runCatching { context.startActivity(chooser) }
            onExportShared()
        }
    }

    if (showDeleteSelectedDialog) {
        DeleteConfirmationDialog(
            title = "Delete this profile's data?",
            message = "This will delete the current profile, movement checks, and exercise records stored on this device. This cannot be undone.",
            confirmText = "Delete Profile Data",
            onDismiss = { showDeleteSelectedDialog = false },
            onConfirm = {
                showDeleteSelectedDialog = false
                onDeleteSelectedUserData()
            },
        )
    }

    if (showDeleteAllDialog) {
        DeleteConfirmationDialog(
            title = "Delete all local data?",
            message = "This will delete every profile and record stored by Steply on this device. This cannot be undone.",
            confirmText = "Delete All Data",
            onDismiss = { showDeleteAllDialog = false },
            onConfirm = {
                showDeleteAllDialog = false
                onDeleteAllLocalData()
            },
        )
    }

    if (showQrScanner) {
        AlertDialog(
            onDismissRequest = { showQrScanner = false },
            title = {
                Text(
                    text = "Scan PC QR",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RemoteCameraQrScanner(
                        onQrCodeScanned = { rawValue ->
                            val saved = onRemoteCameraQrScanned(rawValue)
                            qrScanMessage = if (saved) {
                                "PC camera IP saved."
                            } else {
                                "This QR code is not a Steply camera connection."
                            }
                            showQrScanner = false
                        },
                    )
                    Text(
                        text = "Point the camera at the QR code on the PC viewer screen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showQrScanner = false }) {
                    Text("Close")
                }
            },
        )
    }

    SteplyScaffold(
        title = "Settings",
        subtitle = "Manage local data and profile options.",
        bottomBar = {
            if (showBottomNavigation) {
                SteplyBottomNavigation(
                    currentTab = SteplyMainTab.Settings,
                    onToday = onToday,
                    onCheck = onCheck,
                    onHistory = onHistory,
                    onSettings = {},
                )
            }
        },
    ) { paddingValues ->
        SteplyScreenColumn(paddingValues = paddingValues) {
            if (uiState.isLoading) {
                Text(
                    text = "Loading settings.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                return@SteplyScreenColumn
            }

            SettingsSection(title = "Local Data") {
                WarmNoteSurface(
                    title = "No account needed",
                    text = "Profiles and movement records stay on this device.",
                    icon = Icons.Default.Person,
                )
                Text(
                    text = "No signup or cloud sync. Records cannot be restored if this device is lost.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            CurrentUserSection(
                selectedUserName = uiState.selectedUser?.displayName,
                onChangeProfile = onChangeProfile,
            )

            SettingsSection(title = "PC Camera Connection") {
                WarmNoteSurface(
                    title = if (uiState.remoteCameraHost == null) "No PC linked" else "PC linked",
                    text = uiState.remoteCameraHost ?: "Scan the QR code on the PC viewer once. Steply will remember the PC IP.",
                    icon = Icons.Default.CastConnected,
                )
                qrScanMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                SteplySecondaryButton(
                    text = "Scan PC QR",
                    icon = Icons.Default.QrCodeScanner,
                    onClick = {
                        if (hasCameraPermission) {
                            showQrScanner = true
                            qrScanMessage = null
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                )
            }

            SettingsSection(title = "Export") {
                Text(
                    text = "Create a local text export for the current profile.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                SteplyPrimaryButton(
                    text = if (isExporting) "Exporting" else "Export Current Profile Data",
                    icon = Icons.Default.Share,
                    enabled = uiState.selectedUser != null && !isExporting && !isDeleting,
                    onClick = onExportSelectedUserData,
                )
            }

            SettingsSection(
                title = "Delete Data",
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ) {
                WarmNoteSurface(
                    title = "Deletion cannot be undone",
                    text = "Use these actions only when you are sure the local records are no longer needed.",
                    icon = Icons.Default.Delete,
                )
                SteplyDestructiveButton(
                    text = "Delete Current Profile Data",
                    icon = Icons.Default.Delete,
                    enabled = uiState.selectedUser != null && !isDeleting,
                    onClick = { showDeleteSelectedDialog = true },
                )
                Text(
                    text = "Most cautious action",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                SteplyDestructiveButton(
                    text = "Delete All Local Data",
                    icon = Icons.Default.DeleteForever,
                    enabled = !isDeleting,
                    onClick = { showDeleteAllDialog = true },
                )
            }

            SettingsSection(title = "About Steply") {
                WarmNoteSurface(
                    title = "Movement-care support",
                    text = "Steply helps older adults complete gentle movement checks and review local exercise guidance.",
                    icon = Icons.Default.Info,
                )
                Text(
                    text = "Steply is not a medical diagnosis tool. It does not require signup and does not sync records to the cloud.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            uiState.successMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun CurrentUserSection(
    selectedUserName: String?,
    onChangeProfile: () -> Unit,
) {
    SettingsSection(title = "Current Profile") {
        Text(
            text = selectedUserName ?: "No profile is currently selected.",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Switch profiles to view another person's records.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SteplySecondaryButton(
            text = "Change Profile",
            icon = Icons.Default.Person,
            onClick = onChangeProfile,
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    SteplyCard(containerColor = containerColor) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}
