package com.example.lokikeyboard

import android.inputmethodservice.InputMethodService
import android.view.*
import android.view.inputmethod.InputConnection
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.content.Intent
import android.content.pm.PackageManager
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.Executors

class LokiImeService : InputMethodService(), TextToSpeech.OnInitListener {
    private val executor = Executors.newSingleThreadExecutor()
    private var buffer = StringBuilder()
    private var tts: TextToSpeech? = null
    private var speech: SpeechRecognizer? = null
    private var ready = false
    private var status: TextView? = null

    override fun onCreate() { super.onCreate(); tts = TextToSpeech(this, this) }
    override fun onInit(code: Int) { if (code == TextToSpeech.SUCCESS) { tts?.language = Locale.US; ready = true } }

    override fun onCreateInputView(): View {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(5, 7, 5, 7)
            setBackgroundColor(Color.rgb(16, 16, 24))
        }
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        status = TextView(this).apply {
            text = "  LOKI • GOD OF MISCHIEF"
            textSize = 13f; setTextColor(0xFFE4C56A.toInt())
            gravity = Gravity.CENTER_VERTICAL
        }
        top.addView(status, LinearLayout.LayoutParams(0, 42, 1f))
        top.addView(action("MIC", 0xFF315C45) { dictate() })
        top.addView(action("SPEAK", 0xFF665326) { speak(buffer.toString()) })
        outer.addView(top)
        val rows = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
        rows.forEachIndexed { index, chars ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            if (index == 2) row.addView(action("⇧", 0xFF343443) { })
            chars.forEach { ch ->
                val b = action(ch.toString(), 0xFF292936) { type(ch.toString()) }
                row.addView(b, LinearLayout.LayoutParams(0, 46, 1f).apply { setMargins(2, 3, 2, 3) })
            }
            if (index == 2) row.addView(action("⌫", 0xFF654040) { backspace() })
            outer.addView(row)
        }
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        controls.addView(action("?123", 0xFF343443) { type("123") })
        controls.addView(action(",", 0xFF343443) { type(",") })
        controls.addView(action("SPACE", 0xFF343443) { type(" ") }, LinearLayout.LayoutParams(0, 46, 3f))
        controls.addView(action(".", 0xFF343443) { type(".") })
        controls.addView(action("↵", 0xFF343443) { type("\n") })
        outer.addView(controls)
        val finalRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        finalRow.addView(action("LOKI ✦", 0xFF8B6A22) { rewrite() }, LinearLayout.LayoutParams(0, 48, 2f))
        finalRow.addView(action("CLEAR", 0xFF654040) { buffer.clear(); currentInputConnection?.deleteSurroundingText(1000, 0); updateStatus("Cleared") }, LinearLayout.LayoutParams(0, 48, 1f))
        outer.addView(finalRow)
        return outer
    }

    private fun action(label: String, color: Int, click: () -> Unit): Button =
        Button(this).apply {
            text = label; textSize = 12f; isAllCaps = false
            setTextColor(Color.WHITE); setTypeface(typeface, Typeface.BOLD)
            setBackgroundColor(color); setPadding(3, 0, 3, 0)
            setOnClickListener { click() }
        }

    private fun type(s: String) {
        buffer.append(s)
        currentInputConnection?.commitText(s, 1)
        updateStatus(buffer.toString().takeLast(28))
    }
    private fun backspace() {
        if (buffer.isNotEmpty()) buffer.deleteCharAt(buffer.length - 1)
        currentInputConnection?.deleteSurroundingText(1, 0)
        updateStatus(buffer.toString().takeLast(28))
    }
    private fun updateStatus(s: String) { status?.text = "  LOKI • ${s.take(30)}" }

    private fun rewrite() {
        val source = buffer.toString().trim()
        if (source.isBlank()) { updateStatus("Type something first"); return }
        updateStatus("Crafting mischief…")
        executor.execute {
            val result = try { aiRewrite(source) } catch (_: Exception) { offlineRewrite(source) }
            Handler(Looper.getMainLooper()).post {
                buffer = StringBuilder(result)
                currentInputConnection?.deleteSurroundingText(source.length, 0)
                currentInputConnection?.commitText(result, 1)
                updateStatus("Loki has spoken")
            }
        }
    }

    private fun aiRewrite(source: String): String {
        val key = getSharedPreferences("loki", MODE_PRIVATE).getString("api_key", "").orEmpty()
        if (key.isBlank()) return offlineRewrite(source)
        val conn = (URL("https://api.openai.com/v1/chat/completions").openConnection() as HttpURLConnection)
        conn.requestMethod = "POST"; conn.connectTimeout = 15000; conn.readTimeout = 20000
        conn.setRequestProperty("Authorization", "Bearer $key")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        val body = JSONObject().put("model", "gpt-4o-mini")
            .put("messages", org.json.JSONArray()
                .put(JSONObject().put("role", "system").put("content", "Rewrite the user's message as an original MCU-Loki-inspired line: regal, witty, theatrical, sly, charming, and mischievous. Preserve the meaning. Do not claim to be the actual character. Return only the rewritten message."))
                .put(JSONObject().put("role", "user").put("content", source)))
            .put("temperature", 0.9).toString()
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        val response = stream.bufferedReader().use { it.readText() }
        if (conn.responseCode !in 200..299) throw Exception("AI request failed")
        return JSONObject(response).getJSONArray("choices").getJSONObject(0)
            .getJSONObject("message").getString("content").trim()
    }

    private fun offlineRewrite(s: String): String {
        val lower = s.lowercase()
        val prefix = when {
            lower.startsWith("hi") || lower.startsWith("hello") || lower.startsWith("hey") -> "Ah, greetings. "
            lower.contains("please") -> "I shall consider it. "
            lower.contains("thank") -> "Your gratitude is noted, naturally. "
            lower.contains("?") -> "Pray tell, "
            else -> "Well then, mortal: "
        }
        val polished = s.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        return prefix + polished.trimEnd('.', '!', '?') + " — a most interesting turn of events."
    }

    private fun speak(text: String) {
        if (text.isBlank()) { updateStatus("Nothing to speak"); return }
        if (ready) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "loki-utterance")
        updateStatus("Speaking…")
    }

    private fun dictate() {
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            updateStatus("Grant microphone permission in Loki app"); return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { updateStatus("Speech recognition unavailable"); return }
        speech?.destroy()
        speech = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { updateStatus("Listening…") }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { updateStatus("Processing voice…") }
                override fun onError(error: Int) { updateStatus("Voice input error: $error") }
                override fun onResults(results: Bundle?) {
                    val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                    if (spoken.isNotBlank()) {
                        buffer = StringBuilder(spoken)
                        currentInputConnection?.commitText(spoken, 1)
                        updateStatus("Dictated • tap LOKI to transform")
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        speech?.startListening(intent)
    }

    override fun onDestroy() {
        speech?.destroy()
        tts?.shutdown()
        executor.shutdown()
        super.onDestroy()
    }
}
