package com.smarttoolfactory.imagecropper

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.gesture.pointerMotionEvents
import kotlin.math.sqrt


@Composable
fun EditBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onTextChange: (String) -> Unit,
    content: @Composable () -> Unit
) {

    val context = LocalContext.current

    var xScale by remember { mutableStateOf(1f) }
    var yScale by remember { mutableStateOf(1f) }

    var xTranslation by remember { mutableStateOf(0f) }
    var yTranslation by remember { mutableStateOf(0f) }

    var touchRegion by remember { mutableStateOf(TouchRegion.None) }
    val touchRegionWidth = LocalDensity.current.density * 8

//    var offset by remember { mutableStateOf(IntOffset.Zero) }
    var positionInParent by remember { mutableStateOf(Offset.Zero) }

    var positionRaw by remember { mutableStateOf(Offset.Zero) }
    var positionScaled by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(modifier) {

        val rectBounds by remember {
            mutableStateOf(
                Rect(
                    offset = Offset.Zero,
                    size = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
                )
            )
        }

        var size by remember {
            mutableStateOf(
                IntSize(
                    constraints.maxWidth,
                    constraints.maxHeight
                )
            )
        }

        var rectDraw by remember {
            mutableStateOf(rectBounds)
        }

        var rectTemp by remember {
            mutableStateOf(
                Rect(
                    offset = Offset.Zero,
                    size = Size(size.width.toFloat(), size.height.toFloat())
                )
            )
        }

        val editModifier = if (enabled) {
            Modifier
        } else {
            Modifier
        }
            .drawAnimatedDashRectBorder(rect = rectDraw)
            .drawWithContent {
                drawContent()
                if (rectDraw != Rect.Zero) {
                    val radius = touchRegionWidth

                    drawRect(
                        Color.Yellow,
                        topLeft = rectBounds.topLeft,
                        size = rectBounds.size,
                        style = Stroke(1.dp.toPx())
                    )

                    drawRect(
                        Color.Blue,
                        topLeft = rectTemp.topLeft,
                        size = rectTemp.size,
                        style = Stroke(5.dp.toPx())
                    )

                    drawRect(
                        Color.White,
                        topLeft = rectDraw.topLeft,
                        size = rectDraw.size,
                        style = Stroke(2.dp.toPx())
                    )
                    drawBorderCircle(radius = radius, center = rectDraw.topLeft)
                    drawBorderCircle(radius = radius, center = rectDraw.topRight)
                    drawBorderCircle(radius = radius, center = rectDraw.bottomLeft)
                    drawBorderCircle(radius = radius, center = rectDraw.bottomRight)

                    drawCircle(color = Color.Cyan, radius = 15f, center = positionRaw)
                    drawCircle(color = Color.Magenta, radius = 15f, center = positionScaled)

                }
            }
            .graphicsLayer {
                translationX = xTranslation
                translationY = yTranslation
                scaleX = xScale
                scaleY = yScale
                transformOrigin = TransformOrigin(0f, 0f)
            }

            .pointerMotionEvents(Unit,
                onDown = { change: PointerInputChange ->

                    rectTemp = rectDraw.copy()

                    positionRaw = change.position

                    val scaledX =
                        rectDraw.left + positionRaw.x * rectDraw.width / rectBounds.width
                    val scaledY =
                        rectDraw.top + positionRaw.y * rectDraw.height / rectBounds.height

                    positionScaled = Offset(scaledX, scaledY)

                    val translatedRect = Rect(offset = Offset.Zero, size = rectTemp.size)

                    touchRegion = getTouchRegion(
                        position = positionScaled,
                        rect = rectDraw,
                        threshold = touchRegionWidth * 4
                    )

                    println("✊ onDown() positionRaw: $positionRaw, touchRegion: $touchRegion, translatedRect: $translatedRect")

                    onTextChange(
                        "✊ onDown() region: $touchRegion\n" +
                                "positionRaw: $positionRaw\n" +
                                "positionChange: ${change.positionChange()}\n" +
                                "positionInParent: $positionInParent\n" +
                                "size: $size\n" +
                                "RECT translatedRect: $translatedRect\n" +
                                "RECT DRAW: $rectDraw\n" +
                                "width: ${rectDraw.width}, height: ${rectDraw.height}\n" +
                                "RECT TEMP: $rectTemp\n" +
                                " width: ${rectTemp.width.toInt()}, height: ${rectTemp.height.toInt()}\n" +
                                "xScale: $xScale, yScale: $yScale\n" +
                                "xTranslation: $xTranslation, yTranslation: $yTranslation\n\n"
                    )

                    Toast
                        .makeText(
                            context,
                            "Clicked position: ${change.position}",
                            Toast.LENGTH_SHORT
                        )
                        .show()

                },
                onMove = { change: PointerInputChange ->

                    val position = change.position
                    positionRaw = position
                    val scaledX = rectDraw.left + position.x * rectDraw.width / rectBounds.width
                    val scaledY = rectDraw.top + position.y * rectDraw.height / rectBounds.height
                    positionScaled = Offset(scaledX, scaledY)

                    when (touchRegion) {
                        TouchRegion.TopLeft -> {

                            rectDraw = Rect(
                                left = scaledX,
                                top = scaledY,
                                right = rectTemp.right,
                                bottom = rectTemp.bottom,
                            )

                            xScale = rectDraw.width / rectBounds.width
                            yScale = rectDraw.height / rectBounds.height
                            xTranslation = scaledX / 1f
                            yTranslation = scaledY / 1f

                        }

                        TouchRegion.BottomLeft -> {

                            rectDraw = Rect(
                                left = scaledX,
                                top = rectTemp.top,
                                right = rectTemp.right,
                                bottom = scaledY,
                            )

                            xScale = rectDraw.width / rectBounds.width
                            yScale = rectDraw.height / rectBounds.height
                            xTranslation = scaledX / 1f
                            yTranslation = scaledY / 1f - rectDraw.height


                        }

                        TouchRegion.TopRight -> {
                            rectDraw = Rect(
                                left = rectTemp.left,
                                top = scaledY,
                                right = scaledX,
                                bottom = rectTemp.bottom,
                            )

                            xScale = rectDraw.width / rectBounds.width
                            yScale = rectDraw.height / rectBounds.height

                            xTranslation = scaledX / 1f - rectDraw.width
                            yTranslation = scaledY / 1f
                        }

                        TouchRegion.BottomRight -> {
                            rectDraw = Rect(
                                left = rectTemp.left,
                                top = rectTemp.top,
                                right = scaledX,
                                bottom = scaledY,
                            )

                            xScale = rectDraw.width / rectBounds.width
                            yScale = rectDraw.height / rectBounds.height

                            xTranslation = scaledX / 1f - rectDraw.width
                            yTranslation = scaledY / 1f - rectDraw.height
                        }

                        TouchRegion.Inside -> {
                            val drag = change.positionChange()

                            val scaledDragX = drag.x * rectDraw.width / rectBounds.width
                            val scaledDragY = drag.y * rectDraw.height / rectBounds.height

                            xTranslation += scaledDragX
                            yTranslation += scaledDragY
                            rectDraw = rectDraw.translate(scaledDragX, scaledDragY)
                        }

                        else -> Unit
                    }

                    if (touchRegion != TouchRegion.None) {
                        change.consume()
                    }

                    onTextChange(
                        "🚀 onMove() region: $touchRegion\n" +
                                "position: $position\n" +
                                "scaledX: $scaledX, scaledY: $scaledY\n" +
                                "positionChange: ${change.positionChange()}\n" +
                                "positionScaled: $positionScaled\n" +
                                "positionInParent: $positionInParent\n" +
                                "RECT DRAW: $rectDraw\n" +
                                "width: ${rectDraw.width}, height: ${rectDraw.height}\n" +
                                "RECT TEMP: $rectTemp\n" +
                                " width: ${rectTemp.width.toInt()}, height: ${rectTemp.height.toInt()}\n" +
                                "xScale: $xScale, yScale: $yScale\n" +
                                "xTranslation: $xTranslation, yTranslation: $yTranslation\n\n"
                    )

                    println(
                        "🚀 onMove() region: $touchRegion\n" +
                                "position: $position\n" +
                                "scaledX: $scaledX, scaledY: $scaledY\n" +
                                "positionChange: ${change.positionChange()}\n" +
                                "positionScaled: $positionScaled\n" +
                                "positionInParent: $positionInParent\n" +
                                "RECT DRAW: $rectDraw\n" +
                                "width: ${rectDraw.width}, height: ${rectDraw.height}\n" +
                                "RECT TEMP: $rectTemp\n" +
                                " width: ${rectTemp.width.toInt()}, height: ${rectTemp.height.toInt()}\n" +
                                "xScale: $xScale, yScale: $yScale\n" +
                                "xTranslation: $xTranslation, yTranslation: $yTranslation\n\n"
                    )

                },
                onUp = {
                    touchRegion = TouchRegion.None
                    rectTemp = rectDraw.copy()

                    println("😜 onUp() rectTemp: $rectTemp")
                }
            )
        Box(
            editModifier


        ) {
            content()
        }
    }
}

private fun getTouchRegion(
    position: Offset,
    rect: Rect,
    threshold: Float
): TouchRegion {
    return when {

        inDistance(
            position,
            rect.topLeft, threshold
        ) -> TouchRegion.TopLeft
        inDistance(
            position,
            rect.topRight,
            threshold
        ) -> TouchRegion.TopRight
        inDistance(
            position,
            rect.bottomLeft,
            threshold
        ) -> TouchRegion.BottomLeft
        inDistance(
            position,
            rect.bottomRight,
            threshold
        ) -> TouchRegion.BottomRight
        rect.contains(offset = position) -> TouchRegion.Inside
        else -> TouchRegion.None
    }
}


private fun inDistance(offset1: Offset, offset2: Offset, target: Float): Boolean {
    val x1 = offset1.x
    val y1 = offset1.y

    val x2 = offset2.x
    val y2 = offset2.y

    val distance = sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
    return distance < target
}



private fun DrawScope.drawBorderCircle(
    radius: Float,
    center: Offset
) {
    drawCircle(color = blue, radius = radius, center = center)
    drawCircle(color = Color.White, radius = radius, center = center, style = Stroke(1.dp.toPx()))
}


