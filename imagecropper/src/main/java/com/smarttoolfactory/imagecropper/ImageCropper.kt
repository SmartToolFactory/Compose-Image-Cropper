package com.smarttoolfactory.imagecropper

import android.graphics.Bitmap
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttoolfactory.gesture.pointerMotionEvents
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

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


        val bitmapWidth = scaledImageBitmap.width
        val bitmapHeight = scaledImageBitmap.height

        val imageWidthInPx: Float
        val imageHeightInPx: Float
        with(LocalDensity.current) {
            imageWidthInPx = imageWidth.toPx()
            imageHeightInPx = imageHeight.toPx()
        }

        /**
         * Rectangle that cover whole Composable area
         */
        val boundsRect = remember {
            Rect(offset = Offset.Zero, size = Size(imageWidthInPx, imageHeightInPx))
        }

        /**
         * Rectangle that is used for cropping image, this rectangle is not the
         * one that draws on screen. We might have 4000x3000 rect while we
         * draw 1000x750px Composable on screen
         */
        var cropRect by remember() {
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

        /**
         * This rectangle is the section of drawing on screen it's correlated with boundRect
         * and it's dimensions cannot be greater than [boundsRect].
         *
         * Corners of this [Rect] is used as handle to change bounds and grid is drawn
         * inside this rect
         */
        var drawRect by remember(imageWidthInPx, imageHeightInPx, contentScale) {
            mutableStateOf(
                Rect(
                    offset = Offset.Zero,
                    size = Size(imageWidthInPx, imageHeightInPx)
                )
            )
        }

        /**
         * Temporary rectangle for translating rectangle when user move the image
         */
        var tempRect by remember {
            mutableStateOf(drawRect)
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

        var touchRegion by remember { mutableStateOf(TouchRegion.None) }
        var touchPosition by remember { mutableStateOf(Offset.Unspecified) }
        val touchRegionWidth = 70f

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
//            .pointerInput(scaledImageBitmap, imageWidthInPx, imageHeightInPx, contentScale) {
//                detectTransformGestures(
//                    onGesture = { gestureCentroid, gesturePan, gestureZoom, gestureRotate ->
//
//                        val oldScale = zoom
//                        val newScale = zoom * gestureZoom
//                        val newOffset =
//                            (offset + gestureCentroid / oldScale).rotateBy(gestureRotate) -
//                                    (gestureCentroid / newScale + gesturePan / oldScale)
//
//                        // TODO instead of coercing offset in image, let it animate back to
//                        //  valid bounds after up motion in next iteration
//                        val offsetX = (newOffset.x)
//                            .coerceIn(0f, imageWidthInPx - imageWidthInPx / zoom)
//                        val offsetY = (newOffset.y)
//                            .coerceIn(0f, imageHeightInPx - imageHeightInPx / zoom)
//
//                        offset = Offset(offsetX, offsetY)
//
//                        val offsetXInBitmap = offsetX * bitmapWidth / imageWidthInPx
//                        val offsetYInBitmap = offsetY * bitmapHeight / imageHeightInPx
//
//                        cropRect = Rect(
//                            topLeft = Offset(offsetXInBitmap, offsetYInBitmap),
//                            bottomRight = Offset(
//                                offsetXInBitmap + bitmapWidth / zoom,
//                                offsetYInBitmap + bitmapHeight / zoom
//                            )
//                        )
//
//                        zoom = newScale.coerceIn(1f..10f)
//                        angle += gestureRotate
//
//                    }
//                )
//            }


//            .pointerInput(scaledImageBitmap, imageWidthInPx, imageHeightInPx, contentScale) {
//                detectTransformGestures(
//                    onGesture = { gestureCentroid, gesturePan, gestureZoom, gestureRotate ->
//                        val newZoom = zoom * gestureZoom
//                        zoom = newZoom.coerceIn(1f..10f)
//                        angle += gestureRotate
//                    }
//                )
//            }

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

                        zoom = newScale.coerceIn(1f..10f)
                        angle += gestureRotate

                    }
                )
            }
            .pointerMotionEvents(Unit,
                onDown = { change: PointerInputChange ->
                    touchPosition = change.position
                    touchRegion = getTouchRegion(
                        position = change.position,
                        rect = drawRect,
                        threshold = touchRegionWidth
                    )
                    tempRect = drawRect
                },
                onMove = { change: PointerInputChange ->
                    val position = change.position


                    val width = constraints.maxWidth.toFloat()
                    val height = constraints.maxHeight.toFloat()


                    val offsetX =
                        position.x.coerceIn(0f, width)
                    val offsetY =
                        position.y.coerceIn(0f, height)


                    when (touchRegion) {
                        TouchRegion.TopLeft -> {

                            drawRect = Rect(
                                left = offsetX,
                                top = offsetY,
                                right = drawRect.right,
                                bottom = drawRect.bottom,
                            )
                        }
                        TouchRegion.TopRight -> {

                            drawRect = Rect(
                                left = drawRect.left,
                                top = offsetY,
                                right = offsetX,
                                bottom = drawRect.bottom,
                            )
                        }
                        TouchRegion.BottomLeft -> {


                            drawRect = Rect(
                                left = offsetX,
                                top = drawRect.top,
                                right = drawRect.right,
                                bottom = offsetY,
                            )
                        }
                        TouchRegion.BottomRight -> {
                            drawRect = Rect(
                                left = drawRect.left,
                                top = drawRect.top,
                                right = offsetX,
                                bottom = offsetY,
                            )
                        }

                        TouchRegion.Inside -> {

                            val xChange = offsetX - touchPosition.x
                            val yChange = offsetY - touchPosition.y
                            drawRect = tempRect.translate(xChange, yChange)
                        }

                        else -> {

                        }
                    }

                    if (touchRegion != TouchRegion.None) {
                        change.consume()
                    }

                },
                onUp = { change: PointerInputChange ->

                    // FIXME create correct bounding algorithm and animate to image bounds
                    //  when drawing rectangle is out of bounds of image display
                    // bound draw rectangle to image domain
                    if (drawRect.top < 0) {
                        drawRect = Rect(
                            left = drawRect.left,
                            top = 0f,
                            right = drawRect.right,
                            bottom = drawRect.height.coerceAtMost(imageHeightInPx)
                        )
                    } else if (drawRect.left < 0) {
                        drawRect = Rect(
                            left = 0f,
                            top = drawRect.top,
                            right = drawRect.width.coerceAtMost(imageWidthInPx),
                            bottom = drawRect.bottom
                        )
                    } else if (drawRect.right > imageWidthInPx) {
                        drawRect = Rect(
                            left = (imageWidthInPx - drawRect.width).coerceAtLeast(0f),
                            top = drawRect.top,
                            right = imageWidthInPx,
                            bottom = drawRect.bottom
                        )
                    } else if (drawRect.bottom > imageHeightInPx) {
                        drawRect = Rect(
                            left = drawRect.left,
                            top = (imageHeightInPx - drawRect.height).coerceAtLeast(0f),
                            right = drawRect.right,
                            bottom = imageHeightInPx
                        )
                    }

                    // Update crop rect
                    val widthRatio = bitmapWidth / imageWidthInPx
                    val heightRatio = bitmapHeight / imageHeightInPx

                    val offsetXInBitmap =
                        widthRatio * (offset.x + drawRect.left / zoom)
                    val offsetYInBitmap =
                        heightRatio * (offset.y + drawRect.top / zoom)

                    cropRect = Rect(
                        offset = Offset(offsetXInBitmap, offsetYInBitmap),
                        size = Size(
                            widthRatio * drawRect.width / zoom,
                            heightRatio * drawRect.height / zoom
                        )
                    )

                    touchRegion = TouchRegion.None
                }
            )


        Box(contentAlignment = Alignment.Center) {
            CropperImpl(
                modifier = Modifier.size(imageWidth, imageHeight),
                imageOverlayModifier = imageModifier,
                imageDrawingModifier = drawingModifier,
                imageBitmap = scaledImageBitmap,
                rect = drawRect,
                touchRegion = touchRegion,
                touchRegionWidth = touchRegionWidth
            )

            Text(
                modifier = Modifier.align(Alignment.BottomStart),
                color = Color.White,
                fontSize = 10.sp,
                text = "Zoom: $zoom\n" +
                        "imageWidthInPx: $imageWidthInPx, imageHeightInPx: $imageHeightInPx\n" +
                        "translationX: ${-offset.x * zoom}, translationY: ${-offset.y * zoom}\n" +
                        "offset: $offset\n" +
                        "scaledImageWidth: ${imageWidthInPx / zoom}, scaledImageHeight: ${imageHeightInPx / zoom}\n" +
                        "scaledBitmapWidth: ${bitmapWidth / zoom}, scaledBitmapHeight: ${bitmapHeight / zoom}\n" +
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
    rect: Rect,
    touchRegion: TouchRegion,
    touchRegionWidth: Float
) {
    Box(modifier) {
        ImageOverlay(modifier = imageOverlayModifier, imageBitmap = imageBitmap)
        DrawingOverlay(
            modifier = imageDrawingModifier,
            rect = rect,
            touchRegion = touchRegion,
            touchRegionWidth = touchRegionWidth
        )
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
    rect: Rect,
    touchRegion: TouchRegion,
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

    androidx.compose.foundation.Canvas(modifier = modifier) {

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
            if (touchRegion == TouchRegion.None) color else Color.DarkGray.copy(.9f),
            style = Stroke(strokeWidth * 2)
        )
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

enum class TouchRegion {
    TopLeft, TopRight, BottomLeft, BottomRight, Inside, None
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
