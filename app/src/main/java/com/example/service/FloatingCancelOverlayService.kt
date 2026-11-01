package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.hypot

/**
 * Android Foreground / System Overlay Service that keeps the floating cancel button
 * visible over any app on the phone (WhatsApp, TikTok, etc.) even when the keyboard is lowered/closed.
 */
class FloatingCancelOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    companion object {
        const val CHANNEL_ID = "floating_cancel_channel"
        const val NOTIFICATION_ID = 1042

        fun start(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                return
            }
            val intent = Intent(context, FloatingCancelOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingCancelOverlayService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        initOverlayView()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Botón Flotante Permanente",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Permite mantener el botón de cancelar visible en todo el celular"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Botón Flotante Activo")
            .setContentText("Mueve el botón por toda la pantalla para cancelar envíos automáticos")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun initOverlayView() {
        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 100
                y = 300
            }

            // Build Draggable Pill Layout
            val pillLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8))

                val bg = GradientDrawable().apply {
                    cornerRadius = dpToPx(24).toFloat()
                    setColor(Color.parseColor("#1E293B"))
                    setStroke(dpToPx(2), Color.parseColor("#EF4444"))
                }
                background = bg
            }

            val iconView = ImageView(this).apply {
                setImageResource(android.R.drawable.ic_delete)
                setColorFilter(Color.parseColor("#EF4444"))
                val lp = LinearLayout.LayoutParams(dpToPx(20), dpToPx(20))
                lp.marginEnd = dpToPx(8)
                layoutParams = lp
            }

            val textView = TextView(this).apply {
                text = "🛑 Cancelar Auto"
                setTextColor(Color.WHITE)
                textSize = 13f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            pillLayout.addView(iconView)
            pillLayout.addView(textView)

            // Touch Listener that allows moving across entire screen
            pillLayout.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        try {
                            windowManager?.updateViewLayout(pillLayout, params)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val diffX = event.rawX - initialTouchX
                        val diffY = event.rawY - initialTouchY
                        val distance = hypot(diffX.toDouble(), diffY.toDouble())
                        if (distance < 15.0) {
                            // Tap detected!
                            AutoCancelController.triggerCancel()
                            Toast.makeText(this, "🛑 Envíos automáticos cancelados", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    else -> false
                }
            }

            // Observe automatic active state to highlight button
            serviceScope.launch {
                AutoCancelController.isAutomaticActive.collectLatest { isActive ->
                    val bg = pillLayout.background as? GradientDrawable
                    if (isActive) {
                        bg?.setColor(Color.parseColor("#DC2626"))
                        bg?.setStroke(dpToPx(2), Color.WHITE)
                        textView.text = "🛑 DETENER"
                    } else {
                        bg?.setColor(Color.parseColor("#1E293B"))
                        bg?.setStroke(dpToPx(2), Color.parseColor("#EF4444"))
                        textView.text = "🛑 Cancelar Auto"
                    }
                }
            }

            windowManager?.addView(pillLayout, params)
            overlayView = pillLayout
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
