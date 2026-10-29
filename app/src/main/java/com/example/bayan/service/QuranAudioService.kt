package com.example.bayan.service

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
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.bayan.util.QuranAudioPlayer

/**
 * Foreground service to keep Quran recitation playing in the background
 * and provide media controls in the system notification drawer.
 */
class QuranAudioService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val CHANNEL_ID = "quran_recitation_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY_PAUSE = "com.example.bayan.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.bayan.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.bayan.ACTION_PREVIOUS"
        const val ACTION_STOP = "com.example.bayan.ACTION_STOP"
        const val ACTION_UPDATE = "com.example.bayan.ACTION_UPDATE"

        const val EXTRA_SURAH_NAME = "extra_surah_name"
        const val EXTRA_AYAH = "extra_ayah"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
        const val EXTRA_GAPLESS = "extra_gapless"

        fun startOrUpdate(
            context: Context,
            surahName: String,
            ayah: Int,
            isPlaying: Boolean,
            isGapless: Boolean
        ) {
            val intent = Intent(context, QuranAudioService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_SURAH_NAME, surahName)
                putExtra(EXTRA_AYAH, ayah)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
                putExtra(EXTRA_GAPLESS, isGapless)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // If background start restriction hits, ignore safely
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, QuranAudioService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val player = QuranAudioPlayer.getInstance(applicationContext)

        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                val state = player.audioState.value
                if (state.isPlaying) {
                    player.pause()
                } else if (state.isPaused) {
                    player.resume()
                }
            }
            ACTION_NEXT -> {
                player.nextAyah()
            }
            ACTION_PREVIOUS -> {
                player.previousAyah()
            }
            ACTION_STOP -> {
                player.stop()
                stopForeground(true)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UPDATE -> {
                val surahName = intent.getStringExtra(EXTRA_SURAH_NAME) ?: "Quran"
                val ayah = intent.getIntExtra(EXTRA_AYAH, 1)
                val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, true)
                val isGapless = intent.getBooleanExtra(EXTRA_GAPLESS, true)
                val notification = buildNotification(surahName, ayah, isPlaying, isGapless)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
        }

        return START_STICKY
    }

    private fun buildNotification(
        surahName: String,
        ayah: Int,
        isPlaying: Boolean,
        isGapless: Boolean
    ): Notification {
        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val activityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = Intent(this, QuranAudioService::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val playPausePendingIntent = PendingIntent.getService(
            this,
            1,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = Intent(this, QuranAudioService::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getService(
            this,
            2,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = Intent(this, QuranAudioService::class.java).apply {
            action = ACTION_PREVIOUS
        }
        val prevPendingIntent = PendingIntent.getService(
            this,
            3,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, QuranAudioService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            4,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        val gaplessText = if (isGapless) " • Gapless Recitation" else ""

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mishary Rashid Alafasy")
            .setContentText("Surah $surahName • Ayah $ayah$gaplessText")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(activityPendingIntent)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Quran Recitation Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground playback controls for Sheikh Mishary Alafasy Quran recitation"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "BayanNotes:QuranAudioWakeLock"
            )?.apply {
                setReferenceCounted(false)
                acquire(3 * 60 * 60 * 1000L) // 3 hours max safeguard
            }
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }

    override fun onDestroy() {
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
