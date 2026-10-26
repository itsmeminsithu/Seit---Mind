package org.minsithu.stillmind

import android.app.*
import android.content.*
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import java.time.LocalDate

/** Foreground media player: reward is counted only while sound is actually playing. */
class MeditationService : Service() {
    companion object {
        const val ACTION_START = "org.minsithu.stillmind.START"
        const val ACTION_PAUSE = "org.minsithu.stillmind.PAUSE"
        const val ACTION_RESUME = "org.minsithu.stillmind.RESUME"
        const val ACTION_FINISH = "org.minsithu.stillmind.FINISH"
        const val ACTION_STOP = "org.minsithu.stillmind.STOP"
        const val CHANNEL = "mind_audio"
        const val PREFS = "stillmind_profile"
    }
    private val handler = Handler(Looper.getMainLooper())
    private val audioManager by lazy { getSystemService(AUDIO_SERVICE) as AudioManager }
    private val focusRequest by lazy {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setOnAudioFocusChangeListener { change ->
                if (change < 0) pause()
            }.build()
    }
    private var media: MediaPlayer? = null
    private var targetSeconds = 600
    private var elapsedMs = 0L
    private var lastTick = 0L
    private var active = false
    private var prepared = false
    private var completed = false
    private var fade = true
    private var focusAcquired = false
    private var sessionRunning = false
    private var wantsPause = false

    private val tick = object : Runnable {
        override fun run() {
            if (!active) return
            val now = SystemClock.elapsedRealtime()
            if (media?.isPlaying == true) elapsedMs += (now - lastTick).coerceAtLeast(0)
            lastTick = now
            val remaining = (targetSeconds * 1000L - elapsedMs).coerceAtLeast(0)
            if (fade) {
                val volume = (remaining / 30000f).coerceIn(0f, 1f)
                media?.setVolume(volume, volume)
            }
            prefs().edit().putLong("session_elapsed", elapsedMs).apply()
            if (remaining == 0L) { finish(true); return }
            handler.postDelayed(this, 250)
        }
    }
    private fun prefs() = getSharedPreferences(PREFS, MODE_PRIVATE)
    override fun onCreate() {
        super.onCreate()
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(NotificationChannel(CHANNEL, "Meditation audio", NotificationManager.IMPORTANCE_LOW))
    }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                targetSeconds = intent.getIntExtra("minutes", 10).coerceIn(1, 180) * 60
                fade = intent.getBooleanExtra("fade", true)
                start(intent.getStringExtra("audio") ?: "")
            }
            ACTION_PAUSE -> pause()
            ACTION_RESUME -> resume()
            ACTION_FINISH -> if (sessionRunning) finish(true) else stopSelf()
            ACTION_STOP -> if (sessionRunning) finish(false) else stopSelf()
            else -> if (!sessionRunning) stopSelf()
        }
        return START_NOT_STICKY
    }
    private fun actionIntent(action: String, requestCode: Int): PendingIntent = PendingIntent.getService(
        this, requestCode, Intent(this, MeditationService::class.java).setAction(action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    private fun notification(): Notification {
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("စိတ် - Mind")
            .setContentText(if (active) "Meditation playing" else "Meditation paused or loading")
            .setContentIntent(open)
            .addAction(Notification.Action.Builder(null, if (active) "Pause" else "Resume",
                actionIntent(if (active) ACTION_PAUSE else ACTION_RESUME, 1001)).build())
            .addAction(Notification.Action.Builder(null, "Finish", actionIntent(ACTION_FINISH, 1002)).build())
            .setOngoing(true).build()
    }
    private fun notifyState() {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(302, notification())
    }
    private fun start(url: String) {
        handler.removeCallbacks(tick)
        media?.release(); media = null
        if (focusAcquired) audioManager.abandonAudioFocusRequest(focusRequest)
        focusAcquired = false
        completed = false; active = false; prepared = false; wantsPause = false
        elapsedMs = 0L; sessionRunning = true
        prefs().edit().putLong("session_elapsed", 0L).putInt("session_target", targetSeconds)
            .putBoolean("session_running", true).putBoolean("session_playing", false)
            .putBoolean("session_completed", false).putInt("completed_minutes", 0)
            .putInt("completed_earned", 0).putString("session_error", "").apply()
        // Foreground promotion occurs synchronously before asynchronous audio preparation.
        try { startForeground(302, notification()) }
        catch (e: Exception) { android.util.Log.e("MindAudio", "Foreground start failed", e); finish(false); return }
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            fail("Invalid audio URL"); return
        }
        if (audioManager.requestAudioFocus(focusRequest) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            fail("Audio focus unavailable"); return
        }
        focusAcquired = true
        try {
            val p = MediaPlayer()
            media = p
            p.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            p.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK)
            p.setDataSource(url)
            p.isLooping = true
            p.setOnPreparedListener { mp ->
                if (completed || media !== mp) return@setOnPreparedListener
                prepared = true
                if (!wantsPause) resume()
            }
            p.setOnErrorListener { _, _, _ -> fail("Audio could not be played. Check your connection."); true }
            p.prepareAsync()
        } catch (e: Exception) {
            android.util.Log.e("MindAudio", "Playback failed", e)
            fail("Audio could not be loaded.")
        }
    }
    private fun pause() {
        if (!sessionRunning || completed) return
        wantsPause = true
        if (!active) return
        active = false
        handler.removeCallbacks(tick)
        try { media?.pause() } catch (_: Exception) { }
        prefs().edit().putBoolean("session_playing", false).apply()
        notifyState()
    }
    private fun resume() {
        if (!sessionRunning || active || completed) return
        wantsPause = false
        if (!prepared) return
        try {
            media?.start()
            active = true
            lastTick = SystemClock.elapsedRealtime()
            prefs().edit().putBoolean("session_playing", true).apply()
            handler.removeCallbacks(tick)
            handler.post(tick)
            notifyState()
        } catch (e: Exception) { fail("Unable to resume audio.") }
    }
    private fun fail(message: String) {
        prefs().edit().putString("session_error", message).apply()
        finish(false)
    }
    private fun finish(awardIfEligible: Boolean) {
        if (completed) return
        completed = true; sessionRunning = false; active = false
        handler.removeCallbacks(tick)
        try { media?.release() } catch (_: Exception) { }
        media = null
        if (focusAcquired) { audioManager.abandonAudioFocusRequest(focusRequest); focusAcquired = false }
        val p = prefs()
        val reward = if (awardIfEligible) RewardEngine.complete(
            RewardState(p.getInt("stars", 0), p.getInt("streak", 0),
                p.getString("last_checkin", "") ?: "", p.getInt("stars_today", 0),
                p.getString("stars_date", "") ?: ""), elapsedMs, targetSeconds, LocalDate.now()
        ) else null
        val editor = p.edit().putBoolean("session_running", false)
            .putBoolean("session_playing", false).putLong("session_elapsed", elapsedMs)
        if (reward != null) {
            editor.putInt("stars", reward.state.stars)
                .putInt("streak", reward.state.streak)
                .putString("last_checkin", reward.state.lastCompleted)
                .putInt("stars_today", reward.state.dailyStars)
                .putString("stars_date", reward.state.dailyStarsDate)
                .putInt("completed_minutes", reward.completedMinutes)
                .putInt("completed_earned", reward.earned)
                .putBoolean("session_completed", true)
        } else {
            editor.putInt("completed_minutes", 0).putInt("completed_earned", 0)
                .putBoolean("session_completed", false)
            if (awardIfEligible && p.getString("session_error", "").isNullOrBlank())
                editor.putString("session_error", "Complete at least 80% to earn a reward.")
        }
        editor.apply()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    override fun onDestroy() {
        handler.removeCallbacks(tick)
        try { media?.release() } catch (_: Exception) {}
        if (focusAcquired) audioManager.abandonAudioFocusRequest(focusRequest)
        if (sessionRunning) prefs().edit().putBoolean("session_running", false)
            .putBoolean("session_playing", false).apply()
        super.onDestroy()
    }
}
