package com.example.lokikeyboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.*
import android.graphics.Color

class SetupActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("loki", MODE_PRIVATE)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 32, 28, 24)
            setBackgroundColor(Color.rgb(16, 16, 24))
        }
        fun label(s: String, size: Float = 16f) = TextView(this).apply {
            text = s; textSize = size; setTextColor(Color.WHITE)
            setPadding(0, 12, 0, 12)
        }
        layout.addView(label("LOKI KEYBOARD", 26f))
        layout.addView(label("A green-and-gold keyboard for eloquent mischief. Enable it, then select it as your phone's keyboard."))
        val key = EditText(this).apply {
            hint = "Optional OpenAI API key for AI-powered rewrites"
            setSingleLine(true)
            setTextColor(Color.WHITE); setHintTextColor(0xFFAAAAAA.toInt())
            setBackgroundColor(0xFF292936.toInt())
            setPadding(14, 12, 14, 12)
            setText(prefs.getString("api_key", ""))
        }
        layout.addView(key)
        layout.addView(label("The key is stored on this device. Without one, built-in Loki-style rewriting still works."))
        val save = Button(this).apply {
            text = "SAVE SETTINGS"
            setOnClickListener { prefs.edit().putString("api_key", key.text.toString().trim()).apply(); Toast.makeText(this@SetupActivity, "Saved", Toast.LENGTH_SHORT).show() }
        }
        layout.addView(save)
        val enable = Button(this).apply {
            text = "1. ENABLE LOKI KEYBOARD"
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        }
        layout.addView(enable)
        val choose = Button(this).apply {
            text = "2. SELECT LOKI KEYBOARD"
            setOnClickListener {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
            }
        }
        layout.addView(choose)
        val mic = Button(this).apply {
            text = "3. GRANT MICROPHONE PERMISSION"
            setOnClickListener {
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                    requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 41)
                else Toast.makeText(this@SetupActivity, "Microphone permission already granted", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(mic)
        layout.addView(label("How to use: type a message, tap LOKI to rewrite it, tap SPEAK to hear it, or tap MIC to dictate."))
        setContentView(ScrollView(this).apply { addView(layout) })
    }
}
