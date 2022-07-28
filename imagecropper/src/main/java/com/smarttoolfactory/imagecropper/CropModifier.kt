package com.smarttoolfactory.imagecropper

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import com.smarttoolfactory.gesture.detectTransformGestures
import com.smarttoolfactory.gesture.pointerMotionEvents
import com.smarttoolfactory.image.transform.TouchRegion
import com.smarttoolfactory.imagecropper.util.getTouchRegion
import kotlinx.coroutines.launch

fun Modifier.crop(
    vararg keys: Any?,
    touchRegionSize: Float,
    minDimension: Float,
    cropState: CropState,
    onDown: (CropData) -> Unit={},
    onMove: (CropData) -> Unit={},
    onUp: (CropData) -> Unit={},
) = composed(

    factory = {

        // Rectangle that covers bounds of Image
        val rectBounds = remember {
            cropState.rectDraw.copy()
        }

        // This rectangle is needed to set bounds set at first touch position while
        // moving to constraint current bounds to temp one from first down
        // When pointer is up
        var rectTemp by remember { mutableStateOf(Rect.Zero) }

        var touchRegion by remember { mutableStateOf(TouchRegion.None) }

        // Touch position for edge of the rectangle, used for not jumping to edge of rectangle
        // when user moves a handle. We set positionActual as position of selected handle
        // and using this distance as offset to not have a jump from touch position

        var distanceToEdgeFromTouch = Offset.Zero

        val coroutineScope = rememberCoroutineScope()

        val transformModifier = Modifier.pointerInput(keys) {
            detectTransformGestures(
                consume = true,
                onGestureStart = {
                    onDown(cropState.cropData)
                },
                onGestureEnd = {
                    onUp(cropState.cropData)
                },
                onGesture = { _, gesturePan, gestureZoom, gestureRotate, _, _ ->

//                    if (touchRegion == TouchRegion.None) {
                        coroutineScope.launch {
                            cropState.updateZoomState(
                                size = size,
                                gestureZoom = gestureZoom,
                                gesturePan = gesturePan,
                                gestureRotate = gestureRotate
                            )
                        }
                        onMove(cropState.cropData)
//                    }
                }
            )
        }

        val tapModifier = Modifier.pointerInput(keys) {
            detectTapGestures(
                onDoubleTap = {
                    coroutineScope.run {
                        launch { cropState.animatePanTo(Offset.Zero) }
                        launch { cropState.animateZoomTo(1f) }
                        launch { cropState.animateRotationTo(0f) }
                    }
                }
            )
        }

        val graphicsModifier = Modifier.graphicsLayer {
            this.update(cropState)
        }


        val touchModifier = Modifier.pointerMotionEvents(
            onDown = { change: PointerInputChange ->

                rectTemp = cropState.rectDraw.copy()

                val position = change.position
                val touchPositionScreenX = position.x
                val touchPositionScreenY = position.y

                val touchPositionOnScreen = Offset(touchPositionScreenX, touchPositionScreenY)
                touchRegion = getTouchRegion(
                    position = touchPositionOnScreen,
                    rect = cropState.rectDraw,
                    threshold = touchRegionSize
                )

                // This is the difference between touch position and edge
                // This is required for not moving edge of draw rect to touch position on move
                distanceToEdgeFromTouch =
                    getDistanceToEdgeFromTouch(touchRegion, rectTemp, touchPositionOnScreen)

                onDown(cropState.cropData)
            },
            onMove = { change: PointerInputChange ->

                cropState.rectDraw = updateDrawRect(
                    distanceToEdgeFromTouch = distanceToEdgeFromTouch,
                    touchRegion = touchRegion,
                    minDimension = minDimension,
                    rectTemp = rectTemp,
                    rectDraw = cropState.rectDraw,
                    change = change
                )

                if (touchRegion != TouchRegion.None) {
                    change.consume()
                }

                onMove(cropState.cropData)
            },
            onUp = {
                touchRegion = TouchRegion.None
                cropState.rectDraw = moveIntoBounds(rectBounds, cropState.rectDraw)


                // Get rectangle that can crop actual image
                cropState.rectCrop = getCropRect(
                    bitmapWidth = cropState.bitmapSize.width,
                    bitmapHeight =cropState.bitmapSize.height,
                    imageWidth = cropState.imageSize.width,
                    imageHeight = cropState.imageSize.height,
                    pan = cropState.pan,
                    zoom = cropState.zoom,
                    rectBounds = cropState.rectDraw
                )

                rectTemp = cropState.rectDraw.copy()
                onUp(cropState.cropData)
            }
        )


        this.then(
            Modifier
                .clipToBounds()
                .then(transformModifier)
//                .then(tapModifier)
//                .then(touchModifier)
                .then(graphicsModifier)
        )
    },
    inspectorInfo = {
        name = "enhancedZoom"
        // add name and value of each argument
        properties["touchRegionRadius"] = touchRegionSize
        properties["minDimension"] = minDimension
        properties["onDown"] = onDown
        properties["onMove"] = onMove
        properties["onUp"] = onUp
    }
)

internal fun moveIntoBounds(rectBounds: Rect, rectCurrent: Rect): Rect {
    var width = rectCurrent.width
    var height = rectCurrent.height


    if (width > rectBounds.width) {
        width = rectBounds.width
    }

    if (height > rectBounds.height) {
        height = rectBounds.height
    }

    var rect = Rect(offset = rectCurrent.topLeft, size = Size(width, height))

    if (rect.left < rectBounds.left) {
        rect = rect.translate(rectBounds.left - rect.left, 0f)
    }

    if (rect.top < rectBounds.top) {
        rect = rect.translate(0f, rectBounds.top - rect.top)
    }

    if (rect.right > rectBounds.right) {
        rect = rect.translate(rectBounds.right - rect.right, 0f)
    }

    if (rect.bottom > rectBounds.bottom) {
        rect = rect.translate(0f, rectBounds.bottom - rect.bottom)
    }

    return rect
}

/**
 * Update draw rect based on user touch
 */
fun updateDrawRect(
    distanceToEdgeFromTouch: Offset,
    touchRegion: TouchRegion,
    minDimension: Float,
    rectTemp: Rect,
    rectDraw: Rect,
    change: PointerInputChange
): Rect {

    val position = change.position
    // Get screen coordinates from touch position inside composable
    // and add how far it's from corner to not jump edge to user's touch position
    val screenPositionX = position.x + distanceToEdgeFromTouch.x
    val screenPositionY = position.y + distanceToEdgeFromTouch.y

    return when (touchRegion) {

        // Corners
        TouchRegion.TopLeft -> {

            // Set position of top left while moving with top left handle and
            // limit position to not intersect other handles
            val left = screenPositionX.coerceAtMost(rectTemp.right - minDimension)
            val top = screenPositionY.coerceAtMost(rectTemp.bottom - minDimension)
            Rect(
                left = left,
                top = top,
                right = rectTemp.right,
                bottom = rectTemp.bottom
            )
        }

        TouchRegion.BottomLeft -> {

            // Set position of top left while moving with bottom left handle and
            // limit position to not intersect other handles
            val left = screenPositionX.coerceAtMost(rectTemp.right - minDimension)
            val bottom = screenPositionY.coerceAtLeast(rectTemp.top + minDimension)
            Rect(
                left = left,
                top = rectTemp.top,
                right = rectTemp.right,
                bottom = bottom,
            )

        }

        TouchRegion.TopRight -> {

            // Set position of top left while moving with top right handle and
            // limit position to not intersect other handles
            val right = screenPositionX.coerceAtLeast(rectTemp.left + minDimension)
            val top = screenPositionY.coerceAtMost(rectTemp.bottom - minDimension)

            Rect(
                left = rectTemp.left,
                top = top,
                right = right,
                bottom = rectTemp.bottom,
            )

        }

        TouchRegion.BottomRight -> {

            // Set position of top left while moving with bottom right handle and
            // limit position to not intersect other handles
            val right = screenPositionX.coerceAtLeast(rectTemp.left + minDimension)
            val bottom = screenPositionY.coerceAtLeast(rectTemp.top + minDimension)

            Rect(
                left = rectTemp.left,
                top = rectTemp.top,
                right = right,
                bottom = bottom
            )
        }

        TouchRegion.Inside -> {
            val drag = change.positionChange()

            val scaledDragX = drag.x
            val scaledDragY = drag.y

            rectDraw.translate(scaledDragX, scaledDragY)
        }

        else -> rectDraw
    }
}
