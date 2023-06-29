package ru.gustavo.astonhomework1

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ru.gustavo.astonhomework1.Const.ACTION_NEXT
import ru.gustavo.astonhomework1.Const.ACTION_PAUSE
import ru.gustavo.astonhomework1.Const.ACTION_PLAY
import ru.gustavo.astonhomework1.Const.ACTION_PREVIOUS
import ru.gustavo.astonhomework1.Const.REQUEST_POST_NOTIFICATIONS_PERMISSION
import ru.gustavo.astonhomework1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mediaPlayerService: MediaPlayerService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as MediaPlayerService.PlayerBinder
            mediaPlayerService = binder.getService()
            updateSeekBar()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            mediaPlayerService = null
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MediaPlayerService::class.java)
        bindService(intent, connection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission()
        }
        val intent = Intent(this, MediaPlayerService::class.java)
        val playBtn = findViewById<ImageButton>(R.id.playButton)
        val nextBtn = findViewById<ImageButton>(R.id.nextButton)
        val previousBtn = findViewById<ImageButton>(R.id.previousButton)
        playBtn.setOnClickListener {
            if (mediaPlayerService?.isPlaying() == true) {
                intent.action = ACTION_PAUSE
                startService(intent)
                playBtn.setImageResource(R.drawable.play)
            } else {
                intent.action = ACTION_PLAY
                startService(intent)
                playBtn.setImageResource(R.drawable.pause)
            }
        }
        nextBtn.setOnClickListener {
            intent.action = ACTION_NEXT
            startService(intent)
        }
        previousBtn.setOnClickListener {
            intent.action = ACTION_PREVIOUS
            startService(intent)
        }
    }

    private fun updateSeekBar() {
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        seekBar.isEnabled = false
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                val currentPosition = mediaPlayerService?.getCurrentPosition() ?: 0
                val totalDuration = mediaPlayerService?.getTotalDuration() ?: 0
                seekBar.progress = currentPosition
                seekBar.max = totalDuration
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                POST_NOTIFICATIONS
            ) != PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(POST_NOTIFICATIONS),
                REQUEST_POST_NOTIFICATIONS_PERMISSION
            )
        }
    }
}