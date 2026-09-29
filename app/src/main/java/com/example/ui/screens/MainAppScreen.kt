package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserRole
import com.example.ui.components.ActiveAlarmDialog
import com.example.ui.components.AppHeader
import com.example.ui.viewmodel.HealthViewModel
import kotlinx.coroutines.launch

enum class PatientTab(val title: String, val icon: ImageVector, val tag: String) {
    HOME("Inicio", Icons.Default.Home, "tab_home"),
    MEDICATION("Calendario", Icons.Default.CalendarMonth, "tab_medication"),
    LABS("Laboratorio", Icons.Default.Science, "tab_labs"),
    ADHERENCE("Adherencia", Icons.Default.Timeline, "tab_adherence"),
    NUTRITION("Nutrición", Icons.Default.Restaurant, "tab_nutrition")
}

@Composable
fun MainAppScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val patientProfile by viewModel.currentPatient.collectAsStateWithLifecycle()
    val pickups by viewModel.medicationPickups.collectAsStateWithLifecycle()
    val labExams by viewModel.labExams.collectAsStateWithLifecycle()
    val adherenceLogs by viewModel.adherenceLogs.collectAsStateWithLifecycle()
    val vitalSigns by viewModel.vitalSigns.collectAsStateWithLifecycle()
    val doctorNotes by viewModel.doctorNotes.collectAsStateWithLifecycle()
    val allPatients by viewModel.allPatients.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val activeAlarmEvent by viewModel.activeAlarmEvent.collectAsStateWithLifecycle()
    val doctorProfile by viewModel.doctorProfile.collectAsStateWithLifecycle()
    val doctorLoginHistory by viewModel.doctorLoginHistory.collectAsStateWithLifecycle()
    val fcmToken by viewModel.fcmToken.collectAsStateWithLifecycle()
    val fcmStatusMessage by viewModel.fcmStatusMessage.collectAsStateWithLifecycle()
    val fcmActiveTopics by viewModel.fcmActiveTopics.collectAsStateWithLifecycle()

    var showPublicMedicalInfo by remember { mutableStateOf(false) }
    var showEditDoctorProfileDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(PatientTab.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Request System Notification and Calendar Permissions
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    var hasCalendarPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showPermissionBanner by remember { mutableStateOf(false) }

    val systemPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms[Manifest.permission.POST_NOTIFICATIONS] ?: hasNotificationPermission
        } else true
        val calGranted = perms[Manifest.permission.WRITE_CALENDAR] ?: hasCalendarPermission

        hasNotificationPermission = notifGranted
        hasCalendarPermission = calGranted
        showPermissionBanner = !(notifGranted && calGranted)

        if (calGranted) {
            viewModel.syncAllPickupsWithCalendarAndClock()
        }
        if (notifGranted && calGranted) {
            scope.launch {
                snackbarHostState.showSnackbar("¡Permisos concedidos! Sincronización con Calendario y Reloj activa para las 8:00 AM.")
            }
        }
    }

    LaunchedEffect(Unit) {
        val needsNotif = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission
        val needsCal = !hasCalendarPermission
        if (needsNotif || needsCal) {
            showPermissionBanner = true
            val permsToRequest = mutableListOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            systemPermissionsLauncher.launch(permsToRequest.toTypedArray())
        }
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Public / Unauthenticated Flow
    if (!authState.isAuthenticated) {
        if (showPublicMedicalInfo) {
            HivVerifiedMedicalScreen(
                onBackToLogin = { showPublicMedicalInfo = false },
                modifier = modifier
            )
        } else {
            LoginScreen(
                viewModel = viewModel,
                onOpenPublicMedicalInfo = { showPublicMedicalInfo = true },
                modifier = modifier
            )
        }
        return
    }

    if (authState.isFirstTimePatient) {
        FirstTimePatientRegistrationDialog(
            onDismiss = { /* required */ },
            onConfirm = { firstName, lastName, age, gender, healthCenter ->
                viewModel.registerFirstTimePatient(firstName, lastName, age, gender, healthCenter)
            }
        )
    }

    if (authState.isFirstTimeDoctor || showEditDoctorProfileDialog) {
        FirstTimeDoctorRegistrationDialog(
            existingProfile = doctorProfile,
            onDismiss = { showEditDoctorProfileDialog = false },
            onConfirm = { fullName, cmpNumber, specialty, healthCenter ->
                viewModel.registerDoctorProfile(
                    fullName = fullName,
                    cmp = cmpNumber,
                    specialty = specialty,
                    healthCenter = healthCenter
                )
                showEditDoctorProfileDialog = false
            }
        )
    }

    activeAlarmEvent?.let { alarmEvent ->
        ActiveAlarmDialog(
            alarmEvent = alarmEvent,
            onDismiss = { viewModel.dismissAlarm() },
            onSkipAlarm = {
                viewModel.skipPickupAlarm(alarmEvent.pickupId)
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                AppHeader(
                    currentRole = currentRole,
                    patientProfile = patientProfile,
                    onRoleSelected = { role -> viewModel.setRole(role) },
                    onTogglePrivacy = { viewModel.togglePrivacyMode() },
                    onLogout = { viewModel.logout() }
                )

                // Notification & Calendar Permission Request Banner if not granted
                AnimatedVisibility(visible = showPermissionBanner && (!hasNotificationPermission || !hasCalendarPermission)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Permisos de Calendario y Alarmas (8:00 AM)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Requerido para sincronizar recojos en Calendario y Reloj (2 días antes y el mismo día).",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val permsToRequest = mutableListOf(
                                        Manifest.permission.READ_CALENDAR,
                                        Manifest.permission.WRITE_CALENDAR
                                    )
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    systemPermissionsLauncher.launch(permsToRequest.toTypedArray())
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("btn_request_notification_permission")
                            ) {
                                Text("Activar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { showPermissionBanner = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (currentRole == UserRole.PACIENTE) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 6.dp,
                    windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
                ) {
                    PatientTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (selectedTab == tab) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRole) {
                UserRole.PACIENTE -> {
                    when (selectedTab) {
                        PatientTab.HOME -> HomeScreen(
                            patientProfile = patientProfile,
                            pickups = pickups,
                            labExams = labExams,
                            adherenceLogs = adherenceLogs,
                            vitalSigns = vitalSigns,
                            onCompletePickup = { pickup -> viewModel.completePickup(pickup) },
                            onLogTodayDose = { status -> viewModel.logDoseToday(status) },
                            onNavigateToPickups = { selectedTab = PatientTab.MEDICATION },
                            onNavigateToLabs = { selectedTab = PatientTab.LABS },
                            onNavigateToAdherence = { selectedTab = PatientTab.ADHERENCE },
                            onNavigateToNutrition = { selectedTab = PatientTab.NUTRITION },
                            onTriggerTestNotification = { type -> viewModel.triggerSimulationReminder(type) },
                            fcmToken = fcmToken,
                            fcmStatus = fcmStatusMessage,
                            fcmTopics = fcmActiveTopics,
                            onTriggerFcmPickup = { isDayOf -> viewModel.triggerFcmMedicationPickupReminder(isDayOf) },
                            onTriggerFcmViralLoad = { viewModel.triggerFcmViralLoadReminder() }
                        )

                        PatientTab.MEDICATION -> MedicationScheduleScreen(
                            patientProfile = patientProfile,
                            pickups = pickups,
                            onAddPickup = { date, med, center, days, notes, auto, r2d ->
                                viewModel.scheduleMedicationPickup(date, med, center, days, notes, auto, r2d, true)
                            },
                            onCompletePickup = { pickup -> viewModel.completePickup(pickup) },
                            onDeletePickup = { id -> viewModel.deletePickup(id) },
                            onTestAlarm = { pickup -> viewModel.triggerTwoDaysBeforeAlarm(pickup) },
                            onTestGeneralAlarm = { viewModel.testAlarmSound() },
                            onSyncPickupWithCalendarAndClock = { pickup -> viewModel.syncPickupWithCalendarAndClock(pickup) },
                            onSyncAllWithCalendarAndClock = { viewModel.syncAllPickupsWithCalendarAndClock() }
                        )

                        PatientTab.LABS -> LabExamsScreen(
                            patientProfile = patientProfile,
                            labExams = labExams,
                            onScheduleLab = { date, labName, notes, auto ->
                                viewModel.scheduleLabExam(date, labName, notes, auto)
                            },
                            onCompleteLabWithResults = { exam, vl, isUndetectable, cd4, ratio, notes ->
                                viewModel.completeLabExamWithResults(exam, vl, isUndetectable, cd4, ratio, notes)
                            },
                            onDeleteLab = { id -> viewModel.deleteLabExam(id) }
                        )

                        PatientTab.ADHERENCE -> AdherenceAndVitalsScreen(
                            patientProfile = patientProfile,
                            adherenceLogs = adherenceLogs,
                            vitalSigns = vitalSigns,
                            labExams = labExams,
                            onLogTodayDose = { status -> viewModel.logDoseToday(status) },
                            onLogDoseForDate = { dateStr, status -> viewModel.logDoseForDate(dateStr, status) },
                            onRecordVitals = { sys, dia, hr, temp, weight, height, glucose, o2, notes ->
                                viewModel.recordVitals(sys, dia, hr, temp, weight, height, glucose, o2, notes)
                            },
                            onDeleteVital = { id -> viewModel.deleteVitalSign(id) }
                        )

                        PatientTab.NUTRITION -> NutritionScreen(
                            patientProfile = patientProfile,
                            onUpdateWeightAndHeight = { newWeight, newHeight ->
                                viewModel.updateWeightAndHeight(newWeight, newHeight)
                            },
                            onUpdateChronicConditions = { conditionsCsv ->
                                viewModel.updatePatientConditions(conditionsCsv)
                            }
                        )
                    }
                }

                UserRole.MEDICO -> {
                    DoctorScreen(
                        allPatients = allPatients,
                        selectedPatient = patientProfile,
                        pickups = pickups,
                        labExams = labExams,
                        adherenceLogs = adherenceLogs,
                        doctorNotes = doctorNotes,
                        onSelectPatient = { id -> viewModel.selectPatient(id) },
                        onAddDoctorNote = { note, level, docName ->
                            viewModel.addDoctorNote(note, level, docName)
                        },
                        onScheduleBatchLab = { center, date, lab, notes ->
                            viewModel.scheduleBatchViralLoadAndCd4(center, date, lab, notes)
                        },
                        onScheduleSingleLab = { patientId, date, lab, notes ->
                            viewModel.scheduleSingleViralLoadAndCd4(patientId, date, lab, notes)
                        },
                        onExportReport = { ctx, report, asCsv ->
                            viewModel.exportViralLoadCd4Report(ctx, report, asCsv)
                        },
                        onCopyReport = { ctx, report ->
                            viewModel.copyViralLoadCd4Report(ctx, report)
                        },
                        doctorProfile = doctorProfile,
                        doctorLoginHistory = doctorLoginHistory,
                        onOpenServerDrive = { viewModel.openServerDrive(context) },
                        onCopyServerUrl = { viewModel.copyServerUrl(context) },
                        onExportToServer = { viewModel.exportAndSyncToServer(context) },
                        onEditDoctorProfile = { showEditDoctorProfileDialog = true }
                    )
                }
            }
        }
    }
}
