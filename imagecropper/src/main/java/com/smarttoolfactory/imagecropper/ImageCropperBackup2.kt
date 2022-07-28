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
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import com.smarttoolfactory.gesture.pointerMotionEvents
import com.smarttoolfactory.image.ImageWithConstraints
import com.smarttoolfactory.image.transform.TouchRegion
import com.smarttoolfactory.imagecropper.util.getScaledImageBitmap
import com.smarttoolfactory.imagecropper.util.getTouchRegion
import com.smarttoolfactory.imagecropper.util.rotateBy

@Composable
fun ImageCropperAlt2(
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
        val scaledImageBitmap =
            getScaledImageBitmap(
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                rect = rect,
                bitmap = imageBitmap,
                contentScale = contentScale,
            )

        val bitmapWidth = scaledImageBitmap.width
        val bitmapHeight = scaledImageBitmap.height

        val imageWidthInPx: Float
        val imageHeightInPx: Float
        with(LocalDensity.current) {
            imageWidthInPx = imageWidth.toPx()
            imageHeightInPx = imageHeight.toPx()
        }

        /**
         * Rectangle that is used for cropping image, this rectangle is not the
         * one that draws on screen. We might have 4000x3000 rect while we
         * draw 1000x750px Composable on screen
         */
        var rectCrop by remember {
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
         * Rectangle that is used for cropping image, this rectangle is not the
         * one that draws on screen. We might have 4000x3000 rect while we
         * draw 1000x750px Composable on screen
         */
        var rectCrop2 by remember {
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
         * and its dimensions cannot be bigger than draw area.
         *
         * Corners of this [Rect] is used as handle to change bounds and grid is drawn
         * inside this rect
         */
        var rectDraw by remember(imageWidthInPx, imageHeightInPx, contentScale) {
            mutableStateOf(
                Rect(
                    offset = Offset.Zero,
                    size = Size(imageWidthInPx, imageHeightInPx)
                )
            )
        }

        /**
         * Temporary rectangle for translating rectangle when user moves image
         */
        var rectTemp by remember {
            mutableStateOf(rectDraw)
        }

        var zoom by remember(
            scaledImageBitmap,
            imageWidthInPx,
            imageHeightInPx,
            contentScale
        ) { mutableStateOf(1f) }

        var pan by remember(
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
                    rectCrop.left.toInt(),
                    rectCrop.top.toInt(),
                    rectCrop.width.toInt(),
                    rectCrop.height.toInt()
                ).asImageBitmap()

                onCropSuccess(croppedBitmap)
            }
        }

        val imageModifier =
            Modifier
                .fillMaxSize()
                .clipToBounds()
                .graphicsLayer {
                    translationX = -pan.x * zoom
                    translationY = -pan.y * zoom
                    scaleX = zoom
                    scaleY = zoom
//                    rotationZ = angle
                    TransformOrigin(0f, 0f).also { transformOrigin = it }
                }


        val drawingModifier = Modifier
            .fillMaxSize()
            .pointerInput(scaledImageBitmap, imageWidthInPx, imageHeightInPx, contentScale) {
                detectTransformGestures(
                    onGesture = { gestureCentroid, gesturePan, gestureZoom, gestureRotate ->

                        val oldScale = zoom
                        val newScale = zoom * gestureZoom
                        val newOffset =
                            (pan + gestureCentroid / oldScale).rotateBy(gestureRotate) -
                                    (gestureCentroid / newScale + gesturePan / oldScale)

                        // TODO instead of coercing offset in image, let it animate back to
                        //  valid bounds after up motion in next iteration
                        val offsetX = (newOffset.x)
                            .coerceIn(0f, imageWidthInPx - imageWidthInPx / zoom)
                        val offsetY = (newOffset.y)
                            .coerceIn(0f, imageHeightInPx - imageHeightInPx / zoom)

                        pan = Offset(offsetX, offsetY)

                        val offsetXInBitmap = offsetX * bitmapWidth / imageWidthInPx
                        val offsetYInBitmap = offsetY * bitmapHeight / imageHeightInPx

                        rectCrop2 = Rect(
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
            .pointerMotionEvents(Unit,
                onDown = { change: PointerInputChange ->
                    touchPosition = change.position
                    touchRegion = getTouchRegion(
                        position = change.position,
                        rect = rectDraw,
                        threshold = touchRegionWidth
                    )
                    rectTemp = rectDraw
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

                            rectDraw = Rect(
                                left = offsetX,
                                top = offsetY,
                                right = rectDraw.right,
                                bottom = rectDraw.bottom,
                            )

                        }
                        TouchRegion.TopRight -> {

                            rectDraw = Rect(
                                left = rectDraw.left,
                                top = offsetY,
                                right = offsetX,
                                bottom = rectDraw.bottom,
                            )
                        }
                        TouchRegion.BottomLeft -> {


                            rectDraw = Rect(
                                left = offsetX,
                                top = rectDraw.top,
                                right = rectDraw.right,
                                bottom = offsetY,
                            )
                        }
                        TouchRegion.BottomRight -> {
                            rectDraw = Rect(
                                left = rectDraw.left,
                                top = rectDraw.top,
                                right = offsetX,
                                bottom = offsetY,
                            )
                        }

                        TouchRegion.Inside -> {

                            val xChange = offsetX - touchPosition.x
                            val yChange = offsetY - touchPosition.y
                            rectDraw = rectTemp.translate(xChange, yChange)
                        }

                        else -> Unit
                    }

                    if (touchRegion != TouchRegion.None) {
                        change.consume()
                    }

                },
                onUp = { change: PointerInputChange ->

                    // Update crop rect
                    rectCrop = getCropRect(
                        bitmapWidth = bitmapWidth,
                        bitmapHeight = bitmapHeight,
                        imageWidth = imageWidthInPx,
                        imageHeight = imageHeightInPx,
                        zoom = zoom,
                        pan = pan,
                        rectBounds = rectDraw
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
                rect = rectDraw,
                touchRegionWidth = touchRegionWidth
            )

            Text(
                modifier = Modifier.align(Alignment.BottomStart),
                color = Color.White,
                fontSize = 10.sp,
                text = "Zoom: $zoom\n" +
                        "imageWidthInPx: $imageWidthInPx, imageHeightInPx: $imageHeightInPx\n" +
                        "translationX: ${-pan.x * zoom}, translationY: ${-pan.y * zoom}\n" +
                        "offset: $pan\n" +
                        "scaledImageWidth: ${imageWidthInPx / zoom}, scaledImageHeight: ${imageHeightInPx / zoom}\n" +
                        "scaledBitmapWidth: ${bitmapWidth / zoom}, scaledBitmapHeight: ${bitmapHeight / zoom}\n" +
                        "cropRect: $rectCrop\n, size: ${rectCrop.size}\n" +
                        "cropRect2: $rectCrop2\n, size: ${rectCrop2.size}\n" +
                        "drawRect: $rectDraw, size: ${rectDraw.size}"
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
    touchRegionWidth: Float
) {
    Box(modifier) {
        ImageOverlay(modifier = imageOverlayModifier, imageBitmap = imageBitmap)
        DrawingOverlay(
            modifier = imageDrawingModifier,
            rect = rect,
            touchRegionWidth = touchRegionWidth
        )
    }
}
