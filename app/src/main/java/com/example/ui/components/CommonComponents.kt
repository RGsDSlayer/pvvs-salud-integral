package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LabExam
import com.example.data.model.MedicationPickup
import com.example.data.model.PatientProfile
import com.example.data.model.UserRole
import com.example.util.NutritionAdvisor
import com.example.ui.theme.AdherenceGreen
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.RibbonRed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    currentRole: UserRole,
    patientProfile: PatientProfile?,
    onRoleSelected: (UserRole) -> Unit,
    onTogglePrivacy: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPrivacy = patientProfile?.privacyModeEnabled == true && currentRole == UserRole.PACIENTE
    val initials = remember(patientProfile?.name, isPrivacy) {
        if (isPrivacy || patientProfile?.name.isNullOrBlank()) "PV"
        else {
            val parts = patientProfile!!.name.trim().split(" ")
            if (parts.size >= 2) "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
            else parts[0].take(2).uppercase()
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        shadowElevation = 3.dp,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Centro de Salud Integral",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (currentRole) {
                            UserRole.PACIENTE -> if (isPrivacy) "Hola, Usuario" else "Hola, ${patientProfile?.name ?: "Carlos M."}"
                            UserRole.MEDICO -> "Portal Infectología"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = when (currentRole) {
                                UserRole.PACIENTE -> if (isPrivacy) "Modo Privado Activo" else "Usuario PVVS • Adherente"
                                UserRole.MEDICO -> "Especialista Clínico"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (currentRole == UserRole.PACIENTE && patientProfile != null) {
                        IconButton(
                            onClick = onTogglePrivacy,
                            modifier = Modifier.testTag("btn_toggle_privacy")
                        ) {
                            Icon(
                                imageVector = if (patientProfile.privacyModeEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Modo Privacidad",
                                tint = if (patientProfile.privacyModeEnabled) RibbonRed else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Avatar circle
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Logout Button
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("btn_logout")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (currentRole == UserRole.PACIENTE && patientProfile != null && patientProfile.healthCenterName.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Centro de Salud: ${patientProfile.healthCenterName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoleChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = modifier
            .height(38.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    color: Color,
    backgroundColor: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AddPickupDialog(
    initialMedicine: String,
    initialCenter: String,
    initialDateMillis: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (scheduledDate: Long, medicine: String, center: String, days: Int, notes: String, autoMonthly: Boolean, reminder2Days: Boolean) -> Unit
) {
    val initialCal = remember(initialDateMillis) {
        Calendar.getInstance().apply {
            if (initialDateMillis != null) {
                timeInMillis = initialDateMillis
            } else {
                add(Calendar.DAY_OF_YEAR, 30)
            }
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
        }
    }

    var selectedDayOfMonth by remember { mutableStateOf(initialCal.get(Calendar.DAY_OF_MONTH).toString()) }
    var medicine by remember { mutableStateOf(initialMedicine) }
    var center by remember {
        mutableStateOf(
            if (com.example.data.model.HealthCenters.LIST.any { it.equals(initialCenter.trim(), ignoreCase = true) })
                initialCenter
            else
                com.example.data.model.HealthCenters.DEFAULT_CENTER
        )
    }
    var notes by remember { mutableStateOf("Llevar DNI, carnet de atención y frasco vacío") }
    var autoMonthly by remember { mutableStateOf(true) }
    var reminder2Days by remember { mutableStateOf(true) }

    val currentCal = Calendar.getInstance()
    val targetDay = selectedDayOfMonth.toIntOrNull()?.coerceIn(1, 31) ?: initialCal.get(Calendar.DAY_OF_MONTH)
    val computedCal = Calendar.getInstance().apply {
        timeInMillis = initialCal.timeInMillis
        val maxDays = getActualMaximum(Calendar.DAY_OF_MONTH)
        set(Calendar.DAY_OF_MONTH, minOf(targetDay, maxDays))
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 0)
        // If the date is already passed in current month, schedule for next month
        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.MONTH, 1)
        }
    }
    val dateFormat = SimpleDateFormat("EEEE d 'de' MMMM yyyy", Locale("es", "ES"))
    val formattedTargetDate = dateFormat.format(computedCal.time)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Programar Fecha de Recojo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "🗓️ Próxima Entrega: $formattedTargetDate",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "🔁 Se programará automáticamente el día $targetDay de cada mes con alarma y evento de Calendario a las 8:00 AM (2 días antes y el día del recojo).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                OutlinedTextField(
                    value = selectedDayOfMonth,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 2) {
                            selectedDayOfMonth = input
                        }
                    },
                    label = { Text("Día del mes para el recojo (1 al 31)") },
                    placeholder = { Text("Ej: 15 o 20") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = medicine,
                    onValueChange = { medicine = it },
                    label = { Text("Medicamento / Esquema") },
                    modifier = Modifier.fillMaxWidth()
                )

                HealthCenterDropdownField(
                    selectedCenter = center,
                    onCenterSelected = { center = it },
                    label = "Centro de Salud para Recojo",
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Requisitos / Notas") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Repetición mensual automática",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Auto-programa la misma fecha cada mes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = autoMonthly, onCheckedChange = { autoMonthly = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alarma sonora 2 días antes (8:00 AM)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Suena a las 8:00 AM 2 días antes y el mismo día",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = reminder2Days, onCheckedChange = { reminder2Days = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(computedCal.timeInMillis, medicine, center, 30, notes, autoMonthly, reminder2Days)
                }
            ) {
                Text("Guardar y Programar")
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
fun AddLabResultDialog(
    exam: LabExam,
    onDismiss: () -> Unit,
    onConfirm: (viralLoad: Int?, isUndetectable: Boolean, cd4: Int?, ratio: Float?, notes: String) -> Unit
) {
    var isUndetectable by remember { mutableStateOf(true) }
    var viralLoadText by remember { mutableStateOf("0") }
    var cd4Text by remember { mutableStateOf("750") }
    var cd4RatioText by remember { mutableStateOf("1.2") }
    var doctorNotes by remember { mutableStateOf("Carga viral indetectable y CD4 en rango óptimo de protección.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Registrar Resultados de Laboratorio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Ingresa tus valores de Carga Viral y Linfocitos CD4 para actualizar tu historial seguro.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¿Carga Viral Indetectable? (<20 copias)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isUndetectable) AdherenceGreen else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Indetectable = Intransmisible (I=I)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = isUndetectable, onCheckedChange = { isUndetectable = it })
                }

                if (!isUndetectable) {
                    OutlinedTextField(
                        value = viralLoadText,
                        onValueChange = { viralLoadText = it },
                        label = { Text("Copias de Carga Viral (copias/mL)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = cd4Text,
                    onValueChange = { cd4Text = it },
                    label = { Text("Recuento CD4 (células/mm³ o µL)") },
                    placeholder = { Text("Ej: 650 (Normal >500)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cd4RatioText,
                    onValueChange = { cd4RatioText = it },
                    label = { Text("Razón CD4/CD8 (opcional)") },
                    placeholder = { Text("Ej: 1.1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = doctorNotes,
                    onValueChange = { doctorNotes = it },
                    label = { Text("Observaciones / Indicaciones del médico") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val vl = if (isUndetectable) 0 else viralLoadText.toIntOrNull()
                    val cd4 = cd4Text.toIntOrNull()
                    val ratio = cd4RatioText.toFloatOrNull()
                    onConfirm(vl, isUndetectable, cd4, ratio, doctorNotes)
                }
            ) {
                Text("Guardar Resultados")
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
fun RecordVitalsDialog(
    currentWeight: Float,
    currentHeight: Float = 170f,
    onDismiss: () -> Unit,
    onConfirm: (systolic: Int?, diastolic: Int?, hr: Int?, temp: Float?, weight: Float?, height: Float?, glucose: Int?, o2: Int?, notes: String) -> Unit
) {
    var systolicText by remember { mutableStateOf("120") }
    var diastolicText by remember { mutableStateOf("80") }
    var hrText by remember { mutableStateOf("72") }
    var tempText by remember { mutableStateOf("36.5") }
    var weightText by remember { mutableStateOf(currentWeight.toString()) }
    var heightText by remember { mutableStateOf(if (currentHeight > 0) currentHeight.toInt().toString() else "170") }
    var glucoseText by remember { mutableStateOf("90") }
    var o2Text by remember { mutableStateOf("98") }
    var notes by remember { mutableStateOf("Control rutinario") }

    val wVal = weightText.toFloatOrNull() ?: currentWeight
    val hVal = heightText.toFloatOrNull() ?: currentHeight
    val liveBmi = NutritionAdvisor.calculateBmi(wVal, hVal)
    val liveBmiCategory = NutritionAdvisor.getBmiCategory(liveBmi)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Registrar Signos Vitales e IMC",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Peso y Talla (Calculan el IMC)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Peso (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Talla (cm)") },
                        placeholder = { Text("ej. 170") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Cálculo de IMC en tiempo real con Talla y Peso
                Surface(
                    color = if (liveBmi in 18.5f..24.9f) AdherenceGreen.copy(alpha = 0.12f) else AlertAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "IMC Calculado: $liveBmi",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (liveBmi in 18.5f..24.9f) AdherenceGreen else AlertAmber
                            )
                            Text(
                                text = "Estado: $liveBmiCategory",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (liveBmi in 18.5f..24.9f) AdherenceGreen else AlertAmber
                        ) {
                            Text(
                                text = if (liveBmi in 18.5f..24.9f) "Normal" else "Atención",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Presión arterial
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = systolicText,
                        onValueChange = { systolicText = it },
                        label = { Text("Presión Sist.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = diastolicText,
                        onValueChange = { diastolicText = it },
                        label = { Text("Presión Diast.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Pulso y Temperatura
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hrText,
                        onValueChange = { hrText = it },
                        label = { Text("Pulso (lpm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tempText,
                        onValueChange = { tempText = it },
                        label = { Text("Temp (°C)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Glucosa y Saturación O2
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = glucoseText,
                        onValueChange = { glucoseText = it },
                        label = { Text("Glucosa (mg/dL)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = o2Text,
                        onValueChange = { o2Text = it },
                        label = { Text("Sat. O2 (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas de control") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        systolicText.toIntOrNull(),
                        diastolicText.toIntOrNull(),
                        hrText.toIntOrNull(),
                        tempText.toFloatOrNull(),
                        weightText.toFloatOrNull(),
                        heightText.toFloatOrNull(),
                        glucoseText.toIntOrNull(),
                        o2Text.toIntOrNull(),
                        notes
                    )
                }
            ) {
                Text("Guardar Signos e IMC")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
