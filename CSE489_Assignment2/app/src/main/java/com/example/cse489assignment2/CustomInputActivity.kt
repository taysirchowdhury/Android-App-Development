package com.example.cse489assignment2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CustomInputActivity : AppCompatActivity() {
    companion object { const val EXTRA_MESSAGE = "com.example.cse489assignment2.EXTRA_MESSAGE" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_input)
        AppInsets.apply(this, R.id.root_screen, includeKeyboard = true)
        findViewById<MaterialToolbar>(R.id.detail_toolbar).setNavigationOnClickListener { finish() }
        val input = findViewById<TextInputEditText>(R.id.custom_message_input)
        val container = findViewById<TextInputLayout>(R.id.message_input_layout)
        findViewById<MaterialButton>(R.id.send_custom_button).setOnClickListener {
            val message = input.text?.toString().orEmpty()
            if (message.isBlank()) {
                container.error = getString(R.string.send_error)
            } else {
                container.error = null
                startActivity(Intent(this, CustomReceiverActivity::class.java).apply {
                    putExtra(EXTRA_MESSAGE, message)
                })
            }
        }
    }
}
