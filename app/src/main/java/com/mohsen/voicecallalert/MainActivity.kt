package com.mohsen.voicecallalert

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlin.math.log10
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    private var running = false
    private var recorder: AudioRecord? = null
    private var thread: Thread? = null
    private lateinit var status: TextView
    private val threshold = 72.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 70, 40, 40) }
        status = TextView(this).apply { text = "آماده تست"; textSize = 24f; setPadding(0, 0, 0, 40) }
        val button = Button(this).apply { text = "شروع تشخیص صدا" }
        layout.addView(status); layout.addView(button); setContentView(layout)
        button.setOnClickListener { if (running) stopDetection(button) else requestAndStart(button) }
    }

    private fun requestAndStart(button: Button) {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 10); return
        }
        startDetection(button)
    }

    private fun startDetection(button: Button) {
        val min = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        recorder = AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, min * 2)
        recorder!!.startRecording(); running = true; button.text = "توقف تشخیص"
        status.text = "در حال گوش دادن..."
        thread = Thread {
            val buffer = ShortArray(min)
            while (running) {
                val n = recorder!!.read(buffer, 0, buffer.size)
                if (n > 0) {
                    var sum = 0.0
                    for (i in 0 until n) sum += buffer[i].toDouble() * buffer[i]
                    val rms = sqrt(sum / n)
                    val db = if (rms > 0) 20 * log10(rms / 32768.0) + 100 else 0.0
                    runOnUiThread { status.text = "صدای فعلی: ${db.toInt()} dB" }
                    if (db >= threshold) { vibrate(); Thread.sleep(1200) }
                }
            }
        }.also { it.start() }
    }

    private fun vibrate() {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= 31) getSystemService(VibratorManager::class.java).defaultVibrator else getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun stopDetection(button: Button) {
        running = false; recorder?.stop(); recorder?.release(); recorder = null; thread = null
        button.text = "شروع تشخیص صدا"; status.text = "متوقف شد"
    }

    override fun onDestroy() { running = false; recorder?.release(); super.onDestroy() }
}
