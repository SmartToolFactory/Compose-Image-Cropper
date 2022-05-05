package com.smarttoolfactory.imagecropper

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.*

enum class ImageScale {
    Fit, FillBounds
}

/**
 * A composable that lays out and draws a given [ImageBitmap]. This will attempt to
 * size the composable according to the [ImageBitmap]'s given width and height.
 *
 * * [ScalableImageScope] contains [Constraints] since [ScalableImage] uses [BoxWithConstraints]
 * also it contains information about canvas width, height and top left position relative
 * to parent [BoxWithConstraints]
 *
 * @param alignment determines where image will be aligned inside [BoxWithConstraints]
 * This is observable when bitmap image/width ratio differs from [Canvas] that draws [ImageBitmap]
 * @param contentDescription
 * @param imageScale how image should be scaled inside Canvas to match parent dimensions.
 * [ImageScale.Fit] for instance maintains src ratio and scales image to fit inside the parent.
 * @param alpha Opacity to be applied to [bitmap] from 0.0f to 1.0f representing
 * fully transparent to fully opaque respectively
 * @param colorFilter ColorFilter to apply to the [bitmap] when drawn into the destination
 * @param filterQuality Sampling algorithm applied to the [bitmap] when it is scaled and drawn
 * into the destination. The default is [FilterQuality.Low] which scales using a bilinear
 * sampling algorithm
 */
@Composable
fun ScalableImage(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap,
    alignment: Alignment = Alignment.Center,
    contentDescription: String?,
    imageScale: ImageScale = ImageScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality
) {

    ScalableImage(
        modifier = modifier,
        bitmap = bitmap,
        alignment = alignment,
        imageScale = imageScale,
        contentDescription = contentDescription,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality,
        content = {})
}

/**
 * A composable that lays out and draws a given [ImageBitmap]. This will attempt to
 * size the composable according to the [ImageBitmap]'s given width and height.
 *
 * * [ScalableImageScope] contains [Constraints] since [ScalableImage] uses [BoxWithConstraints]
 * also it contains information about canvas width, height and top left position relative
 * to parent [BoxWithConstraints].
 *
 * @param alignment determines where image will be aligned inside [BoxWithConstraints]
 * This is observable when bitmap image/width ratio differs from [Canvas] that draws [ImageBitmap]
 * @param contentDescription
 * @param imageScale how image should be scaled inside Canvas to match parent dimensions.
 * [ImageScale.Fit] for instance maintains src ratio and scales image to fit inside the parent.
 * @param alpha Opacity to be applied to [bitmap] from 0.0f to 1.0f representing
 * fully transparent to fully opaque respectively
 * @param colorFilter ColorFilter to apply to the [bitmap] when drawn into the destination
 * @param filterQuality Sampling algorithm applied to the [bitmap] when it is scaled and drawn
 * into the destination. The default is [FilterQuality.Low] which scales using a bilinear
 * sampling algorithm
 * @param content is a Composable that can be matched at exact position where image is drawn.
 * This is useful for drawing thumbs, cropping or another layout that should match position
 * with the image that is scaled is drawn
 */
@Composable
fun ScalableImage(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap,
    alignment: Alignment = Alignment.Center,
    imageScale: ImageScale = ImageScale.Fit,
    contentDescription: String? = null,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    content: @Composable ScalableImageScope.() -> Unit
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
            .then(semantics)
            .clipToBounds(),
        contentAlignment = alignment,
    ) {

        val boxWidth: Int = constraints.maxWidth
        val boxHeight: Int = constraints.maxHeight

        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        val bitmapAspectRatio: Float = bitmapWidth.toFloat() / bitmapHeight

        var width: Int
        var height: Int

        when (imageScale) {
            ImageScale.FillBounds -> {
                width = boxWidth.coerceAtLeast(bitmapWidth)
                height = boxHeight.coerceAtLeast(bitmapHeight)
            }
            ImageScale.Fit -> {
                width = boxWidth
                height = (boxWidth / bitmapAspectRatio).toInt()

                if (height > boxHeight) {
                    height = boxHeight.toFloat().toInt().coerceAtLeast(bitmapHeight)
                    width = (boxHeight * bitmapAspectRatio).toInt().coerceAtLeast(bitmapWidth)
                }
            }
        }

        val hasBoundedDimens = constraints.hasBoundedWidth && constraints.hasBoundedHeight
        val hasFixedDimens = constraints.hasFixedWidth && constraints.hasFixedHeight

        println("🚀ScalableImage() imageScale: $imageScale, width: $width, height: $width, " +
                "hasBoundedDimens $hasBoundedDimens, hasFixedDimens: $hasFixedDimens\n" +
                "constraints: $constraints")


        val widthInDp: Dp
        val heightInDp: Dp

        with(LocalDensity.current) {
            widthInDp = width.toDp()
            heightInDp = height.toDp()
        }

        val density = LocalDensity.current

        val imageScopeImpl = remember(key1 = constraints) {
            ImageScopeImpl(
                density = density,
                constraints = constraints,
                imageWidth = widthInDp,
                imageHeight = heightInDp,
                topLeft = Offset(
                    x = (boxWidth - width).toFloat(),
                    y = (boxHeight - height).toFloat()
                )
            )
        }

        ScalableImageImpl(
            modifier = Modifier.size(widthInDp, heightInDp),
            bitmap = bitmap,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality
        )

        imageScopeImpl.content()
    }
}

@Composable
private fun ScalableImageImpl(
    modifier: Modifier,
    bitmap: ImageBitmap,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
) {
    val bitmapWidth = bitmap.width
    val bitmapHeight = bitmap.height

    Canvas(
        modifier = modifier.border(2.dp, Color.Yellow)
    ) {

        val canvasWidth = size.width.toInt()
        val canvasHeight = size.height.toInt()

        drawImage(
            bitmap,
            srcSize = IntSize(bitmapWidth, bitmapHeight),
            dstSize = IntSize(canvasWidth, canvasHeight),
            dstOffset = IntOffset(0, 0),
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality
        )
    }
}

private fun computeFillMaxDimension(srcSize: Size, dstSize: Size): Float {
    val widthScale = computeFillWidth(srcSize, dstSize)
    val heightScale = computeFillHeight(srcSize, dstSize)
    return kotlin.math.max(widthScale, heightScale)
}

private fun computeFillMinDimension(srcSize: Size, dstSize: Size): Float {
    val widthScale = computeFillWidth(srcSize, dstSize)
    val heightScale = computeFillHeight(srcSize, dstSize)
    return kotlin.math.min(widthScale, heightScale)
}


private fun computeFillWidth(srcSize: Size, dstSize: Size): Float =
    dstSize.width / srcSize.width

private fun computeFillHeight(srcSize: Size, dstSize: Size): Float =
    dstSize.height / srcSize.height


/**
 * Receiver scope being used by the children parameter of [ScalableImage]
 */
@Stable
interface ScalableImageScope {
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
     * Width of Image that is scaled based on [ImageScale]
     */
    val imageWidth: Dp

    /**
     * Height of Image that is scaled based on [ImageScale]
     */
    val imageHeight: Dp

    /**
     * Top left position of Image in this Composable
     */
    val topLeft: Offset
}

private data class ImageScopeImpl(
    private val density: Density,
    override val constraints: Constraints,
    override val imageWidth: Dp,
    override val imageHeight: Dp,
    override val topLeft: Offset,
) : ScalableImageScope {

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
