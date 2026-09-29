package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import com.example.data.model.DoctorLoginRecord
import com.example.data.model.DoctorProfile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.AlertLevel
import com.example.data.model.DoctorNote
import com.example.data.model.HealthCenters
import com.example.data.model.LabExam
import com.example.data.model.MedicationPickup
import com.example.data.model.PatientProfile
import com.example.ui.components.HealthCenterDropdownField
import com.example.ui.components.MonthlyReportDoctorCard
import com.example.ui.components.StatusBadge
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

@Composable
fun DoctorScreen(
    allPatients: List<PatientProfile>,
    selectedPatient: PatientProfile?,
    pickups: List<MedicationPickup>,
    labExams: List<LabExam>,
    adherenceLogs: List<AdherenceLog>,
    doctorNotes: List<DoctorNote>,
    onSelectPatient: (Int) -> Unit,
    onAddDoctorNote: (noteText: String, alertLevel: AlertLevel, doctorName: String) -> Unit,
    onScheduleBatchLab: ((healthCenter: String, dateMillis: Long, laboratory: String, notes: String) -> Unit)? = null,
    onScheduleSingleLab: ((patientId: Int, dateMillis: Long, laboratory: String, notes: String) -> Unit)? = null,
    onExportReport: ((Context, MonthlyViralLoadCd4Report, Boolean) -> Unit)? = null,
    onCopyReport: ((Context, MonthlyViralLoadCd4Report) -> Unit)? = null,
    doctorProfile: DoctorProfile? = null,
    doctorLoginHistory: List<DoctorLoginRecord> = emptyList(),
    onOpenServerDrive: (() -> Unit)? = null,
    onCopyServerUrl: (() -> Unit)? = null,
    onExportToServer: (() -> Unit)? = null,
    onEditDoctorProfile: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showBatchLabDialog by remember { mutableStateOf(false) }
    var showSingleLabDialog by remember { mutableStateOf(false) }
    var showLoginHistoryDialog by remember { mutableStateOf(false) }
    var selectedCenterFilter by remember { mutableStateOf<String?>(null) } // null = Todos los centros
    val context = androidx.compose.ui.platform.LocalContext.current

    // Filter patients by chosen center if any
    val filteredPatients = remember(allPatients, selectedCenterFilter) {
        if (selectedCenterFilter == null) {
            allPatients
        } else {
            allPatients.filter { it.healthCenterName.trim().equals(selectedCenterFilter!!.trim(), ignoreCase = true) }
        }
    }

    // Auto-select first patient of the filtered center if current selected patient is not in it
    androidx.compose.runtime.LaunchedEffect(selectedCenterFilter, filteredPatients) {
        if (filteredPatients.isNotEmpty() && (selectedPatient == null || filteredPatients.none { it.id == selectedPatient.id })) {
            onSelectPatient(filteredPatients.first().id)
        }
    }

    val completedLabs = labExams.filter { it.isCompleted }.sortedByDescending { it.examDate ?: it.scheduledDate }
    val latestLab = completedLabs.firstOrNull()

    val nextPickup = pickups.filter { it.status == com.example.data.model.PickupStatus.PROGRAMADO }.minByOrNull { it.scheduledDate }

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
                // Clinical Center Summary Banner
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Panel Clínico de Infectología TARV",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Monitoreo de pacientes registrados por Centro de Salud y programación masiva de controles de Carga Viral y CD4.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "${allPatients.size}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    Text(text = "Total Pacientes", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "23", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MedicalTeal)
                                    Text(text = "Red de C.S.", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "100%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = AdherenceGreen)
                                    Text(text = "I = I (Indetectables)", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            // TARJETA DE REGISTRO MÉDICO Y AUDITORÍA
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MedicalTeal.copy(alpha = 0.09f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MedicalTeal.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Badge,
                                            contentDescription = null,
                                            tint = MedicalTeal,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Registro Médico Oficial",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MedicalTeal
                                    )
                                    Text(
                                        text = "Dr(a). ${doctorProfile?.fullName ?: "Especialista TARV"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AdherenceGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "SESIÓN RECORDADA",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AdherenceGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("CMP / Colegiatura", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(doctorProfile?.cmpNumber ?: "CMP-54210", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MedicalTeal)
                                }
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Especialidad", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(doctorProfile?.specialty ?: "Infectología y TARV", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Centro de Salud / Hospital Base", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(doctorProfile?.healthCenterName ?: "C.S. 25 de Diciembre", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showLoginHistoryDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auditoría Sesión (${doctorLoginHistory.size})", fontSize = 11.sp)
                            }

                            if (onEditDoctorProfile != null) {
                                OutlinedButton(
                                    onClick = onEditDoctorProfile,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Editar Registro", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ACTION: PROGRAMAR CARGA VIRAL Y CD4 POR CENTRO DE SALUD
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MedicalTeal.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MedicalTeal),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Programar Carga Viral y CD4",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Emitir y enviar orden a pacientes por Centro de Salud",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = AdherenceGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "FCM Push Activo",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AdherenceGreen
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { showBatchLabDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_schedule_batch_lab")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Programar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // SELECCIÓN Y FILTRO DE PACIENTES POR UNO DE LOS 23 CENTROS DE SALUD
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Pacientes por Centro de Salud",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Selecciona uno de los 23 centros para consultar sus pacientes",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${filteredPatients.size} pac.",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Selector dropdown para elegir cualquiera de los 23 Centros de Salud
                        HealthCenterDropdownField(
                            selectedCenter = selectedCenterFilter ?: "Todos los Centros (23 Centros)",
                            onCenterSelected = { selected ->
                                selectedCenterFilter = if (selected == "Todos los Centros (23 Centros)") null else selected
                            },
                            label = "Seleccionar uno de los 23 Centros de Salud",
                            includeAllOption = true,
                            allOptionLabel = "Todos los Centros (23 Centros)",
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Acceso rápido horizontal con todos los 23 centros de salud
                        Text(
                            text = "Acceso rápido por Centro de Salud (23 C.S.):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedCenterFilter == null,
                                onClick = { selectedCenterFilter = null },
                                label = { Text("Todos (${allPatients.size})") },
                                leadingIcon = if (selectedCenterFilter == null) {
                                    { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )

                            HealthCenters.LIST.forEach { center ->
                                val count = allPatients.count { it.healthCenterName.trim().equals(center, ignoreCase = true) }
                                val isSelected = selectedCenterFilter?.equals(center, ignoreCase = true) == true
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedCenterFilter = if (isSelected) null else center
                                    },
                                    label = { Text("$center ($count)") },
                                    leadingIcon = if (isSelected) {
                                        { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else {
                                        { Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MedicalTeal.copy(alpha = 0.2f),
                                        selectedLabelColor = MedicalTeal
                                    )
                                )
                            }
                        }

                        // Banner de estado del filtro activo
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedCenterFilter != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (selectedCenterFilter != null) Icons.Default.LocalHospital else Icons.Default.People,
                                        contentDescription = null,
                                        tint = if (selectedCenterFilter != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedCenterFilter != null)
                                            "Centro Activo: $selectedCenterFilter • ${filteredPatients.size} paciente(s)"
                                        else
                                            "Mostrando todos los pacientes (${allPatients.size} en total en la red de 23 centros)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (selectedCenterFilter != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (selectedCenterFilter != null) {
                                    TextButton(
                                        onClick = { selectedCenterFilter = null },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Text("Ver Todos", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (filteredPatients.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Sin pacientes en este Centro de Salud",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Actualmente ningún paciente registrado ha seleccionado \"$selectedCenterFilter\" para la toma de medicamentos ni controles.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { selectedCenterFilter = null },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Ver Todos los Centros (${allPatients.size})")
                            }
                        }
                    }
                }
            } else {
                items(filteredPatients) { patient ->
                    val isSelected = patient.id == selectedPatient?.id
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isSelected) 3.dp else 0.5.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPatient(patient.id) }
                            .testTag("patient_card_${patient.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = patient.name.take(1),
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = patient.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "HC: ${patient.medicalRecordNumber} • ${patient.age} años • ${patient.gender}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (isSelected) {
                                    StatusBadge(
                                        text = "En Consulta",
                                        color = AdherenceGreen,
                                        backgroundColor = AdherenceGreen.copy(alpha = 0.15f)
                                    )
                                } else {
                                    StatusBadge(
                                        text = "Ver expediente",
                                        color = MaterialTheme.colorScheme.primary,
                                        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Health Center & Medication details badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalHospital,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = patient.healthCenterName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Text(
                                    text = "Toma: ${patient.dailyDoseTime} hrs",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Selected Patient Clinical Dossier
            val isPatientInFilteredList = selectedPatient != null && filteredPatients.any { it.id == selectedPatient.id }
            if (selectedPatient != null && isPatientInFilteredList) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Expediente Clínico: ${selectedPatient.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Detalles Clínicos del Tratamiento",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { showSingleLabDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("btn_schedule_single_lab")
                                    ) {
                                        Icon(imageVector = Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Examen CV/CD4", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = { showAddNoteDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("btn_add_doctor_note")
                                    ) {
                                        Icon(imageVector = Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Nota", fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(text = "• Centro de Salud: ${selectedPatient.healthCenterName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text(text = "• Esquema TARV: ${selectedPatient.medicineName}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "• Horario de toma habitual: ${selectedPatient.dailyDoseTime}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "• Condiciones Crónicas: ${if (selectedPatient.chronicConditions.isNotBlank()) selectedPatient.chronicConditions else "Ninguna"}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "• Próximo recojo de farmacia: ${nextPickup?.let { SimpleDateFormat("d 'de' MMMM yyyy", Locale.forLanguageTag("es-ES")).format(Date(it.scheduledDate)) } ?: "Al día"}", style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(10.dp))

                            // Latest lab result preview
                            Surface(
                                color = AdherenceGreen.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = AdherenceGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Último Control Laboratorial:",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AdherenceGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "• Carga Viral: ${if (latestLab?.isUndetectable == true || (latestLab?.viralLoadCopies != null && latestLab.viralLoadCopies < 20)) "Indetectable (<20 copias/mL) I=I" else "${latestLab?.viralLoadCopies} copias/mL"}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "• Recuento Linfocitos CD4: ${latestLab?.cd4Count ?: 780} cél/µL (Nivel Protector Óptimo)",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                // Monthly Viral Load and CD4 Summary Report Generator Card
                item {
                    MonthlyReportDoctorCard(
                        patient = selectedPatient,
                        labExams = labExams,
                        adherenceLogs = adherenceLogs,
                        onExportReport = onExportReport ?: { ctx, rep, csv ->
                            HealthReportGenerator.exportReportViaShareSheet(ctx, rep, csv)
                        },
                        onCopyReport = onCopyReport ?: { ctx, rep ->
                            HealthReportGenerator.copyToClipboard(ctx, HealthReportGenerator.formatAsPlainText(rep))
                        }
                    )
                }

                // Doctor Notes History
                if (doctorNotes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Historial de Notas Clínicas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(doctorNotes) { note ->
                        val dateFormat = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.forLanguageTag("es-ES"))
                        OutlinedCard(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = note.doctorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = dateFormat.format(Date(note.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = note.noteText, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }

    // DIÁLOGO: PROGRAMAR CARGA VIRAL Y CD4 POR CENTRO DE SALUD
    if (showBatchLabDialog) {
        ScheduleBatchViralLoadCd4Dialog(
            allPatients = allPatients,
            initialSelectedCenter = selectedCenterFilter ?: HealthCenters.DEFAULT_CENTER,
            onDismiss = { showBatchLabDialog = false },
            onConfirm = { center, dateMillis, lab, notes ->
                onScheduleBatchLab?.invoke(center, dateMillis, lab, notes)
                showBatchLabDialog = false
            }
        )
    }

    // DIÁLOGO: PROGRAMAR EXAMEN INDIVIDUAL PARA PACIENTE SELECCIONADO
    if (showSingleLabDialog && selectedPatient != null) {
        ScheduleSinglePatientLabDialog(
            patient = selectedPatient,
            onDismiss = { showSingleLabDialog = false },
            onConfirm = { dateMillis, lab, notes ->
                onScheduleSingleLab?.invoke(selectedPatient.id, dateMillis, lab, notes)
                showSingleLabDialog = false
            }
        )
    }

    // DIÁLOGO: AUDITORÍA DE INICIOS DE SESIÓN DEL MÉDICO
    if (showLoginHistoryDialog) {
        DoctorLoginHistoryDialog(
            doctorProfile = doctorProfile,
            loginHistory = doctorLoginHistory,
            onDismiss = { showLoginHistoryDialog = false }
        )
    }

    // DIÁLOGO: AGREGAR NOTA MÉDICA
    if (showAddNoteDialog && selectedPatient != null) {
        var noteText by remember { mutableStateOf("") }
        var doctorName by remember { mutableStateOf("Dra. Sofía Mendoza (Infectología)") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = {
                Text("Agregar Nota Médica", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paciente: ${selectedPatient.name}", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = doctorName,
                        onValueChange = { doctorName = it },
                        label = { Text("Nombre del Médico") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Evolución clínica / Indicaciones") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            onAddDoctorNote(noteText, AlertLevel.NORMAL, doctorName)
                            showAddNoteDialog = false
                        }
                    }
                ) {
                    Text("Guardar Nota")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Diálogo de Programación Masiva de Carga Viral y CD4 por Centro de Salud
 */
@Composable
fun ScheduleBatchViralLoadCd4Dialog(
    allPatients: List<PatientProfile>,
    initialSelectedCenter: String,
    onDismiss: () -> Unit,
    onConfirm: (healthCenter: String, dateMillis: Long, laboratory: String, notes: String) -> Unit
) {
    val context = LocalContext.current
    var targetCenter by remember { mutableStateOf(initialSelectedCenter) }
    var laboratory by remember { mutableStateOf("Laboratorio Central de Referencia") }
    var notes by remember { mutableStateOf("Control periódico semestral de Carga Viral y CD4. Supresión viral y respuesta inmunológica.") }
    var selectedDateMillis by remember {
        val initialCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 15)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        mutableStateOf(initialCal.timeInMillis)
    }

    val calendar = remember(selectedDateMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    }
    val datePickerDialog = remember(context, selectedDateMillis) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 8)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDateMillis = picked.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }
    }

    val matchingPatients = remember(targetCenter, allPatients) {
        allPatients.filter { it.healthCenterName.trim().equals(targetCenter.trim(), ignoreCase = true) }
    }

    val dateFormatted = remember(selectedDateMillis) {
        SimpleDateFormat("EEEE d 'de' MMMM yyyy", Locale.forLanguageTag("es-ES")).format(Date(selectedDateMillis))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("es-ES")) else it.toString() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = MedicalTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Programar Carga Viral y CD4",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Emite y envía la orden médica de Carga Viral y CD4 a todos los pacientes registrados en un Centro de Salud:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Selector del Centro de Salud de destino
                HealthCenterDropdownField(
                    selectedCenter = targetCenter,
                    onCenterSelected = { targetCenter = it },
                    label = "Centro de Salud Destinatario",
                    modifier = Modifier.fillMaxWidth()
                )

                // Badge con conteo de pacientes en ese centro
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (matchingPatients.isNotEmpty()) MedicalTeal.copy(alpha = 0.15f) else AlertAmber.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (matchingPatients.isNotEmpty()) Icons.Default.People else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (matchingPatients.isNotEmpty()) MedicalTeal else AlertAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (matchingPatients.isNotEmpty()) {
                                "Se enviará la orden a ${matchingPatients.size} paciente(s) registrados en este centro."
                            } else {
                                "Actualmente no hay pacientes registrados en este centro."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (matchingPatients.isNotEmpty()) MaterialTheme.colorScheme.onSurface else AlertAmber
                        )
                    }
                }

                // Notificación Push FCM
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Transmisión: Los pacientes recibirán la orden y aviso de su control en sus teléfonos.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Selección de Fecha por Calendario
                Text(
                    text = "Fecha programada del examen (Calendario):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MedicalTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MedicalTeal,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Calendario",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dateFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Toca para elegir fecha en el calendario",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedicalTeal
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MedicalTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Indicaciones Médicas") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(targetCenter, selectedDateMillis, laboratory, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MedicalTeal),
                enabled = matchingPatients.isNotEmpty(),
                modifier = Modifier.testTag("btn_confirm_batch_lab_schedule")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Programar y Enviar (${matchingPatients.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo de Programación de Examen Individual para un Paciente
 */
@Composable
fun ScheduleSinglePatientLabDialog(
    patient: PatientProfile,
    onDismiss: () -> Unit,
    onConfirm: (dateMillis: Long, laboratory: String, notes: String) -> Unit
) {
    val context = LocalContext.current
    var laboratory by remember { mutableStateOf("Laboratorio Central de Referencia") }
    var notes by remember { mutableStateOf("Control individual de Carga Viral y recuento CD4.") }
    var selectedDateMillis by remember {
        val initialCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 15)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        mutableStateOf(initialCal.timeInMillis)
    }

    val calendar = remember(selectedDateMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    }
    val datePickerDialog = remember(context, selectedDateMillis) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 8)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDateMillis = picked.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }
    }

    val dateFormatted = remember(selectedDateMillis) {
        SimpleDateFormat("EEEE d 'de' MMMM yyyy", Locale.forLanguageTag("es-ES")).format(Date(selectedDateMillis))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("es-ES")) else it.toString() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Programar Examen de Laboratorio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Paciente: ${patient.name} (${patient.healthCenterName})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )

                // Selección de Fecha por Calendario
                Text(
                    text = "Fecha programada del examen (Calendario):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Calendario",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dateFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Toca para elegir fecha en el calendario",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Indicaciones Clínicas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDateMillis, laboratory, notes) },
                modifier = Modifier.testTag("btn_confirm_single_lab_schedule")
            ) {
                Text("Programar Examen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DoctorLoginHistoryDialog(
    doctorProfile: DoctorProfile?,
    loginHistory: List<DoctorLoginRecord>,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.History, contentDescription = null, tint = MedicalTeal)
        },
        title = {
            Text(
                text = "Auditoría de Inicios de Sesión",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Registros de auditoría y accesos registrados en el sistema:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MedicalTeal.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Médico: Dr(a). ${doctorProfile?.fullName ?: "Especialista TARV"}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = MedicalTeal
                        )
                        Text(
                            text = "CMP: ${doctorProfile?.cmpNumber ?: "N/A"} • ${doctorProfile?.healthCenterName ?: "N/A"}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                if (loginHistory.isEmpty()) {
                    Text(
                        text = "No se registran inicios de sesión previos en este dispositivo.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    loginHistory.forEachIndexed { index, record ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "#${loginHistory.size - index} • ${record.doctorName}",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (record.remembered) AdherenceGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = if (record.remembered) "Recordada" else "Manual",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (record.remembered) AdherenceGreen else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Fecha: ${dateFormat.format(Date(record.timestamp))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "CMP: ${record.cmpNumber} • ${record.healthCenterName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
