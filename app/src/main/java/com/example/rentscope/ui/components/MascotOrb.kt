package com.example.rentscope.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class MascotState {
    IDLE,
    THINKING,
    SPEAKING,
    ERROR
}

@Composable
fun MascotOrb(
    modifier: Modifier = Modifier,
    orbSize: Dp = 180.dp,
    followStrength: Float = 14f,
    state: MascotState = MascotState.IDLE
) {
    val density = LocalDensity.current
    var touchPoint by remember { mutableStateOf<Offset?>(null) }

    val floatDuration = when (state) {
        MascotState.IDLE -> 2200
        MascotState.THINKING -> 900
        MascotState.SPEAKING -> 1400
        MascotState.ERROR -> 700
    }

    val glowDuration = when (state) {
        MascotState.IDLE -> 1800
        MascotState.THINKING -> 700
        MascotState.SPEAKING -> 1000
        MascotState.ERROR -> 600
    }

    val floatAmplitude = when (state) {
        MascotState.IDLE -> 6f
        MascotState.THINKING -> 10f
        MascotState.SPEAKING -> 8f
        MascotState.ERROR -> 4f
    }

    val orbColors = when (state) {
        MascotState.IDLE -> listOf(
            Color(0xFF2A7BFF),
            Color(0xFF0E4BEF),
            Color(0xFF02152D)
        )

        MascotState.THINKING -> listOf(
            Color(0xFF5B8CFF),
            Color(0xFF3B5BDB),
            Color(0xFF081B4B)
        )

        MascotState.SPEAKING -> listOf(
            Color(0xFF4CC9F0),
            Color(0xFF4361EE),
            Color(0xFF14213D)
        )

        MascotState.ERROR -> listOf(
            Color(0xFFFF6B6B),
            Color(0xFFE63946),
            Color(0xFF3A0D13)
        )
    }

    val glowColors = when (state) {
        MascotState.IDLE -> listOf(
            Color(0xAA7A00FF),
            Color(0x33006BFF),
            Color.Transparent
        )

        MascotState.THINKING -> listOf(
            Color(0xAA6C63FF),
            Color(0x334361EE),
            Color.Transparent
        )

        MascotState.SPEAKING -> listOf(
            Color(0xAA00B4D8),
            Color(0x334361EE),
            Color.Transparent
        )

        MascotState.ERROR -> listOf(
            Color(0xAAFF4D6D),
            Color(0x33E63946),
            Color.Transparent
        )
    }

    val transition = rememberInfiniteTransition(label = "mascot_orb")

    val floatY by transition.animateFloat(
        initialValue = -floatAmplitude,
        targetValue = floatAmplitude,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = floatDuration,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    val glowPulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = glowDuration,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    val innerShift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "innerShift"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = 0,
                        y = with(density) { floatY.dp.roundToPx() }
                    )
                }
                .size(orbSize)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            touchPoint = offset
                            tryAwaitRelease()
                            touchPoint = null
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val orbRadius = size.minDimension * 0.35f
                val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

                val glowRadius = orbRadius * 1.45f * glowPulse
                val shadowCenter = Offset(center.x, center.y + orbRadius * 0.95f)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = glowColors,
                        center = shadowCenter,
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = shadowCenter
                )

                val movingInnerCenter = lerp(
                    start = Offset(
                        center.x - orbRadius * 0.18f,
                        center.y + orbRadius * 0.18f
                    ),
                    stop = Offset(
                        center.x + orbRadius * 0.10f,
                        center.y - orbRadius * 0.05f
                    ),
                    fraction = innerShift
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = orbColors,
                        center = movingInnerCenter,
                        radius = orbRadius * 1.1f
                    ),
                    radius = orbRadius,
                    center = center
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x33000000),
                            Color.Transparent
                        ),
                        center = Offset(
                            center.x + orbRadius * 0.35f,
                            center.y - orbRadius * 0.35f
                        ),
                        radius = orbRadius * 1.2f
                    ),
                    radius = orbRadius,
                    center = center,
                    blendMode = BlendMode.Multiply
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x66FFFFFF),
                            Color.Transparent
                        ),
                        center = Offset(
                            center.x - orbRadius * 0.25f,
                            center.y - orbRadius * 0.25f
                        ),
                        radius = orbRadius * 0.95f
                    ),
                    radius = orbRadius,
                    center = center
                )

                drawCircle(
                    color = Color(0x30FFFFFF),
                    radius = orbRadius,
                    center = center,
                    style = Stroke(width = orbRadius * 0.07f)
                )

                val leftEyeBase = Offset(
                    x = center.x - orbRadius * 0.32f,
                    y = center.y - orbRadius * 0.10f
                )

                val rightEyeBase = Offset(
                    x = center.x - orbRadius * 0.05f,
                    y = center.y - orbRadius * 0.06f
                )

                val eyeFollowStrength = when (state) {
                    MascotState.IDLE -> followStrength
                    MascotState.THINKING -> followStrength + 4f
                    MascotState.SPEAKING -> followStrength + 2f
                    MascotState.ERROR -> followStrength + 1f
                }

                val pupilOffset = calculateEyeOffset(
                    center = center,
                    target = touchPoint,
                    maxDistance = orbRadius * 0.13f,
                    followStrength = eyeFollowStrength
                )

                drawRoundedEye(
                    center = leftEyeBase + pupilOffset,
                    width = orbRadius * 0.18f,
                    height = orbRadius * 0.34f
                )

                drawRoundedEye(
                    center = rightEyeBase + pupilOffset,
                    width = orbRadius * 0.15f,
                    height = orbRadius * 0.28f
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x99FFFFFF),
                            Color.Transparent
                        ),
                        center = Offset(
                            center.x - orbRadius * 0.45f,
                            center.y - orbRadius * 0.48f
                        ),
                        radius = orbRadius * 0.38f
                    ),
                    radius = orbRadius * 0.25f,
                    center = Offset(
                        center.x - orbRadius * 0.45f,
                        center.y - orbRadius * 0.48f
                    )
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x55FFFFFF),
                            Color.Transparent
                        ),
                        center = Offset(
                            center.x - orbRadius * 0.18f,
                            center.y - orbRadius * 0.52f
                        ),
                        radius = orbRadius * 0.18f
                    ),
                    radius = orbRadius * 0.10f,
                    center = Offset(
                        center.x - orbRadius * 0.18f,
                        center.y - orbRadius * 0.52f
                    )
                )
            }
        }
    }
}

private fun DrawScope.drawRoundedEye(
    center: Offset,
    width: Float,
    height: Float
) {
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(
            x = center.x - width / 2f,
            y = center.y - height / 2f
        ),
        size = Size(width, height),
        cornerRadius = CornerRadius(width / 2f, width / 2f)
    )
}

private fun calculateEyeOffset(
    center: Offset,
    target: Offset?,
    maxDistance: Float,
    followStrength: Float
): Offset {
    if (target == null) return Offset.Zero

    val angle = atan2(target.y - center.y, target.x - center.x)
    val distanceFactor = minOf(
        1f,
        distanceBetween(center, target) / (followStrength * 10f)
    )

    val distance = maxDistance * distanceFactor

    return Offset(
        x = cos(angle) * distance,
        y = sin(angle) * distance
    )
}

private fun distanceBetween(a: Offset, b: Offset): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return sqrt(dx * dx + dy * dy)
}