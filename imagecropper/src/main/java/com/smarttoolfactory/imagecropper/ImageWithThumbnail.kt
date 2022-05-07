package com.smarttoolfactory.imagecropper

import android.graphics.Bitmap
import androidx.annotation.IntRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isFinite
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.consumeDownChange
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.gesture.pointerMotionEvents
import kotlin.math.roundToInt

/**
 * [ScalableImage] with [ThumbnailLayout] displays thumbnail of bitmap it draws.
 */
@Composable
fun ImageWithThumbnail(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap,
    imageScale: ImageScale = ImageScale.Fit,
    alignment: Alignment = Alignment.Center,
    contentDescription: String?,
    thumbnailSize: Dp = 80.dp,
    @IntRange(from = 100, to = 500) thumbnailZoom: Int=200,
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

        val scaledBitmap = if (bitmap.width == rect.width && bitmap.height == rect.height) {
            bitmap
        } else {
            remember(this) {
                Bitmap.createBitmap(
                    bitmap.asAndroidBitmap(),
                    rect.left,
                    rect.top,
                    rect.width,
                    rect.height

                ).asImageBitmap()
            }
        }

        println("Bitmap: $bitmap, scaledBitmap: $scaledBitmap")

        ThumbnailLayout(
            modifier = Modifier
                .size(this.imageWidth, this.imageHeight)
                .border(5.dp, Color.Cyan),
            bitmap = scaledBitmap,
            thumbnailSize = thumbnailSize,
            thumbnailZoom = thumbnailZoom,
            onTouchEvent = onTouchEvent,
            onThumbCenterChange = onThumbnailCenterChange,
        )

        DisposableEffect(key1 = Unit) {
            onDispose {
                if (!scaledBitmap.asAndroidBitmap().isRecycled) {
                    scaledBitmap.asAndroidBitmap().recycle()
                }
            }
        }
    }
}

@Composable
fun ThumbnailLayout(
    modifier: Modifier,
    bitmap: ImageBitmap,
    thumbnailSize: Dp,
    thumbnailZoom: Int = 200,
    offset: Offset,
    onThumbCenterChange: ((Offset) -> Unit)? = null
) {
    ThumbnailLayoutImpl(
        modifier = modifier,
        bitmap = bitmap,
        thumbnailSize = thumbnailSize,
        thumbnailZoom = thumbnailZoom,
        offset = offset,
        onThumbCenterChange = onThumbCenterChange
    )
}

@Composable
fun ThumbnailLayout(
    modifier: Modifier,
    bitmap: ImageBitmap,
    thumbnailSize: Dp,
    thumbnailZoom: Int = 200,
    onTouchEvent: ((Offset) -> Unit)? = null,
    onThumbCenterChange: ((Offset) -> Unit)? = null
) {

    BoxWithConstraints(modifier) {
        var offset by remember { mutableStateOf(Offset.Infinite) }

        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()


        val thumbnailModifier = Modifier
            .pointerMotionEvents(Unit,
                onDown = { pointerInputChange ->

                    val offsetX = pointerInputChange.position.x.coerceIn(0f, width)
                    val offsetY = pointerInputChange.position.y.coerceIn(0f, height)

                    offset = Offset(offsetX, offsetY)
                    onTouchEvent?.invoke(offset)

                    pointerInputChange.consumeDownChange()
                },
                onMove = { pointerInputChange ->

                    val offsetX = pointerInputChange.position.x.coerceIn(0f, width)
                    val offsetY = pointerInputChange.position.y.coerceIn(0f, height)

                    offset = Offset(offsetX, offsetY)
                    onTouchEvent?.invoke(offset)

                    pointerInputChange.consumePositionChange()
                }
            )

        ThumbnailLayoutImpl(
            modifier = thumbnailModifier.fillMaxSize(),
            bitmap = bitmap,
            thumbnailSize = thumbnailSize,
            thumbnailZoom=thumbnailZoom,
            offset = offset,
            onThumbCenterChange = onThumbCenterChange
        )
    }
}

@Composable
private fun ThumbnailLayoutImpl(
    modifier: Modifier,
    bitmap: ImageBitmap,
    thumbnailSize: Dp,
    thumbnailZoom: Int = 200,
    offset: Offset,
    onThumbCenterChange: ((Offset) -> Unit)? = null
) {
    var center by remember { mutableStateOf(Offset.Zero) }

    Canvas(modifier = modifier) {

        val canvasWidth = size.width
        val canvasHeight = size.height

        val offsetX = offset.x
            .coerceIn(0f, canvasWidth)
        val offsetY = offset.y
            .coerceIn(0f, canvasHeight)

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

        val zoom = thumbnailZoom.coerceAtLeast(100)
        val zoomScale = zoom / 100

        // Setting offset for src moves the position in Bitmap
        // Bitmap is SRC while where we draw is DST.
        // Setting offset of dst moves where we draw in Canvas
        // Setting src moves to which part of the bitmap we draw
        // Coercing at right bound (bitmap.width - imageThumbnailSize) lets to limit offset
        // to thumbnailSize when user moves pointer to right.
        // If image has 100px width and thumbnail 10 when user moves to 95 we see a width with 5px
        // coercing lets you keep 10px all the time
//        val srcOffsetX =
//            (offsetX * bitmap.width / canvasWidth - imageThumbnailSize / zoomScale / 2).toInt()
//                .coerceIn(0, bitmap.width - imageThumbnailSize / zoomScale)
//        val srcOffsetY =
//            (offsetY * bitmap.height / canvasHeight - imageThumbnailSize / zoomScale / 2).toInt()
//                .coerceIn(0, bitmap.height - imageThumbnailSize / zoomScale)


        val srcOffset = getSrcOffset(
            offset = offset,
            bitmap = bitmap,
            zoomScale = zoomScale,
            size = size,
            imageThumbnailSize = imageThumbnailSize
        )
        val srcOffsetX = srcOffset.x
        val srcOffsetY = srcOffset.y

        println(
            "😍 ThumbnailLayout() offset: $offset, offsetX: $offsetX, offsetY: $offsetY\n" +
                    "srcOffsetX $srcOffsetX, srcOffsetY: $srcOffsetY, " +
                    "canvasWidth: $canvasWidth, canvasHeight: $canvasHeight\n" +
                    "topLeftImageThumbX: $topLeftImageThumbX, imageThumbSize: $imageThumbnailSize"
        )
        drawImage(
            image = bitmap,
            srcOffset = IntOffset(x = srcOffsetX, y = srcOffsetY),
            srcSize = IntSize(imageThumbnailSize / zoomScale, imageThumbnailSize / zoomScale),
            dstOffset = IntOffset(x = topLeftImageThumbX, y = 0),
            dstSize = IntSize(imageThumbnailSize, imageThumbnailSize)
        )
    }
}

private fun getThumbnailPosition(){

}

private fun getSrcOffset(
    offset: Offset,
    bitmap: ImageBitmap,
    zoomScale: Int,
    size: Size,
    imageThumbnailSize: Int
): IntOffset {

    val canvasWidth = size.width
    val canvasHeight = size.height

    val bitmapWidth = bitmap.width
    val bitmapHeight = bitmap.height

    val offsetX = offset.x
        .coerceIn(0f, canvasWidth)
    val offsetY = offset.y
        .coerceIn(0f, canvasHeight)

    // Setting offset for src moves the position in Bitmap
    // Bitmap is SRC while where we draw is DST.
    // Setting offset of dst moves where we draw in Canvas
    // Setting src moves to which part of the bitmap we draw
    // Coercing at right bound (bitmap.width - imageThumbnailSize) lets to limit offset
    // to thumbnailSize when user moves pointer to right.
    // If image has 100px width and thumbnail 10 when user moves to 95 we see a width with 5px
    // coercing lets you keep 10px all the time
    val srcOffsetX =
        (offsetX * bitmapWidth / canvasWidth - imageThumbnailSize / zoomScale / 2).toInt()
            .coerceIn(0, bitmapWidth - imageThumbnailSize / zoomScale)
    val srcOffsetY =
        (offsetY * bitmapHeight / canvasHeight - imageThumbnailSize / zoomScale / 2).toInt()
            .coerceIn(0, bitmapHeight - imageThumbnailSize / zoomScale)

    return IntOffset(srcOffsetX, srcOffsetY)
}
