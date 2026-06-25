package com.steply.app.ui.screens.safety

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.steply.app.ui.screens.components.SafetyChecklistCard
import com.steply.app.ui.screens.components.SafetyChecklistItem
import com.steply.app.ui.screens.components.SafetyNoticeCard
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyPrimaryButton
import com.steply.app.ui.screens.components.SteplyScaffold
import com.steply.app.ui.screens.components.SteplyScreenColumn
import com.steply.app.ui.text.SteplyCopy

@Composable
fun SafetyScreen(
    challengeTitle: String,
    onBack: () -> Unit,
    onStart: () -> Unit,
) {
    var safetyChecked by rememberSaveable { mutableStateOf(false) }

    SteplyScaffold(
        title = "Before You Start",
        subtitle = challengeTitle,
        onBack = onBack,
    ) { paddingValues ->
        SteplyScreenColumn(paddingValues = paddingValues) {
            SafetyChecklistCard(
                items = listOf(
                    SafetyChecklistItem("Stable chair"),
                    SafetyChecklistItem("Clear floor"),
                    SafetyChecklistItem("Full body visible"),
                    SafetyChecklistItem("Use support if needed"),
                    SafetyChecklistItem("Stop if uncomfortable"),
                ),
            )

            SteplyCard(
                modifier = Modifier.clickable { safetyChecked = !safetyChecked },
                containerColor = if (safetyChecked) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = safetyChecked,
                        onCheckedChange = { safetyChecked = it },
                    )
                    Text(
                        text = "I have checked the safety instructions.",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            SteplyPrimaryButton(
                text = "Start Check",
                icon = Icons.Default.PlayArrow,
                onClick = onStart,
                enabled = safetyChecked,
            )

            SafetyNoticeCard(
                title = "Medical disclaimer",
                text = SteplyCopy.MedicalDisclaimerWithStop,
            )
        }
    }
}
