package com.smarttoolfactory.imagecropper

import android.graphics.Bitmap
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ImageCropper(
    modifier: Modifier = Modifier,
    imageBitmap: ImageBitmap,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    contentDescription: String?,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    crop: Boolean = false,
    onCropSuccess: (ImageBitmap) -> Unit
) {

    ImageWithConstraints(
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment,
        contentDescription = contentDescription,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality,
        imageBitmap = imageBitmap,
        drawImage = false
    ) {

        // No crop operation is applied by ScalableImage so rect points to bounds of original
        // bitmap
        val scaledImageBitmap = getScaledImageBitmap(imageBitmap, contentScale)

        println("")

        val bitmapWidth = scaledImageBitmap.width
        val bitmapHeight = scaledImageBitmap.height

        val imageWidthInPx: Float
        val imageHeightInPx: Float
        with(LocalDensity.current) {
            imageWidthInPx = imageWidth.toPx()
            imageHeightInPx = imageHeight.toPx()
        }


        var cropRect by remember(scaledImageBitmap) {
            mutableStateOf(
                Rect(
                    offset = Offset.Zero,
                    size = Size(
                        bitmapWidth.toFloat(),
                        bitmapHeight.toFloat()
                    )
                )
            )
        }

        var drawRect by remember(scaledImageBitmap, imageWidthInPx, imageHeightInPx, contentScale) {
            mutableStateOf(
                Rect(
                    offset = Offset.Zero,
                    size = Size(imageWidthInPx, imageHeightInPx)
                )
            )
        }

        var zoom by remember(
            scaledImageBitmap,
            imageWidthInPx,
            imageHeightInPx,
            contentScale
        ) { mutableStateOf(1f) }

        var offset by remember(
            scaledImageBitmap,
            imageWidthInPx,
            imageHeightInPx,
            contentScale
        ) { mutableStateOf(Offset.Zero) }

        var angle by remember(
            scaledImageBitmap,
            imageWidthInPx,
            imageHeightInPx,
            contentScale
        ) { mutableStateOf(0f) }

        LaunchedEffect(crop) {
            if (crop) {
                val croppedBitmap = Bitmap.createBitmap(
                    scaledImageBitmap.asAndroidBitmap(),
                    cropRect.left.toInt(),
                    cropRect.top.toInt(),
                    cropRect.width.toInt(),
                    cropRect.height.toInt()
                ).asImageBitmap()

                onCropSuccess(croppedBitmap)
            }
        }

        val imageModifier =
            Modifier
                .fillMaxSize()
                .clipToBounds()
                .border(4.dp, Color.Green)

                .graphicsLayer {
                    translationX = -offset.x * zoom
                    translationY = -offset.y * zoom
                    scaleX = zoom
                    scaleY = zoom
//                    rotationZ = angle
                    TransformOrigin(0f, 0f).also { transformOrigin = it }
                }


        val drawingModifier = Modifier
            .fillMaxSize()
            .border(6.dp, Color.Black)
            .pointerInput(scaledImageBitmap, imageWidthInPx, imageHeightInPx, contentScale) {
                detectTransformGestures(
                    onGesture = { gestureCentroid, gesturePan, gestureZoom, gestureRotate ->

                        val oldScale = zoom
                        val newScale = zoom * gestureZoom
                        val newOffset =
                            (offset + gestureCentroid / oldScale).rotateBy(gestureRotate) -
                                    (gestureCentroid / newScale + gesturePan / oldScale)

                        // TODO instead of coercing offset in image, let it animate back to
                        //  valid bounds after up motion in next iteration
                        val offsetX = (newOffset.x)
                            .coerceIn(0f, imageWidthInPx - imageWidthInPx / zoom)
                        val offsetY = (newOffset.y)
                            .coerceIn(0f, imageHeightInPx - imageHeightInPx / zoom)

                        offset = Offset(offsetX, offsetY)

                        val offsetXInBitmap = offsetX * bitmapWidth / imageWidthInPx
                        val offsetYInBitmap = offsetY * bitmapHeight / imageHeightInPx

                        cropRect = Rect(
                            topLeft = Offset(offsetXInBitmap, offsetYInBitmap),
                            bottomRight = Offset(
                                offsetXInBitmap + bitmapWidth / zoom,
                                offsetYInBitmap + bitmapHeight / zoom
                            )
                        )

                        zoom = newScale.coerceIn(1f..10f)
                        angle += gestureRotate

                    }
                )
            }


        Column {
            CropperImpl(
                modifier = Modifier.size(imageWidth, imageHeight),
                imageOverlayModifier = imageModifier,
                imageDrawingModifier = drawingModifier,
                imageBitmap = scaledImageBitmap,
                rect = drawRect
            )

            Text(
                "Zoom: $zoom\n" +
                        "imageWidthInPx: $imageWidthInPx, imageHeightInPx: $imageHeightInPx\n" +
                        "translationX: ${-offset.x * zoom}, translationY: ${-offset.y * zoom}\n" +
                        "offset: $offset\n" +
                        "scaledWidth: ${imageHeightInPx / zoom}, scaledHeight: ${imageHeightInPx / zoom}\n" +
                        "cropRect: $cropRect\n" +
                        "drawRect: $drawRect"
            )
        }
    }
}

@Composable
private fun CropperImpl(
    modifier: Modifier,
    imageOverlayModifier: Modifier,
    imageDrawingModifier: Modifier,
    imageBitmap: ImageBitmap,
    rect: Rect
) {
    Box(modifier) {
        ImageOverlay(modifier = imageOverlayModifier, imageBitmap = imageBitmap)
        DrawingOverlay(modifier = imageDrawingModifier, rect = rect)
    }
}


@Composable
private fun ImageOverlay(
    modifier: Modifier,
    imageBitmap: ImageBitmap
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {

        val canvasWidth = size.width.roundToInt()
        val canvasHeight = size.height.roundToInt()

        drawImage(
            image = imageBitmap,
            srcSize = IntSize(imageBitmap.width, imageBitmap.height),
            dstSize = IntSize(canvasWidth, canvasHeight)
        )
    }
}

@Composable
private fun DrawingOverlay(
    modifier: Modifier,
    rect: Rect
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {

        val canvasWidth = size.width.roundToInt()
        val canvasHeight = size.height.roundToInt()

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
    }
}


/**
 * Rotates the given offset around the origin by the given angle in degrees.
 *
 * A positive angle indicates a counterclockwise rotation around the right-handed 2D Cartesian
 * coordinate system.
 *
 * See: [Rotation matrix](https://en.wikipedia.org/wiki/Rotation_matrix)
 */
fun Offset.rotateBy(angle: Float): Offset {
    val angleInRadians = angle * PI / 180
    return Offset(
        (x * cos(angleInRadians) - y * sin(angleInRadians)).toFloat(),
        (x * sin(angleInRadians) + y * cos(angleInRadians)).toFloat()
    )
}

private const val PI = Math.PI
