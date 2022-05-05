package com.smarttoolfactory.imagecropper

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isFinite
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.consumeDownChange
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.gesture.pointerMotionEvents
import kotlin.math.roundToInt

/**
 * [ScalableImage] with [ThumbnailLayout] that display thumbnail of bitmap it displays.
 */
@Composable
fun ImageWithThumbnail(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap,
    imageScale: ImageScale = ImageScale.Fit,
    alignment: Alignment = Alignment.Center,
    contentDescription: String?,
    thumbnailSize: Dp = 80.dp,
    onTouchEvent: ((Offset) -> Unit)? = null,
    onThumbnailCenterChange: ((Offset) -> Unit)? = null
) {

    ScalableImage(
        modifier = modifier,
        imageScale = imageScale,
        alignment = alignment,
        contentDescription = contentDescription,
        bitmap = bitmap,
    ) {

        ThumbnailLayout(
            modifier = Modifier
                .size(this.imageWidth, this.imageHeight)
                .border(5.dp, Color.Cyan),
            bitmap = bitmap,
            thumbnailSize = thumbnailSize,
            onTouchEvent = onTouchEvent,
            onThumbCenterChange = onThumbnailCenterChange,
        )
    }
}

@Composable
fun ThumbnailLayout(
    modifier: Modifier,
    bitmap: ImageBitmap,
    thumbnailSize: Dp,
    offset: Offset,
    onThumbCenterChange: ((Offset) -> Unit)? = null
) {
    ThumbnailLayoutImpl(
        modifier = modifier,
        bitmap = bitmap,
        thumbnailSize = thumbnailSize,
        offset = offset,
        onThumbCenterChange = onThumbCenterChange
    )
}

@Composable
fun ThumbnailLayout(
    modifier: Modifier,
    bitmap: ImageBitmap,
    thumbnailSize: Dp,
    onTouchEvent: ((Offset) -> Unit)? = null,
    onThumbCenterChange: ((Offset) -> Unit)? = null
) {

    var offset by remember { mutableStateOf(Offset.Infinite) }

    val thumbnailModifier = modifier
        .pointerMotionEvents(Unit,
            onDown = { pointerInputChange ->

                val offsetX = pointerInputChange.position.x
                val offsetY = pointerInputChange.position.y
                offset = Offset(offsetX, offsetY)
                onTouchEvent?.invoke(offset)

                pointerInputChange.consumeDownChange()
            },
            onMove = { pointerInputChange ->

                val offsetX = pointerInputChange.position.x
                val offsetY = pointerInputChange.position.y
                offset = Offset(offsetX, offsetY)
                onTouchEvent?.invoke(offset)

                pointerInputChange.consumePositionChange()
            }
        )

    ThumbnailLayoutImpl(
        modifier = thumbnailModifier,
        bitmap = bitmap,
        thumbnailSize = thumbnailSize,
        offset = offset,
        onThumbCenterChange = onThumbCenterChange
    )
}

@Composable
private fun ThumbnailLayoutImpl(
    modifier: Modifier,
    bitmap: ImageBitmap,
    thumbnailSize: Dp,
    offset: Offset,
    onThumbCenterChange: ((Offset) -> Unit)? = null
) {
    var center by remember { mutableStateOf(Offset.Zero) }

    Canvas(modifier = modifier) {

        val canvasWidth = size.width
        val canvasHeight = size.height

        val offsetX = offset.x.coerceIn(0f, canvasWidth)
        val offsetY = offset.y.coerceIn(0f, canvasHeight)

        // Get thumbnail size as parameter but limit max size to minimum of canvasWidth and Height
        val imageThumbnailSize: Int =
            thumbnailSize.toPx()
                .coerceAtMost(canvasWidth.coerceAtLeast(canvasHeight)).roundToInt()

        // If we are close by 25% of dimension of display on left side
        // move to right side to display image on top lef
        val isTouchOnLeftSide = if (offset.isFinite) {
            (offsetX < imageThumbnailSize * 5 / 4 &&
                    offsetY < imageThumbnailSize * 5 / 4)
        } else true

        // top left x coordinate of image thumb which can be either left or
        // right side based on user touch position
        val topLeftImageThumbX: Int = if (offset.isFinite) {
            if (isTouchOnLeftSide)
                (canvasWidth - imageThumbnailSize).coerceAtLeast(0f).toInt() else 0
        } else 0

        // Center of  thumbnail
        val centerX: Float = topLeftImageThumbX + imageThumbnailSize / 2f
        val centerY: Float = imageThumbnailSize / 2f

        if (center.x != centerX || center.y != centerY) {
            center = Offset(centerX, centerY)
            onThumbCenterChange?.invoke(Offset(centerX, centerY))
        }

        // Setting offset for src moves the position in Bitmap
        // Bitmap is SRC while where we draw is DST.
        // Setting offset of dst moves where we draw in Canvas
        // Setting src moves to which part of the bitmap we draw
        // Coercing at right bound (bitmap.width - imageThumbnailSize) lets to limit offset
        // to thumbnailSize when user moves pointer to right.
        // If image has 100px width and thumbnail 10 when user moves to 95 we see a width with 5px
        // coercing lets you keep 10px all the time
        val srcOffsetX = (offsetX * bitmap.width / canvasWidth - imageThumbnailSize / 4).toInt()
            .coerceIn(0, bitmap.width - imageThumbnailSize)
        val srcOffsetY = (offsetY * bitmap.height / canvasHeight - imageThumbnailSize / 4).toInt()
            .coerceIn(0, bitmap.height - imageThumbnailSize)

        println(
            "😍 ThumbnailLayout() offset: $offset, offsetX: $offsetX, offsetY: $offsetY\n" +
                    "srcOffsetX $srcOffsetX, srcOffsetY: $srcOffsetY, " +
                    "canvasWidth: $canvasWidth, canvasHeight: $canvasHeight\n" +
                    "topLeftImageThumbX: $topLeftImageThumbX, imageThumbSize: $imageThumbnailSize"
        )
        drawImage(
            image = bitmap,
            srcOffset = IntOffset(x = srcOffsetX, y = srcOffsetY),
            srcSize = IntSize(imageThumbnailSize / 2, imageThumbnailSize / 2),
            dstOffset = IntOffset(x = topLeftImageThumbX, y = 0),
            dstSize = IntSize(imageThumbnailSize, imageThumbnailSize)
        )
    }
}
