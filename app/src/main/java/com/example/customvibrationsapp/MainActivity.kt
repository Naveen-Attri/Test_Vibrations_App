package com.example.customvibrationsapp

import android.os.Bundle
import android.os.VibrationEffect
import android.os.VibratorManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch

class MainActivity : AppCompatActivity() {
    private lateinit var vibratorManager: VibratorManager;
    private lateinit var patternSpinner: Spinner;
    private lateinit var repeatSwitch: Switch;
    private lateinit var amplitudeBar: SeekBar;
    private lateinit var amplitudeText: TextView;
    private lateinit var vibrationButton: Button;
    private lateinit var stopButton: Button;

    private var amplitude = 150;
    private var repeatMode = -1;
    private var isVibrating = false;

    private val  patterns = mapOf(
        "Continuous" to longArrayOf(0),
        "Short Buzz" to longArrayOf(0, 200),
        "Heartbeat" to longArrayOf(0, 100, 100, 300, 400, 100, 100, 300),
        "Ramp Up" to longArrayOf(0, 100, 100, 200, 100, 400),
        "Double Tap" to longArrayOf(0, 50, 100, 50),
        "Machine Gun" to longArrayOf(0, 50, 50, 50, 50, 50, 50, 50)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager;

        patternSpinner = findViewById(R.id.patternSpinner)
        repeatSwitch = findViewById(R.id.repeatSwitch);
        vibrationButton = findViewById<Button>(R.id.vibBtn)
        stopButton = findViewById<Button>(R.id.stopBtn)
        amplitudeBar = findViewById<SeekBar>(R.id.amplitudeBar)
        amplitudeText = findViewById<TextView>(R.id.amplitudeText)

        vibrationButton.setOnClickListener() {
            vibratePhone()
        }
        stopButton.setOnClickListener(){
            stopVibration();
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, patterns.keys.toList())
        patternSpinner.adapter = adapter

        amplitudeBar.setOnSeekBarChangeListener( object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val normalizedAmp = progress + 1;
                amplitudeText.text = "Amplitude: $normalizedAmp"
                amplitude = (normalizedAmp * 2.55).toInt().coerceIn(1, 255)
                if(isVibrating) {
                    stopVibration();
                    vibratePhone();
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        repeatSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isVibrating) {
                stopVibration();
                vibratePhone();
            }
        }

        setInfo();
    }

    fun setInfo(){
        val infoBox = findViewById<TextView>(R.id.InfoBox)
        val arr = vibratorManager.vibratorIds
        infoBox.text = arr.size.toString()
    }

    fun vibratePhone() {

        isVibrating = true;
        val vibrator = vibratorManager.defaultVibrator;
        val patternName = patternSpinner.selectedItem as String;
        val pattern = patterns[patternName] ?: longArrayOf(0, 200);

        pattern?.let {
            repeatMode = repeatSwitch .let { if (it.isChecked) 0 else -1 }
            val amplitudeArr = IntArray(it.size) { amplitude }

            if(patternName == "Continuous") {
                var duration: Long;
                if(repeatMode == 0)  duration = 9999999
                else duration = 5000;
                val effect = VibrationEffect.createOneShot(duration, amplitude)

                vibrator.vibrate(effect)
            }
            else {
                val effect = VibrationEffect.createWaveform(it, amplitudeArr, repeatMode)
                vibrator.vibrate(effect)
            }
        }
        if(repeatMode == -1) isVibrating = false;
    }

    fun stopVibration() {
        vibratorManager.defaultVibrator.cancel();
        isVibrating = false;
    }

}