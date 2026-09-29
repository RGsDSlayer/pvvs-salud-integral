package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AdherenceLog
import com.example.data.model.DoctorNote
import com.example.data.model.LabExam
import com.example.data.model.MedicationPickup
import com.example.data.model.MedicationRecord
import com.example.data.model.PatientProfile
import com.example.data.model.VitalSign
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ==========================================
    // 1. TABLA PACIENTES (patient_profiles)
    // ==========================================
    @Query("SELECT * FROM patient_profiles ORDER BY id ASC")
    fun getAllPatients(): Flow<List<PatientProfile>>

    @Query("SELECT * FROM patient_profiles WHERE id = :id LIMIT 1")
    fun getPatientById(id: Int): Flow<PatientProfile?>

    @Query("SELECT * FROM patient_profiles WHERE id = :id LIMIT 1")
    suspend fun getPatientByIdDirect(id: Int): PatientProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientProfile): Long

    @Update
    suspend fun updatePatient(patient: PatientProfile)

    @Query("DELETE FROM patient_profiles WHERE id = :id")
    suspend fun deletePatientById(id: Int)

    // ==========================================
    // 2. TABLA REGISTROS DE MEDICAMENTOS PRESCRITOS (medication_records)
    // ==========================================
    @Query("SELECT * FROM medication_records WHERE patientId = :patientId ORDER BY isActive DESC, id ASC")
    fun getMedicationsForPatient(patientId: Int): Flow<List<MedicationRecord>>

    @Query("SELECT * FROM medication_records WHERE patientId = :patientId AND isActive = 1 ORDER BY id ASC")
    fun getActiveMedicationsForPatient(patientId: Int): Flow<List<MedicationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationRecord(record: MedicationRecord): Long

    @Update
    suspend fun updateMedicationRecord(record: MedicationRecord)

    @Query("DELETE FROM medication_records WHERE id = :id")
    suspend fun deleteMedicationRecord(id: Int)

    // ==========================================
    // 3. TABLA DISPENSACIÓN Y RETIRO DE MEDICAMENTOS (medication_pickups)
    // ==========================================
    @Query("SELECT * FROM medication_pickups WHERE patientId = :patientId ORDER BY scheduledDate ASC")
    fun getPickupsForPatient(patientId: Int): Flow<List<MedicationPickup>>

    @Query("SELECT * FROM medication_pickups WHERE id = :id LIMIT 1")
    suspend fun getPickupById(id: Int): MedicationPickup?

    @Query("SELECT * FROM medication_pickups ORDER BY scheduledDate ASC")
    fun getAllPickups(): Flow<List<MedicationPickup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPickup(pickup: MedicationPickup): Long

    @Update
    suspend fun updatePickup(pickup: MedicationPickup)

    @Query("DELETE FROM medication_pickups WHERE id = :id")
    suspend fun deletePickup(id: Int)

    // ==========================================
    // 4. TABLA RESULTADOS DE EXÁMENES CD4 / CARGA VIRAL (lab_exams)
    // ==========================================
    @Query("SELECT * FROM lab_exams WHERE patientId = :patientId ORDER BY scheduledDate ASC")
    fun getLabExamsForPatient(patientId: Int): Flow<List<LabExam>>

    @Query("SELECT * FROM lab_exams WHERE patientId = :patientId AND isCompleted = 1 ORDER BY (CASE WHEN examDate IS NOT NULL THEN examDate ELSE scheduledDate END) DESC LIMIT 1")
    fun getLatestLabExamForPatient(patientId: Int): Flow<LabExam?>

    @Query("SELECT * FROM lab_exams ORDER BY scheduledDate ASC")
    fun getAllLabExams(): Flow<List<LabExam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabExam(exam: LabExam): Long

    @Update
    suspend fun updateLabExam(exam: LabExam)

    @Query("DELETE FROM lab_exams WHERE id = :id")
    suspend fun deleteLabExam(id: Int)

    // ==========================================
    // 5. TABLA REGISTRO DE ADHERENCIA DIARIA (adherence_logs)
    // ==========================================
    @Query("SELECT * FROM adherence_logs WHERE patientId = :patientId ORDER BY dateString DESC")
    fun getAdherenceLogsForPatient(patientId: Int): Flow<List<AdherenceLog>>

    @Query("SELECT * FROM adherence_logs WHERE patientId = :patientId AND dateString = :dateString LIMIT 1")
    suspend fun getAdherenceLogByDate(patientId: Int, dateString: String): AdherenceLog?

    @Query("SELECT * FROM adherence_logs WHERE patientId = :patientId AND dateString BETWEEN :startDate AND :endDate ORDER BY dateString ASC")
    fun getAdherenceLogsInRange(patientId: Int, startDate: String, endDate: String): Flow<List<AdherenceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdherenceLog(log: AdherenceLog): Long

    @Update
    suspend fun updateAdherenceLog(log: AdherenceLog)

    @Query("DELETE FROM adherence_logs WHERE id = :id")
    suspend fun deleteAdherenceLog(id: Int)

    // ==========================================
    // 6. TABLA HISTORIAL DE SIGNOS VITALES (vital_signs)
    // ==========================================
    @Query("SELECT * FROM vital_signs WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getVitalSignsForPatient(patientId: Int): Flow<List<VitalSign>>

    @Query("SELECT * FROM vital_signs WHERE patientId = :patientId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestVitalSignForPatient(patientId: Int): Flow<VitalSign?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitalSign(vitalSign: VitalSign): Long

    @Update
    suspend fun updateVitalSign(vitalSign: VitalSign)

    @Query("DELETE FROM vital_signs WHERE id = :id")
    suspend fun deleteVitalSign(id: Int)

    // ==========================================
    // 7. TABLA NOTAS CLÍNICAS (doctor_notes)
    // ==========================================
    @Query("SELECT * FROM doctor_notes WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getDoctorNotesForPatient(patientId: Int): Flow<List<DoctorNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctorNote(note: DoctorNote): Long
}
