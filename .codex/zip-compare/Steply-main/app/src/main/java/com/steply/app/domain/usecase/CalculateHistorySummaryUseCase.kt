package com.steply.app.domain.usecase

import com.steply.app.domain.model.ChairStandHistoryItem

data class ChairStandHistorySummary(
    val latestCount: Int?,
    val bestCount: Int?,
    val averageCount: Float?,
    val trendMessage: String?,
)

object CalculateHistorySummaryUseCase {
    operator fun invoke(results: List<ChairStandHistoryItem>): ChairStandHistorySummary {
        val counts = results.map { it.repetitionCount }
        return ChairStandHistorySummary(
            latestCount = counts.firstOrNull(),
            bestCount = counts.maxOrNull(),
            averageCount = counts.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            trendMessage = trendMessageFor(results),
        )
    }

    fun trendMessageFor(results: List<ChairStandHistoryItem>): String? {
        if (results.size < 2) return null

        val latest = results[0].repetitionCount
        val previous = results[1].repetitionCount
        return when {
            latest > previous -> "Your latest result improved from the previous check."
            latest == previous -> "Your latest result stayed about the same."
            else -> "Your latest result was a little lower today. Move slowly and safely."
        }
    }
}
