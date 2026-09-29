package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicationPickup
import com.example.data.model.PickupStatus
import com.example.ui.theme.AdherenceGreen
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.RibbonRed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun MedicationCalendarView(
    pickups: List<MedicationPickup>,
    isPrivacy: Boolean,
    onCompletePickup: (MedicationPickup) -> Unit,
    onDeletePickup: (Int) -> Unit,
    onScheduleForDate: (calendar: Calendar) -> Unit,
    onTestAlarm: (MedicationPickup) -> Unit,
    modifier: Modifier = Modifier
) {
    // Current viewed calendar month
    val currentCal = remember { Calendar.getInstance() }
    var displayedYear by remember { mutableStateOf(currentCal.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableStateOf(currentCal.get(Calendar.MONTH)) }

    // Selected day in the displayed month
    var selectedDay by remember { mutableStateOf(currentCal.get(Calendar.DAY_OF_MONTH)) }

    // Calendar calculations
    val monthCal = remember(displayedYear, displayedMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, displayedYear)
            set(Calendar.MONTH, displayedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    // In Java Calendar, SUNDAY=1, MONDAY=2. In Spanish calendar, week starts on Monday
    val firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK)
    val dayOffset = (firstDayOfWeek - Calendar.MONDAY + 7) % 7

    val monthNames = arrayOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    val displayedMonthName = monthNames[displayedMonth]

    // Check today's date
    val todayCal = Calendar.getInstance()
    val isCurrentMonthToday = todayCal.get(Calendar.YEAR) == displayedYear &&
            todayCal.get(Calendar.MONTH) == displayedMonth
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    // Build events mapping for fast lookup in displayed month
    val pickupDays = remember(pickups, displayedYear, displayedMonth) {
        val map = mutableMapOf<Int, MutableList<MedicationPickup>>()
        pickups.forEach { p ->
            val cal = Calendar.getInstance().apply { timeInMillis = p.scheduledDate }
            if (cal.get(Calendar.YEAR) == displayedYear && cal.get(Calendar.MONTH) == displayedMonth) {
                val day = cal.get(Calendar.DAY_OF_MONTH)
                map.getOrPut(day) { mutableListOf() }.add(p)
            }
        }
        map
    }

    // 2-Days-before reminder days mapping
    val reminderTwoDays = remember(pickups, displayedYear, displayedMonth) {
        val map = mutableMapOf<Int, MutableList<MedicationPickup>>()
        pickups.filter { it.status == PickupStatus.PROGRAMADO && it.reminder2DaysBefore }.forEach { p ->
            val remCal = Calendar.getInstance().apply {
                timeInMillis = p.scheduledDate - (2 * 24 * 60 * 60 * 1000L)
            }
            if (remCal.get(Calendar.YEAR) == displayedYear && remCal.get(Calendar.MONTH) == displayedMonth) {
                val day = remCal.get(Calendar.DAY_OF_MONTH)
                map.getOrPut(day) { mutableListOf() }.add(p)
            }
        }
        map
    }

    // Selected date pickups and reminders
    val selectedPickups = pickupDays[selectedDay] ?: emptyList()
    val selectedReminders = reminderTwoDays[selectedDay] ?: emptyList()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month Navigation Header
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (displayedMonth == 0) {
                                displayedMonth = 11
                                displayedYear -= 1
                            } else {
                                displayedMonth -= 1
                            }
                            selectedDay = 1
                        },
                        modifier = Modifier.testTag("btn_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Mes anterior"
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$displayedMonthName $displayedYear",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Control Mensual de Recojos TARV",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            onClick = {
                                displayedYear = todayCal.get(Calendar.YEAR)
                                displayedMonth = todayCal.get(Calendar.MONTH)
                                selectedDay = todayCal.get(Calendar.DAY_OF_MONTH)
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_today")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hoy", fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = {
                                if (displayedMonth == 11) {
                                    displayedMonth = 0
                                    displayedYear += 1
                                } else {
                                    displayedMonth += 1
                                }
                                selectedDay = 1
                            },
                            modifier = Modifier.testTag("btn_next_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Mes siguiente"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(
                        color = MaterialTheme.colorScheme.primary,
                        icon = Icons.Default.LocalPharmacy,
                        label = "Recojo TARV"
                    )
                    LegendItem(
                        color = AlertAmber,
                        icon = Icons.Default.NotificationsActive,
                        label = "Alarma 2 Días Antes"
                    )
                    LegendItem(
                        color = AdherenceGreen,
                        icon = Icons.Default.CheckCircle,
                        label = "Recogido"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Days of week header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val dayHeaders = listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM")
                    dayHeaders.forEach { header ->
                        Text(
                            text = header,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Calendar Grid
                val totalCells = dayOffset + daysInMonth
                val rows = (totalCells + 6) / 7

                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 0..6) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - dayOffset + 1

                            if (cellIndex < dayOffset || dayNumber > daysInMonth) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            } else {
                                val isToday = isCurrentMonthToday && dayNumber == todayDay
                                val isSelected = dayNumber == selectedDay
                                val dayPickups = pickupDays[dayNumber] ?: emptyList()
                                val hasScheduledPickup = dayPickups.any { it.status == PickupStatus.PROGRAMADO }
                                val hasCompletedPickup = dayPickups.any { it.status != PickupStatus.PROGRAMADO }
                                val hasTwoDayReminder = reminderTwoDays.containsKey(dayNumber)

                                CalendarDayCell(
                                    dayNumber = dayNumber,
                                    isToday = isToday,
                                    isSelected = isSelected,
                                    hasScheduledPickup = hasScheduledPickup,
                                    hasCompletedPickup = hasCompletedPickup,
                                    hasTwoDayReminder = hasTwoDayReminder,
                                    onClick = { selectedDay = dayNumber },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("calendar_day_$dayNumber")
                                )
                            }
                        }
                    }
                }
            }
        }

        // Selected Day Details Card
        SelectedDayCard(
            year = displayedYear,
            month = displayedMonth,
            day = selectedDay,
            pickups = selectedPickups,
            reminderPickups = selectedReminders,
            isPrivacy = isPrivacy,
            onCompletePickup = onCompletePickup,
            onDeletePickup = onDeletePickup,
            onSchedulePickup = {
                val targetCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, displayedYear)
                    set(Calendar.MONTH, displayedMonth)
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                }
                onScheduleForDate(targetCal)
            },
            onTestAlarm = onTestAlarm
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(9.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    isToday: Boolean,
    isSelected: Boolean,
    hasScheduledPickup: Boolean,
    hasCompletedPickup: Boolean,
    hasTwoDayReminder: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else -> Color.Transparent
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected || isToday) 1.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected || isToday || hasScheduledPickup) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )

            // Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasScheduledPickup) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                if (hasTwoDayReminder) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AlertAmber)
                    )
                }
                if (hasCompletedPickup) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AdherenceGreen)
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedDayCard(
    year: Int,
    month: Int,
    day: Int,
    pickups: List<MedicationPickup>,
    reminderPickups: List<MedicationPickup>,
    isPrivacy: Boolean,
    onCompletePickup: (MedicationPickup) -> Unit,
    onDeletePickup: (Int) -> Unit,
    onSchedulePickup: () -> Unit,
    onTestAlarm: (MedicationPickup) -> Unit
) {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, day)
    }
    val dateFormat = SimpleDateFormat("EEEE d 'de' MMMM yyyy", Locale("es", "ES"))
    val dateStr = dateFormat.format(cal.time)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Día $day de cada mes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Quick schedule button for this date
                FilledTonalButton(
                    onClick = onSchedulePickup,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_schedule_selected_day")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Programar", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Show Pickups on this day
            if (pickups.isNotEmpty()) {
                pickups.forEach { pickup ->
                    PickupDetailCard(
                        pickup = pickup,
                        isPrivacy = isPrivacy,
                        onComplete = { onCompletePickup(pickup) },
                        onDelete = { onDeletePickup(pickup.id) },
                        onTestAlarm = { onTestAlarm(pickup) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // 2. Show 2-Days-Before Reminder notice if this day is a 2-day reminder
            if (reminderPickups.isNotEmpty()) {
                reminderPickups.forEach { pickup ->
                    val pickupDateFormat = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "ES"))
                    val pickupDateStr = pickupDateFormat.format(Date(pickup.scheduledDate))

                    Surface(
                        color = AlertAmber.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AlertAmber.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = AlertAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "🔔 Alarma y Notificación 2 Días Antes",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Recordatorio automático para el recojo del $pickupDateStr.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { onTestAlarm(pickup) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_test_reminder_alarm"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertAmber)
                            ) {
                                Icon(imageVector = Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Probar Alarma y Notificación (2 Días Antes)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // 3. If neither pickups nor reminders
            if (pickups.isEmpty() && reminderPickups.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay recojos programados para este día",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Toca 'Programar' para fijar el día $day de cada mes como tu fecha de recojo en farmacia.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PickupDetailCard(
    pickup: MedicationPickup,
    isPrivacy: Boolean,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onTestAlarm: () -> Unit
) {
    val isCompleted = pickup.status != PickupStatus.PROGRAMADO
    val now = System.currentTimeMillis()
    val diffMillis = pickup.scheduledDate - now
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()

    Surface(
        color = if (isCompleted) AdherenceGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCompleted) AdherenceGreen.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        tint = if (isCompleted) AdherenceGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isPrivacy) "Tratamiento de control" else pickup.medicineName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = pickup.healthCenterName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Monthly recurrence note
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (pickup.autoMonthly) "Repetición automática la misma fecha cada mes" else "Recojo puntual",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!isCompleted) {
                Spacer(modifier = Modifier.height(6.dp))
                // 2-Day reminder active status
                Surface(
                    color = AlertAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AlarmOn,
                            contentDescription = null,
                            tint = AlertAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Alarma y notificación activas (2 días antes a las 09:00 AM)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onTestAlarm,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_test_alarm_pickup_${pickup.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertAmber)
                    ) {
                        Icon(imageVector = Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Probar Alarma", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onComplete,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_complete_pickup_${pickup.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Recogido", fontSize = 11.sp)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "✓ Medicamento recogido exitosamente",
                    style = MaterialTheme.typography.bodySmall,
                    color = AdherenceGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
