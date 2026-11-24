package com.habitnudge.app.reminder.alarm

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import com.habitnudge.app.data.local.dao.HabitDao
import com.habitnudge.app.data.local.entity.HabitEntity
import com.habitnudge.app.data.preferences.UserPreferencesRepository
import com.habitnudge.app.reminder.HabitNotificationRegistry
import com.habitnudge.app.reminder.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HabitAlarmSoundService : Service() {

    @Inject
    lateinit var habitDao: HabitDao

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private val handler = Handler(Looper.getMainLooper())
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var currentHabitId: String? = null
    private var currentReminderId: Long = -1L
    private var currentNotificationId: Int = 0

    companion object {
        const val ACTION_START_ALARM = "com.habitnudge.app.ACTION_START_ALARM"
        const val ACTION_STOP_ALARM = "com.habitnudge.app.ACTION_STOP_ALARM"

        const val EXTRA_MAX_DURATION = "extra_max_duration"
        const val EXTRA_VOLUME = "extra_volume"
        const val EXTRA_TONE_URI = "extra_tone_uri"
        const val EXTRA_VIBRATION = "extra_vibration"
        const val EXTRA_IS_TEST = "extra_is_test"

        @Volatile
        var isRinging: Boolean = false
            private set

        fun startAlarm(
            context: Context,
            habitId: String,
            reminderId: Long = -1L,
            maxDurationSeconds: Int = 60,
            volume: Float = 0.85f,
            toneUri: String? = null,
            vibrationEnabled: Boolean = true,
            isTest: Boolean = false
        ) {
            val intent = Intent(context, HabitAlarmSoundService::class.java).apply {
                action = ACTION_START_ALARM
                putExtra(NotificationHelper.EXTRA_HABIT_ID, habitId)
                putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId)
                putExtra(EXTRA_MAX_DURATION, maxDurationSeconds)
                putExtra(EXTRA_VOLUME, volume)
                putExtra(EXTRA_TONE_URI, toneUri)
                putExtra(EXTRA_VIBRATION, vibrationEnabled)
                putExtra(EXTRA_IS_TEST, isTest)
            }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stopAlarm(context: Context) {
            val intent = Intent(context, HabitAlarmSoundService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_STOP_ALARM) {
            stopAlarmPlayback()
            stopSelf()
            return START_NOT_STICKY
        }

        if (action == ACTION_START_ALARM) {
            val habitId = intent.getStringExtra(NotificationHelper.EXTRA_HABIT_ID) ?: return START_NOT_STICKY
            val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
            val maxDurationSeconds = intent.getIntExtra(EXTRA_MAX_DURATION, 60).coerceAtLeast(5)
            val volume = intent.getFloatExtra(EXTRA_VOLUME, 0.85f).coerceIn(0f, 1f)
            val toneUri = intent.getStringExtra(EXTRA_TONE_URI)
            val vibrationEnabled = intent.getBooleanExtra(EXTRA_VIBRATION, true)
            val isTest = intent.getBooleanExtra(EXTRA_IS_TEST, false)

            currentHabitId = habitId
            currentReminderId = reminderId
            val notificationId = if (isTest) NotificationHelper.TEST_NOTIFICATION_ID else NotificationHelper.getNotificationIdForHabit(habitId)
            currentNotificationId = notificationId

            serviceScope.launch {
                val habit = habitDao.getHabitById(habitId) ?: run {
                    val identity = HabitNotificationRegistry.getIdentity(habitId)
                    HabitEntity(
                        id = identity.habitId,
                        title = identity.title,
                        tagline = "Habit Reminder",
                        description = identity.message,
                        durationDays = 21,
                        tasksJson = "[]",
                        category = identity.category,
                        accentColorHex = identity.accentColorHex
                    )
                }

                val notification = notificationHelper.buildAlarmNotification(
                    habit = habit,
                    reminderId = reminderId,
                    notificationId = notificationId,
                    isRinging = true
                )

                // Start Foreground Service with mediaPlayback type
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(
                            notificationId,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                        )
                    } else {
                        startForeground(notificationId, notification)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Acquire partial wakelock to keep CPU awake while playing
                acquireWakeLock(maxDurationSeconds)

                // Start looping audio
                startAudioPlayback(toneUri, volume)

                // Start repeating vibration
                if (vibrationEnabled) {
                    startVibration()
                }

                isRinging = true

                // Auto-timeout after maxDurationSeconds
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    onRingingTimeout(habit)
                }, maxDurationSeconds * 1000L)
            }
        }

        return START_NOT_STICKY
    }

    private fun startAudioPlayback(toneUriString: String?, volume: Float) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // Request audio focus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener { /* maintain alarm */ }
                .build()
            audioFocusRequest?.let { audioManager.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }

        // Determine Tone URI
        val uri = resolveAlarmUri(toneUriString)

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                setDataSource(applicationContext, uri)
                isLooping = true
                setVolume(volume, volume)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to default notification sound if alarm sound failed
            try {
                val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(audioAttributes)
                    setDataSource(applicationContext, fallbackUri)
                    isLooping = true
                    setVolume(volume, volume)
                    prepare()
                    start()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun resolveAlarmUri(toneUriString: String?): Uri {
        if (!toneUriString.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(toneUriString)
                if (uri != null) return uri
            } catch (e: Exception) {
                // fall through
            }
        }
        val defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (defaultAlarmUri != null) return defaultAlarmUri

        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    }

    private fun startVibration() {
        try {
            val pattern = longArrayOf(0, 450, 250, 450, 850)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun acquireWakeLock(maxDurationSeconds: Int) {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "habitnudge:alarm_sound_wakelock"
            ).apply {
                acquire((maxDurationSeconds + 5) * 1000L)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onRingingTimeout(habit: HabitEntity) {
        stopAlarmPlayback()

        // Keep the notification visible with static actions (Mark Done, Snooze)
        val stoppedNotification = notificationHelper.buildAlarmNotification(
            habit = habit,
            reminderId = currentReminderId,
            notificationId = currentNotificationId,
            isRinging = false
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(currentNotificationId, stoppedNotification)

        stopSelf()
    }

    private fun stopAlarmPlayback() {
        isRinging = false
        handler.removeCallbacksAndMessages(null)

        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            wakeLock = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        stopAlarmPlayback()
        serviceJob.cancel()
        super.onDestroy()
    }
}
