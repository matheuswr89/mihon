package eu.kanade.tachiyomi.ui.reader.setting

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalTime

class ColorFilterScheduleTest {

    private fun minutes(hour: Int, minute: Int = 0) = hour * 60 + minute

    @Test
    fun `window within the same day`() {
        val start = minutes(9)
        val end = minutes(17)

        assertFalse(ColorFilterSchedule.isActive(minutes(8, 59), start, end))
        assertTrue(ColorFilterSchedule.isActive(minutes(9), start, end))
        assertTrue(ColorFilterSchedule.isActive(minutes(12), start, end))
        assertTrue(ColorFilterSchedule.isActive(minutes(16, 59), start, end))
        assertFalse(ColorFilterSchedule.isActive(minutes(17), start, end))
        assertFalse(ColorFilterSchedule.isActive(minutes(23, 59), start, end))
    }

    @Test
    fun `window crossing midnight`() {
        val start = minutes(22)
        val end = minutes(6)

        assertFalse(ColorFilterSchedule.isActive(minutes(21, 59), start, end))
        assertTrue(ColorFilterSchedule.isActive(minutes(22), start, end))
        assertTrue(ColorFilterSchedule.isActive(minutes(23, 59), start, end))
        assertTrue(ColorFilterSchedule.isActive(minutes(0), start, end))
        assertTrue(ColorFilterSchedule.isActive(minutes(5, 59), start, end))
        assertFalse(ColorFilterSchedule.isActive(minutes(6), start, end))
        assertFalse(ColorFilterSchedule.isActive(minutes(12), start, end))
    }

    @Test
    fun `equal start and end is an empty window`() {
        for (now in listOf(0, minutes(6), minutes(12), minutes(23, 59))) {
            assertFalse(ColorFilterSchedule.isActive(now, minutes(6), minutes(6)))
        }
    }

    @Test
    fun `defaults cover the night`() {
        val start = ColorFilterSchedule.DEFAULT_START
        val end = ColorFilterSchedule.DEFAULT_END

        assertTrue(ColorFilterSchedule.isActive(LocalTime.of(23, 30), start, end))
        assertTrue(ColorFilterSchedule.isActive(LocalTime.of(3, 0), start, end))
        assertFalse(ColorFilterSchedule.isActive(LocalTime.of(15, 0), start, end))
    }

    @Test
    fun `millis until next minute`() {
        assertEquals(60_000L, ColorFilterSchedule.millisUntilNextMinute(LocalTime.of(10, 0, 0, 0)))
        assertEquals(30_000L, ColorFilterSchedule.millisUntilNextMinute(LocalTime.of(10, 0, 30, 0)))
        assertEquals(1L, ColorFilterSchedule.millisUntilNextMinute(LocalTime.of(10, 0, 59, 999_000_000)))
        assertEquals(500L, ColorFilterSchedule.millisUntilNextMinute(LocalTime.of(10, 0, 59, 500_000_000)))
    }
}
