package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.MedicationPickup
import com.example.data.model.PatientProfile
import com.example.data.model.PickupStatus
import com.example.ui.components.AddPickupDialog
import com.example.ui.components.MedicationCalendarView
import com.example.ui.theme.AdherenceGreen
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.MedicalTeal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class ScheduleViewMode(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CALENDAR("Calendario Mensual", Icons.Default.CalendarMonth),
    LIST("Lista de Recojos", Icons.AutoMirrored.Filled.FormatListBulleted)
}

@Composable
fun MedicationScheduleScreen(
    patientProfile: PatientProfile?,
    pickups: List<MedicationPickup>,
    onAddPickup: (scheduledDate: Long, medicine: String, center: String, days: Int, notes: String, autoMonthly: Boolean, reminder2Days: Boolean) -> Unit,
    onCompletePickup: (MedicationPickup) -> Unit,
    onDeletePickup: (Int) -> Unit,
    onTestAlarm: (MedicationPickup) -> Unit = {},
    onTestGeneralAlarm: () -> Unit = {},
    onSyncPickupWithCalendarAndClock: (MedicationPickup) -> Unit = {},
    onSyncAllWithCalendarAndClock: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDateForAdd by remember { mutableStateOf<Long?>(null) }
    var viewMode by remember { mutableStateOf(ScheduleViewMode.CALENDAR) }
    val isPrivacy = patientProfile?.privacyModeEnabled == true
    val context = LocalContext.current

    var hasCalendarPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.WRITE_CALENDAR] ?: false
        hasCalendarPermission = granted
        if (granted) {
            onSyncAllWithCalendarAndClock()
        }
    }

    val activePickups = pickups.filter { it.status == PickupStatus.PROGRAMADO }.sortedBy { it.scheduledDate }
    val pastPickups = pickups.filter { it.status != PickupStatus.PROGRAMADO }.sortedByDescending { it.scheduledDate }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedDateForAdd = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_pickup")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Programar Recojo")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Mode Selector: Calendario Mensual | Lista de Recojos
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ScheduleViewMode.values().forEach { mode ->
                            val isSelected = viewMode == mode
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                shape = RoundedCornerShape(12.dp),
                                shadowElevation = if (isSelected) 2.dp else 0.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewMode = mode }
                                    .testTag("tab_${mode.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = mode.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = mode.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sincronización Automática con Calendario y Reloj (Alarmas a las 8:00 AM)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasCalendarPermission)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        else
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sincronización con Calendario y Reloj",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Añade automáticamente los recordatorios a tu app de Calendario y activa las alarmas en el Reloj del sistema a las 8:00 AM (2 días antes y el mismo día de recojo de medicamentos).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!hasCalendarPermission) {
                                Button(
                                    onClick = {
                                        calendarPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.READ_CALENDAR,
                                                Manifest.permission.WRITE_CALENDAR
                                            )
                                        )
                                    },
                                    modifier = Modifier.testTag("btn_grant_calendar_permission")
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Conectar Calendario y Reloj", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = onTestGeneralAlarm,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertAmber),
                                    modifier = Modifier.testTag("btn_test_general_alarm_sound_no_perm")
                                ) {
                                    Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Probar Alarma", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Surface(
                                    color = AdherenceGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = AdherenceGreen,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Sincronización Activa",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = AdherenceGreen
                                        )
                                    }
                                }

                                Button(
                                    onClick = onSyncAllWithCalendarAndClock,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("btn_sync_all_pickups")
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sincronizar Todo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = onTestGeneralAlarm,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertAmber),
                                    modifier = Modifier.testTag("btn_test_general_alarm_sound")
                                ) {
                                    Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Probar Alarma", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Body depending on ViewMode
            if (viewMode == ScheduleViewMode.CALENDAR) {
                item {
                    MedicationCalendarView(
                        pickups = pickups,
                        isPrivacy = isPrivacy,
                        onCompletePickup = onCompletePickup,
                        onDeletePickup = onDeletePickup,
                        onScheduleForDate = { cal ->
                            selectedDateForAdd = cal.timeInMillis
                            showAddDialog = true
                        },
                        onTestAlarm = onTestAlarm,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // LIST VIEW
                item {
                    Text(
                        text = "Recojos Activos Programados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (activePickups.isEmpty()) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No hay recojos activos",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Toca el botón (+) para programar tu fecha de recojo mensual en farmacia.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(activePickups) { pickup ->
                        ActivePickupCard(
                            pickup = pickup,
                            isPrivacy = isPrivacy,
                            onComplete = { onCompletePickup(pickup) },
                            onDelete = { onDeletePickup(pickup.id) },
                            onTestAlarm = { onTestAlarm(pickup) },
                            onSyncWithCalendarAndClock = { onSyncPickupWithCalendarAndClock(pickup) }
                        )
                    }
                }

                if (pastPickups.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Historial de Recojos Anteriores",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(pastPickups) { pickup ->
                        PastPickupCard(
                            pickup = pickup,
                            isPrivacy = isPrivacy,
                            onDelete = { onDeletePickup(pickup.id) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AddPickupDialog(
            initialMedicine = patientProfile?.medicineName ?: "TLD (Tenofovir + Lamivudina + Dolutegravir)",
            initialCenter = patientProfile?.healthCenterName ?: "Centro de Salud - Farmacia TARV",
            initialDateMillis = selectedDateForAdd,
            onDismiss = {
                showAddDialog = false
                selectedDateForAdd = null
            },
            onConfirm = { dateMillis, medicine, center, days, notes, autoMonthly, reminder2Days ->
                onAddPickup(dateMillis, medicine, center, days, notes, autoMonthly, reminder2Days)
                showAddDialog = false
                selectedDateForAdd = null
            }
        )
    }
}

@Composable
private fun ActivePickupCard(
    pickup: MedicationPickup,
    isPrivacy: Boolean,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onTestAlarm: () -> Unit,
    onSyncWithCalendarAndClock: () -> Unit = {}
) {
    val now = System.currentTimeMillis()
    val diffMillis = pickup.scheduledDate - now
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()

    val dateFormat = SimpleDateFormat("EEEE d 'de' MMMM yyyy", Locale.forLanguageTag("es-ES"))
    val dateStr = dateFormat.format(Date(pickup.scheduledDate))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("es-ES")) else it.toString() }

    Card(
        shape = RoundedCornerShape(16.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (pickup.autoMonthly) "Repetición cada mes activa" else "Recojo puntual",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Alarm status notice
            Surface(
                color = if (diffDays <= 2) AlertAmber.copy(alpha = 0.15f) else MedicalTeal.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (diffDays <= 2) Icons.Default.NotificationsActive else Icons.Default.AlarmOn,
                        contentDescription = null,
                        tint = if (diffDays <= 2) AlertAmber else MedicalTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (diffDays <= 2) {
                            "🔔 ¡Atención! Faltan 2 días para tu recojo. Alarma a las 8:00 AM y sincronización con Calendario/Reloj activa."
                        } else {
                            "⏰ Alarma y Calendario: Notificación y sonido a las 8:00 AM (2 días antes y el mismo día de recojo)."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (diffDays <= 2) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "📍 Centro: ${pickup.healthCenterName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "💊 Medicamento: ${if (isPrivacy) "Tratamiento de control" else pickup.medicineName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (pickup.notes.isNotBlank()) {
                Text(
                    text = "📝 Notas: ${pickup.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSyncWithCalendarAndClock,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_sync_pickup_${pickup.id}"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Sincronizar", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onTestAlarm,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_test_alarm_list_${pickup.id}"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertAmber)
                ) {
                    Icon(imageVector = Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Probar 8 AM", fontSize = 11.sp)
                }

                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("btn_complete_pickup_list_${pickup.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Recogido", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun PastPickupCard(
    pickup: MedicationPickup,
    isPrivacy: Boolean,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("d 'de' MMMM yyyy", Locale.forLanguageTag("es-ES"))
    val dateStr = dateFormat.format(Date(pickup.completedDate ?: pickup.scheduledDate))

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AdherenceGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Recogido el $dateStr",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${pickup.healthCenterName} (${pickup.quantityDays} días)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
