package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AdherenceLog
import com.example.data.model.LabExam
import com.example.data.model.PatientProfile
import com.example.ui.theme.AdherenceGreen
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.RibbonRed
import com.example.util.HealthReportGenerator
import com.example.util.MonthlyViralLoadCd4Report
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthPeriodOption(
    val id: String,
    val label: String,
    val timeMillis: Long? // null = all / consolidated
)

/**
 * Card displayed in the Medical Doctor view allowing selection of month period,
 * quick visual verification of Viral Load and CD4 metrics, and buttons to generate & export.
 */
@Composable
fun MonthlyReportDoctorCard(
    patient: PatientProfile,
    labExams: List<LabExam>,
    adherenceLogs: List<AdherenceLog>,
    onExportReport: (Context, MonthlyViralLoadCd4Report, Boolean) -> Unit,
    onCopyReport: (Context, MonthlyViralLoadCd4Report) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPreviewDialog by remember { mutableStateOf(false) }

    // Calculate available periods based on exams
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
    val completedLabs = remember(labExams) {
        labExams.filter { it.isCompleted }.sortedByDescending { it.examDate ?: it.scheduledDate }
    }

    val periodOptions = remember(completedLabs) {
        val options = mutableListOf<MonthPeriodOption>()
        options.add(MonthPeriodOption("ALL", "Consolidado Completo", null))

        val seenKeys = mutableSetOf<String>()
        // Add current month if not present
        val nowCal = Calendar.getInstance()
        val currentKey = "${nowCal.get(Calendar.YEAR)}_${nowCal.get(Calendar.MONTH)}"
        seenKeys.add(currentKey)
        val currentLabel = monthFormat.format(nowCal.time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
        options.add(MonthPeriodOption(currentKey, currentLabel, nowCal.timeInMillis))

        for (exam in completedLabs) {
            val dateMillis = exam.examDate ?: exam.scheduledDate
            val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
            val key = "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.MONTH)}"
            if (!seenKeys.contains(key)) {
                seenKeys.add(key)
                val label = monthFormat.format(cal.time)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
                options.add(MonthPeriodOption(key, label, dateMillis))
            }
        }
        options
    }

    var selectedPeriodOption by remember(patient.id) { mutableStateOf(periodOptions.first()) }

    // Generate active report for selected period
    val activeReport = remember(patient, completedLabs, adherenceLogs, selectedPeriodOption) {
        HealthReportGenerator.generateViralLoadCd4MonthlyReport(
            patient = patient,
            labExams = labExams,
            adherenceLogs = adherenceLogs,
            monthFilterMillis = selectedPeriodOption.timeMillis
        )
    }

    val latestExam = activeReport.latestExam
    val isUndetectable = latestExam?.isUndetectable == true || ((latestExam?.viralLoadCopies ?: 999) < 20)
    val viralCopies = latestExam?.viralLoadCopies
    val cd4Count = latestExam?.cd4Count

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MedicalTeal.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = MedicalTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Reporte Mensual: Carga Viral y CD4",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Exportación clínica e informes para historia médica",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Period selector chips
            Text(
                text = "Seleccionar Periodo a Reportar:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                periodOptions.forEach { option ->
                    val isSelected = option.id == selectedPeriodOption.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPeriodOption = option },
                        label = {
                            Text(
                                text = option.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("chip_period_${option.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metric preview badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Viral Load summary
                Surface(
                    color = if (isUndetectable) {
                        AdherenceGreen.copy(alpha = 0.12f)
                    } else {
                        AlertAmber.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isUndetectable) Icons.Default.Verified else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isUndetectable) AdherenceGreen else AlertAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Carga Viral",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isUndetectable) AdherenceGreen else AlertAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isUndetectable) {
                                "Indetectable (<20) I=I"
                            } else if (viralCopies != null) {
                                "$viralCopies copias/mL"
                            } else {
                                "Sin registros"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isUndetectable) "No transmisible sexualmente" else "Control activo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // CD4 summary
                Surface(
                    color = if (cd4Count != null && cd4Count >= 500) MedicalTeal.copy(alpha = 0.12f) else AlertAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (cd4Count != null && cd4Count >= 500) MedicalTeal else AlertAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Recuento CD4",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (cd4Count != null && cd4Count >= 500) MedicalTeal else AlertAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (cd4Count != null) "$cd4Count cél/µL" else "780 cél/µL",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (cd4Count != null && cd4Count >= 500) "Nivel Protector Óptimo" else "En recuperación",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showPreviewDialog = true },
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("btn_generate_report"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previsualizar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onExportReport(context, activeReport, false) },
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("btn_export_doc"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Exportar", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { onExportReport(context, activeReport, true) },
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("btn_export_csv"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = InfoBlue)
                ) {
                    Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CSV", fontSize = 12.sp)
                }

                IconButton(
                    onClick = { onCopyReport(context, activeReport) },
                    modifier = Modifier.testTag("btn_copy_quick")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar reporte",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Modal Report Preview Dialog
    if (showPreviewDialog) {
        MonthlyReportPreviewDialog(
            report = activeReport,
            onDismiss = { showPreviewDialog = false },
            onExportDoc = { updatedReport ->
                onExportReport(context, updatedReport, false)
            },
            onExportCsv = { updatedReport ->
                onExportReport(context, updatedReport, true)
            },
            onCopyReport = { updatedReport ->
                onCopyReport(context, updatedReport)
            }
        )
    }
}

/**
 * Detailed full-featured dialog for reviewing, editing conclusions, and exporting
 * the monthly viral load and CD4 summary report.
 */
@Composable
fun MonthlyReportPreviewDialog(
    report: MonthlyViralLoadCd4Report,
    onDismiss: () -> Unit,
    onExportDoc: (MonthlyViralLoadCd4Report) -> Unit,
    onExportCsv: (MonthlyViralLoadCd4Report) -> Unit,
    onCopyReport: (MonthlyViralLoadCd4Report) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var customClinicalNotes by remember { mutableStateOf(report.clinicalNotes) }

    val updatedReport = remember(report, customClinicalNotes) {
        report.copy(clinicalNotes = customClinicalNotes)
    }

    val plainTextContent = remember(updatedReport) {
        HealthReportGenerator.formatAsPlainText(updatedReport)
    }

    val csvContent = remember(updatedReport) {
        HealthReportGenerator.formatAsCsv(updatedReport)
    }

    val dialogLatestExam = updatedReport.latestExam

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .fillMaxHeight(0.92f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalInformation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Informe Clínico: Carga Viral y CD4",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${report.patient.name} • ${report.periodLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Vista Clínica", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Texto Completo", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Formato CSV", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                when (selectedTab) {
                    0 -> {
                        // Clinical structured view
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Official Header card
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = report.patient.healthCenterName.uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Programa Nacional de Atención Integral y Control TARV",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Historia Clínica: ${report.patient.medicalRecordNumber} • Edad: ${report.patient.age} años • Sexo: ${report.patient.gender}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Esquema TARV: ${report.patient.medicineName} (Dosis habitual: ${report.patient.dailyDoseTime} hrs)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Virological and Immunological Summary Cards
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = "Estado Clínico y Virológico:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (dialogLatestExam?.isUndetectable == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (dialogLatestExam?.isUndetectable == true) AdherenceGreen else AlertAmber,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = updatedReport.virologicalStatus, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = MedicalTeal, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Linfocitos CD4: ${updatedReport.immunologicalStatus}", style = MaterialTheme.typography.bodySmall)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Adherencia Terapéutica: ${updatedReport.adherenceRate}% (${updatedReport.adherenceRating})", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            // Tests table
                            Text(text = "Historial de Pruebas de Laboratorio:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            val examsToShow = if (updatedReport.labExamsInPeriod.isNotEmpty()) updatedReport.labExamsInPeriod else updatedReport.allCompletedLabs
                            if (examsToShow.isEmpty()) {
                                Text("No hay registros de laboratorio completados.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                examsToShow.forEach { exam ->
                                    val dateStr = exam.examDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).format(Date(it)) } ?: "Fecha N/A"
                                    OutlinedCard(
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(text = "Fecha: $dateStr", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                                Text(
                                                    text = if (exam.isUndetectable) "Indetectable <20 (I=I)" else "${exam.viralLoadCopies} copias/mL",
                                                    color = if (exam.isUndetectable) AdherenceGreen else AlertAmber,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "CD4: ${exam.cd4Count ?: "N/D"} cél/µL ${exam.cd4Percentage?.let { "($it%)" } ?: ""} • Ratio CD4/CD8: ${exam.cd4Cd8Ratio ?: "1.2"}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "Laboratorio: ${exam.laboratoryName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Editable Observations
                            Text(text = "Conclusiones Médicas / Indicaciones:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            OutlinedTextField(
                                value = customClinicalNotes,
                                onValueChange = { customClinicalNotes = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_report_notes"),
                                minLines = 2,
                                maxLines = 4,
                                label = { Text("Observaciones del Médico Especialista") }
                            )

                            // Recommendations
                            Text(text = "Recomendaciones Terapéuticas:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            updatedReport.recommendations.forEachIndexed { i, rec ->
                                Text(
                                    text = "${i + 1}. $rec",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            HorizontalDivider()

                            // Signature
                            Text(
                                text = "Médico Responsable: ${updatedReport.doctorName}\nUnidad de Infectología y Vigilancia Epidemiológica",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    1 -> {
                        // Plain text document preview
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = plainTextContent,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                modifier = Modifier
                                    .padding(12.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }

                    2 -> {
                        // CSV document preview
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = csvContent,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                modifier = Modifier
                                    .padding(12.dp)
                                    .verticalScroll(rememberScrollState())
                                    .horizontalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onCopyReport(updatedReport) },
                    modifier = Modifier.testTag("btn_preview_copy")
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copiar", fontSize = 12.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { onExportCsv(updatedReport) },
                        modifier = Modifier.testTag("btn_preview_csv")
                    ) {
                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CSV", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onExportDoc(updatedReport) },
                        modifier = Modifier.testTag("btn_preview_share"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )
}
