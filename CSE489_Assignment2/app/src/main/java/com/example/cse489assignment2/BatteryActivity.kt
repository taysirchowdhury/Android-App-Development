package com.example.cse489assignment2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

/** Displays the real ACTION_BATTERY_CHANGED system broadcast in Activity 2. */
class BatteryActivity : AppCompatActivity() {
    private lateinit var percentage: TextView
    private lateinit var batteryStatus: TextView
    private lateinit var bar: ProgressBar
    private var registered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = updateBattery(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battery)
        AppInsets.apply(this, R.id.root_screen)
        findViewById<MaterialToolbar>(R.id.detail_toolbar).setNavigationOnClickListener { finish() }
        percentage = findViewById(R.id.battery_percentage)
        batteryStatus = findViewById(R.id.battery_status)
        bar = findViewById(R.id.battery_progress)
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.let { updateBattery(it) }
        registered = true
    }

    private fun updateBattery(data: Intent) {
        val level = data.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = data.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percent = BatteryPercentage.from(level, scale)
        if (percent == null) {
            batteryStatus.setText(R.string.battery_waiting)
            return
        }
        percentage.text = "$percent%"
        bar.progress = percent
        batteryStatus.text = when (data.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Fully charged"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            else -> "Status unavailable"
        }
    }

    override fun onStop() {
        if (registered) {
            unregisterReceiver(receiver)
            registered = false
        }
        super.onStop()
    }
}
