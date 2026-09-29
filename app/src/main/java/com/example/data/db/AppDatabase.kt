package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AdherenceLog
import com.example.data.model.AdherenceStatus
import com.example.data.model.DoctorNote
import com.example.data.model.LabExam
import com.example.data.model.MedicationPickup
import com.example.data.model.MedicationRecord
import com.example.data.model.PatientProfile
import com.example.data.model.PickupStatus
import com.example.data.model.VitalSign
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        PatientProfile::class,
        MedicationRecord::class,
        MedicationPickup::class,
        LabExam::class,
        AdherenceLog::class,
        VitalSign::class,
        DoctorNote::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "salud_vih_database.db"
                ).addCallback(DatabaseCallback(context))
                 .fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                populateInitialData(getInstance(context).appDao())
            }
        }
    }
}

private suspend fun populateInitialData(dao: AppDao) {
    // 1. Initial Patient Profiles across different health centers
    val patient1 = PatientProfile(
        id = 1,
        name = "Juan Carlos Mendoza",
        nickname = "Juan",
        medicalRecordNumber = "HC-98241",
        age = 34,
        gender = "Masculino",
        heightCm = 174f,
        weightKg = 70.0f,
        chronicConditions = "Hipertensión arterial",
        nutritionGoal = "Mantener defensas altas y sistema inmune fuerte",
        privacyModeEnabled = false,
        medicineName = "TLD (Tenofovir 300mg + Lamivudina 300mg + Dolutegravir 50mg)",
        dailyDoseTime = "20:00",
        healthCenterName = "C.S. 25 de Diciembre",
        doctorName = "Dra. Sofía Mendoza (Infectología)"
    )
    dao.insertPatient(patient1)

    val patient2 = PatientProfile(
        id = 2,
        name = "María Elena Flores",
        nickname = "María",
        medicalRecordNumber = "HC-87410",
        age = 29,
        gender = "Femenino",
        heightCm = 160f,
        weightKg = 54.0f,
        chronicConditions = "",
        nutritionGoal = "Ganar masa muscular y energía",
        privacyModeEnabled = false,
        medicineName = "TLD (Tenofovir + Lamivudina + Dolutegravir)",
        dailyDoseTime = "08:00",
        healthCenterName = "C.S. 25 de Diciembre",
        doctorName = "Dra. Sofía Mendoza (Infectología)"
    )
    dao.insertPatient(patient2)

    val patient3 = PatientProfile(
        id = 3,
        name = "Carlos Alberto Ríos",
        nickname = "Carlos",
        medicalRecordNumber = "HC-65230",
        age = 41,
        gender = "Masculino",
        heightCm = 168f,
        weightKg = 73.0f,
        chronicConditions = "Dislipidemia",
        nutritionGoal = "Dieta equilibrada cardiosaludable",
        privacyModeEnabled = false,
        medicineName = "TLD (Tenofovir + Lamivudina + Dolutegravir)",
        dailyDoseTime = "21:00",
        healthCenterName = "C.S. Preventiva Sud",
        doctorName = "Dra. Sofía Mendoza (Infectología)"
    )
    dao.insertPatient(patient3)

    val patient4 = PatientProfile(
        id = 4,
        name = "Rosa Angélica Vaca",
        nickname = "Rosa",
        medicalRecordNumber = "HC-54190",
        age = 36,
        gender = "Femenino",
        heightCm = 163f,
        weightKg = 61.5f,
        chronicConditions = "",
        nutritionGoal = "Mantener sistema inmune fuerte",
        privacyModeEnabled = false,
        medicineName = "TLD (Tenofovir + Lamivudina + Dolutegravir)",
        dailyDoseTime = "19:30",
        healthCenterName = "C.S. 12 de Diciembre",
        doctorName = "Dra. Sofía Mendoza (Infectología)"
    )
    dao.insertPatient(patient4)

    val patient5 = PatientProfile(
        id = 5,
        name = "David Justiniano",
        nickname = "David",
        medicalRecordNumber = "HC-77320",
        age = 32,
        gender = "Masculino",
        heightCm = 175f,
        weightKg = 68.0f,
        chronicConditions = "",
        nutritionGoal = "Aporte proteico y micronutrientes",
        privacyModeEnabled = false,
        medicineName = "TLD (Tenofovir + Lamivudina + Dolutegravir)",
        dailyDoseTime = "20:00",
        healthCenterName = "C.S. Mi Salud",
        doctorName = "Dra. Sofía Mendoza (Infectología)"
    )
    dao.insertPatient(patient5)

    // 2. Initial Medication Pickups: Next pickup in 5 days, previous pickup 25 days ago
    val now = System.currentTimeMillis()
    val calNextPickup = Calendar.getInstance()
    calNextPickup.add(Calendar.DAY_OF_YEAR, 4) // Pickup in 4 days (so 2-day reminder will be in 2 days)
    calNextPickup.set(Calendar.HOUR_OF_DAY, 9)
    calNextPickup.set(Calendar.MINUTE, 0)

    val calPrevPickup = Calendar.getInstance()
    calPrevPickup.add(Calendar.DAY_OF_YEAR, -26)

    dao.insertPickup(
        MedicationPickup(
            patientId = 1,
            scheduledDate = calNextPickup.timeInMillis,
            status = PickupStatus.PROGRAMADO,
            healthCenterName = "Centro de Salud - Farmacia TARV (Ventanilla 3)",
            medicineName = "TLD (1 Frasco de 30 tabletas)",
            quantityDays = 30,
            notes = "Llevar frasco anterior vacío y documento de identidad.",
            autoMonthly = true,
            reminder2DaysBefore = true,
            reminderDayOf = true
        )
    )
    dao.insertPickup(
        MedicationPickup(
            patientId = 1,
            scheduledDate = calPrevPickup.timeInMillis,
            status = PickupStatus.RECOGIDO,
            completedDate = calPrevPickup.timeInMillis,
            healthCenterName = "Centro de Salud - Farmacia TARV (Ventanilla 3)",
            medicineName = "TLD (1 Frasco de 30 tabletas)",
            quantityDays = 30,
            notes = "Entrega mensual completada a tiempo.",
            autoMonthly = true
        )
    )

    // 3. Initial Lab Exams (Carga Viral y CD4)
    val calNextLab = Calendar.getInstance()
    calNextLab.add(Calendar.DAY_OF_YEAR, 45) // Next lab in 45 days

    val calPrevLab1 = Calendar.getInstance()
    calPrevLab1.add(Calendar.MONTH, -5)

    val calPrevLab2 = Calendar.getInstance()
    calPrevLab2.add(Calendar.MONTH, -11)

    dao.insertLabExam(
        LabExam(
            patientId = 1,
            scheduledDate = calNextLab.timeInMillis,
            isCompleted = false,
            autoSixMonths = true,
            laboratoryName = "Laboratorio Central de Referencia",
            doctorNotes = "Orden emitida para control semestral de Carga Viral y recuento CD4."
        )
    )
    dao.insertLabExam(
        LabExam(
            patientId = 1,
            scheduledDate = calPrevLab1.timeInMillis,
            examDate = calPrevLab1.timeInMillis,
            isCompleted = true,
            autoSixMonths = true,
            viralLoadCopies = 0, // Indetectable (<20)
            isUndetectable = true,
            cd4Count = 780,
            cd4Percentage = 36.5f,
            cd4Cd8Ratio = 1.25f,
            laboratoryName = "Laboratorio Central de Referencia",
            doctorNotes = "Resultado extraordinario: Indetectable = Intransmisible (I=I). CD4 en rango de protección inmunológica óptima."
        )
    )
    dao.insertLabExam(
        LabExam(
            patientId = 1,
            scheduledDate = calPrevLab2.timeInMillis,
            examDate = calPrevLab2.timeInMillis,
            isCompleted = true,
            autoSixMonths = true,
            viralLoadCopies = 45,
            isUndetectable = false,
            cd4Count = 590,
            cd4Percentage = 31.0f,
            cd4Cd8Ratio = 0.95f,
            laboratoryName = "Laboratorio Central de Referencia",
            doctorNotes = "Inicio de supresión viral satisfactoria. Continuar con adherencia al 100%."
        )
    )

    // 4. Initial Adherence logs for the last 30 days
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    for (i in 0..28) {
        val calDay = Calendar.getInstance()
        calDay.add(Calendar.DAY_OF_YEAR, -i)
        val dateStr = sdf.format(calDay.time)
        // 96% compliance
        val status = if (i == 14) AdherenceStatus.TOMADO_TARDE else if (i == 22) AdherenceStatus.OLVIDADO else AdherenceStatus.TOMADO_A_TIEMPO
        val takenTime = if (status == AdherenceStatus.OLVIDADO) null else if (status == AdherenceStatus.TOMADO_TARDE) "22:45" else "20:05"
        dao.insertAdherenceLog(
            AdherenceLog(
                patientId = 1,
                dateString = dateStr,
                scheduledTime = "20:00",
                takenTime = takenTime,
                status = status
            )
        )
    }

    // 5. Initial Vital Signs
    val calVital1 = Calendar.getInstance()
    calVital1.add(Calendar.DAY_OF_YEAR, -2)
    dao.insertVitalSign(
        VitalSign(
            patientId = 1,
            timestamp = calVital1.timeInMillis,
            systolic = 118,
            diastolic = 76,
            heartRate = 70,
            temperature = 36.5f,
            weightKg = 70.0f,
            heightCm = 174f,
            glucose = 92,
            oxygenSaturation = 99,
            notes = "Control post-ejercicio, signos estables."
        )
    )

    val calVital2 = Calendar.getInstance()
    calVital2.add(Calendar.DAY_OF_YEAR, -15)
    dao.insertVitalSign(
        VitalSign(
            patientId = 1,
            timestamp = calVital2.timeInMillis,
            systolic = 122,
            diastolic = 80,
            heartRate = 74,
            temperature = 36.6f,
            weightKg = 69.8f,
            heightCm = 174f,
            glucose = 95,
            oxygenSaturation = 98,
            notes = "Presión bien controlada con hábitos y dieta baja en sodio."
        )
    )

    // 6. Initial Doctor Note
    dao.insertDoctorNote(
        DoctorNote(
            patientId = 1,
            doctorName = "Dra. Sofía Mendoza",
            timestamp = calPrevLab1.timeInMillis,
            noteText = "Paciente con excelente adherencia (>95%). Carga viral indetectable y CD4 en 780. Se renueva prescripción de TARV por 3 meses.",
            alertLevel = com.example.data.model.AlertLevel.NORMAL
        )
    )
}
