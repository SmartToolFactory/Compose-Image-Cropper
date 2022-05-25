package com.smarttoolfactory.imagecropper

import android.graphics.Bitmap
import androidx.annotation.IntRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.gesture.pointerMotionEvents
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * [ImageWithConstraints] with [ThumbnailLayout] displays thumbnail of bitmap it draws in corner specified
 * by [thumbnailPosition]. When touch position is close to thumbnail position if [moveableThumbnail]
 * is set to true moves thumbnail to corner specified by [moveTo]
 *
 * @param imageBitmap The [ImageBitmap] to draw
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be used
 * if the bounds are a different size from the intrinsic size of the [ImageBitmap]
 * @param alignment Optional alignment parameter used to place the [ImageBitmap] in the given
 * bounds defined by the width and height
 * @param contentDescription
 * @param alpha Optional opacity to be applied to the [ImageBitmap] when it is rendered onscreen
 * @param colorFilter Optional ColorFilter to apply for the [ImageBitmap] when it is rendered
 * onscreen
 * @param filterQuality Sampling algorithm applied to the [imageBitmap] when it is scaled and drawn
 * into the destination. The default is [FilterQuality.Low] which scales using a bilinear
 * sampling algorithm
 * @param thumbnailSize size of the thumbnail
 * @param thumbnailPosition position of the thumbnail. It's top left corner by default
 * @param moveableThumbnail flag that changes mobility of thumbnail when user touch is
 * in proximity of the thumbnail
 * @param moveTo corner to move thumbnail if user touch is in proximity of the thumbnail. By default
 * it's top right corner.
 * @param thumbnailZoom zoom amount of thumbnail. It's in range of [100-500]. 100 corresponds
 * to 1x or 100% zoom
 * @param onDown callback that notifies at first interaction and returns offset
 * @param onMove callback that notifier user's touch position when a pointer is moving
 * @param onUp callback that notifies last pointer on screen is up
 * event occurs on this Composable
 * @param onThumbnailCenterChange callback to get center of thumbnail
 * @param content is an optional Composable that can be matched at exact position
 * where [imageBitmap] is drawn. content can be used for drawing over thumb or using [Offset] from
 * motion callbacks or displaying image, icon or watermark above this Composable with
 * exact bounds [imageBitmap] is drawn
 */
@Composable
fun ImageWithThumbnail(
    modifier: Modifier = Modifier,
    imageBitmap: ImageBitmap,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    contentDescription: String?,
    thumbnailSize: Dp = 80.dp,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    thumbnailPosition: ThumbnailPosition = ThumbnailPosition.TopLeft,
    moveableThumbnail: Boolean = true,
    moveTo: ThumbnailPosition = ThumbnailPosition.TopRight,
    @IntRange(from = 100, to = 500) thumbnailZoom: Int = 200,
    onDown: ((Offset) -> Unit)? = null,
    onMove: ((Offset) -> Unit)? = null,
    onUp: (() -> Unit)? = null,
    onThumbnailCenterChange: ((Offset) -> Unit)? = null,
    drawImage: Boolean = true,
    content: @Composable ImageScope.() -> Unit = {}
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
        drawImage = drawImage
    ) {

        val imageScope = this
        val density = LocalDensity.current

        val scaledImageBitmap = getScaledImageBitmap(imageBitmap, contentScale)

        val size = rememberUpdatedState(
            newValue = Size(
                imageWidth.value * density.density, imageHeight.value * density.density
            )
        )

        var offset by remember(key1 = contentScale, key2 = scaledImageBitmap) {
            mutableStateOf(
                Offset.Unspecified
            )
        }

        fun updateOffset(pointerInputChange: PointerInputChange): Offset {
            val offsetX = pointerInputChange.position.x
                .coerceIn(0f, size.value.width)
            val offsetY = pointerInputChange.position.y
                .coerceIn(0f, size.value.height)
            pointerInputChange.consume()
            return Offset(offsetX, offsetY)
        }

        val thumbnailModifier = Modifier
            .pointerMotionEvents(key1 = contentScale, key2 = scaledImageBitmap,
                onDown = { pointerInputChange: PointerInputChange ->
                    offset = updateOffset(pointerInputChange)
                    onDown?.invoke(offset)
                },
                onMove = { pointerInputChange: PointerInputChange ->
                    offset = updateOffset(pointerInputChange)
                    onMove?.invoke(offset)
                },
                onUp = {
                    onUp?.invoke()
                }
            )

        ThumbnailLayout(
            modifier = thumbnailModifier.size(this.imageWidth, this.imageHeight),
            imageBitmap = scaledImageBitmap,
            thumbnailSize = thumbnailSize,
            thumbnailZoom = thumbnailZoom,
            thumbnailPosition = thumbnailPosition,
            moveableThumbnail = moveableThumbnail,
            moveTo = moveTo,
            offset = offset,
            onThumbnailCenterChange = onThumbnailCenterChange
        )

        Box(
            modifier = Modifier
                .size(this.imageWidth, this.imageHeight),
        ) {
            imageScope.content()
        }
    }
}

/**
 * [ThumbnailLayout] displays thumbnail of bitmap it draws in corner specified
 * by [thumbnailPosition]. When touch position is close to thumbnail position if [moveableThumbnail]
 * is set to true moves thumbnail to corner specified by [moveTo]
 *
 * @param imageBitmap The [ImageBitmap] to draw
 * into the destination. The default is [FilterQuality.Low] which scales using a bilinear
 * sampling algorithm
 * @param thumbnailSize size of the thumbnail
 * @param thumbnailPosition position of the thumbnail. It's top left corner by default
 * @param moveableThumbnail flag that changes mobility of thumbnail when user touch is
 * in proximity of the thumbnail
 * @param moveTo corner to move thumbnail if user touch is in proximity of the thumbnail. By default
 * it's top right corner.
 * @param thumbnailZoom zoom amount of thumbnail. It's in range of [100-500]. 100 corresponds
 * to 1x or 100% zoom
 * @param onThumbnailCenterChange callback to get center of thumbnail
 */
@Composable
fun ThumbnailLayout(
    modifier: Modifier,
    imageBitmap: ImageBitmap,
    thumbnailSize: Dp = 80.dp,
    thumbnailPosition: ThumbnailPosition = ThumbnailPosition.TopLeft,
    moveableThumbnail: Boolean = true,
    moveTo: ThumbnailPosition = ThumbnailPosition.TopRight,
    @IntRange(from = 100, to = 500) thumbnailZoom: Int = 200,
    offset: Offset,
    onThumbnailCenterChange: ((Offset) -> Unit)? = null
) {
    ThumbnailLayoutImpl(
        modifier = modifier,
        imageBitmap = imageBitmap,
        thumbnailSize = thumbnailSize,
        thumbnailZoom = thumbnailZoom,
        thumbnailPosition = thumbnailPosition,
        moveableThumbnail = moveableThumbnail,
        moveTo = moveTo,
        offset = offset,
        onThumbnailCenterChange = onThumbnailCenterChange
    )
}

@Composable
private fun ThumbnailLayoutImpl(
    modifier: Modifier,
    imageBitmap: ImageBitmap,
    thumbnailSize: Dp,
    thumbnailZoom: Int = 200,
    thumbnailPosition: ThumbnailPosition = ThumbnailPosition.TopLeft,
    moveableThumbnail: Boolean = true,
    moveTo: ThumbnailPosition = ThumbnailPosition.TopRight,
    offset: Offset,
    onThumbnailCenterChange: ((Offset) -> Unit)? = null
) {

    Canvas(modifier = modifier) {

        val canvasWidth = size.width
        val canvasHeight = size.height

        // Get thumbnail size as parameter but limit max size to minimum of canvasWidth and Height
        val imageThumbnailSize: Int =
            thumbnailSize.toPx()
                .coerceAtMost(canvasWidth.coerceAtLeast(canvasHeight)).roundToInt()

        val thumbnailOffset = getThumbnailPositionOffset(
            offset = offset,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            imageThumbnailSize = imageThumbnailSize,
            thumbnailPosition = thumbnailPosition,
            moveableThumbnail = moveableThumbnail,
            moveTo = moveTo
        )

        // Center of  thumbnail
        val centerX: Float = thumbnailOffset.x + imageThumbnailSize / 2f
        val centerY: Float = thumbnailOffset.y + imageThumbnailSize / 2f
        onThumbnailCenterChange?.invoke(Offset(centerX, centerY))

        val zoom = thumbnailZoom.coerceAtLeast(100)
        val zoomScale = zoom / 100f

        val srcOffset = if (offset.isSpecified && offset.isFinite) {
            getSrcOffset(
                offset = offset,
                imageBitmap = imageBitmap,
                zoomScale = zoomScale,
                size = size,
                imageThumbnailSize = imageThumbnailSize
            )
        } else {
            IntOffset.Zero
        }

        drawImage(
            image = imageBitmap,
            srcOffset = srcOffset,
            srcSize = IntSize(
                width = (imageThumbnailSize / zoomScale).toInt(),
                height = (imageThumbnailSize / zoomScale).toInt()
            ),
            dstOffset = thumbnailOffset,
            dstSize = IntSize(imageThumbnailSize, imageThumbnailSize)
        )
    }
}

@Composable
internal fun ImageScope.getScaledImageBitmap(
    bitmap: ImageBitmap,
    contentScale: ContentScale
): ImageBitmap {
    val scaledBitmap =

        remember(bitmap, rect, imageWidth, imageHeight, contentScale) {
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
    return scaledBitmap
}

private fun getThumbnailPositionOffset(
    offset: Offset,
    canvasWidth: Float,
    canvasHeight: Float,
    thumbnailPosition: ThumbnailPosition = ThumbnailPosition.TopLeft,
    moveableThumbnail: Boolean = true,
    moveTo: ThumbnailPosition = ThumbnailPosition.TopRight,
    imageThumbnailSize: Int
): IntOffset {

    val thumbnailOffset = calculateThumbnailOffset(
        thumbnailPosition,
        canvasWidth,
        canvasHeight,
        imageThumbnailSize
    )

    if (offset.isUnspecified || !offset.isFinite) return thumbnailOffset
    if (!moveableThumbnail || thumbnailPosition == moveTo) return thumbnailOffset

    val offsetX = offset.x
        .coerceIn(0f, canvasWidth)
    val offsetY = offset.y
        .coerceIn(0f, canvasHeight)

    // Calculate distance from touch position to center of thumbnail
    val x = offsetX - (thumbnailOffset.x + imageThumbnailSize / 2)
    val y = offsetY - (thumbnailOffset.y + imageThumbnailSize / 2)
    val distanceToThumbnailCenter = sqrt(x * x + y * y)

    // pointer position is in bounds of thumbnail, calculate alternative position to move to
    return if (distanceToThumbnailCenter < imageThumbnailSize) {
        calculateThumbnailOffset(moveTo, canvasWidth, canvasHeight, imageThumbnailSize)
    } else {
        thumbnailOffset
    }
}

/**
 * Calculate thumbnail position based on which corner it's in
 */
private fun calculateThumbnailOffset(
    thumbnailPosition: ThumbnailPosition,
    canvasWidth: Float,
    canvasHeight: Float,
    imageThumbnailSize: Int
): IntOffset {
    return when (thumbnailPosition) {
        ThumbnailPosition.TopLeft -> {
            IntOffset(x = 0, y = 0)
        }
        ThumbnailPosition.TopRight -> {
            IntOffset(x = (canvasWidth - imageThumbnailSize).toInt(), y = 0)
        }

        ThumbnailPosition.BottomLeft -> {
            IntOffset(x = 0, y = (canvasHeight - imageThumbnailSize).toInt())
        }

        ThumbnailPosition.BottomRight -> {
            IntOffset(
                x = (canvasWidth - imageThumbnailSize).toInt(),
                y = (canvasHeight - imageThumbnailSize).toInt()
            )
        }
    }
}

/**
 * Get offset for Src. Src is the bitmap that will be drawn to canvas. Based on it's
 * size and offset any section or whole bitmap can be drawn.
 * Setting positive offset on x axis moves visible section of bitmap to the left.
 * @param offset pointer touch position
 * @param imageBitmap is image that will be drawn
 * @param zoomScale scale of zoom between [1f-5f]
 */
private fun getSrcOffset(
    offset: Offset,
    imageBitmap: ImageBitmap,
    zoomScale: Float,
    size: Size,
    imageThumbnailSize: Int
): IntOffset {

    val canvasWidth = size.width
    val canvasHeight = size.height

    val bitmapWidth = imageBitmap.width
    val bitmapHeight = imageBitmap.height

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
        (offsetX * bitmapWidth / canvasWidth - imageThumbnailSize / zoomScale / 2)
            .coerceIn(0f, bitmapWidth - imageThumbnailSize / zoomScale)
    val srcOffsetY =
        (offsetY * bitmapHeight / canvasHeight - imageThumbnailSize / zoomScale / 2)
            .coerceIn(0f, bitmapHeight - imageThumbnailSize / zoomScale)

    return IntOffset(srcOffsetX.toInt(), srcOffsetY.toInt())
}

enum class ThumbnailPosition {
    TopLeft, TopRight, BottomLeft, BottomRight
}