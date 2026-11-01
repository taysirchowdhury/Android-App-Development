package com.example.cse489assignment2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar

/** Receives the custom broadcast in onReceive(), not by directly showing an Intent extra. */
class CustomReceiverActivity : AppCompatActivity() {
    companion object {
        private const val ACTION_CUSTOM_MESSAGE = "com.example.cse489assignment2.ACTION_CUSTOM_MESSAGE"
        private const val EXTRA_BROADCAST_TEXT = "com.example.cse489assignment2.EXTRA_BROADCAST_TEXT"
    }
    private lateinit var receiverStatus: TextView
    private lateinit var receivedMessage: TextView
    private var registered = false

    private val customReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_CUSTOM_MESSAGE) {
                receivedMessage.text = intent.getStringExtra(EXTRA_BROADCAST_TEXT).orEmpty()
                receivedMessage.visibility = View.VISIBLE
                receiverStatus.setText(R.string.broadcast_received)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_receiver)
        AppInsets.apply(this, R.id.root_screen)
        findViewById<MaterialToolbar>(R.id.detail_toolbar).setNavigationOnClickListener { finish() }
        receiverStatus = findViewById(R.id.receiver_status)
        receivedMessage = findViewById(R.id.received_message)
    }

    override fun onStart() {
        super.onStart()
        // Receiver must be registered before sending the non-sticky custom broadcast.
        ContextCompat.registerReceiver(this, customReceiver, IntentFilter(ACTION_CUSTOM_MESSAGE),
            ContextCompat.RECEIVER_NOT_EXPORTED)
        registered = true
        val message = intent.getStringExtra(CustomInputActivity.EXTRA_MESSAGE).orEmpty()
        sendBroadcast(Intent(ACTION_CUSTOM_MESSAGE).apply {
            setPackage(packageName)
            putExtra(EXTRA_BROADCAST_TEXT, message)
        })
    }

    override fun onStop() {
        if (registered) {
            unregisterReceiver(customReceiver)
            registered = false
        }
        super.onStop()
    }
}
