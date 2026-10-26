package org.minsithu.stillmind

import java.time.LocalDate

fun main() {
    val day = LocalDate.of(2026, 10, 26)
    val base = RewardState()
    check(RewardEngine.complete(base, 479_999L, 600, day) == null)
    val eight = RewardEngine.complete(base, 480_000L, 600, day)!!
    check(eight.earned == 8 && eight.state.streak == 1)
    val again = RewardEngine.complete(eight.state, 600_000L, 600, day)!!
    check(again.state.streak == 1 && again.state.stars == 18)
    val cap = RewardEngine.complete(RewardState(100, 4, "2026-10-26", 59, "2026-10-26"), 1800_000L, 1800, day)!!
    check(cap.earned == 1 && cap.state.dailyStars == 60)
    check(RewardEngine.complete(RewardState(streak=4,lastCompleted="2026-10-25"), 600_000L, 600, day)!!.state.streak == 5)
    check(RewardEngine.complete(RewardState(stars=100,streak=4,lastCompleted="2026-10-20"), 600_000L, 600, day)!!.state.streak == 1)
    check(RewardEngine.complete(base, 1_000_000L, 0, day) == null)
    check(DailyQuotes.forDay(day).isNotBlank())
    check(DailyQuotes.forDay(day) == DailyQuotes.forDay(day))
    println("Android Kotlin RewardEngine + DailyQuotes: 9 checks passed")
}
