package com.smarttoolfactory.imagecropper

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.image.transform.TouchRegion
import kotlin.math.roundToInt

@Composable
internal fun DrawingOverlay(
    modifier: Modifier,
    rect: Rect,
    touchRegionWidth: Float
) {

    val path = remember(rect) {
        Path().apply {

            if (rect != Rect.Zero) {
                // Top left lines
                moveTo(rect.topLeft.x, rect.topLeft.y + touchRegionWidth)
                lineTo(rect.topLeft.x, rect.topLeft.y)
                lineTo(rect.topLeft.x + touchRegionWidth, rect.topLeft.y)

                // Top right lines
                moveTo(rect.topRight.x - touchRegionWidth, rect.topRight.y)
                lineTo(rect.topRight.x, rect.topRight.y)
                lineTo(rect.topRight.x, rect.topRight.y + touchRegionWidth)

                // Bottom right lines
                moveTo(rect.bottomRight.x, rect.bottomRight.y - touchRegionWidth)
                lineTo(rect.bottomRight.x, rect.bottomRight.y)
                lineTo(rect.bottomRight.x - touchRegionWidth, rect.bottomRight.y)

                // Bottom left lines
                moveTo(rect.bottomLeft.x + touchRegionWidth, rect.bottomLeft.y)
                lineTo(rect.bottomLeft.x, rect.bottomLeft.y)
                lineTo(rect.bottomLeft.x, rect.bottomLeft.y - touchRegionWidth)
            }
        }
    }

    Canvas(modifier = modifier) {

        val color = Color.White
        val strokeWidth = 2.dp.toPx()

        with(drawContext.canvas.nativeCanvas) {
            val checkPoint = saveLayer(null, null)

            // Destination
            drawRect(Color(0x77000000))

            // Source
            drawRect(
                topLeft = rect.topLeft,
                size = rect.size,
                color = Color.Transparent,
                blendMode = BlendMode.Clear
            )
            restoreToCount(checkPoint)
        }

        drawGrid(rect)
        drawPath(
            path,
            color,
            style = Stroke(strokeWidth * 2)
        )
    }
}

@Composable
internal fun ImageOverlay(
    modifier: Modifier,
    imageBitmap: ImageBitmap
) {
    Canvas(modifier = modifier) {

        val canvasWidth = size.width.roundToInt()
        val canvasHeight = size.height.roundToInt()

        drawImage(
            image = imageBitmap,
            srcSize = IntSize(imageBitmap.width, imageBitmap.height),
            dstSize = IntSize(canvasWidth, canvasHeight)
        )
    }
}

