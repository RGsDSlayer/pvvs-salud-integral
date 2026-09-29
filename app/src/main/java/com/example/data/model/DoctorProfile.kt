package com.example.data.model

/**
 * Constante del enlace al servidor central en Google Drive proporcionado por el usuario
 */
const val SERVER_DRIVE_FOLDER_URL = "https://drive.google.com/drive/folders/1TPWAYtDaW15e2eMQH06hIrZJUjGt_FIG?usp=drive_link"

/**
 * Perfil y registro del Médico en la aplicación
 */
data class DoctorProfile(
    val id: Int = 1,
    val fullName: String = "Dr. Roberto Carlos Mendoza Silva",
    val cmpNumber: String = "CMP 48291",
    val rneNumber: String = "RNE 19482",
    val specialty: String = "Infectología y Tratamiento TARV",
    val healthCenterName: String = "C.S. San Juan de Miraflores",
    val email: String = "dr.mendoza.infectologia@minsa.gob.pe",
    val phone: String = "+51 987 654 321",
    val registeredDate: Long = System.currentTimeMillis(),
    val serverSyncUrl: String = SERVER_DRIVE_FOLDER_URL
)

/**
 * Registro de auditoría para cada inicio de sesión de médicos
 */
data class DoctorLoginRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val doctorName: String,
    val cmpNumber: String,
    val specialty: String,
    val healthCenterName: String,
    val loginTimestamp: Long = System.currentTimeMillis(),
    val remembered: Boolean = true,
    val serverDestinationUrl: String = SERVER_DRIVE_FOLDER_URL
) {
    val timestamp: Long get() = loginTimestamp
}
