package com.example.ui.keyboard

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.AutoCancelController
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun FullDraggableFloatingCancelButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onCancelRequested: () -> Unit = { AutoCancelController.triggerCancel() }
) {
    if (!enabled) return

    val context = LocalContext.current
    val density = LocalDensity.current

    val isAutoRunning by AutoCancelController.isAutomaticActive.collectAsState()
    val statusText by AutoCancelController.statusText.collectAsState()

    // Pulsing animation when automatic burst/send is active
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isAutoRunning) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        // Approximate button dimensions
        val estimatedBtnWidthPx = with(density) { 170.dp.toPx() }
        val estimatedBtnHeightPx = with(density) { 52.dp.toPx() }

        // Start near bottom-right above navigation bar, but unrestricted
        var offsetX by remember {
            mutableFloatStateOf(max(20f, screenWidthPx - estimatedBtnWidthPx - with(density) { 16.dp.toPx() }))
        }
        var offsetY by remember {
            mutableFloatStateOf(max(60f, screenHeightPx - estimatedBtnHeightPx - with(density) { 120.dp.toPx() }))
        }

        // Drag tracking variables to differentiate drag vs click
        var dragDistanceX by remember { mutableFloatStateOf(0f) }
        var dragDistanceY by remember { mutableFloatStateOf(0f) }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .scale(pulseScale)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                dragDistanceX = 0f
                                dragDistanceY = 0f
                            },
                            onDragEnd = {
                                val totalDistance = kotlin.math.hypot(dragDistanceX.toDouble(), dragDistanceY.toDouble())
                                if (totalDistance < 15.0) {
                                    // It was a tap on the button!
                                    triggerVibrate(context)
                                    onCancelRequested()
                                    Toast.makeText(context, "🛑 Envío cancelado por botón flotante", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDragCancel = {
                                dragDistanceX = 0f
                                dragDistanceY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragDistanceX += dragAmount.x
                                dragDistanceY += dragAmount.y

                                // Unrestricted full-screen movement, clamped to visible display area
                                val maxAllowedX = max(0f, screenWidthPx - estimatedBtnWidthPx)
                                val maxAllowedY = max(0f, screenHeightPx - estimatedBtnHeightPx)

                                offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxAllowedX)
                                offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxAllowedY)
                            }
                        )
                    }
                    .testTag("floating_cancel_pill_full")
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = if (isAutoRunning) Color(0xFFDC2626) else Color(0xFF1E293B),
                    shadowElevation = 10.dp,
                    tonalElevation = 8.dp,
                    border = BorderStroke(
                        width = if (isAutoRunning) 2.5.dp else 1.5.dp,
                        color = if (isAutoRunning) Color.White else Color(0xFFEF4444).copy(alpha = 0.8f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Drag handle visual icon
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Mover botón",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Stop icon
                        Surface(
                            shape = CircleShape,
                            color = if (isAutoRunning) Color.White else Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.StopCircle,
                                    contentDescription = "Cancelar",
                                    tint = if (isAutoRunning) Color(0xFFDC2626) else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (isAutoRunning) statusText.uppercase() else "Cancelar Auto",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

private fun triggerVibrate(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    } catch (_: Exception) {}
}
