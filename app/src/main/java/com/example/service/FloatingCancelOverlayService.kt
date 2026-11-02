package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.hypot

/**
 * Android System Overlay Service that keeps the floating cancel button
 * visible over any app on the phone (WhatsApp, TikTok, etc.) even when the keyboard is lowered/closed.
 * Fully hardened against SecurityException, Android 14+ FGS requirements, and BadTokenException.
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
        private const val TAG = "FloatingCancelOverlay"
        const val CHANNEL_ID = "floating_cancel_channel"
        const val NOTIFICATION_ID = 1042

        fun start(context: Context) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                    Log.w(TAG, "Cannot start overlay: overlay permission not granted")
                    return
                }
                val intent = Intent(context, FloatingCancelOverlayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Error starting FloatingCancelOverlayService", t)
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, FloatingCancelOverlayService::class.java)
                context.stopService(intent)
            } catch (t: Throwable) {
                Log.e(TAG, "Error stopping FloatingCancelOverlayService", t)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
            val notification = buildNotification()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error starting foreground notification", t)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.w(TAG, "Overlay permission revoked, stopping service")
            stopSelf()
            return
        }

        try {
            initOverlayView()
        } catch (t: Throwable) {
            Log.e(TAG, "Error initializing overlay view", t)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (overlayView == null && (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this))) {
            try {
                initOverlayView()
            } catch (t: Throwable) {
                Log.e(TAG, "Error re-initializing overlay in onStartCommand", t)
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Botón Flotante Permanente",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Mantiene el botón de cancelar visible en todo el celular"
                    setShowBadge(false)
                }
                val manager = getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to create notification channel", t)
            }
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Botón Flotante Activo")
            .setContentText("Toca para cancelar envíos automáticos en cualquier app")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun initOverlayView() {
        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            if (windowManager == null) return

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

            // Build Draggable Pill Layout using native safe components
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
                isClickable = true
                isFocusable = false
            }

            val iconView = TextView(this).apply {
                text = "🛑"
                textSize = 16f
                gravity = Gravity.CENTER
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.marginEnd = dpToPx(6)
                layoutParams = lp
            }

            val textView = TextView(this).apply {
                text = "Cancelar Auto"
                setTextColor(Color.WHITE)
                textSize = 13f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            pillLayout.addView(iconView)
            pillLayout.addView(textView)

            // Touch Listener that allows unrestricted movement across entire screen
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
                        } catch (t: Throwable) {
                            Log.e(TAG, "Error updating overlay layout", t)
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val diffX = event.rawX - initialTouchX
                        val diffY = event.rawY - initialTouchY
                        val distance = hypot(diffX.toDouble(), diffY.toDouble())
                        if (distance < 18.0) {
                            // Tap detected on the overlay button!
                            AutoCancelController.triggerCancel()
                            Toast.makeText(this@FloatingCancelOverlayService, "🛑 Envíos automáticos cancelados", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    else -> false
                }
            }

            // Observe automatic active state to update button colors in real time
            serviceScope.launch {
                AutoCancelController.isAutomaticActive.collectLatest { isActive ->
                    try {
                        val bg = pillLayout.background as? GradientDrawable
                        if (isActive) {
                            bg?.setColor(Color.parseColor("#DC2626"))
                            bg?.setStroke(dpToPx(2), Color.WHITE)
                            textView.text = "DETENER"
                        } else {
                            bg?.setColor(Color.parseColor("#1E293B"))
                            bg?.setStroke(dpToPx(2), Color.parseColor("#EF4444"))
                            textView.text = "Cancelar Auto"
                        }
                    } catch (t: Throwable) {
                        Log.e(TAG, "Error updating pill appearance", t)
                    }
                }
            }

            windowManager?.addView(pillLayout, params)
            overlayView = pillLayout
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to create overlay view", t)
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
            } catch (t: Throwable) {
                Log.e(TAG, "Error removing overlay view in onDestroy", t)
            }
        }
        overlayView = null
    }
}
