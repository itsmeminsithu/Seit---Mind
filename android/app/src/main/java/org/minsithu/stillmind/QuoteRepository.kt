package org.minsithu.stillmind

import android.content.Context
import org.json.JSONArray
import java.time.LocalDate

/** Offline original reflections shared with the web client as quotes.json. */
data class MindQuote(val category: String, val en: String, val mm: String)

object QuoteRepository {
    @Volatile private var cached: List<MindQuote>? = null
    private fun all(context: Context): List<MindQuote> {
        cached?.let { return it }
        return synchronized(this) {
            cached ?: run {
                val json = context.assets.open("quotes.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
                val array = JSONArray(json)
                (0 until array.length()).map { n ->
                    val item = array.getJSONObject(n)
                    MindQuote(item.getString("category"), item.getString("en"), item.getString("mm"))
                }.also { cached = it }
            }
        }
    }
    fun daily(context: Context, day: LocalDate = LocalDate.now()): MindQuote {
        val quotes = all(context)
        return quotes[Math.floorMod(day.toEpochDay(), quotes.size.toLong()).toInt()]
    }
    fun forMood(context: Context, mood: String, day: LocalDate = LocalDate.now()): MindQuote {
        val category = when {
            mood.contains("Positive",true) -> "gratitude"
            mood.contains("Focused",true) -> "growth"
            mood.contains("Refreshed",true) -> "rest"
            else -> "peace"
        }
        val quotes = all(context).filter { it.category == category }
        val offset = when(category){ "gratitude" -> 3; "growth" -> 7; "rest" -> 11; else -> 0 }
        return quotes[Math.floorMod(day.toEpochDay()+offset, quotes.size.toLong()).toInt()]
    }
}
