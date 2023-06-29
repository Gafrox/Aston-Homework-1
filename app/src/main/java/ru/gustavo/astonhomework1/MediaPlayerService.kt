package ru.gustavo.astonhomework1

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ru.gustavo.astonhomework1.Const.ACTION_NEXT
import ru.gustavo.astonhomework1.Const.ACTION_PAUSE
import ru.gustavo.astonhomework1.Const.ACTION_PLAY
import ru.gustavo.astonhomework1.Const.ACTION_PREVIOUS
import ru.gustavo.astonhomework1.Const.CHANNEL_ID
import ru.gustavo.astonhomework1.Const.CHANNEL_NAME
import ru.gustavo.astonhomework1.Const.NOTIFICATION_ID

private val musicsList = listOf(R.raw.ty_eto_seryozno, R.raw.moskva_ljubit, R.raw.privychka)
private var musicIndex = 0

class MediaPlayerService : Service(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener {
    private var mediaPlayer: MediaPlayer? = null
    private var currentPosition: Int = 0
    private var totalDuration: Int = 0
    private lateinit var notificationManager: NotificationManager


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        val notification = createNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
        updateSeekBar()
        when (intent.action) {
            ACTION_PLAY -> {
                if (mediaPlayer == null) {
                    initPlayer()
                    onStart()
                } else {
                    onStart()
                }
                updateSeekBar()
            }
            ACTION_PAUSE -> {
                mediaPlayer?.pause()
            }
            ACTION_NEXT -> {
                musicIndex = if (musicIndex + 1 > musicsList.lastIndex) 0 else musicIndex + 1
                if (mediaPlayer?.isPlaying == true) {
                    initPlayer()
                    onStart()
                } else {
                    initPlayer()
                }
            }
            ACTION_PREVIOUS -> {
                musicIndex = if (musicIndex - 1 < 0) musicsList.lastIndex else musicIndex - 1
                if (mediaPlayer?.isPlaying == true) {
                    initPlayer()
                    onStart()
                } else {
                    initPlayer()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initPlayer() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, musicsList[musicIndex])
        mediaPlayer?.apply {
            setOnPreparedListener(this@MediaPlayerService)
            setOnCompletionListener(this@MediaPlayerService)
        }
    }

    override fun onPrepared(mediaPlayer: MediaPlayer?) {
    }

    override fun onCompletion(mediaPlayer: MediaPlayer?) {
        musicIndex = if (musicIndex + 1 > musicsList.lastIndex) 0 else musicIndex + 1
        initPlayer()
        onStart()
    }

    private fun onStart() = mediaPlayer?.start()

    override fun onBind(p0: Intent?): IBinder {
        return PlayerBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    fun isPlaying() = mediaPlayer?.isPlaying
    inner class PlayerBinder : Binder() {
        fun getService() = this@MediaPlayerService
    }

    private fun updateSeekBar() {
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                totalDuration = mediaPlayer?.duration ?: 0
                currentPosition = mediaPlayer?.currentPosition ?: 0
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    fun getCurrentPosition(): Int = currentPosition
    fun getTotalDuration(): Int = totalDuration

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.setShowBadge(false)
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val activityIntent = Intent(this, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            this,
            1,
            activityIntent,
            FLAG_IMMUTABLE
        )
        val playIntent = Intent(this, MediaPlayerService::class.java)
        playIntent.action = ACTION_PLAY
        val playPendingIntent = PendingIntent.getService(this, 0, playIntent, FLAG_IMMUTABLE)
        val playAction = NotificationCompat.Action.Builder(
            R.drawable.play,
            "Play",
            playPendingIntent
        ).build()

        val pauseIntent = Intent(this, MediaPlayerService::class.java)
        pauseIntent.action = ACTION_PAUSE
        val pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, FLAG_IMMUTABLE)
        val pauseAction = NotificationCompat.Action.Builder(
            R.drawable.pause,
            "Pause",
            pausePendingIntent
        ).build()

        val nextIntent = Intent(this, MediaPlayerService::class.java)
        nextIntent.action = ACTION_NEXT
        val nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, FLAG_IMMUTABLE)
        val nextAction = NotificationCompat.Action.Builder(
            R.drawable.next,
            "Next",
            nextPendingIntent
        ).build()

        val previousIntent = Intent(this, MediaPlayerService::class.java)
        previousIntent.action = ACTION_PREVIOUS
        val previousPendingIntent =
            PendingIntent.getService(this, 0, previousIntent, FLAG_IMMUTABLE)
        val previousAction = NotificationCompat.Action.Builder(
            R.drawable.previous,
            "Previous",
            previousPendingIntent
        ).build()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.play)
            .setContentTitle("Media Player")
            .setOngoing(true)
            .setContentIntent(activityPendingIntent)
            .addAction(previousAction)
            .addAction(playAction)
            .addAction(pauseAction)
            .addAction(nextAction)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle())
            .build()
    }
}