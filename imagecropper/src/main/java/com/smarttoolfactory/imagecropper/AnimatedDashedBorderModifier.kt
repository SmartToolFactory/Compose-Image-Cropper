package com.smarttoolfactory.imagecropper

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Composed [Modifier] that draws animated dashed border with [Modifier.drawWithContent]
 *
 * @param width width of the border in dp
 * @param shape shape of the border
 * @param durationMillis duration of the animation spec
 * @param intervals [FloatArray] with 2 elements that contain on(first), and off(second) interval
 * @param animatedColor color that is animated with [InfiniteTransition]
 * @param staticColor this color is drawn behind the [animatedColor] color to act as layout
 * for animated color
 */
fun Modifier.drawAnimatedDashBorder(
    width: Dp = 2.dp,
    shape: Shape = RectangleShape,
    durationMillis: Int = 500,
    intervals: FloatArray = floatArrayOf(20f, 20f),
    animatedColor: Color = Color.Black,
    staticColor: Color = Color.White

) = composed(
    inspectorInfo = debugInspectorInfo {
        // name should match the name of the modifier
        name = "drawAnimatedDashBorder"
        // add name and value of each argument
        properties["width"] = width
        properties["shape"] = shape
        properties["durationMillis"] = durationMillis
        properties["intervals"] = intervals
        properties["animatedColor"] = animatedColor
        properties["staticColor"] = staticColor
    },

    factory = {

        require(intervals.size == 2) {
            "There should be on and off values in intervals array"
        }

        val density = LocalDensity.current

        val transition: InfiniteTransition = rememberInfiniteTransition()

        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = (4 * intervals.average()).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMillis,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )

        val pathEffect = PathEffect.dashPathEffect(
            intervals = intervals,
            phase = phase
        )
        // add your modifier implementation here
        this.then(
            Modifier
                .drawWithContent {
                    drawContent()
                    val outline: Outline =
                        shape.createOutline(
                            size,
                            layoutDirection = layoutDirection,
                            density = density
                        )

                    drawOutline(
                        outline = outline, color = staticColor, style = Stroke(
                            width = width.toPx()
                        )
                    )
                    drawOutline(
                        outline = outline, color = animatedColor, style = Stroke(
                            width = width.toPx(),
                            pathEffect = pathEffect
                        )
                    )
                }
        )
    }
)

/**
 * Composed [Modifier] that draws animated dashed border with [Modifier.drawWithContent]
 *
 * @param width width of the border in dp
 * @param rect that covers border we draw
 * @param durationMillis duration of the animation spec
 * @param intervals [FloatArray] with 2 elements that contain on(first), and off(second) interval
 * @param animatedColor color that is animated with [InfiniteTransition]
 * @param staticColor this color is drawn behind the [animatedColor] color to act as layout
 * for animated color
 */
fun Modifier.drawAnimatedDashRectBorder(
    width: Dp = 2.dp,
    rect: Rect,
    durationMillis: Int = 500,
    intervals: FloatArray = floatArrayOf(20f, 20f),
    animatedColor: Color = Color.Black,
    staticColor: Color = Color.White

) = composed(
    inspectorInfo = debugInspectorInfo {
        // name should match the name of the modifier
        name = "drawAnimatedDashBorder"
        // add name and value of each argument
        properties["width"] = width
        properties["rect"] = rect
        properties["durationMillis"] = durationMillis
        properties["intervals"] = intervals
        properties["animatedColor"] = animatedColor
        properties["staticColor"] = staticColor
    },

    factory = {

        require(intervals.size == 2) {
            "There should be on and off values in intervals array"
        }

        val transition: InfiniteTransition = rememberInfiniteTransition()

        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = (4 * intervals.average()).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMillis,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )

        val pathEffect = PathEffect.dashPathEffect(
            intervals = intervals,
            phase = phase
        )
        // add your modifier implementation here
        this.then(
            Modifier
                .drawWithContent {

                    drawRect(
                        topLeft = rect.topLeft,
                        size = rect.size,
                        color = staticColor,
                        style = Stroke(
                            width = width.toPx()
                        )
                    )
                    drawRect(
                        topLeft = rect.topLeft,
                        size = rect.size,
                        color = animatedColor,
                        style = Stroke(
                            width = width.toPx(),
                            pathEffect = pathEffect
                        )
                    )

                    drawContent()
                }
        )
    }
)

val blue = Color(0xff2196F3)
