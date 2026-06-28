package com.steply.app.domain.usecase

import com.steply.app.domain.model.ChairStandHistoryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculateHistorySummaryUseCaseTest {
    @Test
    fun `summary calculates latest best and average counts`() {
        val summary = CalculateHistorySummaryUseCase(
            listOf(
                historyItem(resultId = "latest", count = 10, createdAt = 3L),
                historyItem(resultId = "previous", count = 9, createdAt = 2L),
                historyItem(resultId = "oldest", count = 8, createdAt = 1L),
            ),
        )

        assertEquals(10, summary.latestCount)
        assertEquals(10, summary.bestCount)
        assertEquals(9f, summary.averageCount ?: 0f, 0.001f)
    }

    @Test
    fun `trend message handles improving matching and lower latest counts`() {
        val improving = listOf(
            historyItem(resultId = "latest", count = 11, createdAt = 2L),
            historyItem(resultId = "previous", count = 9, createdAt = 1L),
        )
        val matching = listOf(
            historyItem(resultId = "latest", count = 9, createdAt = 2L),
            historyItem(resultId = "previous", count = 9, createdAt = 1L),
        )
        val lower = listOf(
            historyItem(resultId = "latest", count = 7, createdAt = 2L),
            historyItem(resultId = "previous", count = 9, createdAt = 1L),
        )

        assertEquals(
            "Your latest result improved from the previous check.",
            CalculateHistorySummaryUseCase.trendMessageFor(improving),
        )
        assertEquals(
            "Your latest result stayed about the same.",
            CalculateHistorySummaryUseCase.trendMessageFor(matching),
        )
        assertEquals(
            "Your latest result was a little lower today. Move slowly and safely.",
            CalculateHistorySummaryUseCase.trendMessageFor(lower),
        )
    }

    @Test
    fun `empty summary has no counts or trend`() {
        val summary = CalculateHistorySummaryUseCase(emptyList())

        assertNull(summary.latestCount)
        assertNull(summary.bestCount)
        assertNull(summary.averageCount)
        assertNull(summary.trendMessage)
    }

    private fun historyItem(
        resultId: String,
        count: Int,
        createdAt: Long,
        userId: String = "user-a",
    ): ChairStandHistoryItem {
        return ChairStandHistoryItem(
            resultId = resultId,
            userId = userId,
            repetitionCount = count,
            durationSeconds = 30,
            recommendationLevel = ChairStandRecommendationLevelCalculator.STEADY,
            createdAt = createdAt,
        )
    }
}
