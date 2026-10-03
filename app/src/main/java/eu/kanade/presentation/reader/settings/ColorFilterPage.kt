package eu.kanade.presentation.reader.settings

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import eu.kanade.tachiyomi.ui.reader.setting.ColorFilterSchedule
import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences.Companion.ColorFilterMode
import eu.kanade.tachiyomi.ui.reader.setting.ReaderSettingsViewModel
import tachiyomi.core.common.preference.getAndSet
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.CheckboxItem
import tachiyomi.presentation.core.components.SettingsChipRow
import tachiyomi.presentation.core.components.SettingsItemsPaddings
import tachiyomi.presentation.core.components.SliderItem
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun ColumnScope.ColorFilterPage(viewModel: ReaderSettingsViewModel) {
    val customBrightness by viewModel.preferences.customBrightness.collectAsState()
    CheckboxItem(
        label = stringResource(MR.strings.pref_custom_brightness),
        pref = viewModel.preferences.customBrightness,
    )

    /*
     * Sets the brightness of the screen. Range is [-75, 100].
     * From -75 to -1 a semi-transparent black view is shown at the top with the minimum brightness.
     * From 1 to 100 it sets that value as brightness.
     * 0 sets system brightness and hides the overlay.
     */
    if (customBrightness) {
        val customBrightnessValue by viewModel.preferences.customBrightnessValue.collectAsState()
        SliderItem(
            value = customBrightnessValue,
            valueRange = -75..100,
            steps = 0,
            label = stringResource(MR.strings.pref_custom_brightness),
            onChange = { viewModel.preferences.customBrightnessValue.set(it) },
            pillColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
    }

    val colorFilter by viewModel.preferences.colorFilter.collectAsState()
    CheckboxItem(
        label = stringResource(MR.strings.pref_custom_color_filter),
        pref = viewModel.preferences.colorFilter,
    )
    if (colorFilter) {
        val colorFilterValue by viewModel.preferences.colorFilterValue.collectAsState()
        SliderItem(
            value = colorFilterValue.red,
            valueRange = 0..255,
            steps = 0,
            label = stringResource(MR.strings.color_filter_r_value),
            onChange = { newRValue ->
                viewModel.preferences.colorFilterValue.getAndSet {
                    getColorValue(it, newRValue, RED_MASK, 16)
                }
            },
            pillColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        SliderItem(
            value = colorFilterValue.green,
            valueRange = 0..255,
            steps = 0,
            label = stringResource(MR.strings.color_filter_g_value),
            onChange = { newGValue ->
                viewModel.preferences.colorFilterValue.getAndSet {
                    getColorValue(it, newGValue, GREEN_MASK, 8)
                }
            },
            pillColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        SliderItem(
            value = colorFilterValue.blue,
            valueRange = 0..255,
            steps = 0,
            label = stringResource(MR.strings.color_filter_b_value),
            onChange = { newBValue ->
                viewModel.preferences.colorFilterValue.getAndSet {
                    getColorValue(it, newBValue, BLUE_MASK, 0)
                }
            },
            pillColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        SliderItem(
            value = colorFilterValue.alpha,
            valueRange = 0..255,
            steps = 0,
            label = stringResource(MR.strings.color_filter_a_value),
            onChange = { newAValue ->
                viewModel.preferences.colorFilterValue.getAndSet {
                    getColorValue(it, newAValue, ALPHA_MASK, 24)
                }
            },
            pillColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )

        val colorFilterMode by viewModel.preferences.colorFilterMode.collectAsState()
        SettingsChipRow(MR.strings.pref_color_filter_mode) {
            ColorFilterMode.mapIndexed { index, it ->
                FilterChip(
                    selected = colorFilterMode == index,
                    onClick = { viewModel.preferences.colorFilterMode.set(index) },
                    label = { Text(stringResource(it.first)) },
                )
            }
        }

        val colorFilterSchedule by viewModel.preferences.colorFilterSchedule.collectAsState()
        CheckboxItem(
            label = stringResource(MR.strings.pref_color_filter_schedule),
            pref = viewModel.preferences.colorFilterSchedule,
        )
        if (colorFilterSchedule) {
            val scheduleStart by viewModel.preferences.colorFilterScheduleStart.collectAsState()
            val scheduleEnd by viewModel.preferences.colorFilterScheduleEnd.collectAsState()
            TimeItem(
                label = stringResource(MR.strings.pref_color_filter_schedule_start),
                minutes = scheduleStart,
                onChange = { viewModel.preferences.colorFilterScheduleStart.set(it) },
            )
            TimeItem(
                label = stringResource(MR.strings.pref_color_filter_schedule_end),
                minutes = scheduleEnd,
                onChange = { viewModel.preferences.colorFilterScheduleEnd.set(it) },
            )
        }
    }

    CheckboxItem(
        label = stringResource(MR.strings.pref_grayscale),
        pref = viewModel.preferences.grayscale,
    )
    CheckboxItem(
        label = stringResource(MR.strings.pref_inverted_colors),
        pref = viewModel.preferences.invertedColors,
    )
}

@Composable
private fun TimeItem(
    label: String,
    minutes: Int,
    onChange: (Int) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    val time = remember(minutes) {
        val safe = minutes.coerceIn(0, ColorFilterSchedule.MINUTES_PER_DAY - 1)
        LocalTime.of(safe / 60, safe % 60)
    }
    val formatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }

    Row(
        modifier = Modifier
            .clickable { showDialog = true }
            .fillMaxWidth()
            .padding(
                horizontal = SettingsItemsPaddings.Horizontal,
                vertical = SettingsItemsPaddings.Vertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = time.format(formatter),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }

    if (showDialog) {
        TimePickerDialog(
            title = label,
            initialTime = time,
            onConfirm = {
                onChange(it.hour * 60 + it.minute)
                showDialog = false
            },
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun TimePickerDialog(
    title: String,
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = DateFormat.is24HourFormat(LocalContext.current),
    )
    AlertDialog(
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text(stringResource(MR.strings.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(MR.strings.action_cancel))
            }
        },
        onDismissRequest = onDismiss,
    )
}

private fun getColorValue(currentColor: Int, color: Int, mask: Long, bitShift: Int): Int {
    return (color shl bitShift) or (currentColor and mask.inv().toInt())
}
private const val ALPHA_MASK: Long = 0xFF000000
private const val RED_MASK: Long = 0x00FF0000
private const val GREEN_MASK: Long = 0x0000FF00
private const val BLUE_MASK: Long = 0x000000FF
