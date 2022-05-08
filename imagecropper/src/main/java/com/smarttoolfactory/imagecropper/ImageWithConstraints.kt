package com.smarttoolfactory.imagecropper

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.*


/**
 * A composable that lays out and draws a given [ImageBitmap]. This will attempt to
 * size the composable according to the [ImageBitmap]'s given width and height.
 *
 * * [ImageScope] contains [Constraints] since [ImageWithConstraints] uses [BoxWithConstraints]
 * also it contains information about canvas width, height and top left position relative
 * to parent [BoxWithConstraints].
 *
 * @param alignment determines where image will be aligned inside [BoxWithConstraints]
 * This is observable when bitmap image/width ratio differs from [Canvas] that draws [ImageBitmap]
 * @param contentDescription
 * @param contentScale how image should be scaled inside Canvas to match parent dimensions.
 * [ImageScale.Fit] for instance maintains src ratio and scales image to fit inside the parent.
 * @param alpha Opacity to be applied to [bitmap] from 0.0f to 1.0f representing
 * fully transparent to fully opaque respectively
 * @param colorFilter ColorFilter to apply to the [bitmap] when drawn into the destination
 * @param filterQuality Sampling algorithm applied to the [bitmap] when it is scaled and drawn
 * into the destination. The default is [FilterQuality.Low] which scales using a bilinear
 * sampling algorithm
 * @param content is a Composable that can be matched at exact position where [bitmap] is drawn.
 * This is useful for drawing thumbs, cropping or another layout that should match position
 * with the image that is scaled is drawn
 */
@Composable
fun ImageWithConstraints(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = null,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    content: @Composable ImageScope.() -> Unit = {}
) {

    val semantics = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }

    BoxWithConstraints(
        modifier = modifier
            .then(semantics),
        contentAlignment = alignment,
    ) {

        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        val (boxWidth: Int, boxHeight: Int) = getParentSize(bitmapWidth, bitmapHeight)

        // Src is Bitmap, Dst is the container(Image) that Bitmap will be displayed
        val srcSize = Size(bitmapWidth.toFloat(), bitmapHeight.toFloat())
        val dstSize = Size(boxWidth.toFloat(), boxHeight.toFloat())

        val scaleFactor = contentScale.computeScaleFactor(srcSize, dstSize)

        // Image is the container for bitmap that is located inside Box
        // image bounds can be smaller or bigger than its parent based on how it's scaled
        val imageWidth = bitmapWidth * scaleFactor.scaleX
        val imageHeight = bitmapHeight * scaleFactor.scaleY

        // Get scale of box to width of the image, image
        // We need a rect that contains Bitmap bounds to pass if any child requires it
        // For a image with 100x100 px with 300x400 px container and image with crop 400x400px
        // So we need to pass top left as 0,50 and size
        val scaledBitmapX = boxWidth / imageWidth
        val scaledBitmapY = boxHeight / imageHeight

        val topLeft = IntOffset(
            x = (bitmapWidth * (imageWidth - boxWidth) / imageWidth / 2)
                .coerceAtLeast(0f).toInt(),
            y = (bitmapHeight * (imageHeight - boxHeight) / imageHeight / 2)
                .coerceAtLeast(0f).toInt()
        )

        val size = IntSize(
            width = (bitmapWidth * scaledBitmapX).toInt().coerceAtMost(bitmapWidth),
            height = (bitmapHeight * scaledBitmapY).toInt().coerceAtMost(bitmapHeight)
        )

        val scaledImageRect = IntRect(offset = topLeft, size = size)

        ImageLayout(
            constraints = constraints,
            bitmap = bitmap,
            scaleX = scaleFactor.scaleX,
            scaleY = scaleFactor.scaleY,
            scaledImageRect = scaledImageRect,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality,
            content = content
        )
    }
}

@Composable
private fun BoxWithConstraintsScope.getParentSize(
    bitmapWidth: Int,
    bitmapHeight: Int
): IntSize {
    // Check if Composable has fixed size dimensions
    val hasBoundedDimens = constraints.hasBoundedWidth && constraints.hasBoundedHeight
    // Check if Composable has infinite dimensions
    val hasFixedDimens = constraints.hasFixedWidth && constraints.hasFixedHeight

    // Box is the parent(BoxWithConstraints) that contains Canvas under the hood
    // Canvas aspect ratio or size might not match parent but it's upper bounds are
    // what are passed from parent. Canvas cannot be bigger or taller than BoxWithConstraints
    val boxWidth: Int = if (hasBoundedDimens || hasFixedDimens) {
        constraints.maxWidth
    } else {
        constraints.minWidth.coerceAtLeast(bitmapWidth)
    }
    val boxHeight: Int = if (hasBoundedDimens || hasFixedDimens) {
        constraints.maxHeight
    } else {
        constraints.minHeight.coerceAtLeast(bitmapHeight)
    }
    return IntSize(boxWidth, boxHeight)
}

@Composable
private fun ImageLayout(
    constraints: Constraints,
    bitmap: ImageBitmap,
    scaleX: Float,
    scaleY: Float,
    scaledImageRect: IntRect,
    imageWidth: Float,
    imageHeight: Float,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    content: @Composable ImageScope.() -> Unit
) {
    val density = LocalDensity.current
    val bitmapWidth = bitmap.width
    val bitmapHeight = bitmap.height

    // Dimensions of canvas that will draw this Bitmap
    val canvasWidthInDp: Dp
    val canvasHeightInDp: Dp

    with(density) {
        canvasWidthInDp = bitmapWidth * scaleX.toDp()
        canvasHeightInDp = bitmapHeight * scaleY.toDp()
    }

    val imageScopeImpl = remember(key1 = constraints) {
        ImagesScopeImpl(
            density = density,
            constraints = constraints,
            imageWidth = canvasWidthInDp,
            imageHeight = canvasHeightInDp,
            rect = scaledImageRect
        )
    }

    ImageImpl(
        modifier = Modifier.size(canvasWidthInDp, canvasHeightInDp),
        bitmap = bitmap,
        alpha = alpha,
        width = imageWidth.toInt(),
        height = imageHeight.toInt(),
        colorFilter = colorFilter,
        filterQuality = filterQuality
    )

    imageScopeImpl.content()
}


@Composable
private fun ImageImpl(
    modifier: Modifier,
    bitmap: ImageBitmap,
    width: Int,
    height: Int,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
) {
    val bitmapWidth = bitmap.width
    val bitmapHeight = bitmap.height

    Canvas(modifier = modifier.clipToBounds()) {

        val canvasWidth = size.width.toInt()
        val canvasHeight = size.height.toInt()

        // Translate to left or down when Image size is bigger than this canvas.
        // ImageSize is bigger when scale mode like Crop is used
        translate(
            top = (-height + canvasHeight) / 2f,
            left = (-width + canvasWidth) / 2f,

            ) {
            drawImage(
                bitmap,
                srcSize = IntSize(bitmapWidth, bitmapHeight),
                dstSize = IntSize(width, height),
                alpha = alpha,
                colorFilter = colorFilter,
                filterQuality = filterQuality
            )
        }
    }
}


/**
 * Receiver scope being used by the children parameter of [ImageWithConstraints]
 */
@Stable
interface ImageScope {
    /**
     * The constraints given by the parent layout in pixels.
     *
     * Use [minWidth], [maxWidth], [minHeight] or [maxHeight] if you need value in [Dp].
     */
    val constraints: Constraints

    /**
     * The minimum width in [Dp].
     *
     * @see constraints for the values in pixels.
     */
    val minWidth: Dp

    /**
     * The maximum width in [Dp].
     *
     * @see constraints for the values in pixels.
     */
    val maxWidth: Dp

    /**
     * The minimum height in [Dp].
     *
     * @see constraints for the values in pixels.
     */
    val minHeight: Dp

    /**
     * The maximum height in [Dp].
     *
     * @see constraints for the values in pixels.
     */
    val maxHeight: Dp

    /**
     * Width of Image that is scaled based on [ContentScale]
     */
    val imageWidth: Dp

    /**
     * Height of Image that is scaled based on [ContentScale]
     */
    val imageHeight: Dp

    /**
     * [IntRect] that covers boundaries of [ImageBitmap]
     */
    val rect: IntRect
}

private data class ImagesScopeImpl(
    private val density: Density,
    override val constraints: Constraints,
    override val imageWidth: Dp,
    override val imageHeight: Dp,
    override val rect: IntRect,
) : ImageScope {

    override val minWidth: Dp get() = with(density) { constraints.minWidth.toDp() }

    override val maxWidth: Dp
        get() = with(density) {
            if (constraints.hasBoundedWidth) constraints.maxWidth.toDp() else Dp.Infinity
        }

    override val minHeight: Dp get() = with(density) { constraints.minHeight.toDp() }

    override val maxHeight: Dp
        get() = with(density) {
            if (constraints.hasBoundedHeight) constraints.maxHeight.toDp() else Dp.Infinity
        }
}
