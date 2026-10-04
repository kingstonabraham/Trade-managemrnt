package com.example.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class TimeFilter(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month")
}

object TradingDateUtils {
    val ISO_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    val DISPLAY_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)
    val MONTH_YEAR_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
    val SHORT_MONTH_DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)

    /**
     * Given a date, returns the Monday and Friday of that trading week.
     * Note: Monday is first day of trading week, Friday is the last day.
     */
    fun getTradingWeek(date: LocalDate): Pair<LocalDate, LocalDate> {
        val monday = if (date.dayOfWeek == DayOfWeek.SUNDAY) {
            // If Sunday, associate with preceding trading week or following.
            // Using previous Monday:
            date.minusDays(6)
        } else {
            date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }
        val friday = monday.plusDays(4) // Monday + 4 = Friday
        return Pair(monday, friday)
    }

    /**
     * Given a date, returns start and end dates of the calendar month (1st to last day).
     */
    fun getCalendarMonth(yearMonth: YearMonth): Pair<LocalDate, LocalDate> {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        return Pair(start, end)
    }

    fun formatDisplayDate(date: LocalDate): String {
        return date.format(DISPLAY_DATE_FORMATTER)
    }

    fun formatWeekRange(startDate: LocalDate, endDate: LocalDate): String {
        return "${startDate.format(SHORT_MONTH_DAY_FORMATTER)} - ${endDate.format(DISPLAY_DATE_FORMATTER)}"
    }

    fun formatMonthYear(yearMonth: YearMonth): String {
        return yearMonth.format(MONTH_YEAR_FORMATTER)
    }

    fun parseIsoDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, ISO_DATE_FORMATTER)
        } catch (e: Exception) {
            null
        }
    }
}
