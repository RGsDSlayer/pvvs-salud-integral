package com.example.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.MainActivity
import com.example.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ReminderManager(private val context: Context) {

    companion object {
        const val CHANNEL_MEDICATION = "channel_medication_pickup_alarm_v2"
        const val CHANNEL_LAB_EXAMS = "channel_lab_exams"
        const val CHANNEL_DAILY_DOSE = "channel_daily_dose"

        const val ACTION_ALARM_PICKUP = "com.example.ACTION_ALARM_PICKUP"
        const val ACTION_SKIP_ALARM = "com.example.ACTION_SKIP_ALARM"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_PICKUP_ID = "extra_pickup_id"

        private var activeRingtone: Ringtone? = null
        private var activeMediaPlayer: MediaPlayer? = null

        fun getAlarmSoundUri(context: Context): Uri {
            return "android.resource://${context.packageName}/${R.raw.alarm_medicine}".toUri()
        }
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Remove legacy channel if present to refresh sound settings
            try {
                notificationManager.deleteNotificationChannel("channel_medication_pickup")
            } catch (_: Exception) {}

            val alarmSoundUri = getAlarmSoundUri(context)
            val alarmAudioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notifAudioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val medChannel = NotificationChannel(
                CHANNEL_MEDICATION,
                "Alarma de Recojo de Medicamentos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Sonido de alarma para el recojo de medicamentos y citas médicas"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
                setSound(alarmSoundUri, alarmAudioAttributes)
            }

            val labChannel = NotificationChannel(
                CHANNEL_LAB_EXAMS,
                "Exámenes de Laboratorio (Carga Viral y CD4)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios de exámenes semestrales de control"
            }

            val doseChannel = NotificationChannel(
                CHANNEL_DAILY_DOSE,
                "Toma Diaria de Tratamiento",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorio para la toma puntual de tu tratamiento"
                enableVibration(true)
                setSound(defaultSoundUri, notifAudioAttributes)
            }

            notificationManager.createNotificationChannel(medChannel)
            notificationManager.createNotificationChannel(labChannel)
            notificationManager.createNotificationChannel(doseChannel)
        }
    }

    fun showInstantNotification(
        title: String,
        message: String,
        channelId: String = CHANNEL_MEDICATION,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt(),
        pickupId: Int = 0
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_PICKUP_ID, pickupId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = if (channelId == CHANNEL_MEDICATION) {
            getAlarmSoundUri(context)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playAlarmSound() {
        try {
            stopAlarmSound()
            // Reproducción en bucle del sonido de alarma de recojo de medicamentos
            val mediaPlayer = MediaPlayer.create(context, R.raw.alarm_medicine)
            if (mediaPlayer != null) {
                mediaPlayer.isLooping = true
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                mediaPlayer.start()
                activeMediaPlayer = mediaPlayer
            } else {
                // Fallback a Ringtone de alarma si MediaPlayer fallara
                val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, alarmSoundUri)
                ringtone.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                ringtone.play()
                activeRingtone = ringtone
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAlarmSound() {
        try {
            activeMediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            activeMediaPlayer = null

            activeRingtone?.stop()
            activeRingtone = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelPickupAlarms(pickupId: Int) {
        stopAlarmSound()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)

        // Cancel alarm 1 day before
        val pi1 = PendingIntent.getBroadcast(
            context,
            pickupId * 10 + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Cancel alarm same day
        val pi2 = PendingIntent.getBroadcast(
            context,
            pickupId * 10 + 2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Cancel alarm 2 days before
        val pi3 = PendingIntent.getBroadcast(
            context,
            pickupId * 10 + 3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.cancel(pi1)
            alarmManager.cancel(pi2)
            alarmManager.cancel(pi3)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(pickupId * 10 + 1)
            notificationManager.cancel(pickupId * 10 + 2)
            notificationManager.cancel(pickupId * 10 + 3)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerTwoDaysBeforeAlarm(
        pickupId: Int,
        medicineName: String,
        centerName: String,
        scheduledDateMillis: Long,
        isPrivacy: Boolean
    ) {
        val dateFormat = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "ES"))
        val dateFormatted = dateFormat.format(Date(scheduledDateMillis))

        val title = if (isPrivacy) {
            "⏰ ALARMA (8:00 AM): Cita Médica en 2 Días"
        } else {
            "🚨 ALARMA (8:00 AM): Recojo de Medicamentos en 2 Días"
        }

        val message = if (isPrivacy) {
            "Alarma 08:00 AM: Tienes tu cita médica en 2 días ($dateFormatted)."
        } else {
            "Alarma 08:00 AM: Faltan 2 días para tu recojo de medicación ($medicineName) en $centerName ($dateFormatted). Lleva tu DNI y receta."
        }

        showInstantNotification(
            title = title,
            message = message,
            channelId = CHANNEL_MEDICATION,
            notificationId = pickupId * 10 + 3,
            pickupId = pickupId
        )

        playAlarmSound()
    }

    fun triggerOneDayBeforeAlarm(
        pickupId: Int,
        medicineName: String,
        centerName: String,
        scheduledDateMillis: Long,
        isPrivacy: Boolean
    ) {
        val dateFormat = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "ES"))
        val dateFormatted = dateFormat.format(Date(scheduledDateMillis))

        val title = if (isPrivacy) {
            "⏰ ALARMA (8:00 AM): Cita Médica Mañana"
        } else {
            "🚨 ALARMA (8:00 AM): Recojo de Medicamentos Mañana"
        }

        val message = if (isPrivacy) {
            "Alarma de las 08:00 AM: Tienes tu cita médica programada para mañana ($dateFormatted)."
        } else {
            "Alarma de las 08:00 AM: Mañana corresponde recoger tu medicación ($medicineName) en $centerName ($dateFormatted). Lleva tu DNI y receta."
        }

        showInstantNotification(
            title = title,
            message = message,
            channelId = CHANNEL_MEDICATION,
            notificationId = pickupId * 10 + 1,
            pickupId = pickupId
        )

        // Sonido de notificación predeterminado del sistema
        playAlarmSound()
    }

    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Sincroniza automáticamente el recojo con el Calendario de Android:
     * - Evento 2 días antes a las 8:00 AM (Aviso previo de preparación)
     * - Evento el mismo día del recojo a las 8:00 AM (Cita médica / Recojo de medicamentos)
     */
    fun syncWithCalendar(
        pickupId: Int,
        pickupTimestamp: Long,
        medicineName: String,
        centerName: String,
        notes: String = "Llevar DNI, carnet y frasco anterior",
        isPrivacyMode: Boolean = false
    ): Boolean {
        if (!hasCalendarPermission()) return false

        return try {
            val projection = arrayOf(CalendarContract.Calendars._ID)
            val uri = CalendarContract.Calendars.CONTENT_URI
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                CalendarContract.Calendars.VISIBLE + " = 1",
                null,
                CalendarContract.Calendars._ID + " ASC"
            )

            var calId: Long? = null
            cursor?.use {
                if (it.moveToFirst()) {
                    calId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
                }
            }

            if (calId == null) {
                val fallbackCursor = context.contentResolver.query(uri, projection, null, null, null)
                fallbackCursor?.use {
                    if (it.moveToFirst()) {
                        calId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
                    }
                }
            }

            if (calId == null) return false

            val timeZone = TimeZone.getDefault().id

            // 1. Evento el mismo día del recojo a las 8:00 AM
            val calDayOf = Calendar.getInstance().apply {
                timeInMillis = pickupTimestamp
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val titleDayOf = if (isPrivacyMode) {
                "Cita Médica Programada"
            } else {
                "Recojo de Medicamentos TARV ($medicineName)"
            }
            val descDayOf = if (isPrivacyMode) {
                "Acudir al centro asignado: $centerName. $notes"
            } else {
                "Recoger tratamiento mensual de $medicineName en $centerName.\nRequisitos: $notes."
            }

            val valuesDayOf = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, calDayOf.timeInMillis)
                put(CalendarContract.Events.DTEND, calDayOf.timeInMillis + 60 * 60 * 1000) // 1 hora
                put(CalendarContract.Events.TITLE, titleDayOf)
                put(CalendarContract.Events.DESCRIPTION, descDayOf)
                put(CalendarContract.Events.EVENT_LOCATION, centerName)
                put(CalendarContract.Events.CALENDAR_ID, calId)
                put(CalendarContract.Events.EVENT_TIMEZONE, timeZone)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }

            val eventUri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, valuesDayOf)
            val eventId = eventUri?.lastPathSegment?.toLongOrNull()

            if (eventId != null) {
                val remValues0 = ContentValues().apply {
                    put(CalendarContract.Reminders.EVENT_ID, eventId)
                    put(CalendarContract.Reminders.MINUTES, 0)
                    put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                }
                context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, remValues0)
            }

            // 2. Evento 2 DÍAS ANTES a las 8:00 AM
            val cal2Days = Calendar.getInstance().apply {
                timeInMillis = pickupTimestamp
                add(Calendar.DAY_OF_YEAR, -2)
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val title2Days = if (isPrivacyMode) {
                "⏰ Recordatorio Cita en 2 Días"
            } else {
                "⏰ Recordatorio: Recojo de Medicamentos en 2 Días"
            }
            val desc2Days = if (isPrivacyMode) {
                "Faltan 2 días para tu cita en $centerName."
            } else {
                "Faltan 2 días para el recojo de tu medicación de $medicineName en $centerName.\nVerifica tener tu DNI y frascos vacíos."
            }

            val values2Days = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, cal2Days.timeInMillis)
                put(CalendarContract.Events.DTEND, cal2Days.timeInMillis + 30 * 60 * 1000)
                put(CalendarContract.Events.TITLE, title2Days)
                put(CalendarContract.Events.DESCRIPTION, desc2Days)
                put(CalendarContract.Events.EVENT_LOCATION, centerName)
                put(CalendarContract.Events.CALENDAR_ID, calId)
                put(CalendarContract.Events.EVENT_TIMEZONE, timeZone)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }

            val eventUri2 = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values2Days)
            val eventId2 = eventUri2?.lastPathSegment?.toLongOrNull()
            if (eventId2 != null) {
                val remValues2 = ContentValues().apply {
                    put(CalendarContract.Reminders.EVENT_ID, eventId2)
                    put(CalendarContract.Reminders.MINUTES, 0)
                    put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                }
                context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, remValues2)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Sincroniza con la aplicación de Reloj del Sistema:
     * Dispara la creación de alarma a las 8:00 AM mediante AlarmClock.ACTION_SET_ALARM
     */
    fun syncWithSystemClock(
        medicineName: String,
        centerName: String,
        isPrivacyMode: Boolean = false
    ): Boolean {
        return try {
            val msg = if (isPrivacyMode) "Cita Médica 8:00 AM" else "Recojo Medicación TARV 8:00 AM"
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, 8)
                putExtra(AlarmClock.EXTRA_MINUTES, 0)
                putExtra(AlarmClock.EXTRA_MESSAGE, msg)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun schedulePickupAlarms(
        pickupId: Int,
        pickupTimestamp: Long,
        medicineName: String,
        centerName: String,
        notes: String = "Llevar DNI y carnet",
        isPrivacyMode: Boolean = false
    ) {
        // 1. Alarma DOS DÍAS ANTES a las 8:00 AM
        val cal2DaysBefore = Calendar.getInstance().apply {
            timeInMillis = pickupTimestamp
            add(Calendar.DAY_OF_YEAR, -2)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (cal2DaysBefore.timeInMillis > System.currentTimeMillis()) {
            val title2Days = if (isPrivacyMode) {
                "⏰ ALARMA (8:00 AM): Cita Médica en 2 Días"
            } else {
                "🚨 ALARMA (8:00 AM): Recojo de Medicamentos en 2 Días"
            }
            val msg2Days = if (isPrivacyMode) {
                "Recordatorio 8:00 AM: En 2 días tienes tu cita programada en $centerName."
            } else {
                "Recordatorio 8:00 AM: Faltan 2 días para recoger tu medicación ($medicineName) en $centerName. Prepara tu carnet y receta."
            }
            scheduleAlarm(pickupId * 10 + 3, cal2DaysBefore.timeInMillis, title2Days, msg2Days, pickupId)
        }

        // 2. Alarma UN DÍA ANTES a las 8:00 AM
        val cal1DayBefore = Calendar.getInstance().apply {
            timeInMillis = pickupTimestamp
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (cal1DayBefore.timeInMillis > System.currentTimeMillis()) {
            val title1Day = if (isPrivacyMode) {
                "⏰ ALARMA (8:00 AM): Cita Médica Mañana"
            } else {
                "🔔 ALARMA (8:00 AM): Recojo de Medicamentos Mañana"
            }
            val msg1Day = if (isPrivacyMode) {
                "Recordatorio 8:00 AM: Mañana tienes tu cita programada en $centerName."
            } else {
                "Recordatorio 8:00 AM: Mañana debes recoger tu tratamiento ($medicineName) en $centerName."
            }
            scheduleAlarm(pickupId * 10 + 1, cal1DayBefore.timeInMillis, title1Day, msg1Day, pickupId)
        }

        // 3. Alarma EL MISMO DÍA DEL RECOJO a las 8:00 AM
        val calDayOf = Calendar.getInstance().apply {
            timeInMillis = pickupTimestamp
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calDayOf.timeInMillis > System.currentTimeMillis()) {
            val titleDay = if (isPrivacyMode) {
                "⏰ ALARMA (8:00 AM): Cita Médica Hoy"
            } else {
                "🚨 ALARMA (8:00 AM): ¡Hoy es tu día de Recojo de Medicamentos!"
            }
            val msgDay = if (isPrivacyMode) {
                "Es momento de acudir hoy a tu cita en $centerName."
            } else {
                "Hoy debes recoger tu tratamiento ($medicineName) en $centerName. Acude con tu DNI y frasco anterior."
            }
            scheduleAlarm(pickupId * 10 + 2, calDayOf.timeInMillis, titleDay, msgDay, pickupId)
        }

        // Auto-sincronización con calendario si se tiene permiso
        if (hasCalendarPermission()) {
            syncWithCalendar(pickupId, pickupTimestamp, medicineName, centerName, notes, isPrivacyMode)
        }
    }

    private fun scheduleAlarm(requestCode: Int, timeMillis: Long, title: String, message: String, pickupId: Int = 0) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
            putExtra(EXTRA_NOTIFICATION_ID, requestCode)
            putExtra(EXTRA_PICKUP_ID, pickupId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
            } catch (inner: Exception) {
                inner.printStackTrace()
            }
        }
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (Intent.ACTION_BOOT_COMPLETED == action) {
            // El dispositivo se ha reiniciado; acciones de restauración si aplica
            return
        }

        val title = intent.getStringExtra(ReminderManager.EXTRA_TITLE) ?: "Recordatorio de Salud"
        val message = intent.getStringExtra(ReminderManager.EXTRA_MESSAGE) ?: "Tienes una actividad médica programada."
        val notifId = intent.getIntExtra(ReminderManager.EXTRA_NOTIFICATION_ID, 100)
        val pickupId = intent.getIntExtra(ReminderManager.EXTRA_PICKUP_ID, 0)

        val reminderManager = ReminderManager(context)
        reminderManager.showInstantNotification(title, message, ReminderManager.CHANNEL_MEDICATION, notifId, pickupId)
        reminderManager.playAlarmSound()
    }
}
