package com.example.counter_app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.counter_app.MainActivity
import com.example.counter_app.R

/**
 * Manejador centralizado de notificaciones para la aplicación.
 *
 * Gestiona la creación de canales de notificaciones y el envío de notificaciones
 * para eventos importantes como desconexiones de dispositivos.
 */
class NotificationHandler(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "device_alerts"
        private const val CHANNEL_NAME = "Alertas de Dispositivos"
        private const val CHANNEL_DESCRIPTION = "Notificaciones sobre el estado de los dispositivos"
        private const val NOTIFICATION_ID_DISCONNECTION = 1001
    }

    init {
        createNotificationChannel()
    }

    /**
     * Crea el canal de notificaciones para Android 8.0+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Muestra una notificación cuando un dispositivo se desconecta.
     *
     * @param deviceName Nombre del dispositivo que se desconectó
     */
    fun showDisconnectionNotification(deviceName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent para abrir la app cuando se toque la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Construir la notificación
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("⚠️ Dispositivo desconectado")
            .setContentText(deviceName)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("El dispositivo '$deviceName' ha perdido la conexión. Revisa el estado del sensor."))
            .setSmallIcon(android.R.drawable.stat_notify_error) // Ícono de sistema
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()

        notificationManager.notify(NOTIFICATION_ID_DISCONNECTION, notification)
    }

    /**
     * Verifica si el canal de notificaciones está habilitado.
     */
    fun areNotificationsEnabled(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            return channel != null && channel.importance != NotificationManager.IMPORTANCE_NONE
        }

        return notificationManager.areNotificationsEnabled()
    }
}
