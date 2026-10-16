package com.example.data.model

import android.content.ClipboardManager
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object MacroProcessor {

    fun processMacros(rawText: String, context: Context? = null): String {
        if (!rawText.contains('{')) return rawText
        var processed = rawText

        val now = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        processed = processed.replace("{date}", dateFormat.format(now))
        processed = processed.replace("{fecha}", dateFormat.format(now))
        processed = processed.replace("{time}", timeFormat.format(now))
        processed = processed.replace("{hora}", timeFormat.format(now))
        processed = processed.replace("{datetime}", dateTimeFormat.format(now))

        if (processed.contains("{saludo}")) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val saludo = when (hour) {
                in 6..11 -> "Buenos días"
                in 12..18 -> "Buenas tardes"
                else -> "Buenas noches"
            }
            processed = processed.replace("{saludo}", saludo)
        }

        if (context != null && (processed.contains("{clipboard}") || processed.contains("{portapapeles}"))) {
            val clipboardText = try {
                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = clipboardManager?.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    clip.getItemAt(0).text?.toString() ?: ""
                } else ""
            } catch (e: Exception) {
                ""
            }
            processed = processed.replace("{clipboard}", clipboardText)
            processed = processed.replace("{portapapeles}", clipboardText)
        }

        return processed
    }

    fun containsAutoSendMacro(rawText: String): Boolean {
        if (!rawText.contains('{')) return false
        return rawText.contains("{enter}") || rawText.contains("{send}") || rawText.contains("{envio}") || rawText.contains("{enviar}")
    }

    fun stripSendMacros(rawText: String): String {
        if (!rawText.contains('{')) return rawText
        return rawText.replace("{enter}", "")
            .replace("{send}", "")
            .replace("{envio}", "")
            .replace("{enviar}", "")
    }
}

