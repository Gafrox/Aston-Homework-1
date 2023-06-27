package ru.gustavo.astonhomework1

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import ru.gustavo.astonhomework1.databinding.ActivityMainBinding
import ru.gustavo.astonhomework1.Const.ACTION_NEXT
import ru.gustavo.astonhomework1.Const.ACTION_PAUSE
import ru.gustavo.astonhomework1.Const.ACTION_PLAY
import ru.gustavo.astonhomework1.Const.ACTION_PREVIOUS

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mediaPlayerService: MediaPlayerService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as MediaPlayerService.PlayerBinder
            mediaPlayerService = binder.getService()
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        val playBtn = findViewById<ImageButton>(R.id.playButton)
        val nextBtn = findViewById<ImageButton>(R.id.nextButton)
        val previousBtn = findViewById<ImageButton>(R.id.previousButton)

        playBtn.setOnClickListener {
            if (mediaPlayerService?.isPlaying() == true) {
                val intent = Intent(this, MediaPlayerService::class.java)
                intent.action = ACTION_PAUSE
                startService(intent)
                playBtn.setImageResource(R.drawable.play)
            } else {
                val intent = Intent(this, MediaPlayerService::class.java)
                intent.action = ACTION_PLAY
                startService(intent)
                playBtn.setImageResource(R.drawable.pause)
            }
        }
    }
}