package com.steply.app.ui.screens.safety

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import com.steply.app.ui.screens.components.SafetyNoticeCard
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyPrimaryButton
import com.steply.app.ui.screens.components.SteplyScaffold
import com.steply.app.ui.screens.components.SteplyScreenColumn
import com.steply.app.ui.text.SteplyCopy

@Composable
fun SafetyScreen(
    onBack: () -> Unit,
    onStart: () -> Unit,
) {
    var safetyChecked by rememberSaveable { mutableStateOf(false) }

    SteplyScaffold(
        title = "Before You Start",
        subtitle = "Take a moment to set up safely.",
        onBack = onBack,
    ) { paddingValues ->
        SteplyScreenColumn(paddingValues = paddingValues) {
            SafetyNoticeCard(
                title = SteplyCopy.MoveSlowly,
                text = SteplyCopy.UseSupport,
            )

            SteplyCard {
                Text(
                    text = "Safety checklist",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SafetyItem("Stable chair")
                    SafetyItem("Clear floor")
                    SafetyItem("Stop if uncomfortable")
                    SafetyItem(SteplyCopy.UseSupport)
                }
            }

            SafetyNoticeCard(
                title = "Medical disclaimer",
                text = SteplyCopy.MedicalDisclaimerWithStop,
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
        }
    }
}

@Composable
private fun SafetyItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
