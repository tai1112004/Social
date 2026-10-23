package com.taihoang.socialmobile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

internal fun shouldRunCallForegroundService(phase: String): Boolean = phase.uppercase() != "IDLE"

class CallForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val phase = intent?.getStringExtra(EXTRA_PHASE) ?: return START_NOT_STICKY
        val callType = intent.getStringExtra(EXTRA_CALL_TYPE) ?: "AUDIO"
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Cuoc goi"
        val incoming = phase.equals("INCOMING", ignoreCase = true)
        val notification = buildNotification(title, phase, callType, incoming)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val type = if (incoming) {
                    0
                } else {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
                        if (callType.equals("VIDEO", ignoreCase = true)) ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA else 0
                }
                startForeground(NOTIFICATION_ID, notification, type)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (error: SecurityException) {
            android.util.Log.e("SocialCall", "Cannot start call foreground service: missing permission", error)
            stopSelf()
        } catch (error: IllegalArgumentException) {
            android.util.Log.e("SocialCall", "Cannot start call foreground service: invalid type", error)
            stopSelf()
        } catch (error: RuntimeException) {
            android.util.Log.e("SocialCall", "Cannot start call foreground service", error)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(title: String, phase: String, callType: String, incoming: Boolean): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_OPEN,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = when {
            incoming -> "Cuoc goi ${if (callType.equals("VIDEO", true)) "video" else "audio"} den"
            phase.equals("RECONNECTING", true) -> "Dang khoi phuc ket noi"
            else -> "Cuoc goi dang dien ra"
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle(title)
            .setContentText(text)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(if (incoming) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_LOW)
            .setOngoing(!incoming)
            .setAutoCancel(false)
            .setContentIntent(openPendingIntent)
            .setFullScreenIntent(openPendingIntent, incoming)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Cuoc goi",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Thong bao cuoc goi audio va video"
            setSound(null, null)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "social_calls"
        private const val NOTIFICATION_ID = 4101
        private const val REQUEST_OPEN = 4102
        private const val ACTION_START = "com.taihoang.socialmobile.action.START_CALL"
        private const val ACTION_STOP = "com.taihoang.socialmobile.action.STOP_CALL"
        private const val EXTRA_PHASE = "phase"
        private const val EXTRA_CALL_TYPE = "callType"
        private const val EXTRA_TITLE = "title"

        fun start(context: Context, title: String, phase: String, callType: String) {
            val intent = Intent(context, CallForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_PHASE, phase)
                putExtra(EXTRA_CALL_TYPE, callType)
            }
            runCatching { ContextCompat.startForegroundService(context, intent) }
                .onFailure { error -> android.util.Log.e("SocialCall", "Cannot start call foreground service", error) }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CallForegroundService::class.java))
        }
    }
}
