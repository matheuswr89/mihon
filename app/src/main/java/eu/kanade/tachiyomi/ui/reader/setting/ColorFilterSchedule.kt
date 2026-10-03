package eu.kanade.tachiyomi.ui.reader.setting

import java.time.LocalTime

/**
 * Helpers for the "schedule" option of the reader's custom color filter.
 *
 * Times are stored as minutes since midnight, in the range `0 until MINUTES_PER_DAY`.
 */
object ColorFilterSchedule {

    const val MINUTES_PER_DAY = 24 * 60

    const val DEFAULT_START = 22 * 60
    const val DEFAULT_END = 6 * 60

    /**
     * Whether [now] (minutes since midnight) falls inside the window that begins at [start] and ends at [end].
     *
     * The start is inclusive and the end is exclusive. A window whose end is earlier than its start crosses
     * midnight (e.g. 22:00 to 06:00). A window with equal start and end is treated as empty, so the filter
     * stays off instead of silently covering the whole day.
     */
    fun isActive(now: Int, start: Int, end: Int): Boolean {
        return when {
            start == end -> false
            start < end -> now in start until end
            else -> now >= start || now < end
        }
    }

    fun isActive(now: LocalTime, start: Int, end: Int): Boolean {
        return isActive(now.hour * 60 + now.minute, start, end)
    }

    /**
     * Milliseconds from [now] until the next whole minute. Used to re-evaluate the schedule exactly when the
     * clock changes minute, without polling more often than needed.
     */
    fun millisUntilNextMinute(now: LocalTime): Long {
        val elapsedInMinute = now.second * 1000L + now.nano / 1_000_000L
        return (60_000L - elapsedInMinute).coerceIn(1L, 60_000L)
    }
}
