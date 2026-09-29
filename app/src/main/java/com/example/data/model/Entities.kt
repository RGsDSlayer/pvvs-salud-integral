package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole {
    PACIENTE,
    MEDICO
}

enum class PickupStatus {
    PROGRAMADO,
    RECOGIDO,
    RETRASADO
}

enum class AdherenceStatus {
    TOMADO_A_TIEMPO,
    TOMADO_TARDE,
    OLVIDADO
}

enum class AlertLevel {
    NORMAL,
    ATENCION,
    URGENTE
}

/**
 * Tabla de Pacientes (PatientProfile):
 * Almacena el perfil demográfico, clínico y parámetros del tratamiento TARV.
 */
@Entity(tableName = "patient_profiles")
data class PatientProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 1,
    val name: String = "Juan Pérez",
    val nickname: String = "Juan",
    val medicalRecordNumber: String = "HC-98241",
    val age: Int = 34,
    val gender: String = "Masculino",
    val heightCm: Float = 172f,
    val weightKg: Float = 68.5f,
    val chronicConditions: String = "", // Comma-separated: "Diabetes,Hipertensión"
    val nutritionGoal: String = "Mantener defensas altas y sistema inmune fuerte",
    val privacyModeEnabled: Boolean = false,
    val pinCode: String = "",
    val medicineName: String = "TLD (Tenofovir + Lamivudina + Dolutegravir)",
    val dailyDoseTime: String = "20:00",
    val healthCenterName: String = "Centro de Salud Materno Infantil - Unidad TARV",
    val doctorName: String = "Dra. Sofía Mendoza (Infectología)",
    val diagnosisDate: Long? = null,
    val tarvStartDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Tabla de Medicamentos Prescritos / Catálogo Terapéutico (MedicationRecord):
 * Almacena el esquema antirretroviral (TARV) y profilaxis oportunistas prescritas al paciente.
 */
@Entity(
    tableName = "medication_records",
    foreignKeys = [
        ForeignKey(
            entity = PatientProfile::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patientId"])]
)
data class MedicationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patientId: Int = 1,
    val medicineName: String = "TLD (Tenofovir 300mg + Lamivudina 300mg + Dolutegravir 50mg)",
    val dosage: String = "1 tableta recubierta",
    val frequency: String = "Cada 24 horas (una vez al día)",
    val scheduledTime: String = "20:00",
    val purpose: String = "Esquema Antirretroviral de Gran Actividad (TARV)",
    val instructions: String = "Tomar diariamente a la misma hora con agua, con o sin alimentos.",
    val isActive: Boolean = true,
    val startDate: Long = System.currentTimeMillis(),
    val prescriberDoctor: String = "Dra. Sofía Mendoza (Infectología)",
    val notes: String? = "Esquema de primera línea preferente OMS/MINSA."
)

/**
 * Tabla de Dispensación y Retiro de Medicamentos (MedicationPickup):
 * Almacena las citas y programación de retiro de antirretrovirales en farmacia hospitalaria.
 */
@Entity(
    tableName = "medication_pickups",
    foreignKeys = [
        ForeignKey(
            entity = PatientProfile::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patientId"])]
)
data class MedicationPickup(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patientId: Int = 1,
    val scheduledDate: Long, // Epoch millis
    val status: PickupStatus = PickupStatus.PROGRAMADO,
    val completedDate: Long? = null,
    val healthCenterName: String = "Centro de Salud - Unidad TARV",
    val medicineName: String = "TLD (Tenofovir + Lamivudina + Dolutegravir)",
    val quantityDays: Int = 30,
    val notes: String = "Llevar DNI y carnet de atención",
    val autoMonthly: Boolean = true,
    val reminder2DaysBefore: Boolean = false,
    val reminder1DayBefore: Boolean = true,
    val reminderDayOf: Boolean = true,
    val alarmSkipped: Boolean = false
)

/**
 * Tabla de Resultados de Exámenes CD4 y Carga Viral (LabExam):
 * Almacena el seguimiento virológico e inmunológico del paciente.
 */
@Entity(
    tableName = "lab_exams",
    foreignKeys = [
        ForeignKey(
            entity = PatientProfile::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patientId"])]
)
data class LabExam(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patientId: Int = 1,
    val scheduledDate: Long, // Epoch millis
    val examDate: Long? = null,
    val isCompleted: Boolean = false,
    val autoSixMonths: Boolean = true,
    val viralLoadCopies: Int? = null, // e.g., 0 or 15 for undetectable (<20)
    val isUndetectable: Boolean = true,
    val cd4Count: Int? = null, // cells/mm³
    val cd4Percentage: Float? = null, // e.g. 35%
    val cd4Cd8Ratio: Float? = null, // e.g. 1.2
    val laboratoryName: String = "Laboratorio Central de Referencia",
    val doctorNotes: String = "Excelente respuesta al TARV, mantener adherencia estricta."
)

/**
 * Tabla de Registro de Adherencia Diaria a la Medicación (AdherenceLog):
 * Almacena la confirmación diaria de toma de dosis (a tiempo, tarde u olvidada).
 */
@Entity(
    tableName = "adherence_logs",
    foreignKeys = [
        ForeignKey(
            entity = PatientProfile::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["patientId"]),
        Index(value = ["patientId", "dateString"], unique = true)
    ]
)
data class AdherenceLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patientId: Int = 1,
    val dateString: String, // "YYYY-MM-DD"
    val scheduledTime: String = "20:00",
    val takenTime: String? = null,
    val status: AdherenceStatus = AdherenceStatus.TOMADO_A_TIEMPO,
    val notes: String? = null
)

/**
 * Tabla de Historial de Signos Vitales (VitalSign):
 * Almacena los registros periódicos de presión arterial, frecuencia cardíaca, peso, IMC, etc.
 */
@Entity(
    tableName = "vital_signs",
    foreignKeys = [
        ForeignKey(
            entity = PatientProfile::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patientId"])]
)
data class VitalSign(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patientId: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val systolic: Int? = 120, // mmHg
    val diastolic: Int? = 80, // mmHg
    val heartRate: Int? = 72, // bpm
    val temperature: Float? = 36.6f, // °C
    val weightKg: Float? = 68.5f,
    val heightCm: Float? = 172f, // Talla (cm) para cálculo del IMC
    val glucose: Int? = null, // mg/dL
    val oxygenSaturation: Int? = 98, // %
    val notes: String? = "Control rutinario"
)

/**
 * Tabla de Notas Clínicas del Médico Especialista (DoctorNote):
 * Almacena indicaciones médicas y nivel de alerta para el paciente.
 */
@Entity(
    tableName = "doctor_notes",
    foreignKeys = [
        ForeignKey(
            entity = PatientProfile::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patientId"])]
)
data class DoctorNote(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patientId: Int = 1,
    val doctorName: String = "Dra. Sofía Mendoza",
    val timestamp: Long = System.currentTimeMillis(),
    val noteText: String,
    val alertLevel: AlertLevel = AlertLevel.NORMAL
)
