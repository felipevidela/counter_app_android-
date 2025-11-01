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
        // Canal de alertas de dispositivos
        private const val CHANNEL_ID_DEVICES = "device_alerts"
        private const val CHANNEL_NAME_DEVICES = "Alertas de Dispositivos"
        private const val CHANNEL_DESCRIPTION_DEVICES = "Notificaciones sobre el estado de los dispositivos"

        // Canal de alertas de aforo
        private const val CHANNEL_ID_OCCUPANCY = "occupancy_alerts"
        private const val CHANNEL_NAME_OCCUPANCY = "Alertas de Aforo"
        private const val CHANNEL_DESCRIPTION_OCCUPANCY = "Notificaciones sobre niveles de ocupación"

        // IDs de notificación
        private const val NOTIFICATION_ID_DISCONNECTION = 1001
        private const val NOTIFICATION_ID_LOW_OCCUPANCY = 2001
        private const val NOTIFICATION_ID_HIGH_OCCUPANCY = 2002
        private const val NOTIFICATION_ID_TRAFFIC_PEAK = 2003
    }

    init {
        createNotificationChannels()
    }

    /**
     * Crea los canales de notificaciones para Android 8.0+
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Canal para alertas de dispositivos
            val devicesChannel = NotificationChannel(
                CHANNEL_ID_DEVICES,
                CHANNEL_NAME_DEVICES,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION_DEVICES
                enableVibration(true)
                enableLights(true)
            }

            // Canal para alertas de aforo
            val occupancyChannel = NotificationChannel(
                CHANNEL_ID_OCCUPANCY,
                CHANNEL_NAME_OCCUPANCY,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION_OCCUPANCY
                enableVibration(false)
                enableLights(true)
            }

            notificationManager.createNotificationChannel(devicesChannel)
            notificationManager.createNotificationChannel(occupancyChannel)
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
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_DEVICES)
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
     * Muestra una notificación de alerta de aforo bajo.
     *
     * @param deviceName Nombre del dispositivo
     * @param currentOccupancy Aforo actual
     * @param threshold Umbral configurado (%)
     */
    fun showLowOccupancyAlert(deviceName: String, currentOccupancy: Int, threshold: Int) {
        val notification = buildOccupancyNotification(
            title = "📉 Aforo Bajo",
            content = "El aforo en '$deviceName' es bajo: $currentOccupancy personas",
            bigText = "El nivel de ocupación en '$deviceName' está por debajo del $threshold% configurado. Actual: $currentOccupancy personas.",
            notificationId = NOTIFICATION_ID_LOW_OCCUPANCY
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_LOW_OCCUPANCY, notification)
    }

    /**
     * Muestra una notificación de alerta de aforo alto.
     *
     * @param deviceName Nombre del dispositivo
     * @param currentOccupancy Aforo actual
     * @param capacity Capacidad máxima
     */
    fun showHighOccupancyAlert(deviceName: String, currentOccupancy: Int, capacity: Int) {
        val percentage = ((currentOccupancy.toFloat() / capacity) * 100).toInt()

        val notification = buildOccupancyNotification(
            title = "📈 Aforo Alto",
            content = "El aforo en '$deviceName' está cerca del límite: $currentOccupancy/$capacity",
            bigText = "El nivel de ocupación en '$deviceName' está al $percentage% de capacidad. Actual: $currentOccupancy de $capacity personas.",
            notificationId = NOTIFICATION_ID_HIGH_OCCUPANCY
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_HIGH_OCCUPANCY, notification)
    }

    /**
     * Muestra una notificación de alerta de pico de tráfico.
     *
     * @param deviceName Nombre del dispositivo
     * @param entriesCount Número de entradas en el período
     */
    fun showTrafficPeakAlert(deviceName: String, entriesCount: Int) {
        val notification = buildOccupancyNotification(
            title = "🚶 Pico de Tráfico",
            content = "Muchas entradas en '$deviceName': $entriesCount en 5 minutos",
            bigText = "Se ha detectado un pico de tráfico en '$deviceName': $entriesCount entradas en los últimos 5 minutos.",
            notificationId = NOTIFICATION_ID_TRAFFIC_PEAK
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_TRAFFIC_PEAK, notification)
    }

    /**
     * Construye una notificación de aforo genérica.
     */
    private fun buildOccupancyNotification(
        title: String,
        content: String,
        bigText: String,
        notificationId: Int
    ): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_ID_OCCUPANCY)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setSmallIcon(android.R.drawable.stat_notify_chat) // Ícono de sistema
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    /**
     * Verifica si el canal de notificaciones está habilitado.
     */
    fun areNotificationsEnabled(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID_DEVICES)
            return channel != null && channel.importance != NotificationManager.IMPORTANCE_NONE
        }

        return notificationManager.areNotificationsEnabled()
    }
}
