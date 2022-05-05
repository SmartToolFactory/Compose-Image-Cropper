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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.*

enum class ImageScale {
    Fit, FillBounds, Crop
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
            .then(semantics),
//            .clipToBounds()
        contentAlignment = alignment,
    ) {

        val boxWidth: Int = constraints.maxWidth
        val boxHeight: Int = constraints.maxHeight

        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        val srcSize = Size(bitmapWidth.toFloat(), bitmapHeight.toFloat())
        val dstSize = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())

        val scaleFactor = calculateScaleFactor(imageScale, srcSize, dstSize)

        val width = bitmapWidth * scaleFactor.scaleX
        val height = bitmapHeight * scaleFactor.scaleY

        val widthInDp: Dp
        val heightInDp: Dp

        with(LocalDensity.current) {
            widthInDp = bitmapWidth * scaleFactor.scaleX.toDp()
            heightInDp = bitmapHeight * scaleFactor.scaleY.toDp()
        }

        val topLeft = Offset(
            x = (boxWidth - width),
            y = (boxHeight - height)
        )

        println(
            "🚀ScalableImage() imageScale: $imageScale\n" +
                    "constraints: $constraints\n" +
                    "bitmapWidth: $bitmapWidth, bitmapHeight: $bitmapHeight, " +
                    "boxWidth: $boxWidth, boxHeight: $boxHeight\n" +
                    "scaleX: ${scaleFactor.scaleX}, scaleY: ${scaleFactor.scaleY}\n" +
                    "width: $width, height: $height, widthInDp: $widthInDp, heightInDp: $heightInDp\n" +
                    "topLeft: $topLeft\n\n"
        )


        val density = LocalDensity.current

        val imageScopeImpl = remember(key1 = constraints) {
            ImageScopeImpl(
                density = density,
                constraints = constraints,
                imageWidth = widthInDp,
                imageHeight = heightInDp,
                topLeft = Offset(
                    x = (boxWidth - width),
                    y = (boxHeight - height)
                )
            )
        }

        ScalableImageImpl(
            modifier = Modifier.size(widthInDp, heightInDp),
            bitmap = bitmap,
            alpha = alpha,
            width = width.toInt(),
            height = height.toInt(),
            colorFilter = colorFilter,
            filterQuality = filterQuality
        )

        imageScopeImpl.content()
    }
}

@Composable
private fun calculateScaleFactor(
    imageScale: ImageScale,
    srcSize: Size,
    dstSize: Size
) = when (imageScale) {
    ImageScale.FillBounds -> {
        ScaleFactor(
            computeFillWidth(srcSize, dstSize),
            computeFillHeight(srcSize, dstSize)
        )
    }
    ImageScale.Fit -> {
        computeFillMinDimension(srcSize, dstSize).let {
            ScaleFactor(it, it)
        }
    }
    ImageScale.Crop -> {
        computeFillMaxDimension(srcSize, dstSize).let {
            ScaleFactor(it, it)
        }
    }
}

@Composable
private fun ScalableImageImpl(
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

    Canvas(
        modifier = modifier
            .clipToBounds()
            .border(4.dp, Color.Yellow)
    ) {

        val canvasWidth = size.width.toInt()
        val canvasHeight = size.height.toInt()

        println("CANVAS size: $size, width: $width, height: $height")

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

data class ScaleFactor(val x: Float, val y: Float)

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
