package org.minsithu.stillmind

import java.time.LocalDate

/** Short original affirmations; deterministic per local calendar date. */
object DailyQuotes {
    private val quotes = listOf(
        "Peace begins with a moment you give yourself.",
        "Every breath is a new beginning.",
        "Rest is part of your journey, not a detour.",
        "Small mindful moments make meaningful days.",
        "You deserve the kindness you give others.",
        "A calmer mind grows one day at a time."
    )
    fun forDay(day: LocalDate = LocalDate.now()): String =
        quotes[day.toString().sumOf { it.code }.mod(quotes.size)]
}
