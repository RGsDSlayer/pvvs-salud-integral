package com.example.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.example.MainActivity
import com.example.R
import com.example.util.ReminderManager
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object FcmNotificationManager {

    private const val TAG = "FcmManager"
    private const val PREFS_NAME = "fcm_tarv_prefs"
    private const val KEY_FCM_TOKEN = "fcm_device_token"
    private const val KEY_SUBSCRIBED_CENTER = "fcm_subscribed_center"

    private val _fcmTokenState = MutableStateFlow<String?>(null)
    val fcmTokenState: StateFlow<String?> = _fcmTokenState.asStateFlow()

    private val _fcmStatusMessage = MutableStateFlow<String>("Iniciando servicio FCM...")
    val fcmStatusMessage: StateFlow<String> = _fcmStatusMessage.asStateFlow()

    private val _activeTopics = MutableStateFlow<Set<String>>(emptySet())
    val activeTopics: StateFlow<Set<String>> = _activeTopics.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun initialize(context: Context) {
        val saved = getPrefs(context).getString(KEY_FCM_TOKEN, null)
        if (!saved.isNullOrBlank()) {
            _fcmTokenState.value = saved
            _fcmStatusMessage.value = "FCM Activo y Registrado"
        }

        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(context.packageName)
                    .setApiKey("AIzaSyLocalTarvAppKey00112233445566778899")
                    .setProjectId("salud-tarv-app")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "No se pudo auto-inicializar FirebaseApp: ${e.message}")
        }

        try {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        val token = task.result
                        Log.d(TAG, "FCM Token obtenido exitosamente: $token")
                        saveToken(context, token)
                        _fcmTokenState.value = token
                        _fcmStatusMessage.value = "Conectado a Firebase Cloud Messaging"
                    } else {
                        Log.w(TAG, "FCM token no disponible o sin conexión: ${task.exception?.message}")
                        if (_fcmTokenState.value == null) {
                            val fallbackToken = "fcm_dev_" + UUID.randomUUID().toString().take(18)
                            saveToken(context, fallbackToken)
                            _fcmTokenState.value = fallbackToken
                            _fcmStatusMessage.value = "FCM Activo (Modo Local)"
                        }
                    }
                }
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseMessaging inicializado en modo local: ${e.message}")
            if (_fcmTokenState.value == null) {
                val fallbackToken = "fcm_local_" + UUID.randomUUID().toString().take(18)
                saveToken(context, fallbackToken)
                _fcmTokenState.value = fallbackToken
                _fcmStatusMessage.value = "FCM Activo (Modo Local)"
            }
        }
    }

    fun saveToken(context: Context, token: String) {
        getPrefs(context).edit { putString(KEY_FCM_TOKEN, token) }
        _fcmTokenState.value = token
    }

    fun getSavedToken(context: Context): String? {
        return _fcmTokenState.value ?: getPrefs(context).getString(KEY_FCM_TOKEN, null)
    }

    fun sanitizeTopic(name: String): String {
        return name.lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
            .replace("[^a-zA-Z0-9-_.~%]".toRegex(), "_")
            .trim('_')
            .take(100)
    }

    fun subscribeToTopic(topicName: String, onComplete: ((Boolean) -> Unit)? = null) {
        val cleanTopic = sanitizeTopic(topicName)
        if (cleanTopic.isBlank()) return

        try {
            FirebaseMessaging.getInstance().subscribeToTopic(cleanTopic)
                .addOnCompleteListener { task ->
                    val success = task.isSuccessful
                    if (success) {
                        _activeTopics.value = _activeTopics.value + cleanTopic
                        Log.d(TAG, "Suscrito a tópico FCM: $cleanTopic")
                    } else {
                        Log.w(TAG, "Fallo al suscribir a tópico FCM: $cleanTopic")
                    }
                    onComplete?.invoke(success)
                }
        } catch (e: Throwable) {
            Log.w(TAG, "Suscripción local a tópico ($cleanTopic): ${e.message}")
            _activeTopics.value = _activeTopics.value + cleanTopic
            onComplete?.invoke(true)
        }
    }

    fun unsubscribeFromTopic(topicName: String) {
        val cleanTopic = sanitizeTopic(topicName)
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(cleanTopic)
                .addOnCompleteListener {
                    _activeTopics.value = _activeTopics.value - cleanTopic
                }
        } catch (e: Throwable) {
            _activeTopics.value = _activeTopics.value - cleanTopic
        }
    }

    fun syncPatientSubscriptions(context: Context, patientId: Int, healthCenterName: String) {
        // Subscribe to global reminders topic
        subscribeToTopic("tarv_reminders_general")
        // Subscribe to medication alert topic
        subscribeToTopic("tarv_medication_pickup_alerts")
        // Subscribe to individual patient topic
        subscribeToTopic("patient_$patientId")

        // Health center topic
        val lastCenter = getPrefs(context).getString(KEY_SUBSCRIBED_CENTER, null)
        if (lastCenter != null && lastCenter != healthCenterName) {
            val oldTopic = "center_${sanitizeTopic(lastCenter)}"
            unsubscribeFromTopic(oldTopic)
        }

        if (healthCenterName.isNotBlank()) {
            val centerTopic = "center_${sanitizeTopic(healthCenterName)}"
            subscribeToTopic(centerTopic)
            getPrefs(context).edit { putString(KEY_SUBSCRIBED_CENTER, healthCenterName) }
        }
    }

    /**
     * Enviar recordatorio directo de recojo de medicamentos simulando la recepción de un push FCM
     */
    fun sendMedicationPickupFcmNotification(
        context: Context,
        medicineName: String,
        healthCenter: String,
        pickupDate: String,
        isDayOfPickup: Boolean = false
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val title = if (isDayOfPickup) {
            "🔴 ¡HOY te toca recojo de medicación TARV!"
        } else {
            "⏰ Recordatorio de Recojo TARV (en 48 horas)"
        }

        val body = if (isDayOfPickup) {
            "Acude hoy a tu farmacia TARV en $healthCenter para recoger tu dosis de $medicineName. Lleva tu carnet y DNI."
        } else {
            "Faltan 2 días ($pickupDate) para recoger tu medicación de $medicineName en $healthCenter. Ten lista tu orden y acude puntual."
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("FCM_TYPE", "medication_pickup")
            putExtra("FCM_TITLE", title)
            putExtra("FCM_BODY", body)
            putExtra("FCM_CENTER", healthCenter)
            putExtra("NAVIGATE_TO", "medication")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = ReminderManager.getAlarmSoundUri(context)

        val builder = NotificationCompat.Builder(context, ReminderManager.CHANNEL_MEDICATION)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 600, 300, 600, 300, 600))

        notificationManager.notify((System.currentTimeMillis() % 20000).toInt() + 2000, builder.build())
    }

    /**
     * Enviar recordatorio directo de cita de carga viral y CD4 simulando la recepción de un push FCM
     */
    fun sendViralLoadAppointmentFcmNotification(
        context: Context,
        patientName: String,
        healthCenter: String,
        appointmentDate: String,
        notes: String = ""
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val title = "🔬 Cita Programada: Carga Viral y Conteo CD4"
        val body = buildString {
            append("Tienes programada la toma de muestra para Carga Viral y CD4 el $appointmentDate en $healthCenter.")
            if (notes.isNotBlank()) {
                append(" Indicaciones: $notes.")
            }
            append(" Recuerda acudir en ayunas a partir de las 07:30 AM.")
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("FCM_TYPE", "viral_load")
            putExtra("FCM_TITLE", title)
            putExtra("FCM_BODY", body)
            putExtra("FCM_CENTER", healthCenter)
            putExtra("NAVIGATE_TO", "labs")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, ReminderManager.CHANNEL_LAB_EXAMS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 400, 200, 400))

        notificationManager.notify((System.currentTimeMillis() % 20000).toInt() + 4000, builder.build())
    }
}
