package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AdherenceLog
import com.example.data.model.AdherenceStatus
import com.example.data.model.AlertLevel
import com.example.data.model.DoctorLoginRecord
import com.example.data.model.DoctorNote
import com.example.data.model.DoctorProfile
import com.example.data.model.LabExam
import com.example.data.model.MedicationPickup
import com.example.data.model.PatientProfile
import com.example.data.model.PickupStatus
import com.example.data.model.SERVER_DRIVE_FOLDER_URL
import com.example.data.model.UserRole
import com.example.data.model.VitalSign
import com.example.data.repository.HealthRepository
import com.example.service.FcmNotificationManager
import com.example.util.HealthReportGenerator
import com.example.util.MonthlyHealthSummary
import com.example.util.NutritionAdvisor
import com.example.util.NutritionPlan
import com.example.util.ReminderManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AuthState(
    val isAuthenticated: Boolean = false,
    val role: UserRole? = null,
    val isFirstTimePatient: Boolean = false,
    val isFirstTimeDoctor: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = HealthRepository(db.appDao())
    private val reminderManager = ReminderManager(application)
    private val sharedPrefs = application.getSharedPreferences("salud_vih_auth_prefs", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _currentRole = MutableStateFlow(UserRole.PACIENTE)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _selectedPatientId = MutableStateFlow(1)
    val selectedPatientId: StateFlow<Int> = _selectedPatientId.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _activeAlarmEvent = MutableStateFlow<com.example.ui.components.ActiveAlarmEvent?>(null)
    val activeAlarmEvent: StateFlow<com.example.ui.components.ActiveAlarmEvent?> = _activeAlarmEvent.asStateFlow()

    val allPatients: StateFlow<List<PatientProfile>> = repository.allPatients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentPatient: StateFlow<PatientProfile?> = _selectedPatientId
        .flatMapLatest { id -> repository.getPatient(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val medicationPickups: StateFlow<List<MedicationPickup>> = _selectedPatientId
        .flatMapLatest { id -> repository.getPickups(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val labExams: StateFlow<List<LabExam>> = _selectedPatientId
        .flatMapLatest { id -> repository.getLabExams(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adherenceLogs: StateFlow<List<AdherenceLog>> = _selectedPatientId
        .flatMapLatest { id -> repository.getAdherenceLogs(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vitalSigns: StateFlow<List<VitalSign>> = _selectedPatientId
        .flatMapLatest { id -> repository.getVitalSigns(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doctorNotes: StateFlow<List<DoctorNote>> = _selectedPatientId
        .flatMapLatest { id -> repository.getDoctorNotes(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fcmToken: StateFlow<String?> = FcmNotificationManager.fcmTokenState
    val fcmStatusMessage: StateFlow<String> = FcmNotificationManager.fcmStatusMessage
    val fcmActiveTopics: StateFlow<Set<String>> = FcmNotificationManager.activeTopics

    private val _doctorProfile = MutableStateFlow(loadDoctorProfile())
    val doctorProfile: StateFlow<DoctorProfile> = _doctorProfile.asStateFlow()

    private val _doctorLoginHistory = MutableStateFlow<List<DoctorLoginRecord>>(loadDoctorLoginHistory())
    val doctorLoginHistory: StateFlow<List<DoctorLoginRecord>> = _doctorLoginHistory.asStateFlow()

    init {
        // Check if there is a saved session on this device
        val isSessionSaved = sharedPrefs.getBoolean("saved_session_logged_in", false)
        val savedRoleStr = sharedPrefs.getString("saved_session_role", null)
        if (isSessionSaved && savedRoleStr != null) {
            val role = if (savedRoleStr == UserRole.MEDICO.name) UserRole.MEDICO else UserRole.PACIENTE
            val isPatientRegistered = sharedPrefs.getBoolean("is_patient_registered", true)
            val isDoctorRegistered = sharedPrefs.getBoolean("is_doctor_registered", false)
            val savedPatientId = sharedPrefs.getInt("saved_patient_id", 1)

            _currentRole.value = role
            _selectedPatientId.value = savedPatientId
            _authState.value = AuthState(
                isAuthenticated = true,
                role = role,
                isFirstTimePatient = (role == UserRole.PACIENTE && !isPatientRegistered),
                isFirstTimeDoctor = (role == UserRole.MEDICO && !isDoctorRegistered)
            )

            if (role == UserRole.MEDICO && isDoctorRegistered) {
                val prof = _doctorProfile.value
                recordDoctorLogin(prof.fullName, prof.cmpNumber, prof.specialty, prof.healthCenterName)
            }
        }

        // Initialize Firebase Cloud Messaging Engine
        FcmNotificationManager.initialize(application)

        // Observe current patient profile to sync FCM topic subscriptions automatically
        viewModelScope.launch {
            currentPatient.collect { patient ->
                if (patient != null) {
                    FcmNotificationManager.syncPatientSubscriptions(
                        application,
                        patient.id,
                        patient.healthCenterName
                    )
                }
            }
        }
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun loginDoctor(username: String, password: String, rememberSession: Boolean = true): Boolean {
        if (username.trim().equals("MEDPVVS", ignoreCase = true) && password.trim() == "25DEDIC") {
            _loginError.value = null
            val isDoctorRegistered = sharedPrefs.getBoolean("is_doctor_registered", false)
            _currentRole.value = UserRole.MEDICO
            _authState.value = AuthState(
                isAuthenticated = true,
                role = UserRole.MEDICO,
                isFirstTimePatient = false,
                isFirstTimeDoctor = !isDoctorRegistered
            )

            if (rememberSession) {
                sharedPrefs.edit {
                    putBoolean("saved_session_logged_in", true)
                    putString("saved_session_role", UserRole.MEDICO.name)
                }
            } else {
                sharedPrefs.edit {
                    putBoolean("saved_session_logged_in", false)
                    remove("saved_session_role")
                }
            }

            val prof = _doctorProfile.value
            recordDoctorLogin(prof.fullName, prof.cmpNumber, prof.specialty, prof.healthCenterName)

            _userMessage.value = if (!isDoctorRegistered) {
                "¡Bienvenido! Por favor complete el registro de sus datos médicos."
            } else {
                "Sesión iniciada como Médico Especialista: ${prof.fullName}"
            }
            return true
        } else {
            _loginError.value = "Credenciales incorrectas para Médico. Verifique su usuario y contraseña."
            return false
        }
    }

    fun loginPatient(username: String, password: String, rememberSession: Boolean = true): Boolean {
        if (username.trim().equals("PROVIDA", ignoreCase = true) && password.trim() == "25DEDIC") {
            _loginError.value = null
            val isRegistered = sharedPrefs.getBoolean("is_patient_registered", false)
            _currentRole.value = UserRole.PACIENTE
            _authState.value = AuthState(
                isAuthenticated = true,
                role = UserRole.PACIENTE,
                isFirstTimePatient = !isRegistered,
                isFirstTimeDoctor = false
            )

            if (rememberSession) {
                sharedPrefs.edit {
                    putBoolean("saved_session_logged_in", true)
                    putString("saved_session_role", UserRole.PACIENTE.name)
                    putInt("saved_patient_id", 1)
                }
            } else {
                sharedPrefs.edit {
                    putBoolean("saved_session_logged_in", false)
                    remove("saved_session_role")
                }
            }

            _userMessage.value = if (!isRegistered) {
                "¡Bienvenido! Completa tu registro con tu nombre y apellido."
            } else {
                "Sesión iniciada como Paciente."
            }
            return true
        } else {
            _loginError.value = "Credenciales incorrectas para Paciente. Verifique su usuario y contraseña."
            return false
        }
    }

    fun logout() {
        sharedPrefs.edit {
            putBoolean("saved_session_logged_in", false)
            remove("saved_session_role")
        }

        _authState.value = AuthState(
            isAuthenticated = false,
            role = null,
            isFirstTimePatient = false,
            isFirstTimeDoctor = false
        )
        _userMessage.value = "Sesión cerrada correctamente"
    }

    fun registerDoctorProfile(
        fullName: String,
        cmp: String,
        specialty: String,
        healthCenter: String,
        rne: String = "",
        email: String = "",
        phone: String = ""
    ) {
        val trimmedName = fullName.trim().ifBlank { "Dr. Especialista TARV" }
        val trimmedCmp = cmp.trim().ifBlank { "CMP 48291" }
        val trimmedRne = rne.trim()
        val trimmedSpecialty = specialty.trim().ifBlank { "Infectología y TARV" }
        val trimmedCenter = healthCenter.trim().ifBlank { "C.S. San Juan de Miraflores" }
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()

        val profile = DoctorProfile(
            fullName = trimmedName,
            cmpNumber = trimmedCmp,
            rneNumber = trimmedRne,
            specialty = trimmedSpecialty,
            healthCenterName = trimmedCenter,
            email = trimmedEmail,
            phone = trimmedPhone,
            registeredDate = System.currentTimeMillis(),
            serverSyncUrl = SERVER_DRIVE_FOLDER_URL
        )

        sharedPrefs.edit {
            putBoolean("is_doctor_registered", true)
            putString("doctor_full_name", profile.fullName)
            putString("doctor_cmp", profile.cmpNumber)
            putString("doctor_rne", profile.rneNumber)
            putString("doctor_specialty", profile.specialty)
            putString("doctor_health_center", profile.healthCenterName)
            putString("doctor_email", profile.email)
            putString("doctor_phone", profile.phone)
            putLong("doctor_registered_date", profile.registeredDate)
        }

        _doctorProfile.value = profile
        _authState.value = _authState.value.copy(isFirstTimeDoctor = false)

        // Registrar inicio de sesión en historial
        recordDoctorLogin(profile.fullName, profile.cmpNumber, profile.specialty, profile.healthCenterName)
        _userMessage.value = "¡Médico registrado con éxito! Dr. ${profile.fullName} (${profile.cmpNumber})"
    }

    fun recordDoctorLogin(
        doctorName: String,
        cmpNumber: String,
        specialty: String,
        healthCenterName: String
    ) {
        val newRecord = DoctorLoginRecord(
            doctorName = doctorName,
            cmpNumber = cmpNumber,
            specialty = specialty,
            healthCenterName = healthCenterName,
            loginTimestamp = System.currentTimeMillis(),
            serverDestinationUrl = SERVER_DRIVE_FOLDER_URL
        )
        val currentList = _doctorLoginHistory.value.toMutableList()
        currentList.add(0, newRecord)
        _doctorLoginHistory.value = currentList
        saveDoctorLoginHistory(currentList)
    }

    private fun loadDoctorProfile(): DoctorProfile {
        return DoctorProfile(
            fullName = sharedPrefs.getString("doctor_full_name", "Dr. Roberto Carlos Mendoza") ?: "Dr. Roberto Carlos Mendoza",
            cmpNumber = sharedPrefs.getString("doctor_cmp", "CMP 48291") ?: "CMP 48291",
            rneNumber = sharedPrefs.getString("doctor_rne", "RNE 19482") ?: "RNE 19482",
            specialty = sharedPrefs.getString("doctor_specialty", "Infectología y Tratamiento TARV") ?: "Infectología y Tratamiento TARV",
            healthCenterName = sharedPrefs.getString("doctor_health_center", "C.S. San Juan de Miraflores") ?: "C.S. San Juan de Miraflores",
            email = sharedPrefs.getString("doctor_email", "dr.mendoza.infectologia@minsa.gob.pe") ?: "dr.mendoza.infectologia@minsa.gob.pe",
            phone = sharedPrefs.getString("doctor_phone", "+51 987 654 321") ?: "+51 987 654 321",
            registeredDate = sharedPrefs.getLong("doctor_registered_date", System.currentTimeMillis()),
            serverSyncUrl = SERVER_DRIVE_FOLDER_URL
        )
    }

    private fun loadDoctorLoginHistory(): List<DoctorLoginRecord> {
        val raw = sharedPrefs.getString("doctor_login_history_raw", null) ?: return emptyList()
        val list = mutableListOf<DoctorLoginRecord>()
        try {
            val entries = raw.split("###")
            for (entry in entries) {
                if (entry.isBlank()) continue
                val parts = entry.split(":::")
                if (parts.size >= 6) {
                    list.add(
                        DoctorLoginRecord(
                            id = parts[0],
                            doctorName = parts[1],
                            cmpNumber = parts[2],
                            specialty = parts[3],
                            healthCenterName = parts[4],
                            loginTimestamp = parts[5].toLongOrNull() ?: System.currentTimeMillis(),
                            serverDestinationUrl = if (parts.size >= 7) parts[6] else SERVER_DRIVE_FOLDER_URL
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // fallback
        }
        return list
    }

    private fun saveDoctorLoginHistory(records: List<DoctorLoginRecord>) {
        val serialized = records.take(50).joinToString("###") {
            "${it.id}:::${it.doctorName}:::${it.cmpNumber}:::${it.specialty}:::${it.healthCenterName}:::${it.loginTimestamp}:::${it.serverDestinationUrl}"
        }
        sharedPrefs.edit {
            putString("doctor_login_history_raw", serialized)
        }
    }

    fun getSavedPatientCredentials(): Pair<String, String> {
        return Pair("", "")
    }

    fun getSavedDoctorCredentials(): Pair<String, String> {
        return Pair("", "")
    }

    fun openServerDrive(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SERVER_DRIVE_FOLDER_URL)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            copyServerUrl(context)
            Toast.makeText(context, "Enlace copiado: $SERVER_DRIVE_FOLDER_URL", Toast.LENGTH_LONG).show()
        }
    }

    fun copyServerUrl(context: Context) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Servidor Google Drive", SERVER_DRIVE_FOLDER_URL)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Enlace del servidor copiado al portapapeles", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun exportAndSyncToServer(context: Context) {
        val doc = _doctorProfile.value
        val history = _doctorLoginHistory.value
        val patient = currentPatient.value
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val builder = StringBuilder()
        builder.append("=== REPORTE Y REGISTRO CLÍNICO - SERVIDOR GOOGLE DRIVE ===\n")
        builder.append("Servidor Central: $SERVER_DRIVE_FOLDER_URL\n")
        builder.append("Fecha de Exportación: ${sdf.format(Date())}\n\n")

        builder.append("--- MÉDICO REGISTRADO ---\n")
        builder.append("Nombre: ${doc.fullName}\n")
        builder.append("CMP: ${doc.cmpNumber} | RNE: ${doc.rneNumber}\n")
        builder.append("Especialidad: ${doc.specialty}\n")
        builder.append("Centro de Salud: ${doc.healthCenterName}\n")
        builder.append("Contacto: ${doc.email} | ${doc.phone}\n\n")

        builder.append("--- HISTORIAL DE INICIOS DE SESIÓN MÉDICOS (${history.size} ingresos) ---\n")
        history.take(10).forEachIndexed { index, record ->
            builder.append("${index + 1}. [${sdf.format(Date(record.loginTimestamp))}] ${record.doctorName} (${record.cmpNumber}) - ${record.healthCenterName}\n")
        }
        builder.append("\n")

        if (patient != null) {
            builder.append("--- PACIENTE ACTUAL ---\n")
            builder.append("Nombre: ${patient.name} (${patient.nickname})\n")
            builder.append("Centro de Recojo: ${patient.healthCenterName}\n")
            builder.append("Esquema TARV: ${patient.medicineName}\n")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Registro Médico y Pacientes - Servidor Google Drive")
            putExtra(Intent.EXTRA_TEXT, builder.toString())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(Intent.createChooser(shareIntent, "Sincronizar con Servidor Google Drive").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            copyServerUrl(context)
        }
    }

    fun registerFirstTimePatient(
        firstName: String,
        lastName: String,
        age: Int,
        gender: String,
        healthCenter: String
    ) {
        val fullName = "${firstName.trim()} ${lastName.trim()}"
        val nick = firstName.trim()
        
        sharedPrefs.edit {
            putBoolean("is_patient_registered", true)
            putString("patient_full_name", fullName)
        }

        viewModelScope.launch {
            val current = currentPatient.value
            if (current != null) {
                val updated = current.copy(
                    name = fullName,
                    nickname = nick,
                    age = if (age > 0) age else current.age,
                    gender = gender.ifBlank { current.gender },
                    healthCenterName = healthCenter.ifBlank { current.healthCenterName }
                )
                repository.updatePatient(updated)
            }
            _authState.value = _authState.value.copy(isFirstTimePatient = false)
            _userMessage.value = "¡Bienvenido/a $fullName! Tu registro ha sido completado."
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    fun selectPatient(id: Int) {
        _selectedPatientId.value = id
    }

    fun togglePrivacyMode() {
        val patient = currentPatient.value ?: return
        viewModelScope.launch {
            repository.updatePatient(patient.copy(privacyModeEnabled = !patient.privacyModeEnabled))
            _userMessage.value = if (!patient.privacyModeEnabled) {
                "Modo Privacidad activado: Nombres de medicamentos y términos clínicos discretos."
            } else {
                "Modo Privacidad desactivado."
            }
        }
    }

    fun updateWeightAndHeight(weightKg: Float, heightCm: Float) {
        val current = currentPatient.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                weightKg = weightKg,
                heightCm = heightCm
            )
            repository.updatePatient(updated)
            // Also register a vital sign entry so weight tracking is logged
            val vital = VitalSign(
                patientId = current.id,
                timestamp = System.currentTimeMillis(),
                weightKg = weightKg,
                heightCm = heightCm,
                notes = "Actualización de peso y talla desde módulo de nutrición (Talla: ${heightCm.toInt()} cm)"
            )
            repository.saveVitalSign(vital)
            val bmi = ((weightKg / ((heightCm / 100f) * (heightCm / 100f))) * 10).toInt() / 10f
            _userMessage.value = "Datos actualizados: Peso ${weightKg}kg | Talla ${heightCm.toInt()}cm (IMC: $bmi)"
        }
    }

    fun updatePatientConditions(conditionsCsv: String) {
        val current = currentPatient.value ?: return
        viewModelScope.launch {
            repository.updatePatient(current.copy(chronicConditions = conditionsCsv))
        }
    }

    fun updateProfile(
        name: String,
        nickname: String,
        age: Int,
        gender: String,
        heightCm: Float,
        weightKg: Float,
        chronicConditions: String,
        nutritionGoal: String,
        medicineName: String,
        dailyDoseTime: String,
        healthCenterName: String
    ) {
        val current = currentPatient.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                name = name,
                nickname = nickname,
                age = age,
                gender = gender,
                heightCm = heightCm,
                weightKg = weightKg,
                chronicConditions = chronicConditions,
                nutritionGoal = nutritionGoal,
                medicineName = medicineName,
                dailyDoseTime = dailyDoseTime,
                healthCenterName = healthCenterName
            )
            repository.updatePatient(updated)
            _userMessage.value = "Perfil de paciente actualizado correctamente"
        }
    }

    // Medication Pickups & Calendar Scheduling
    fun scheduleMedicationPickup(
        scheduledDate: Long,
        medicineName: String,
        healthCenterName: String,
        quantityDays: Int,
        notes: String,
        autoMonthly: Boolean,
        reminder2DaysBefore: Boolean,
        reminderDayOf: Boolean,
        monthsToProject: Int = 6
    ) {
        val patientId = _selectedPatientId.value
        val isPrivacy = currentPatient.value?.privacyModeEnabled ?: false
        viewModelScope.launch {
            // 1. Primary scheduled pickup
            val pickup = MedicationPickup(
                patientId = patientId,
                scheduledDate = scheduledDate,
                status = PickupStatus.PROGRAMADO,
                healthCenterName = healthCenterName,
                medicineName = medicineName,
                quantityDays = quantityDays,
                notes = notes,
                autoMonthly = autoMonthly,
                reminder2DaysBefore = reminder2DaysBefore,
                reminderDayOf = reminderDayOf
            )
            val newId = repository.savePickup(pickup)
            reminderManager.schedulePickupAlarms(
                newId.toInt(),
                scheduledDate,
                medicineName,
                healthCenterName,
                notes,
                isPrivacy
            )

            // Send FCM push notification confirmation to device
            val dateFormatted = SimpleDateFormat("d 'de' MMMM", Locale("es", "ES")).format(Date(scheduledDate))
            FcmNotificationManager.sendMedicationPickupFcmNotification(
                context = getApplication(),
                medicineName = medicineName,
                healthCenter = healthCenterName,
                pickupDate = dateFormatted,
                isDayOfPickup = false
            )

            // 2. If autoMonthly, automatically schedule for the same date every month for the next monthsToProject months
            if (autoMonthly) {
                val baseCal = java.util.Calendar.getInstance().apply { timeInMillis = scheduledDate }
                val targetDay = baseCal.get(java.util.Calendar.DAY_OF_MONTH)
                val hour = baseCal.get(java.util.Calendar.HOUR_OF_DAY)
                val minute = baseCal.get(java.util.Calendar.MINUTE)

                for (monthOffset in 1..monthsToProject) {
                    val nextMonthCal = java.util.Calendar.getInstance().apply {
                        timeInMillis = scheduledDate
                        add(java.util.Calendar.MONTH, monthOffset)
                        val maxDayInMonth = getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                        set(java.util.Calendar.DAY_OF_MONTH, minOf(targetDay, maxDayInMonth))
                        set(java.util.Calendar.HOUR_OF_DAY, hour)
                        set(java.util.Calendar.MINUTE, minute)
                        set(java.util.Calendar.SECOND, 0)
                    }

                    val recurringPickup = MedicationPickup(
                        patientId = patientId,
                        scheduledDate = nextMonthCal.timeInMillis,
                        status = PickupStatus.PROGRAMADO,
                        healthCenterName = healthCenterName,
                        medicineName = medicineName,
                        quantityDays = quantityDays,
                        notes = notes,
                        autoMonthly = true,
                        reminder2DaysBefore = reminder2DaysBefore,
                        reminderDayOf = reminderDayOf
                    )
                    val recurringId = repository.savePickup(recurringPickup)
                    reminderManager.schedulePickupAlarms(
                        recurringId.toInt(),
                        nextMonthCal.timeInMillis,
                        medicineName,
                        healthCenterName,
                        notes,
                        isPrivacy
                    )
                }
            }

            val baseCal = java.util.Calendar.getInstance().apply { timeInMillis = scheduledDate }
            val dayNum = baseCal.get(java.util.Calendar.DAY_OF_MONTH)
            _userMessage.value = if (autoMonthly) {
                "Recojo programado para el día $dayNum de cada mes. Alarmas automáticas a las 8:00 AM (2 días antes y el mismo día)."
            } else {
                "Recojo programado. Alarmas automáticas a las 8:00 AM (2 días antes y el mismo día)."
            }
        }
    }

    fun triggerTwoDaysBeforeAlarm(pickup: MedicationPickup) {
        val isPrivacy = currentPatient.value?.privacyModeEnabled ?: false
        val dateFormat = SimpleDateFormat("EEEE d 'de' MMMM yyyy", Locale("es", "ES"))
        val dateFormatted = dateFormat.format(Date(pickup.scheduledDate))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }

        reminderManager.triggerTwoDaysBeforeAlarm(
            pickupId = pickup.id,
            medicineName = pickup.medicineName,
            centerName = pickup.healthCenterName,
            scheduledDateMillis = pickup.scheduledDate,
            isPrivacy = isPrivacy
        )

        _activeAlarmEvent.value = com.example.ui.components.ActiveAlarmEvent(
            pickupId = pickup.id,
            title = if (isPrivacy) "⏰ ALARMA (8:00 AM): Cita Médica en 2 Días" else "🚨 ALARMA (8:00 AM): Recojo de Medicamentos en 2 Días",
            message = if (isPrivacy) {
                "Alarma de las 8:00 AM: Recuerda que en 2 días tienes tu cita médica ($dateFormatted)."
            } else {
                "Alarma de las 8:00 AM: Faltan 2 días para recoger tu medicación de ${pickup.medicineName} en ${pickup.healthCenterName} ($dateFormatted). Lleva tu DNI y receta médica."
            },
            medicineName = if (isPrivacy) "Tratamiento de control" else pickup.medicineName,
            centerName = pickup.healthCenterName,
            pickupDateFormatted = dateFormatted,
            isOneDayBefore = false
        )
    }

    fun testAlarmSound() {
        val pickup = medicationPickups.value.firstOrNull { it.status == PickupStatus.PROGRAMADO }
        if (pickup != null) {
            triggerTwoDaysBeforeAlarm(pickup)
        } else {
            val patient = currentPatient.value
            val medName = patient?.medicineName ?: "Tratamiento TARV"
            val center = patient?.healthCenterName ?: "Centro de Salud"
            val dummyPickup = MedicationPickup(
                id = 999,
                medicineName = medName,
                healthCenterName = center,
                scheduledDate = System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000L,
                status = PickupStatus.PROGRAMADO
            )
            triggerTwoDaysBeforeAlarm(dummyPickup)
        }
    }

    fun syncPickupWithCalendarAndClock(pickup: MedicationPickup) {
        val isPrivacy = currentPatient.value?.privacyModeEnabled ?: false
        val calSuccess = reminderManager.syncWithCalendar(
            pickupId = pickup.id,
            pickupTimestamp = pickup.scheduledDate,
            medicineName = pickup.medicineName,
            centerName = pickup.healthCenterName,
            notes = pickup.notes,
            isPrivacyMode = isPrivacy
        )
        val clockSuccess = reminderManager.syncWithSystemClock(
            medicineName = pickup.medicineName,
            centerName = pickup.healthCenterName,
            isPrivacyMode = isPrivacy
        )
        _userMessage.value = when {
            calSuccess && clockSuccess -> "¡Sincronizado con éxito en tu Calendario y Reloj a las 8:00 AM (2 días antes y el día del recojo)!"
            calSuccess -> "¡Sincronizado en tu Calendario de Android (2 días antes y el día del recojo a las 8:00 AM)!"
            else -> "Alarmas registradas a las 8:00 AM (2 días antes y el mismo día)."
        }
    }

    fun syncAllPickupsWithCalendarAndClock() {
        viewModelScope.launch {
            val isPrivacy = currentPatient.value?.privacyModeEnabled ?: false
            val active = medicationPickups.value.filter { it.status == PickupStatus.PROGRAMADO }
            var count = 0
            for (p in active) {
                val ok = reminderManager.syncWithCalendar(
                    pickupId = p.id,
                    pickupTimestamp = p.scheduledDate,
                    medicineName = p.medicineName,
                    centerName = p.healthCenterName,
                    notes = p.notes,
                    isPrivacyMode = isPrivacy
                )
                if (ok) count++
                reminderManager.schedulePickupAlarms(
                    pickupId = p.id,
                    pickupTimestamp = p.scheduledDate,
                    medicineName = p.medicineName,
                    centerName = p.healthCenterName,
                    notes = p.notes,
                    isPrivacyMode = isPrivacy
                )
            }
            reminderManager.syncWithSystemClock(
                medicineName = "Tratamiento TARV",
                centerName = "Centro de Salud",
                isPrivacyMode = isPrivacy
            )
            _userMessage.value = "Sincronizados $count recojo(s) con Calendario y Reloj (8:00 AM)."
        }
    }

    fun skipPickupAlarm(pickupId: Int) {
        viewModelScope.launch {
            val pickup = repository.getPickupById(pickupId)
            if (pickup != null) {
                repository.updatePickup(pickup.copy(alarmSkipped = true))
                reminderManager.cancelPickupAlarms(pickupId)
                _userMessage.value = "Alarma saltada correctamente. No volverá a sonar para este recojo."
            } else {
                reminderManager.cancelPickupAlarms(pickupId)
                _userMessage.value = "Alarma saltada."
            }
            dismissAlarm()
        }
    }

    fun dismissAlarm() {
        reminderManager.stopAlarmSound()
        _activeAlarmEvent.value = null
    }

    fun completePickup(pickup: MedicationPickup) {
        viewModelScope.launch {
            repository.markPickupAsCompleted(pickup)
            _userMessage.value = if (pickup.autoMonthly) {
                "¡Medicamento recogido! Próximo mes programado automáticamente en la misma fecha."
            } else {
                "Recojo marcado como completado."
            }
        }
    }

    fun deletePickup(id: Int) {
        viewModelScope.launch {
            repository.deletePickup(id)
            _userMessage.value = "Recojo eliminado"
        }
    }

    // Lab Exams (CV y CD4)
    fun scheduleLabExam(
        scheduledDate: Long,
        laboratoryName: String,
        notes: String,
        autoSixMonths: Boolean
    ) {
        val patientId = _selectedPatientId.value
        viewModelScope.launch {
            val exam = LabExam(
                patientId = patientId,
                scheduledDate = scheduledDate,
                isCompleted = false,
                autoSixMonths = autoSixMonths,
                laboratoryName = laboratoryName,
                doctorNotes = notes
            )
            repository.saveLabExam(exam)
            _userMessage.value = "Examen de Carga Viral y CD4 programado (control cada 6 meses)."
        }
    }

    fun completeLabExamWithResults(
        exam: LabExam,
        viralLoad: Int?,
        isUndetectable: Boolean,
        cd4: Int?,
        cd4Ratio: Float?,
        doctorNotes: String
    ) {
        viewModelScope.launch {
            repository.completeLabExamWithResults(
                exam = exam,
                viralLoad = viralLoad,
                isUndetectable = isUndetectable,
                cd4 = cd4,
                cd4Ratio = cd4Ratio,
                doctorNotes = doctorNotes
            )
            _userMessage.value = if (exam.autoSixMonths) {
                "Resultados guardados de forma segura. Próximo examen semestral programado automáticamente."
            } else {
                "Resultados guardados con éxito."
            }
        }
    }

    fun deleteLabExam(id: Int) {
        viewModelScope.launch {
            repository.deleteLabExam(id)
            _userMessage.value = "Examen eliminado"
        }
    }

    // Adherence
    fun logDoseToday(status: AdherenceStatus, notes: String? = null) {
        val patientId = _selectedPatientId.value
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            repository.logDailyDose(
                patientId = patientId,
                dateStr = todayStr,
                status = status,
                takenTime = if (status == AdherenceStatus.OLVIDADO) null else nowTime,
                notes = notes
            )
            _userMessage.value = when (status) {
                AdherenceStatus.TOMADO_A_TIEMPO -> "¡Excelente! Dosis de hoy registrada a tiempo."
                AdherenceStatus.TOMADO_TARDE -> "Dosis registrada con retraso. ¡Lo importante es no haberla omitido!"
                AdherenceStatus.OLVIDADO -> "Dosis marcada como no tomada. No dupliques la siguiente dosis."
            }
        }
    }

    fun logDoseForDate(dateStr: String, status: AdherenceStatus, notes: String? = null) {
        val patientId = _selectedPatientId.value
        viewModelScope.launch {
            repository.logDailyDose(
                patientId = patientId,
                dateStr = dateStr,
                status = status,
                takenTime = if (status == AdherenceStatus.OLVIDADO) null else "20:00",
                notes = notes
            )
            _userMessage.value = when (status) {
                AdherenceStatus.TOMADO_A_TIEMPO -> "Día $dateStr: Registrado como tomado a tiempo."
                AdherenceStatus.TOMADO_TARDE -> "Día $dateStr: Registrado como tomado con retraso."
                AdherenceStatus.OLVIDADO -> "Día $dateStr: Marcado como no tomado."
            }
        }
    }

    // Vital Signs
    fun recordVitals(
        systolic: Int?,
        diastolic: Int?,
        heartRate: Int?,
        temperature: Float?,
        weightKg: Float?,
        heightCm: Float? = null,
        glucose: Int?,
        oxygenSat: Int?,
        notes: String
    ) {
        val patientId = _selectedPatientId.value
        viewModelScope.launch {
            val vital = VitalSign(
                patientId = patientId,
                timestamp = System.currentTimeMillis(),
                systolic = systolic,
                diastolic = diastolic,
                heartRate = heartRate,
                temperature = temperature,
                weightKg = weightKg,
                heightCm = heightCm,
                glucose = glucose,
                oxygenSaturation = oxygenSat,
                notes = notes
            )
            repository.saveVitalSign(vital)

            // Update patient's current weight and height in profile if provided
            val profile = currentPatient.value
            if (profile != null) {
                val updatedWeight = if (weightKg != null && weightKg > 0) weightKg else profile.weightKg
                val updatedHeight = if (heightCm != null && heightCm > 0) heightCm else profile.heightCm
                if (updatedWeight != profile.weightKg || updatedHeight != profile.heightCm) {
                    repository.updatePatient(profile.copy(weightKg = updatedWeight, heightCm = updatedHeight))
                }
            }
            _userMessage.value = "Signos vitales registrados con éxito."
        }
    }

    fun deleteVitalSign(id: Int) {
        viewModelScope.launch {
            repository.deleteVitalSign(id)
            _userMessage.value = "Registro de signo vital eliminado"
        }
    }

    // Doctor Notes
    fun addDoctorNote(noteText: String, alertLevel: AlertLevel, doctorName: String) {
        val patientId = _selectedPatientId.value
        viewModelScope.launch {
            val note = DoctorNote(
                patientId = patientId,
                doctorName = doctorName,
                timestamp = System.currentTimeMillis(),
                noteText = noteText,
                alertLevel = alertLevel
            )
            repository.saveDoctorNote(note)
            _userMessage.value = "Nota médica agregada al expediente del paciente."
        }
    }

    // Notification Testing Simulator for user convenience
    fun triggerSimulationReminder(type: String) {
        val patient = currentPatient.value ?: return
        when (type) {
            "2_DAYS_PICKUP" -> {
                val title = if (patient.privacyModeEnabled) "Recordatorio de gestión médica" else "🔔 Recojo de Medicamentos en 2 Días"
                val body = if (patient.privacyModeEnabled) {
                    "Tienes una cita en 48h en ${patient.healthCenterName}."
                } else {
                    "Recuerda: En 2 días te corresponde recoger ${patient.medicineName} en ${patient.healthCenterName}."
                }
                reminderManager.showInstantNotification(title, body, ReminderManager.CHANNEL_MEDICATION)
                _userMessage.value = "Notificación de 2 días antes enviada al dispositivo."
            }
            "TODAY_PICKUP" -> {
                val title = if (patient.privacyModeEnabled) "Cita Médica Hoy" else "⏰ ¡Hoy es tu día de Recojo de Medicamentos!"
                val body = if (patient.privacyModeEnabled) {
                    "Acude hoy a tu gestión en ${patient.healthCenterName}."
                } else {
                    "Hoy debes recoger tu tratamiento mensual (${patient.medicineName}). Lleva tu DNI."
                }
                reminderManager.showInstantNotification(title, body, ReminderManager.CHANNEL_MEDICATION)
                _userMessage.value = "Alarma de recojo de hoy enviada al dispositivo."
            }
            "DAILY_DOSE" -> {
                val title = if (patient.privacyModeEnabled) "Hora de tu recordatorio diario" else "💊 Hora de tu Tratamiento (TARV)"
                val body = if (patient.privacyModeEnabled) {
                    "Son las ${patient.dailyDoseTime}, momento de tu hábito diario de salud."
                } else {
                    "Son las ${patient.dailyDoseTime}. Toma tu dosis de ${patient.medicineName} para mantener tu carga viral indetectable."
                }
                reminderManager.showInstantNotification(title, body, ReminderManager.CHANNEL_DAILY_DOSE)
                _userMessage.value = "Recordatorio de toma diaria enviado al dispositivo."
            }
            "LAB_EXAM" -> {
                val title = "🧪 Control Semestral de Laboratorio"
                val body = "Tienes programado tu control de Carga Viral y CD4 en los próximos días."
                reminderManager.showInstantNotification(title, body, ReminderManager.CHANNEL_LAB_EXAMS)
                _userMessage.value = "Recordatorio de examen semestral enviado."
            }
        }
    }

    fun exportViralLoadCd4Report(context: Context, report: com.example.util.MonthlyViralLoadCd4Report, asCsv: Boolean = false) {
        com.example.util.HealthReportGenerator.exportReportViaShareSheet(context, report, asCsv)
        _userMessage.value = if (asCsv) "Exportando reporte de Carga Viral y CD4 en formato CSV..." else "Exportando informe clínico de Carga Viral y CD4..."
    }

    fun copyViralLoadCd4Report(context: Context, report: com.example.util.MonthlyViralLoadCd4Report) {
        val text = com.example.util.HealthReportGenerator.formatAsPlainText(report)
        com.example.util.HealthReportGenerator.copyToClipboard(context, text, "Informe Carga Viral y CD4 - ${report.patient.name}")
        _userMessage.value = "Reporte clínico copiado al portapapeles."
    }

    fun scheduleBatchViralLoadAndCd4(
        healthCenter: String,
        scheduledDateMillis: Long,
        laboratoryName: String,
        notes: String
    ) {
        viewModelScope.launch {
            val all = allPatients.value
            val targetPatients = if (healthCenter.equals("TODOS", ignoreCase = true) || healthCenter.isBlank()) {
                all
            } else {
                all.filter { it.healthCenterName.trim().equals(healthCenter.trim(), ignoreCase = true) }
            }

            if (targetPatients.isEmpty()) {
                _userMessage.value = "No hay pacientes registrados actualmente en $healthCenter"
                return@launch
            }

            val dateStr = SimpleDateFormat("d 'de' MMMM yyyy", Locale("es", "ES")).format(Date(scheduledDateMillis))
            var count = 0

            targetPatients.forEach { patient ->
                val exam = LabExam(
                    patientId = patient.id,
                    scheduledDate = scheduledDateMillis,
                    isCompleted = false,
                    autoSixMonths = true,
                    laboratoryName = laboratoryName.ifBlank { "Laboratorio Central de Referencia" },
                    doctorNotes = notes.ifBlank { "Orden médica emitida por infectología para control de Carga Viral y CD4." }
                )
                repository.saveLabExam(exam)

                repository.saveDoctorNote(
                    DoctorNote(
                        patientId = patient.id,
                        doctorName = "Dra. Sofía Mendoza (Infectología)",
                        timestamp = System.currentTimeMillis(),
                        noteText = "Se programó tu orden de Carga Viral y CD4 para el $dateStr en ${exam.laboratoryName}. ${exam.doctorNotes}",
                        alertLevel = AlertLevel.ATENCION
                    )
                )
                count++
            }

            _userMessage.value = "✅ Programado con éxito: Orden de Carga Viral y CD4 enviada a $count paciente(s) del $healthCenter."

            // Send Firebase Cloud Messaging push reminder to the target health center topic and devices
            FcmNotificationManager.sendViralLoadAppointmentFcmNotification(
                context = getApplication(),
                patientName = "Pacientes del $healthCenter",
                healthCenter = healthCenter,
                appointmentDate = dateStr,
                notes = notes
            )
        }
    }

    fun scheduleSingleViralLoadAndCd4(
        patientId: Int,
        scheduledDateMillis: Long,
        laboratoryName: String,
        notes: String
    ) {
        viewModelScope.launch {
            val exam = LabExam(
                patientId = patientId,
                scheduledDate = scheduledDateMillis,
                isCompleted = false,
                autoSixMonths = true,
                laboratoryName = laboratoryName.ifBlank { "Laboratorio Central de Referencia" },
                doctorNotes = notes.ifBlank { "Control periódico individual de Carga Viral y CD4." }
            )
            repository.saveLabExam(exam)

            val dateStr = SimpleDateFormat("d 'de' MMMM yyyy", Locale("es", "ES")).format(Date(scheduledDateMillis))
            repository.saveDoctorNote(
                DoctorNote(
                    patientId = patientId,
                    doctorName = "Dra. Sofía Mendoza (Infectología)",
                    timestamp = System.currentTimeMillis(),
                    noteText = "Se programó tu orden de Carga Viral y CD4 para el $dateStr en ${exam.laboratoryName}. ${exam.doctorNotes}",
                    alertLevel = AlertLevel.ATENCION
                )
            )

            val targetPatient = allPatients.value.firstOrNull { it.id == patientId } ?: currentPatient.value
            FcmNotificationManager.sendViralLoadAppointmentFcmNotification(
                context = getApplication(),
                patientName = targetPatient?.name ?: "Paciente",
                healthCenter = targetPatient?.healthCenterName ?: "Centro de Salud",
                appointmentDate = dateStr,
                notes = notes
            )

            _userMessage.value = "✅ Orden de Carga Viral y CD4 programada y enviada vía FCM."
        }
    }

    // Direct FCM Cloud Messaging testing actions
    fun triggerFcmMedicationPickupReminder(isDayOfPickup: Boolean = false) {
        val patient = currentPatient.value ?: return
        val pickup = medicationPickups.value.firstOrNull { it.status == PickupStatus.PROGRAMADO }
        val dateStr = pickup?.let { SimpleDateFormat("d 'de' MMMM", Locale("es", "ES")).format(Date(it.scheduledDate)) } ?: "En 48 horas"
        val center = pickup?.healthCenterName?.ifBlank { patient.healthCenterName } ?: patient.healthCenterName

        FcmNotificationManager.sendMedicationPickupFcmNotification(
            context = getApplication(),
            medicineName = patient.medicineName,
            healthCenter = center,
            pickupDate = dateStr,
            isDayOfPickup = isDayOfPickup
        )
        _userMessage.value = if (isDayOfPickup) "📲 FCM: Alarma enviada para el recojo de HOY." else "📲 FCM: Recordatorio de 48h antes enviado."
    }

    fun triggerFcmViralLoadReminder() {
        val patient = currentPatient.value ?: return
        val nextLab = labExams.value.firstOrNull { !it.isCompleted }
        val dateStr = nextLab?.let { SimpleDateFormat("d 'de' MMMM yyyy", Locale("es", "ES")).format(Date(it.scheduledDate)) } ?: "Próximos días"
        val center = patient.healthCenterName

        FcmNotificationManager.sendViralLoadAppointmentFcmNotification(
            context = getApplication(),
            patientName = patient.name,
            healthCenter = center,
            appointmentDate = dateStr,
            notes = nextLab?.doctorNotes ?: "Control periódico de Carga Viral y Conteo CD4"
        )
        _userMessage.value = "📲 FCM: Cita de Carga Viral y CD4 enviada al dispositivo."
    }
}
