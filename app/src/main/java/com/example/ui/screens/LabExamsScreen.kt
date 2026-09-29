package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LabExam
import com.example.data.model.PatientProfile
import com.example.ui.components.AddLabResultDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AdherenceGreen
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.RibbonRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun LabExamsScreen(
    patientProfile: PatientProfile?,
    labExams: List<LabExam>,
    onScheduleLab: (scheduledDate: Long, labName: String, notes: String, autoSixMonths: Boolean) -> Unit,
    onCompleteLabWithResults: (exam: LabExam, viralLoad: Int?, isUndetectable: Boolean, cd4: Int?, ratio: Float?, notes: String) -> Unit,
    onDeleteLab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var examToInputResults by remember { mutableStateOf<LabExam?>(null) }
    val isPrivacy = patientProfile?.privacyModeEnabled == true

    val pendingExams = labExams.filter { !it.isCompleted }.sortedBy { it.scheduledDate }
    val completedExams = labExams.filter { it.isCompleted }.sortedByDescending { it.examDate ?: it.scheduledDate }
    val latestCompleted = completedExams.firstOrNull()

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Educational Banner on I=I & CD4
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = InfoBlue.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = InfoBlue,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Historial Clínico Privado & Seguro",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = InfoBlue
                            )
                            Text(
                                text = "El seguimiento semestral de Carga Viral y Linfocitos CD4 asegura que tu tratamiento mantenga el virus indetectable (I=I) y tus defensas al 100%.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Summary of current immune status
            if (latestCompleted != null) {
                item {
                    ImmuneStatusDashboardCard(latestCompleted = latestCompleted)
                }
            }

            // Pending Scheduled Exams
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exámenes Programados (Cada 6 Meses)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (pendingExams.isEmpty()) {
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
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = InfoBlue,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay exámenes pendientes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val cal = java.util.Calendar.getInstance()
                                    cal.add(java.util.Calendar.MONTH, 6)
                                    onScheduleLab(cal.timeInMillis, "Laboratorio Central de Referencia", "Control de rutina semestral", true)
                                }
                            ) {
                                Text("Programar Próximo Control Semestral")
                            }
                        }
                    }
                }
            } else {
                items(pendingExams) { exam ->
                    PendingLabCard(
                        exam = exam,
                        onInputResults = { examToInputResults = exam },
                        onDelete = { onDeleteLab(exam.id) }
                    )
                }
            }

            // Completed Exams History
            if (completedExams.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Resultados Históricos de Laboratorio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(completedExams) { exam ->
                    CompletedLabResultCard(
                        exam = exam,
                        onDelete = { onDeleteLab(exam.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }

    examToInputResults?.let { exam ->
        AddLabResultDialog(
            exam = exam,
            onDismiss = { examToInputResults = null },
            onConfirm = { viralLoad, isUndetectable, cd4, ratio, notes ->
                onCompleteLabWithResults(exam, viralLoad, isUndetectable, cd4, ratio, notes)
                examToInputResults = null
            }
        )
    }
}

@Composable
private fun ImmuneStatusDashboardCard(latestCompleted: LabExam) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado Inmunológico Actual",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(
                    text = "I = I Indetectable",
                    color = AdherenceGreen,
                    backgroundColor = AdherenceGreen.copy(alpha = 0.12f),
                    icon = Icons.Default.Shield
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CD4 Level Gauge
            val cd4 = latestCompleted.cd4Count ?: 750
            val cd4Max = 1200f
            val cd4Progress = (cd4 / cd4Max).coerceIn(0f, 1f)

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recuento de Linfocitos CD4: $cd4 cél/µL",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (cd4 >= 500) "Normal (Óptimo)" else if (cd4 >= 200) "Moderado" else "Bajo (<200)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (cd4 >= 500) AdherenceGreen else AlertAmber
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { cd4Progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (cd4 >= 500) AdherenceGreen else AlertAmber,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "0 cél (Alerta)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "500 cél (Zona Segura)", fontSize = 10.sp, color = AdherenceGreen, fontWeight = FontWeight.Bold)
                    Text(text = "1200 cél", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Viral load callout
            Surface(
                color = AdherenceGreen.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AdherenceGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Carga Viral: ${if (latestCompleted.isUndetectable || (latestCompleted.viralLoadCopies != null && latestCompleted.viralLoadCopies < 20)) "Indetectable (<20 copias/mL)" else "${latestCompleted.viralLoadCopies} copias"}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AdherenceGreen
                        )
                        Text(
                            text = "Indetectable = Intransmisible: La cantidad de virus en sangre es tan baja que no se puede transmitir por vía sexual y tu cuerpo se regenera.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingLabCard(
    exam: LabExam,
    onInputResults: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEEE d 'de' MMMM yyyy", Locale("es", "ES"))
    val dateStr = dateFormat.format(Date(exam.scheduledDate))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }

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
                            .background(InfoBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = InfoBlue)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Control Semestral Programado",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateStr,
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

            Text(
                text = "📍 ${exam.laboratoryName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (exam.doctorNotes.isNotBlank()) {
                Text(
                    text = "📝 ${exam.doctorNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onInputResults,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_register_lab_results"),
                colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Resultados de Laboratorio")
            }
        }
    }
}

@Composable
private fun CompletedLabResultCard(
    exam: LabExam,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("d 'de' MMMM yyyy", Locale("es", "ES"))
    val dateStr = dateFormat.format(Date(exam.examDate ?: exam.scheduledDate))

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
                    text = "Fecha: $dateStr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                StatusBadge(
                    text = if (exam.isUndetectable || (exam.viralLoadCopies != null && exam.viralLoadCopies < 20)) "I=I Indetectable" else "Detectable",
                    color = if (exam.isUndetectable) AdherenceGreen else AlertAmber,
                    backgroundColor = if (exam.isUndetectable) AdherenceGreen.copy(alpha = 0.12f) else AlertAmber.copy(alpha = 0.12f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Carga Viral", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (exam.isUndetectable || (exam.viralLoadCopies != null && exam.viralLoadCopies < 20)) "<20 copias (Indetectable)" else "${exam.viralLoadCopies} copias/mL",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (exam.isUndetectable) AdherenceGreen else AlertAmber
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Linfocitos CD4", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${exam.cd4Count ?: "--"} cél/µL",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = InfoBlue
                        )
                    }
                }
            }

            if (exam.doctorNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Nota médica: ${exam.doctorNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
