package com.example.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.util.ReminderManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class TarvFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "TarvFCMService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM generado: $token")
        FcmNotificationManager.saveToken(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensaje FCM recibido de: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val type = data["type"] ?: "general"
        val title = notification?.title ?: data["title"] ?: "Recordatorio del Programa TARV"
        val body = notification?.body ?: data["body"] ?: data["message"] ?: "Tienes una notificación importante sobre tu tratamiento."
        val centerName = data["center_name"] ?: ""
        val targetRoute = data["target_route"]

        showPushNotification(
            context = applicationContext,
            type = type,
            title = title,
            body = body,
            centerName = centerName,
            targetRoute = targetRoute
        )
    }

    private fun showPushNotification(
        context: Context,
        type: String,
        title: String,
        body: String,
        centerName: String,
        targetRoute: String?
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = when (type) {
            "medication_pickup" -> ReminderManager.CHANNEL_MEDICATION
            "viral_load", "cd4_test", "lab_exam" -> ReminderManager.CHANNEL_LAB_EXAMS
            else -> ReminderManager.CHANNEL_MEDICATION
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("FCM_TYPE", type)
            putExtra("FCM_TITLE", title)
            putExtra("FCM_BODY", body)
            if (centerName.isNotBlank()) {
                putExtra("FCM_CENTER", centerName)
            }
            if (!targetRoute.isNullOrBlank()) {
                putExtra("NAVIGATE_TO", targetRoute)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = if (type == "medication_pickup") {
            ReminderManager.getAlarmSoundUri(context)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        val notificationId = (System.currentTimeMillis() % 20000).toInt() + 1000

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 250, 500))

        notificationManager.notify(notificationId, builder.build())
    }
}
