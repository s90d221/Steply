package com.steply.app.ui.screens.check

import com.steply.app.analysis.SteadiAssessmentRules
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.steply.app.ui.screens.components.SteplyAccentOrange
import com.steply.app.ui.screens.components.SteplyDeepTeal
import com.steply.app.ui.screens.components.SteplySoftBlue

object MovementChallengeIds {
    const val ChairStand = "chair_stand"
    const val Balance = "balance"
    const val TimedUpAndGo = "timed_up_and_go"
}

data class MovementChallengeSpec(
    val id: String,
    val title: String,
    val purpose: String,
    val requiredSetup: String,
    val durationText: String,
    val primaryMetric: String,
    val ruleSummary: String,
    val isMeasurementReady: Boolean,
)

val MovementChallenges = listOf(
    MovementChallengeSpec(
        id = MovementChallengeIds.ChairStand,
        title = "30-Second Chair Stand",
        purpose = "Lower-body strength",
        requiredSetup = "17 in chair, full body in camera",
        durationText = "30 seconds",
        primaryMetric = "Camera count and posture",
        ruleSummary = SteadiAssessmentRules.ChairStandRuleSummary,
        isMeasurementReady = true,
    ),
    MovementChallengeSpec(
        id = MovementChallengeIds.Balance,
        title = "4-Stage Balance",
        purpose = "Balance hold",
        requiredSetup = "Chair or support nearby",
        durationText = "10 seconds per stance",
        primaryMetric = "Hold time and sway",
        ruleSummary = SteadiAssessmentRules.BalanceRuleSummary,
        isMeasurementReady = false,
    ),
    MovementChallengeSpec(
        id = MovementChallengeIds.TimedUpAndGo,
        title = "Timed Up and Go",
        purpose = "Walking and turning",
        requiredSetup = "Chair and 10 ft clear path",
        durationText = "Timed from go to seated",
        primaryMetric = "Total time and turn stability",
        ruleSummary = SteadiAssessmentRules.TugRuleSummary,
        isMeasurementReady = false,
    ),
)

fun movementChallengeById(challengeId: String?): MovementChallengeSpec {
    return MovementChallenges.firstOrNull { it.id == challengeId }
        ?: MovementChallenges.first()
}

fun MovementChallengeSpec.icon(): ImageVector {
    return when (id) {
        MovementChallengeIds.Balance -> Icons.Default.AccessibilityNew
        MovementChallengeIds.TimedUpAndGo -> Icons.AutoMirrored.Filled.DirectionsWalk
        else -> Icons.Default.FitnessCenter
    }
}

fun MovementChallengeSpec.accentColor(): Color {
    return when (id) {
        MovementChallengeIds.Balance -> SteplySoftBlue
        MovementChallengeIds.TimedUpAndGo -> SteplyAccentOrange
        else -> SteplyDeepTeal
    }
}

fun MovementChallengeSpec.accentContainerColor(): Color {
    return when (id) {
        MovementChallengeIds.Balance -> Color(0xFFE7EEFB)
        MovementChallengeIds.TimedUpAndGo -> Color(0xFFFFE7D1)
        else -> Color(0xFFDDECE7)
    }
}
