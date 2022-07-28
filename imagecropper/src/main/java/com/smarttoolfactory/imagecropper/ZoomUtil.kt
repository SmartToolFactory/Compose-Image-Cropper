package com.smarttoolfactory.imagecropper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import com.smarttoolfactory.image.transform.TouchRegion
import com.smarttoolfactory.image.zoom.ZoomLevel

/**
 * Calculate zoom level and zoom value when user double taps
 */
internal fun calculateZoom(
    zoomLevel: ZoomLevel,
    initial: Float,
    min: Float,
    max: Float
): Pair<ZoomLevel, Float> {

    val newZoomLevel: ZoomLevel
    val newZoom: Float

    when (zoomLevel) {
        ZoomLevel.Initial -> {
            newZoomLevel = ZoomLevel.Max
            newZoom = max.coerceAtMost(3f)
        }
        ZoomLevel.Max -> {
            newZoomLevel = ZoomLevel.Min
            newZoom = if (min == initial) (min + max.coerceAtMost(3f)) / 2 else min
        }
        else -> {
            newZoomLevel = ZoomLevel.Initial
            newZoom = initial.coerceAtMost(2f)
        }
    }
    return Pair(newZoomLevel, newZoom)
}

/**
 * Get rectangle of current transformation of [pan], [zoom] and current bounds of the Composable's
 * selected area as [rectBounds]
 */
fun getCropRect(
    bitmapWidth: Int,
    bitmapHeight: Int,
    imageWidth: Float,
    imageHeight: Float,
    pan: Offset,
    zoom: Float,
    rectBounds: Rect
): Rect {
    val widthRatio = bitmapWidth / imageWidth
    val heightRatio = bitmapHeight / imageHeight

    val width = (widthRatio * rectBounds.width / zoom).coerceIn(0f, imageWidth)
    val height = (heightRatio * rectBounds.height / zoom).coerceIn(0f, imageHeight)

    val offsetXInBitmap = (widthRatio * (pan.x + rectBounds.left / zoom))
        .coerceIn(0f, imageWidth)
    val offsetYInBitmap = heightRatio * (pan.y + rectBounds.top / zoom)
        .coerceIn(0f, imageHeight)

    return Rect(
        offset = Offset(offsetXInBitmap, offsetYInBitmap),
        size = Size(width, height)
    )
}

/**
 * Update graphic layer with [zoomState]
 */
internal fun GraphicsLayerScope.update(zoomState: ZoomState) {

    // Set zoom
    val zoom = zoomState.zoom
    this.scaleX = zoom
    this.scaleY = zoom

    // Set pan
    val pan = zoomState.pan
    val translationX = pan.x
    val translationY = pan.y
    this.translationX = translationX
    this.translationY = translationY

    // Set rotation
    this.rotationZ = zoomState.rotation
}

/**
 * Returns how far user touched to corner or center of sides of the screen. [TouchRegion]
 * where user exactly has touched is already passed to this function. For instance user
 * touched top left then this function returns distance to top left from user's position so
 * we can add an offset to not jump edge to position user touched.
 */
fun getDistanceToEdgeFromTouch(
    touchRegion: TouchRegion,
    rect: Rect,
    touchPosition: Offset
) = when (touchRegion) {
    // Corners
    TouchRegion.TopLeft -> {
        rect.topLeft - touchPosition
    }
    TouchRegion.TopRight -> {
        rect.topRight - touchPosition
    }
    TouchRegion.BottomLeft -> {
        rect.bottomLeft - touchPosition
    }
    TouchRegion.BottomRight -> {
        rect.bottomRight - touchPosition
    }
    else -> {
        Offset.Zero
    }
}

