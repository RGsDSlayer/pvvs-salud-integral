package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdherenceLog
import com.example.data.model.AdherenceStatus
import com.example.data.model.LabExam
import com.example.data.model.MedicationPickup
import com.example.data.model.PatientProfile
import com.example.data.model.PickupStatus
import com.example.data.model.VitalSign
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AdherenceGreen
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.RibbonRed
import com.example.util.NutritionAdvisor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    patientProfile: PatientProfile?,
    pickups: List<MedicationPickup>,
    labExams: List<LabExam>,
    adherenceLogs: List<AdherenceLog>,
    vitalSigns: List<VitalSign>,
    onCompletePickup: (MedicationPickup) -> Unit,
    onLogTodayDose: (AdherenceStatus) -> Unit,
    onNavigateToPickups: () -> Unit,
    onNavigateToLabs: () -> Unit,
    onNavigateToAdherence: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onTriggerTestNotification: (String) -> Unit,
    fcmToken: String? = null,
    fcmStatus: String = "FCM Activo",
    fcmTopics: Set<String> = emptySet(),
    onTriggerFcmPickup: ((isDayOf: Boolean) -> Unit)? = null,
    onTriggerFcmViralLoad: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val nextPickup = pickups.filter { it.status == PickupStatus.PROGRAMADO }
        .minByOrNull { it.scheduledDate }

    val nextLab = labExams.filter { !it.isCompleted }
        .minByOrNull { it.scheduledDate }

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayLog = adherenceLogs.firstOrNull { it.dateString == todayStr }

    val completedLabs = labExams.filter { it.isCompleted }.sortedByDescending { it.examDate ?: it.scheduledDate }
    val latestLab = completedLabs.firstOrNull()

    // Calculate adherence percentage for the last 30 logs
    val totalRecentLogs = adherenceLogs.take(30).size
    val onTimeCount = adherenceLogs.take(30).count { it.status == AdherenceStatus.TOMADO_A_TIEMPO }
    val lateCount = adherenceLogs.take(30).count { it.status == AdherenceStatus.TOMADO_TARDE }
    val adherencePercent = if (totalRecentLogs > 0) {
        (((onTimeCount + lateCount * 0.8f) / totalRecentLogs) * 100).toInt()
    } else 100

    val isPrivacy = patientProfile?.privacyModeEnabled == true
    val latestVital = vitalSigns.firstOrNull()
    val currentWeight = latestVital?.weightKg ?: patientProfile?.weightKg ?: 70f
    val currentHeight = latestVital?.heightCm ?: patientProfile?.heightCm ?: 170f
    val bmi = NutritionAdvisor.calculateBmi(currentWeight, currentHeight)
    val bmiCategory = NutritionAdvisor.getBmiCategory(bmi)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // 1. Hero Card: Next Medication Pickup with 2-Day Reminder & Alarms (Professional Polish Purple Hero)
        item {
            MedicationPickupHeroCard(
                pickup = nextPickup,
                isPrivacy = isPrivacy,
                onComplete = { nextPickup?.let { onCompletePickup(it) } },
                onViewDetails = onNavigateToPickups
            )
        }

        // 2. Quick Glance 2-Column Grid (Adherencia & Laboratorio from Design Theme)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Adherence Card (EADDFF)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToAdherence() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timeline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Meta 95%+",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Column {
                            Text(
                                text = "ADHERENCIA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$adherencePercent%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (adherencePercent >= 95) "Nivel Óptimo (I=I)" else "Atención requerida",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (adherencePercent >= 95) AdherenceGreen else AlertAmber,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Card 2: Laboratory Card (White Surface with CAC4D0 Border)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToLabs() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Semestral",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Column {
                            Text(
                                text = "LABORATORIO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            val nextLabDateStr = if (nextLab != null) {
                                SimpleDateFormat("d MMM", Locale("es", "ES")).format(Date(nextLab.scheduledDate))
                            } else "Programar"
                            Text(
                                text = nextLabDateStr,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (latestLab?.isUndetectable == true) "Carga Viral: I=I" else "(Carga Viral / CD4)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. Nutrition Personalized Banner (F7F2FA with E7E0EC border)
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToNutrition() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nutrición Personalizada",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Planes de alimentación médicos por patología (DASH, ADA, renal/hepática).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Ver nutrición",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 4. Daily Treatment Adherence Check-In
        item {
            TodayDoseCheckInCard(
                todayLog = todayLog,
                dailyTime = patientProfile?.dailyDoseTime ?: "20:00",
                medicineName = if (isPrivacy) "Medicamento de control" else (patientProfile?.medicineName ?: "TARV"),
                adherencePercent = adherencePercent,
                onLogDose = onLogTodayDose,
                onViewHistory = onNavigateToAdherence
            )
        }

        // 5. Clinical Status & Indicators Overview
        item {
            ClinicalStatusOverviewCard(
                latestLab = latestLab,
                bmi = bmi,
                bmiCategory = bmiCategory,
                latestVital = latestVital,
                isPrivacy = isPrivacy,
                onNavigateNutrition = onNavigateToNutrition
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun MedicationPickupHeroCard(
    pickup: MedicationPickup?,
    isPrivacy: Boolean,
    onComplete: () -> Unit,
    onViewDetails: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            if (pickup != null) {
                val now = System.currentTimeMillis()
                val diffMillis = pickup.scheduledDate - now
                val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
                val dateFormat = SimpleDateFormat("d 'de' MMMM", Locale("es", "ES"))
                val dateStr = dateFormat.format(Date(pickup.scheduledDate))

                val reminderCal = Calendar.getInstance().apply {
                    timeInMillis = pickup.scheduledDate
                    add(Calendar.DAY_OF_YEAR, -2)
                }
                val reminderStr = SimpleDateFormat("d 'de' MMMM", Locale("es", "ES")).format(reminderCal.time)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPrivacy) "Próxima Cita de Control" else "Próximo Recojo de Medicamentos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Recordatorio: $reminderStr (${if (diffDays <= 2) "¡Activo!" else "2 días antes"})",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detail info pill container
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Centro de Salud:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            Text(
                                text = pickup.healthCenterName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        val countdownText = when {
                            diffDays < 0 -> "Vencido"
                            diffDays == 0 -> "¡Hoy!"
                            diffDays == 1 -> "Mañana"
                            diffDays == 2 -> "En 2 días"
                            else -> "En $diffDays días"
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White
                        ) {
                            Text(
                                text = countdownText,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_complete_pickup"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Marcar Recogido", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onViewDetails,
                        modifier = Modifier.testTag("btn_view_pickups"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Calendario")
                    }
                }
            } else {
                Text(
                    text = "No tienes recojos de medicamentos programados.",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Programa tu fecha de recogida mensual con alarma automática 2 días antes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onViewDetails,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir Calendario y Programar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TodayDoseCheckInCard(
    todayLog: AdherenceLog?,
    dailyTime: String,
    medicineName: String,
    adherencePercent: Int,
    onLogDose: (AdherenceStatus) -> Unit,
    onViewHistory: () -> Unit
) {
    val currentStatus = todayLog?.status

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_daily_dose_checkin"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                when (currentStatus) {
                                    AdherenceStatus.TOMADO_A_TIEMPO -> AdherenceGreen.copy(alpha = 0.15f)
                                    AdherenceStatus.TOMADO_TARDE -> AlertAmber.copy(alpha = 0.15f)
                                    AdherenceStatus.OLVIDADO -> RibbonRed.copy(alpha = 0.15f)
                                    null -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = when (currentStatus) {
                                AdherenceStatus.TOMADO_A_TIEMPO -> AdherenceGreen
                                AdherenceStatus.TOMADO_TARDE -> AlertAmber
                                AdherenceStatus.OLVIDADO -> RibbonRed
                                null -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Toma Diaria de Medicamento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$medicineName • Hora programada: $dailyTime",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(
                    text = when (currentStatus) {
                        AdherenceStatus.TOMADO_A_TIEMPO -> "Tomado a tiempo"
                        AdherenceStatus.TOMADO_TARDE -> "Tomado tarde"
                        AdherenceStatus.OLVIDADO -> "No tomado"
                        null -> "Sin marcar"
                    },
                    color = when (currentStatus) {
                        AdherenceStatus.TOMADO_A_TIEMPO -> AdherenceGreen
                        AdherenceStatus.TOMADO_TARDE -> AlertAmber
                        AdherenceStatus.OLVIDADO -> RibbonRed
                        null -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    backgroundColor = when (currentStatus) {
                        AdherenceStatus.TOMADO_A_TIEMPO -> AdherenceGreen.copy(alpha = 0.12f)
                        AdherenceStatus.TOMADO_TARDE -> AlertAmber.copy(alpha = 0.12f)
                        AdherenceStatus.OLVIDADO -> RibbonRed.copy(alpha = 0.12f)
                        null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Informational state card
            if (todayLog != null) {
                val statusTitle = when (todayLog.status) {
                    AdherenceStatus.TOMADO_A_TIEMPO -> "Dosis de hoy tomada a tiempo"
                    AdherenceStatus.TOMADO_TARDE -> "Dosis de hoy tomada con retraso"
                    AdherenceStatus.OLVIDADO -> "Dosis de hoy marcada como no tomada"
                }
                val statusDesc = when (todayLog.status) {
                    AdherenceStatus.TOMADO_A_TIEMPO -> "Registrado a las ${todayLog.takenTime ?: dailyTime}. ¡Excelente! Cumplir el horario previene mutaciones virales."
                    AdherenceStatus.TOMADO_TARDE -> "Registrado a las ${todayLog.takenTime ?: dailyTime}. Lo importante es no haber omitido la toma."
                    AdherenceStatus.OLVIDADO -> "Dosis omitida hoy. Recuerda: No dupliques la siguiente dosis de mañana."
                }
                val statusColor = when (todayLog.status) {
                    AdherenceStatus.TOMADO_A_TIEMPO -> AdherenceGreen
                    AdherenceStatus.TOMADO_TARDE -> AlertAmber
                    AdherenceStatus.OLVIDADO -> RibbonRed
                }
                val statusIcon = when (todayLog.status) {
                    AdherenceStatus.TOMADO_A_TIEMPO -> Icons.Default.CheckCircle
                    AdherenceStatus.TOMADO_TARDE -> Icons.Default.AccessTime
                    AdherenceStatus.OLVIDADO -> Icons.Default.Close
                }

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = statusTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Text(
                                text = statusDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Modificar estado de la toma de hoy:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Marca tu toma diaria para registrar tu adherencia. En caso de no marcar la toma de medicación, el sistema la computará como dosis omitida en tu informe.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "¿Tomaste tu dosis de hoy?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 3 Buttons for marking daily intake: Tomé a tiempo, Tomé tarde, No tomé medicamento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1: Tomé a Tiempo
                val isSelectedOnTime = currentStatus == AdherenceStatus.TOMADO_A_TIEMPO
                Button(
                    onClick = { onLogDose(AdherenceStatus.TOMADO_A_TIEMPO) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_dose_on_time"),
                    colors = if (isSelectedOnTime) {
                        ButtonDefaults.buttonColors(containerColor = AdherenceGreen, contentColor = Color.White)
                    } else {
                        ButtonDefaults.filledTonalButtonColors(
                            containerColor = AdherenceGreen.copy(alpha = 0.12f),
                            contentColor = AdherenceGreen
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isSelectedOnTime) "✓ A Tiempo" else "A Tiempo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Button 2: Tomé Tarde
                val isSelectedLate = currentStatus == AdherenceStatus.TOMADO_TARDE
                Button(
                    onClick = { onLogDose(AdherenceStatus.TOMADO_TARDE) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_dose_late"),
                    colors = if (isSelectedLate) {
                        ButtonDefaults.buttonColors(containerColor = AlertAmber, contentColor = Color.White)
                    } else {
                        ButtonDefaults.filledTonalButtonColors(
                            containerColor = AlertAmber.copy(alpha = 0.12f),
                            contentColor = AlertAmber
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isSelectedLate) "✓ Tarde" else "Tarde",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Button 3: No Tomé Medicamento
                val isSelectedMissed = currentStatus == AdherenceStatus.OLVIDADO
                Button(
                    onClick = { onLogDose(AdherenceStatus.OLVIDADO) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_dose_missed"),
                    colors = if (isSelectedMissed) {
                        ButtonDefaults.buttonColors(containerColor = RibbonRed, contentColor = Color.White)
                    } else {
                        ButtonDefaults.filledTonalButtonColors(
                            containerColor = RibbonRed.copy(alpha = 0.12f),
                            contentColor = RibbonRed
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isSelectedMissed) "✓ No Tomado" else "No Tomé",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer row with report generator shortcut
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .clickable { onViewHistory() }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Generar Informe en Adherencia",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$adherencePercent% de adherencia en el periodo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun LabExamNextCard(
    nextExam: LabExam?,
    latestCompleted: LabExam?,
    isPrivacy: Boolean,
    onViewLabs: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
                            .background(InfoBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = InfoBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Control de Laboratorio (CV y CD4)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Control semestral (cada 6 meses)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(
                    text = "Semestral",
                    color = InfoBlue,
                    backgroundColor = InfoBlue.copy(alpha = 0.12f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (nextExam != null) {
                val dateFormat = SimpleDateFormat("d 'de' MMMM yyyy", Locale("es", "ES"))
                val dateStr = dateFormat.format(Date(nextExam.scheduledDate))
                val now = System.currentTimeMillis()
                val daysUntil = TimeUnit.MILLISECONDS.toDays(nextExam.scheduledDate - now).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Próxima toma de muestra:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    StatusBadge(
                        text = if (daysUntil > 0) "En $daysUntil días" else "Pendiente",
                        color = MaterialTheme.colorScheme.primary,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Latest lab result preview
            if (latestCompleted != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Último CD4:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${latestCompleted.cd4Count ?: "--"} cél/µL",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AdherenceGreen
                            )
                        }

                        Column {
                            Text(
                                text = "Carga Viral:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (latestCompleted.isUndetectable || (latestCompleted.viralLoadCopies != null && latestCompleted.viralLoadCopies < 20)) "Indetectable (I=I)" else "${latestCompleted.viralLoadCopies} copias",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (latestCompleted.isUndetectable) AdherenceGreen else AlertAmber
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onViewLabs,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Ver Historial y Registrar Resultados")
            }
        }
    }
}

@Composable
private fun ClinicalStatusOverviewCard(
    latestLab: LabExam?,
    bmi: Float,
    bmiCategory: String,
    latestVital: VitalSign?,
    isPrivacy: Boolean,
    onNavigateNutrition: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resumen de Salud & Nutrición",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AdherenceGreen.copy(alpha = 0.15f),
                    modifier = Modifier.clickable { onNavigateNutrition() }
                ) {
                    Text(
                        text = "Ver Nutrición",
                        color = AdherenceGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid of 4 indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: I=I
                MetricTile(
                    title = "Carga Viral",
                    value = if (latestLab?.isUndetectable == true || (latestLab?.viralLoadCopies != null && latestLab.viralLoadCopies < 20)) "I = I" else "${latestLab?.viralLoadCopies ?: "--"}",
                    subtitle = if (latestLab?.isUndetectable == true) "Indetectable" else "Copias/mL",
                    color = AdherenceGreen,
                    modifier = Modifier.weight(1f)
                )

                // Metric 2: CD4
                MetricTile(
                    title = "Linfocitos CD4",
                    value = "${latestLab?.cd4Count ?: "780"}",
                    subtitle = "cél/mm³ (Óptimo)",
                    color = InfoBlue,
                    modifier = Modifier.weight(1f)
                )

                // Metric 3: IMC
                MetricTile(
                    title = "IMC Actual",
                    value = "$bmi",
                    subtitle = bmiCategory,
                    color = if (bmi in 18.5f..24.9f) AdherenceGreen else AlertAmber,
                    modifier = Modifier.weight(1f)
                )

                // Metric 4: Presión Arterial
                MetricTile(
                    title = "Presión Art.",
                    value = if (latestVital?.systolic != null) "${latestVital.systolic}/${latestVital.diastolic}" else "120/80",
                    subtitle = "mmHg",
                    color = MedicalTeal,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

