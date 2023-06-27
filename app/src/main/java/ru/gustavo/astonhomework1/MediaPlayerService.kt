package ru.gustavo.astonhomework1

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import ru.gustavo.astonhomework1.Const.ACTION_NEXT
import ru.gustavo.astonhomework1.Const.ACTION_PAUSE
import ru.gustavo.astonhomework1.Const.ACTION_PLAY
import ru.gustavo.astonhomework1.Const.ACTION_PREVIOUS


private val musicsList = listOf(R.raw.ty_eto_seryozno, R.raw.moskva_ljubit, R.raw.privychka)
private var musicIndex = 0

class MediaPlayerService : Service(), MediaPlayer.OnPreparedListener {
    private var mediaPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        when (action) {
            ACTION_PLAY -> {
                if (mediaPlayer == null) {
                    initPlayer()
                } else {
                    mediaPlayer?.start()
                }
            }
            ACTION_PAUSE -> {
                mediaPlayer?.pause()
            }
            ACTION_NEXT -> {
                musicIndex = if (musicIndex + 1 > musicsList.lastIndex) 0 else musicIndex++
                initPlayer()
            }
            ACTION_PREVIOUS -> {
                musicIndex = if (musicIndex - 1 < 0) musicsList.lastIndex else musicIndex--
                initPlayer()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initPlayer() {
        mediaPlayer = MediaPlayer.create(this, musicsList[musicIndex])
//        mediaPlayer?.apply {
//            setOnPreparedListener(this@MediaPlayerService)
//            prepareAsync()
//        }
        mediaPlayer?.start()
    }

    fun isPlaying() = mediaPlayer?.isPlaying

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
    }

    override fun onBind(p0: Intent?): IBinder {
        return PlayerBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    inner class PlayerBinder : Binder() {
        fun getService() = this@MediaPlayerService
    }
}