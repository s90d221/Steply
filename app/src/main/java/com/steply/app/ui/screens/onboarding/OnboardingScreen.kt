package com.steply.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.steply.app.ui.screens.components.SafetyNoticeCard
import com.steply.app.ui.screens.components.SteplyAccentOrange
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyDeepTeal
import com.steply.app.ui.screens.components.SteplyPrimaryButton
import com.steply.app.ui.screens.components.SteplySoftBlue
import com.steply.app.ui.screens.components.SteplyWarmCream
import com.steply.app.ui.text.SteplyCopy

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onContinue: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SteplyWarmCream,
                        Color(0xFFFFFBF5),
                        Color(0xFFEFF5F1),
                    ),
                ),
            )
            .systemBarsPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Steply",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Small steps. Safer moves.",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "A gentle movement companion for safer daily practice.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            VisualHeroCard()

            SteplyPrimaryButton(
                text = if (uiState.isSaving) "Saving" else "Get Started",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = onContinue,
                enabled = !uiState.isSaving,
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BenefitCard(
                    title = "No signup",
                    text = "Start with a simple local profile.",
                    icon = Icons.Default.Person,
                )
                BenefitCard(
                    title = "Local data only",
                    text = "Your data stays on this device.",
                    icon = Icons.Default.Info,
                )
                BenefitCard(
                    title = "Gentle movement checks",
                    text = "Use a calm 30-second chair stand check.",
                    icon = Icons.Default.FitnessCenter,
                )
            }

            SafetyNoticeCard(
                title = "Important note",
                text = SteplyCopy.MedicalDisclaimer,
            )
        }
    }
}

@Composable
private fun VisualHeroCard() {
    SteplyCard(
        containerColor = Color.White.copy(alpha = 0.96f),
        contentPadding = PaddingValues(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepDot(size = 26, color = SteplyAccentOrange)
            StepDot(size = 42, color = MaterialTheme.colorScheme.primary)
            MovementMark()
            StepDot(size = 34, color = SteplySoftBlue)
            StepDot(size = 22, color = SteplyDeepTeal)
        }
        Text(
            text = SteplyCopy.MoveSlowly,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StepDot(
    size: Int,
    color: Color,
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(color.copy(alpha = 0.9f), CircleShape),
    )
}

@Composable
private fun MovementMark() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, SteplyDeepTeal),
                ),
                shape = RoundedCornerShape(22.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.20f), CircleShape),
        )
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(30.dp),
        )
    }
}

@Composable
private fun BenefitCard(
    title: String,
    text: String,
    icon: ImageVector,
) {
    SteplyCard(contentPadding = PaddingValues(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
