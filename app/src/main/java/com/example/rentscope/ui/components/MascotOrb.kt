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

@Composable
fun MascotOrb(
    modifier: Modifier = Modifier,
    orbSize: Dp = 180.dp,
    followStrength: Float = 14f
) {
    val density = LocalDensity.current
    var touchPoint by remember { mutableStateOf<Offset?>(null) }

    val transition = rememberInfiniteTransition(label = "mascot_orb")

    val floatY by transition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2200,
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
                durationMillis = 1800,
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
                        colors = listOf(
                            Color(0xAA7A00FF),
                            Color(0x33006BFF),
                            Color.Transparent
                        ),
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
                        colors = listOf(
                            Color(0xFF2A7BFF),
                            Color(0xFF0E4BEF),
                            Color(0xFF02152D)
                        ),
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

                val pupilOffset = calculateEyeOffset(
                    center = center,
                    target = touchPoint,
                    maxDistance = orbRadius * 0.13f,
                    followStrength = followStrength
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