package com.smarttoolfactory.imagecropper

import android.graphics.Bitmap
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ImageCropper(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    contentDescription: String?,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality
) {
    ImageWithConstraints(
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment,
        contentDescription = contentDescription,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality,
        imageBitmap = bitmap,
        drawImage = false
    ) {


        // No crop operation is applied by ScalableImage so rect points to bounds of original
        // bitmap
        val scaledBitmap = getBitmap(bitmap = bitmap, rect = rect)

        val imageWidthInPx: Float
        val imageHeightInPx: Float
        with(LocalDensity.current) {
            imageWidthInPx = imageWidth.toPx()
            imageHeightInPx = imageHeight.toPx()
        }

        var cropRect by remember {
            mutableStateOf(
//                Rect(
//                    offset = Offset.Zero,
//                    size = Size(imageWidthInPx / 2f, imageHeightInPx / 2f)
//                )
                Rect(
                    offset = Offset.Zero,
                    size = Size(imageWidthInPx, imageHeightInPx)
                )
            )
        }
        val cropPath = remember { Path() }

        var zoom by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var angle by remember { mutableStateOf(0f) }
        var isInBounds by remember { mutableStateOf(false) }

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
            .pointerInput(Unit) {
                detectTransformGestures(
                    onGesture = { gestureCentroid, gesturePan, gestureZoom, gestureRotate ->

                        val oldScale = zoom
                        val newScale = zoom * gestureZoom
                        val newOffset = (offset + gestureCentroid / oldScale).rotateBy(gestureRotate) -
                                (gestureCentroid / newScale + gesturePan / oldScale)

                        val offsetX = (newOffset.x)
//                            .coerceIn(0f, imageWidthInPx)
                        val offsetY = (newOffset.y)
//                            .coerceIn(0f, imageHeightInPx)

                        offset =Offset(offsetX,offsetY)


//                        cropRect = Rect(
//                            offset = offset,
//                            size = Size(imageWidthInPx / 2f, imageHeightInPx / 2f)
//                        )

                        zoom = newScale.coerceIn(1f..10f)
//                            zoom = newScale
                        angle += gestureRotate
                        println("🔥 IMAGE MODIFIER: $offset, zoom: $zoom, " +
                                "translationX: ${-offset.x * zoom}, translationY: ${-offset.y * zoom}")

                    }
                )
            }
//            .pointerMotionEventList(
//                Unit,
//                onDown = {
//                    val position = it.position
//                    isInBounds = cropRect.contains(it.position)
//
//                    println("🍏 Draw MODIFIER onDown() isInBounds: $isInBounds, position: $position")
//
//                    if (isInBounds) {
//                        offset = position
////                        it.consumeDownChange()
//                    }
//                },
//                onMove = {
//
//                    val pointerSize = it.size
////                    if (isInBounds && pointerSize == 1) {
//                        val pointerInputChange = it.first()
//                        val position = pointerInputChange.position
//                        val offsetX = position.x
//                        val offsetY = position.y
//                        offset = Offset(offsetX, offsetY)
//
//                        cropRect = Rect(
//                            offset = offset,
//                            size = Size(imageWidthInPx / 2f, imageHeightInPx / 2f)
//                        )
//
//                        pointerInputChange.consumePositionChange()
//                        println("🍎 Draw MODIFIER onMove() isInBounds: $isInBounds, offset: $offset, pointerSize:$pointerSize")
////                    }
//
//                },
//                onUp = {
//                    isInBounds = false
//                }
//            )

        CropperImpl(
            modifier = Modifier.size(imageWidth, imageHeight),
            imageOverlayModifier = imageModifier,
            imageDrawingModifier = drawingModifier,
            imageBitmap = scaledBitmap,
            rect = cropRect
        )
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

@Composable
private fun getBitmap(bitmap: ImageBitmap, rect: IntRect): ImageBitmap {
    return if (bitmap.width == rect.width &&
        bitmap.height == rect.height &&
        !bitmap.asAndroidBitmap().isRecycled
    ) {
        bitmap
    } else {
        remember(rect.size, rect.topLeft) {
            // This bitmap is needed when we crop original bitmap due to scaling mode
            // and aspect ratio result of cropping
            // We might have center section of the image after cropping, and
            // because of that thumbLayout either should have rectangle and some
            // complex calculation for srcOffset and srcSide along side with touch offset
            // or we can create a new bitmap that only contains area bounded by rectangle
            Bitmap.createBitmap(
                bitmap.asAndroidBitmap(),
                rect.left,
                rect.top,
                rect.width,
                rect.height
            ).asImageBitmap()
        }
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
