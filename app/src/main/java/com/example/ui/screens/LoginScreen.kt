package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import com.example.data.model.DoctorProfile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.HealthCenters
import com.example.data.model.UserRole
import com.example.ui.components.HealthCenterDropdownField
import com.example.ui.theme.AdherenceGreen
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.RibbonRed
import com.example.ui.viewmodel.HealthViewModel

enum class LoginScreenMode {
    MENU,
    PATIENT_LOGIN,
    DOCTOR_LOGIN
}

@Composable
fun LoginScreen(
    viewModel: HealthViewModel,
    onOpenPublicMedicalInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(LoginScreenMode.MENU) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (mode) {
            LoginScreenMode.MENU -> {
                LoginMenuScreen(
                    onSelectPatient = { mode = LoginScreenMode.PATIENT_LOGIN },
                    onSelectDoctor = { mode = LoginScreenMode.DOCTOR_LOGIN },
                    onOpenPublicMedicalInfo = onOpenPublicMedicalInfo
                )
            }
            LoginScreenMode.PATIENT_LOGIN -> {
                PatientLoginScreen(
                    viewModel = viewModel,
                    onBackToMenu = { mode = LoginScreenMode.MENU }
                )
            }
            LoginScreenMode.DOCTOR_LOGIN -> {
                DoctorLoginScreen(
                    viewModel = viewModel,
                    onBackToMenu = { mode = LoginScreenMode.MENU }
                )
            }
        }
    }
}

@Composable
private fun LoginMenuScreen(
    onSelectPatient: () -> Unit,
    onSelectDoctor: () -> Unit,
    onOpenPublicMedicalInfo: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Brand Icon
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = "Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Salud Integral TARV",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Sistema de Gestión y Seguimiento de Tratamiento",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = "Selecciona tu perfil de ingreso:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // OPшёл 1: INICIAR SESIÓN COMO PACIENTE
        ElevatedCard(
            onClick = onSelectPatient,
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_login_as_patient")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Paciente",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Iniciar Sesión como Paciente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Registro de toma diaria, recojo de medicamentos en farmacia, citas y adherencia personal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // OPCIÓN 2: INICIAR SESIÓN COMO MÉDICO
        ElevatedCard(
            onClick = onSelectDoctor,
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_login_as_doctor")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MedicalTeal.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = "Médico",
                        tint = MedicalTeal,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Iniciar Sesión como Médico",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Seguimiento de pacientes por Centro de Salud, programación y envío de Carga Viral y CD4.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MedicalTeal,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // APARTADO: INFORMACIÓN SOBRE EL VIH (INGRESO SIN USUARIO NI CONTRASEÑA)
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_public_medical_info")
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Información sobre el VIH",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verificado",
                                tint = InfoBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Ingreso libre sin usuario ni contraseña",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Consulta información médica rigurosa y verificada sobre qué es el VIH, síntomas por etapas, vías reales de transmisión y métodos de prevención comprobados con respaldo de fuentes como el Manual MSD y la OMS.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onOpenPublicMedicalInfo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_enter_without_login"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ingresar sin Usuario ni Contraseña",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Footer Security Notice
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Confidencialidad médica y datos protegidos bajo los más estrictos estándares de privacidad clínica.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * PANTALLA EXCLUSIVA DE INICIO DE SESIÓN DE PACIENTE
 * En esta pantalla NO aparece ningún apartado ni mención de médico.
 */
@Composable
private fun PatientLoginScreen(
    viewModel: HealthViewModel,
    onBackToMenu: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberSession by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back to Menu Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToMenu,
                modifier = Modifier.testTag("btn_back_to_menu")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver al Menú",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Volver al menú de inicio",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onBackToMenu() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Patient Avatar / Icon
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Paciente",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Acceso Paciente",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Ingresa tus credenciales para acceder a tu plan de medicación y recojos",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Credenciales de Paciente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Username input (masked)
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; errorMessage = null },
                    label = { Text("Usuario Paciente") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (username.isNotEmpty()) {
                            IconButton(onClick = { username = ""; errorMessage = null }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_patient_username")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password input (masked, no visibility toggle)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (password.isNotEmpty()) {
                            IconButton(onClick = { password = ""; errorMessage = null }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        executePatientLogin(viewModel, username, password, rememberSession) { err -> errorMessage = err }
                    }),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_patient_password")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // GUARDAR INICIO DE SESIÓN EN ESTE DISPOSITIVO
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Guardar sesión en este dispositivo",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "No volver a pedir credenciales al abrir la aplicación",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = rememberSession,
                            onCheckedChange = { rememberSession = it },
                            modifier = Modifier.testTag("switch_remember_patient_session")
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RibbonRed.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = RibbonRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = RibbonRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        executePatientLogin(viewModel, username, password, rememberSession) { err -> errorMessage = err }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_login_patient")
                ) {
                    Text(
                        text = "Iniciar Sesión como Paciente",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ℹ️ Al ingresar por primera vez, se te solicitará registrar tu nombre, apellido y seleccionar tu centro de salud de recojo.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

private fun executePatientLogin(
    viewModel: HealthViewModel,
    username: String,
    pass: String,
    rememberSession: Boolean,
    onError: (String) -> Unit
) {
    if (username.isBlank() || pass.isBlank()) {
        onError("Por favor ingresa usuario y contraseña de paciente.")
        return
    }
    val success = viewModel.loginPatient(username, pass, rememberSession)
    if (!success) {
        onError("Credenciales incorrectas. Verifique su usuario y contraseña.")
    }
}

/**
 * PANTALLA EXCLUSIVA DE INICIO DE SESIÓN DE MÉDICO
 */
@Composable
private fun DoctorLoginScreen(
    viewModel: HealthViewModel,
    onBackToMenu: () -> Unit
) {
    val doctorProfile by viewModel.doctorProfile.collectAsStateWithLifecycle()
    val isDoctorRegistered = remember(doctorProfile) { doctorProfile?.cmpNumber?.isNotBlank() == true }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberSession by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back to Menu Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToMenu,
                modifier = Modifier.testTag("btn_back_to_menu")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver al Menú",
                    tint = MedicalTeal
                )
            }
            Text(
                text = "Volver al menú de inicio",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MedicalTeal,
                modifier = Modifier.clickable { onBackToMenu() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Doctor Icon
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(MedicalTeal.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = "Médico",
                tint = MedicalTeal,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Acceso Médico Especialista",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Portal clínico para infectología y monitoreo de pacientes por centro de salud",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Credenciales Médicas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Username input (masked)
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; errorMessage = null },
                    label = { Text("Usuario Médico") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = MedicalTeal
                        )
                    },
                    trailingIcon = {
                        if (username.isNotEmpty()) {
                            IconButton(onClick = { username = ""; errorMessage = null }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MedicalTeal,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_doctor_username")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password input (masked, no visibility toggle)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Contraseña Médica") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MedicalTeal
                        )
                    },
                    trailingIcon = {
                        if (password.isNotEmpty()) {
                            IconButton(onClick = { password = ""; errorMessage = null }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        executeDoctorLogin(viewModel, username, password, rememberSession) { err -> errorMessage = err }
                    }),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MedicalTeal,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_doctor_password")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // GUARDAR INICIO DE SESIÓN EN ESTE DISPOSITIVO
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Guardar sesión en este dispositivo",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Permanecer conectado como médico al reiniciar la app",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = rememberSession,
                            onCheckedChange = { rememberSession = it },
                            modifier = Modifier.testTag("switch_remember_doctor_session")
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RibbonRed.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = RibbonRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = RibbonRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        executeDoctorLogin(viewModel, username, password, rememberSession) { err -> errorMessage = err }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_login_doctor")
                ) {
                    Text(
                        text = "Iniciar Sesión como Médico",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Doctor Registered Card
        if (isDoctorRegistered && doctorProfile != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MedicalTeal.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MedicalTeal.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = MedicalTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Médico Registrado",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTeal
                        )
                        Text(
                            text = "${doctorProfile?.fullName} • CMP: ${doctorProfile?.cmpNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

private fun executeDoctorLogin(
    viewModel: HealthViewModel,
    username: String,
    pass: String,
    rememberSession: Boolean,
    onError: (String) -> Unit
) {
    if (username.isBlank() || pass.isBlank()) {
        onError("Por favor ingresa usuario y contraseña de médico.")
        return
    }
    val success = viewModel.loginDoctor(username, pass, rememberSession)
    if (!success) {
        onError("Credenciales incorrectas. Verifique su usuario y contraseña.")
    }
}

/**
 * Diálogo de Registro Inicial para el Paciente
 * Incluye la selección oficial de los 23 Centros de Salud
 */
@Composable
fun FirstTimePatientRegistrationDialog(
    onDismiss: () -> Unit,
    onConfirm: (firstName: String, lastName: String, age: Int, gender: String, healthCenter: String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("34") }
    var gender by remember { mutableStateOf("Masculino") }
    var healthCenter by remember { mutableStateOf(HealthCenters.DEFAULT_CENTER) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { /* Required completion */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Registro Inicial de Paciente",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Por favor ingresa tus datos y selecciona tu Centro de Salud oficial para recojo de medicación y controles:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it; errorMessage = null },
                    label = { Text("Nombre *") },
                    placeholder = { Text("Ej. Juan Carlos") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_reg_firstname")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it; errorMessage = null },
                    label = { Text("Apellido *") },
                    placeholder = { Text("Ej. Mendoza") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_reg_lastname")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ageStr,
                        onValueChange = { ageStr = it },
                        label = { Text("Edad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("Género") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1.5f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selector de los 23 Centros de Salud Oficiales
                HealthCenterDropdownField(
                    selectedCenter = healthCenter,
                    onCenterSelected = { healthCenter = it },
                    label = "Centro de Salud para Recojo de Medicación",
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = RibbonRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (firstName.isBlank() || lastName.isBlank()) {
                            errorMessage = "Por favor ingresa tanto tu nombre como tu apellido."
                            return@Button
                        }
                        val age = ageStr.toIntOrNull() ?: 30
                        onConfirm(firstName, lastName, age, gender, healthCenter)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_confirm_registration")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Completar Registro y Entrar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Diálogo de Registro Inicial para el Médico al Iniciar Sesión
 * Registra los datos profesionales del médico y los sincroniza con la aplicación y el servidor en Google Drive
 */
@Composable
fun FirstTimeDoctorRegistrationDialog(
    initialDoctorProfile: DoctorProfile? = null,
    existingProfile: DoctorProfile? = initialDoctorProfile,
    onDismiss: () -> Unit = {},
    onConfirm: (fullName: String, cmp: String, specialty: String, healthCenter: String) -> Unit
) {
    val base = existingProfile ?: initialDoctorProfile ?: DoctorProfile()
    var fullName by remember { mutableStateOf(base.fullName) }
    var cmpNumber by remember { mutableStateOf(base.cmpNumber) }
    var specialty by remember { mutableStateOf(base.specialty) }
    var healthCenter by remember { mutableStateOf(base.healthCenterName) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MedicalTeal.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MedicalTeal,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Registro Médico Especialista",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MedicalTeal
                )

                Text(
                    text = "Registre sus datos para vincular sus atenciones clínicas y recetas en la plataforma.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nombre y Apellidos del Médico") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MedicalTeal) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = cmpNumber,
                    onValueChange = { cmpNumber = it },
                    label = { Text("N° Colegiatura (CMP)") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MedicalTeal) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Especialidad Médica") },
                    leadingIcon = { Icon(Icons.Default.Verified, contentDescription = null, tint = MedicalTeal) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                HealthCenterDropdownField(
                    selectedCenter = healthCenter,
                    onCenterSelected = { healthCenter = it },
                    label = "Centro de Salud Asignado",
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = RibbonRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (fullName.isBlank() || cmpNumber.isBlank()) {
                            errorMessage = "Por favor ingrese al menos su Nombre completo y su CMP."
                            return@Button
                        }
                        onConfirm(fullName, cmpNumber, specialty, healthCenter)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_confirm_doctor_registration")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guardar Registro Médico y Continuar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
