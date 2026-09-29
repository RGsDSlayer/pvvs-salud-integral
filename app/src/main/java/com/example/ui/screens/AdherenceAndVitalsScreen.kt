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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdherenceLog
import com.example.data.model.AdherenceStatus
import com.example.data.model.LabExam
import com.example.data.model.PatientProfile
import com.example.data.model.VitalSign
import com.example.ui.components.RecordVitalsDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AdherenceGreen
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.RibbonRed
import com.example.util.HealthReportGenerator
import com.example.util.MonthlyHealthSummary
import com.example.util.NutritionAdvisor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AdherenceAndVitalsScreen(
    patientProfile: PatientProfile?,
    adherenceLogs: List<AdherenceLog>,
    vitalSigns: List<VitalSign>,
    labExams: List<LabExam>,
    onLogTodayDose: (AdherenceStatus) -> Unit,
    onRecordVitals: (systolic: Int?, diastolic: Int?, hr: Int?, temp: Float?, weight: Float?, height: Float?, glucose: Int?, o2: Int?, notes: String) -> Unit,
    onDeleteVital: (Int) -> Unit,
    onLogDoseForDate: ((String, AdherenceStatus) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showVitalsDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedDayForDoseLog by remember { mutableStateOf<Pair<String, AdherenceStatus?>?>(null) }

    // 30-day clinical evaluation period
    val totalEvaluationDays = 30
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val last30Days = remember {
        (0 until totalEvaluationDays).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            sdf.format(cal.time)
        }
    }

    val logsIn30Days = adherenceLogs.filter { it.dateString in last30Days }
    val onTimeCount = logsIn30Days.count { it.status == AdherenceStatus.TOMADO_A_TIEMPO }
    val lateCount = logsIn30Days.count { it.status == AdherenceStatus.TOMADO_TARDE }
    val explicitlyMissedCount = logsIn30Days.count { it.status == AdherenceStatus.OLVIDADO }

    val loggedDates = logsIn30Days.map { it.dateString }.toSet()
    val unmarkedCount = last30Days.count { it !in loggedDates }
    val totalMissedCount = explicitlyMissedCount + unmarkedCount

    val effectiveTaken = onTimeCount + (lateCount * 0.8f)
    val adherencePercent = if (totalEvaluationDays > 0) {
        (((effectiveTaken / totalEvaluationDays) * 1000).toInt() / 10f).toInt()
    } else 100

    val monthlyReport = remember(patientProfile, adherenceLogs, labExams, vitalSigns) {
        if (patientProfile != null) {
            HealthReportGenerator.generateMonthlySummary(patientProfile, adherenceLogs, labExams, vitalSigns)
        } else null
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Adherence Gauge Hero Card
                AdherenceGaugeCard(
                    adherencePercent = adherencePercent,
                    onTimeCount = onTimeCount,
                    lateCount = lateCount,
                    missedCount = explicitlyMissedCount,
                    unmarkedCount = unmarkedCount,
                    totalMissed = totalMissedCount,
                    onGenerateReport = { showReportDialog = true }
                )
            }

            // 30-Day Heatmap
            item {
                AdherenceHeatmapCard(
                    adherenceLogs = adherenceLogs,
                    onSelectDay = { dateStr, status ->
                        selectedDayForDoseLog = Pair(dateStr, status)
                    }
                )
            }

            // Vital Signs Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Registro de Signos Vitales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showVitalsDialog = true },
                        modifier = Modifier.testTag("btn_add_vitals")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Registrar")
                    }
                }
            }

            if (vitalSigns.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = RibbonRed,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Sin registros de signos vitales",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Lleva control de tu presión arterial, frecuencia cardíaca, temperatura y peso.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(vitalSigns) { vital ->
                    VitalSignItemCard(vital = vital, onDelete = { onDeleteVital(vital.id) })
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }

    if (showVitalsDialog) {
        RecordVitalsDialog(
            currentWeight = patientProfile?.weightKg ?: 70f,
            currentHeight = patientProfile?.heightCm ?: 170f,
            onDismiss = { showVitalsDialog = false },
            onConfirm = { sys, dia, hr, temp, weight, height, glucose, o2, notes ->
                onRecordVitals(sys, dia, hr, temp, weight, height, glucose, o2, notes)
                showVitalsDialog = false
            }
        )
    }

    if (showReportDialog && monthlyReport != null) {
        MonthlyHealthReportDialog(
            report = monthlyReport,
            onDismiss = { showReportDialog = false }
        )
    }

    selectedDayForDoseLog?.let { (dateStr, currentStatus) ->
        DayDoseEditDialog(
            dateStr = dateStr,
            currentStatus = currentStatus,
            onDismiss = { selectedDayForDoseLog = null },
            onSaveStatus = { newStatus ->
                onLogDoseForDate?.invoke(dateStr, newStatus) ?: onLogTodayDose(newStatus)
                selectedDayForDoseLog = null
            }
        )
    }
}

@Composable
private fun AdherenceGaugeCard(
    adherencePercent: Int,
    onTimeCount: Int,
    lateCount: Int,
    missedCount: Int,
    unmarkedCount: Int,
    totalMissed: Int,
    onGenerateReport: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().testTag("card_adherence_gauge")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Nivel de Adherencia al Tratamiento",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Periodo: Últimos 30 días • Meta: ≥ 95%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(
                    text = if (adherencePercent >= 95) "Excelente ($adherencePercent%)" else if (adherencePercent >= 90) "Buena ($adherencePercent%)" else "Atención ($adherencePercent%)",
                    color = if (adherencePercent >= 95) AdherenceGreen else if (adherencePercent >= 90) InfoBlue else AlertAmber,
                    backgroundColor = if (adherencePercent >= 95) AdherenceGreen.copy(alpha = 0.12f) else AlertAmber.copy(alpha = 0.12f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Percentage Bar
            LinearProgressIndicator(
                progress = { (adherencePercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp)),
                color = if (adherencePercent >= 95) AdherenceGreen else if (adherencePercent >= 90) InfoBlue else AlertAmber,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Stats breakdown (4 metrics: a tiempo, con retraso, no tomadas, sin marcar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = AdherenceGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$onTimeCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AdherenceGreen)
                        Text(text = "A tiempo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    color = AlertAmber.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$lateCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AlertAmber)
                        Text(text = "Con retraso", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    color = RibbonRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$missedCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RibbonRed)
                        Text(text = "No tomadas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$unmarkedCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Sin marcar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (unmarkedCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "* Los $unmarkedCount días sin marcar la toma diaria computan clínicamente como dosis omitidas.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onGenerateReport,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_generate_monthly_report"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generar Informe en Adherencia")
            }
        }
    }
}

@Composable
private fun AdherenceHeatmapCard(
    adherenceLogs: List<AdherenceLog>,
    onSelectDay: (String, AdherenceStatus?) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().testTag("card_adherence_heatmap")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Historial de Tomas (Últimos 30 Días)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Toca cualquier día para ver o registrar la toma del medicamento",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendItem(color = AdherenceGreen, label = "A tiempo")
                LegendItem(color = AlertAmber, label = "Tarde")
                LegendItem(color = RibbonRed, label = "No tomado")
                LegendItem(color = MaterialTheme.colorScheme.surfaceVariant, label = "Sin marcar", textColor = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 30 Day Bubbles Grid (scrollable row)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dayFormat = SimpleDateFormat("d", Locale.getDefault())

            val daysList = (0..29).map { i ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                val dateStr = sdf.format(cal.time)
                val dayNum = dayFormat.format(cal.time)
                val log = adherenceLogs.firstOrNull { it.dateString == dateStr }
                Triple(dayNum, dateStr, log?.status)
            }.reversed()

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(daysList) { (dayNum, dateStr, status) ->
                    val (bgColor, textColor) = when (status) {
                        AdherenceStatus.TOMADO_A_TIEMPO -> Pair(AdherenceGreen, Color.White)
                        AdherenceStatus.TOMADO_TARDE -> Pair(AlertAmber, Color.White)
                        AdherenceStatus.OLVIDADO -> Pair(RibbonRed, Color.White)
                        null -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onSelectDay(dateStr, status) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, textColor: Color? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = textColor ?: MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DayDoseEditDialog(
    dateStr: String,
    currentStatus: AdherenceStatus?,
    onDismiss: () -> Unit,
    onSaveStatus: (AdherenceStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Toma del Día $dateStr",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Estado actual: ${
                        when (currentStatus) {
                            AdherenceStatus.TOMADO_A_TIEMPO -> "Tomado a tiempo"
                            AdherenceStatus.TOMADO_TARDE -> "Tomado con retraso"
                            AdherenceStatus.OLVIDADO -> "No tomado (Olvidado)"
                            null -> "Sin marcar (computado como omitido)"
                        }
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = when (currentStatus) {
                        AdherenceStatus.TOMADO_A_TIEMPO -> AdherenceGreen
                        AdherenceStatus.TOMADO_TARDE -> AlertAmber
                        AdherenceStatus.OLVIDADO -> RibbonRed
                        null -> MaterialTheme.colorScheme.error
                    }
                )

                Text(
                    text = "Selecciona cómo se realizó la toma de medicamento en esta fecha:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { onSaveStatus(AdherenceStatus.TOMADO_A_TIEMPO) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AdherenceGreen)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tomado a Tiempo")
                }

                Button(
                    onClick = { onSaveStatus(AdherenceStatus.TOMADO_TARDE) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AlertAmber)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tomado con Retraso")
                }

                Button(
                    onClick = { onSaveStatus(AdherenceStatus.OLVIDADO) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RibbonRed)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("No se Tomó Medicamento")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun VitalSignItemCard(
    vital: VitalSign,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("es", "ES"))
    val dateStr = dateFormat.format(Date(vital.timestamp))

    OutlinedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (vital.systolic != null && vital.diastolic != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Presión", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${vital.systolic}/${vital.diastolic}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                if (vital.heartRate != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Pulso", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${vital.heartRate} lpm", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                if (vital.weightKg != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Peso", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${vital.weightKg} kg", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                if (vital.temperature != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Temp", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${vital.temperature}°C", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            val vitalWeight = vital.weightKg ?: 70f
            val vitalHeight = vital.heightCm ?: 170f
            val vitalBmi = NutritionAdvisor.calculateBmi(vitalWeight, vitalHeight)
            val vitalBmiCategory = NutritionAdvisor.getBmiCategory(vitalBmi)

            Spacer(modifier = Modifier.height(6.dp))

            // Secondary metrics row: Talla e IMC Calculado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = AdherenceGreen.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Talla", fontSize = 10.sp, color = AdherenceGreen)
                        Text(
                            text = if (vital.heightCm != null) "${vital.heightCm.toInt()} cm" else "-- cm",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AdherenceGreen
                        )
                    }
                }

                Surface(
                    color = if (vitalBmi in 18.5f..24.9f) AdherenceGreen.copy(alpha = 0.12f) else AlertAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "IMC Calculado",
                            fontSize = 10.sp,
                            color = if (vitalBmi in 18.5f..24.9f) AdherenceGreen else AlertAmber
                        )
                        Text(
                            text = "$vitalBmi ($vitalBmiCategory)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (vitalBmi in 18.5f..24.9f) AdherenceGreen else AlertAmber
                        )
                    }
                }

                if (vital.glucose != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Glucosa", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${vital.glucose}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            if (!vital.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Nota: ${vital.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun MonthlyHealthReportDialog(
    report: MonthlyHealthSummary,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Informe de Adherencia y Salud",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "Paciente: ${report.patientName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Historia Clínica: ${report.medicalRecordNumber}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Periodo de Evaluación: ${report.monthYear} (${report.totalDosesScheduled} días)", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                item {
                    Text(text = "1. Adherencia al Tratamiento (Últimos 30 días)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "Nivel de Adherencia: ${report.adherencePercentage}% (${report.adherenceRating})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(report.adherenceRatingColorHex)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "• Dosis tomadas a tiempo: ${report.dosesTakenOnTime}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Dosis tomadas con retraso: ${report.dosesTakenLate}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Dosis marcadas como no tomadas: ${report.dosesMissed}", style = MaterialTheme.typography.bodySmall)
                    if (report.dosesUnmarked > 0) {
                        Text(
                            text = "• Días sin marcar toma diaria: ${report.dosesUnmarked} (computados como dosis omitidas)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "• Total de dosis omitidas: ${report.dosesMissed + report.dosesUnmarked} de ${report.totalDosesScheduled} días",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                item {
                    Text(text = "2. Control Laboratorial & Inmunológico", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(text = "• Carga Viral: ${report.latestViralLoad}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Linfocitos CD4: ${report.latestCd4 ?: "Sin registro"} células/mm³", style = MaterialTheme.typography.bodySmall)
                    if (report.isUndetectable) {
                        Text(text = "• Estado: Indetectable = Intransmisible (I=I) confirmado.", style = MaterialTheme.typography.bodySmall, color = AdherenceGreen, fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    Text(text = "3. Signos Vitales y Estado Nutricional", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(text = "• Presión Arterial Promedio: ${report.avgBloodPressure}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Peso Actual: ${report.currentWeight ?: "--"} kg (IMC: ${report.bmi} - ${report.bmiCategory})", style = MaterialTheme.typography.bodySmall)
                }

                item {
                    Text(text = "4. Recomendaciones Clínicas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    report.clinicalRecommendations.forEach { rec ->
                        Text(text = "• $rec", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val text = HealthReportGenerator.formatMonthlySummaryAsPlainText(report)
                        HealthReportGenerator.copyToClipboard(context, text)
                    }
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copiar")
                }

                Button(
                    onClick = {
                        HealthReportGenerator.exportMonthlySummaryViaShareSheet(context, report)
                    }
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Compartir")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
