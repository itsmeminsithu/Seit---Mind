package org.minsithu.stillmind

import java.time.LocalDate
import kotlin.math.floor
import kotlin.math.min

data class RewardState(
    val stars: Int = 0,
    val streak: Int = 0,
    val lastCompleted: String = "",
    val dailyStars: Int = 0,
    val dailyStarsDate: String = ""
)

data class EarnedReward(val state: RewardState, val earned: Int, val completedMinutes: Int)

/** Pure reward engine. Time must come from real playback, not the duration picker. */
object RewardEngine {
    fun complete(previous: RewardState, elapsedMs: Long, plannedSeconds: Int, day: LocalDate = LocalDate.now()): EarnedReward? {
        if (plannedSeconds !in 60..10800 || elapsedMs < 0L ||
            elapsedMs < plannedSeconds.toLong() * 800L) return null
        val minutes = floor(min(elapsedMs, plannedSeconds * 1000L) / 60000.0).toInt()
        val today = day.toString()
        val streak = when (previous.lastCompleted) {
            today -> previous.streak.coerceAtLeast(1)
            day.minusDays(1).toString() -> previous.streak.coerceAtLeast(0) + 1
            else -> 1
        }
        val used = if (previous.dailyStarsDate == today) previous.dailyStars.coerceIn(0, 60) else 0
        val earned = min(60 - used, minutes).coerceAtLeast(0)
        return EarnedReward(
            previous.copy(stars = previous.stars.coerceAtLeast(0) + earned,
                streak = streak, lastCompleted = today,
                dailyStars = used + earned, dailyStarsDate = today),
            earned, minutes
        )
    }
}
