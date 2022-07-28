package com.smarttoolfactory.imagecropper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize


/**
 * * Create and [remember] the [ZoomState] based on the currently appropriate transform
 * configuration to allow changing pan, zoom, and rotation.
 *
 *  [key1] is used to reset remember block to initial calculations. This can be used
 * when image, contentScale or any property changes which requires values to be reset to initial
 * values
 *
 * @param minZoom minimum zoom value this Composable can possess
 * @param maxZoom maximum zoom value this Composable can possess
 * @param rotationEnabled when set to true rotation is enabled
 */
@Composable
fun rememberCropState(
    imageSize: Size,
    bitmapSize: IntSize,
    minZoom: Float = 1f,
    maxZoom: Float = 5f,
    rotationEnabled: Boolean = false,
    key1: Any? = Unit
): CropState {
    return remember(key1) {
        CropState(
            imageSize = imageSize,
            bitmapSize = bitmapSize,
            minZoom = minZoom,
            maxZoom = maxZoom,
            rotationEnabled = rotationEnabled
        )
    }
}

/**
 * * Create and [remember] the [ZoomState] based on the currently appropriate transform
 * configuration to allow changing pan, zoom, and rotation.
 *
 *  [key1] or [key2] are used to reset remember block to initial calculations. This can be used
 * when image, contentScale or any property changes which requires values to be reset to initial
 * values
 *
 * @param minZoom minimum zoom value this Composable can possess
 * @param maxZoom maximum zoom value this Composable can possess
 * @param rotationEnabled when set to true rotation is enabled
 */
@Composable
fun rememberCropState(
    imageSize: Size,
    bitmapSize: IntSize,
    minZoom: Float = 1f,
    maxZoom: Float = 5f,
    rotationEnabled: Boolean = false,
    key1: Any?,
    key2: Any?,
): CropState {
    return remember(key1, key2) {
        CropState(
            imageSize = imageSize,
            bitmapSize = bitmapSize,
            minZoom = minZoom,
            maxZoom = maxZoom,
            rotationEnabled = rotationEnabled,
        )
    }
}

/**
 * * Create and [remember] the [ZoomState] based on the currently appropriate transform
 * configuration to allow changing pan, zoom, and rotation.
 *
 * @param minZoom minimum zoom value this Composable can possess
 * @param maxZoom maximum zoom value this Composable can possess
 * @param rotationEnabled when set to true rotation is enabled
 * @param keys are used to reset remember block to initial calculations. This can be used
 * when image, contentScale or any property changes which requires values to be reset to initial
 * values
 */
@Composable
fun rememberCropState(
    imageSize: Size,
    bitmapSize: IntSize,
    minZoom: Float = 1f,
    maxZoom: Float = 5f,
    rotationEnabled: Boolean = false,
    vararg keys: Any?
): CropState {
    return remember(keys) {
        CropState(
            imageSize = imageSize,
            bitmapSize = bitmapSize,
            minZoom = minZoom,
            maxZoom = maxZoom,
            rotationEnabled = rotationEnabled
        )
    }
}

/**
 *  * State of the zoom. Allows the developer to change zoom, pan,  translate,
 *  or get current state by
 * calling methods on this object. To be hosted and passed to [Modifier.zoom]
 * @param limitPan limits pan to bounds of parent Composable. Using this flag prevents creating
 * empty space on sides or edges of parent.

 * @param zoomEnabled when set to true zoom is enabled
 * @param panEnabled when set to true pan is enabled
 * @param rotationEnabled when set to true rotation is enabled
 */
open class CropState constructor(
    var imageSize: Size,
    var bitmapSize: IntSize,
    minZoom: Float = .5f,
    maxZoom: Float = 5f,
    override var zoomEnabled: Boolean = true,
    override var panEnabled: Boolean = true,
    override var rotationEnabled: Boolean = true,
    override var limitPan: Boolean = true
) : ZoomState(
    1f, 0f, minZoom, maxZoom, zoomEnabled
) {

    val isZooming = animatableZoom.value != animatableZoom.targetValue
    val isPanning = animatablePan.value != animatablePan.targetValue
    val isRotating = animatableRotation.value != animatableRotation.targetValue

    var rectDraw = Rect(offset = Offset.Zero, size = imageSize)
    var rectImage = rectDraw.copy()
    var rectCrop = rectDraw

    val cropData: CropData
        get() = CropData(
            zoom = zoom,
            pan = pan,
            rotation = rotation,
            drawRect = rectDraw,
            cropRect = rectCrop
        )


    override suspend fun updateZoomState(
        size: IntSize,
        gesturePan: Offset,
        gestureZoom: Float,
        gestureRotate: Float,
    ) {
        val zoom = (zoom * gestureZoom).coerceIn(zoomMin, zoomMax)
        val rotation = if (rotationEnabled) {
            rotation + gestureRotate
        } else {
            0f
        }

        if (panEnabled) {
            val offset = pan
            var newOffset = offset + gesturePan.times(zoom)
            val boundPan = limitPan && !rotationEnabled

            if (boundPan) {
                val maxX = (size.width * (zoom - 1) / 2f)
                    .coerceAtLeast(0f)
                val maxY = (size.height * (zoom - 1) / 2f)
                    .coerceAtLeast(0f)
                newOffset = Offset(
                    newOffset.x.coerceIn(-maxX, maxX),
                    newOffset.y.coerceIn(-maxY, maxY)
                )
            }
            snapPanTo(newOffset)
            rectImage.translate(newOffset)
        }

        if (zoomEnabled) {
            snapZoomTo(zoom)
        }

        if (rotationEnabled) {
            snapRotationTo(rotation)
        }
    }
}