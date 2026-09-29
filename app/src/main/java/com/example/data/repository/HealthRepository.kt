package com.example.data.repository

import com.example.data.db.AppDao
import com.example.data.model.AdherenceLog
import com.example.data.model.AdherenceStatus
import com.example.data.model.DoctorNote
import com.example.data.model.LabExam
import com.example.data.model.MedicationPickup
import com.example.data.model.MedicationRecord
import com.example.data.model.PatientProfile
import com.example.data.model.PickupStatus
import com.example.data.model.VitalSign
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HealthRepository(private val dao: AppDao) {

    // ==========================================
    // 1. PACIENTES
    // ==========================================
    val allPatients: Flow<List<PatientProfile>> = dao.getAllPatients()

    fun getPatient(id: Int): Flow<PatientProfile?> = dao.getPatientById(id)

    suspend fun getPatientDirect(id: Int): PatientProfile? = dao.getPatientByIdDirect(id)

    suspend fun updatePatient(profile: PatientProfile) = dao.updatePatient(profile)

    suspend fun insertPatient(profile: PatientProfile): Long = dao.insertPatient(profile)

    suspend fun deletePatient(id: Int) = dao.deletePatientById(id)

    // ==========================================
    // 2. REGISTROS DE MEDICAMENTOS (Esquema Terapéutico TARV)
    // ==========================================
    fun getMedications(patientId: Int): Flow<List<MedicationRecord>> = dao.getMedicationsForPatient(patientId)

    fun getActiveMedications(patientId: Int): Flow<List<MedicationRecord>> = dao.getActiveMedicationsForPatient(patientId)

    suspend fun saveMedication(record: MedicationRecord): Long = dao.insertMedicationRecord(record)

    suspend fun updateMedication(record: MedicationRecord) = dao.updateMedicationRecord(record)

    suspend fun deleteMedication(id: Int) = dao.deleteMedicationRecord(id)

    // ==========================================
    // 3. RETIRO Y DISPENSACIÓN DE MEDICAMENTOS
    // ==========================================
    fun getPickups(patientId: Int): Flow<List<MedicationPickup>> = dao.getPickupsForPatient(patientId)

    suspend fun getPickupById(id: Int): MedicationPickup? = dao.getPickupById(id)

    suspend fun savePickup(pickup: MedicationPickup): Long = dao.insertPickup(pickup)

    suspend fun updatePickup(pickup: MedicationPickup) = dao.updatePickup(pickup)

    suspend fun deletePickup(id: Int) = dao.deletePickup(id)

    suspend fun markPickupAsCompleted(pickup: MedicationPickup) {
        val completedPickup = pickup.copy(
            status = PickupStatus.RECOGIDO,
            completedDate = System.currentTimeMillis()
        )
        dao.updatePickup(completedPickup)

        // Automatically schedule next month if autoMonthly is enabled
        if (pickup.autoMonthly) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = pickup.scheduledDate
            cal.add(Calendar.MONTH, 1)

            val nextPickup = MedicationPickup(
                patientId = pickup.patientId,
                scheduledDate = cal.timeInMillis,
                status = PickupStatus.PROGRAMADO,
                healthCenterName = pickup.healthCenterName,
                medicineName = pickup.medicineName,
                quantityDays = pickup.quantityDays,
                notes = pickup.notes,
                autoMonthly = true,
                reminder1DayBefore = true,
                reminderDayOf = true,
                alarmSkipped = false
            )
            dao.insertPickup(nextPickup)
        }
    }

    // Lab Exams
    fun getLabExams(patientId: Int): Flow<List<LabExam>> = dao.getLabExamsForPatient(patientId)

    fun getLatestLabExam(patientId: Int): Flow<LabExam?> = dao.getLatestLabExamForPatient(patientId)

    suspend fun saveLabExam(exam: LabExam): Long = dao.insertLabExam(exam)

    suspend fun updateLabExam(exam: LabExam) = dao.updateLabExam(exam)

    suspend fun deleteLabExam(id: Int) = dao.deleteLabExam(id)

    suspend fun completeLabExamWithResults(
        exam: LabExam,
        viralLoad: Int?,
        isUndetectable: Boolean,
        cd4: Int?,
        cd4Ratio: Float?,
        doctorNotes: String
    ) {
        val updatedExam = exam.copy(
            isCompleted = true,
            examDate = System.currentTimeMillis(),
            viralLoadCopies = viralLoad,
            isUndetectable = isUndetectable,
            cd4Count = cd4,
            cd4Cd8Ratio = cd4Ratio,
            doctorNotes = doctorNotes
        )
        dao.updateLabExam(updatedExam)

        // If autoSixMonths is enabled, automatically schedule next exam 6 months from now
        if (exam.autoSixMonths) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = exam.scheduledDate
            cal.add(Calendar.MONTH, 6)

            val nextExam = LabExam(
                patientId = exam.patientId,
                scheduledDate = cal.timeInMillis,
                isCompleted = false,
                autoSixMonths = true,
                laboratoryName = exam.laboratoryName,
                doctorNotes = "Programación automática semestral de Carga Viral y CD4."
            )
            dao.insertLabExam(nextExam)
        }
    }

    // Adherence
    fun getAdherenceLogs(patientId: Int): Flow<List<AdherenceLog>> = dao.getAdherenceLogsForPatient(patientId)

    suspend fun logDailyDose(
        patientId: Int,
        dateStr: String,
        status: AdherenceStatus,
        takenTime: String?,
        notes: String? = null
    ) {
        val existing = dao.getAdherenceLogByDate(patientId, dateStr)
        val finalTakenTime = if (status == AdherenceStatus.OLVIDADO) null else (takenTime ?: SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
        if (existing != null) {
            dao.updateAdherenceLog(
                existing.copy(
                    status = status,
                    takenTime = finalTakenTime,
                    notes = notes
                )
            )
        } else {
            dao.insertAdherenceLog(
                AdherenceLog(
                    patientId = patientId,
                    dateString = dateStr,
                    scheduledTime = "20:00",
                    takenTime = finalTakenTime,
                    status = status,
                    notes = notes
                )
            )
        }
    }

    // Vital Signs
    fun getVitalSigns(patientId: Int): Flow<List<VitalSign>> = dao.getVitalSignsForPatient(patientId)

    fun getLatestVitalSign(patientId: Int): Flow<VitalSign?> = dao.getLatestVitalSignForPatient(patientId)

    suspend fun saveVitalSign(vitalSign: VitalSign): Long = dao.insertVitalSign(vitalSign)

    suspend fun deleteVitalSign(id: Int) = dao.deleteVitalSign(id)

    // Doctor Notes
    fun getDoctorNotes(patientId: Int): Flow<List<DoctorNote>> = dao.getDoctorNotesForPatient(patientId)

    suspend fun saveDoctorNote(note: DoctorNote): Long = dao.insertDoctorNote(note)
}
