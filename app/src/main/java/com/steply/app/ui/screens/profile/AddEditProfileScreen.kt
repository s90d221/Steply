package com.steply.app.ui.screens.profile

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.input.KeyboardType
import com.steply.app.ui.screens.components.LocalDataNoticeCard
import com.steply.app.ui.screens.components.SteplySecondaryButton
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyPrimaryButton
import com.steply.app.ui.screens.components.SteplyScaffold
import com.steply.app.ui.screens.components.SteplyScreenColumn
import com.steply.app.ui.screens.components.SteplyTextField

@Composable
fun AddEditProfileScreen(
    uiState: AddEditProfileUiState,
    onDisplayNameChanged: (String) -> Unit,
    onBirthYearChanged: (String) -> Unit,
    onGenderChanged: (String) -> Unit,
    onHeightCmChanged: (String) -> Unit,
    onMobilityNoteChanged: (String) -> Unit,
    onEmergencyNoteChanged: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onCancel: () -> Unit,
    onSaved: () -> Unit,
    onSavedHandled: () -> Unit,
) {
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            onSaved()
            onSavedHandled()
        }
    }

    SteplyScaffold(
        title = if (uiState.isEditMode) "Edit Profile" else "Add Profile",
        subtitle = "Keep details simple and easy to recognize.",
        onBack = onCancel,
    ) { paddingValues ->
        SteplyScreenColumn(paddingValues = paddingValues) {
            if (uiState.isLoading) {
                Text(
                    text = "Loading profile details.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                return@SteplyScreenColumn
            }

            LocalDataNoticeCard()

            SteplyCard {
                FormSectionTitle("Profile basics")
                SteplyTextField(
                    value = uiState.displayName,
                    onValueChange = onDisplayNameChanged,
                    label = "Name or Nickname",
                )
                SteplyTextField(
                    value = uiState.birthYear,
                    onValueChange = onBirthYearChanged,
                    label = "Birth Year",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = "Optional, used only to keep profile details clear.",
                )
            }

            SteplyCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                FormSectionTitle("Optional details")
                Text(
                    text = "These fields help caregivers recognize the profile. You can leave them blank.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SteplyTextField(
                    value = uiState.gender,
                    onValueChange = onGenderChanged,
                    label = "Gender",
                )
                SteplyTextField(
                    value = uiState.heightCm,
                    onValueChange = onHeightCmChanged,
                    label = "Height in centimeters",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

            SteplyCard {
                FormSectionTitle("Movement and safety notes")
                SteplyTextField(
                    value = uiState.mobilityNote,
                    onValueChange = onMobilityNoteChanged,
                    label = "Movement Notes",
                    singleLine = false,
                    minHeightDp = 112,
                    supportingText = "Optional notes about balance, support, or daily movement.",
                )
                SteplyTextField(
                    value = uiState.emergencyNote,
                    onValueChange = onEmergencyNoteChanged,
                    label = "Safety Notes",
                    singleLine = false,
                    minHeightDp = 112,
                    supportingText = "Optional reminders for safe practice.",
                )
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            SteplyPrimaryButton(
                text = if (uiState.isSaving) "Saving" else "Save Profile",
                icon = Icons.Default.Check,
                onClick = onSaveProfile,
                enabled = !uiState.isSaving,
            )
            SteplySecondaryButton(
                text = "Cancel",
                icon = Icons.Default.Close,
                onClick = onCancel,
                enabled = !uiState.isSaving,
            )
        }
    }
}

@Composable
private fun FormSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
}
