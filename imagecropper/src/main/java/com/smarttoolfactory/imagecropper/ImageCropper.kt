package com.smarttoolfactory.imagecropper

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import com.smarttoolfactory.image.ImageWithConstraints
import com.smarttoolfactory.imagecropper.util.getScaledImageBitmap

@Composable
fun ImageCropper(
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

        val cropState = rememberCropState(
            minZoom = .5f,
            imageSize = Size(
                imageWidthInPx,
                imageHeightInPx
            ),
            bitmapSize = IntSize(bitmapWidth, bitmapHeight)
        )

        val imageModifier = Modifier
            .size(imageWidth, imageHeight)
            .crop(
                touchRegionSize = 100f,
                minDimension = 200f,
                cropState = cropState,
                onDown = { zoomData: CropData ->
                    rectDraw = zoomData.drawRect
                    rectCrop = zoomData.cropRect
                },
                onMove = { zoomData: CropData ->
                    rectDraw = zoomData.drawRect
                    rectCrop = zoomData.cropRect
                },
                onUp = { zoomData: CropData ->
                    rectDraw = zoomData.drawRect
                    rectCrop = zoomData.cropRect
                }
            )

        Box {
            ImageOverlay(modifier = imageModifier, imageBitmap = imageBitmap)

            DrawingOverlay(
                modifier = Modifier.size(imageWidth, imageHeight),
                rect = rectDraw,
                touchRegionWidth = 100f
            )

            val zoom = cropState.zoom
            val pan = cropState.pan

            Text(
                modifier = Modifier.align(Alignment.BottomStart),
                color = Color.White,
                fontSize = 10.sp,
                text = "Zoom: $zoom\n" +
                        "imageWidthInPx: $imageWidthInPx, imageHeightInPx: $imageHeightInPx\n" +
                        "translationX: ${zoom}, translationY: ${-pan.y * zoom}\n" +
                        "offset: $pan\n" +
                        "scaledImageWidth: ${imageWidthInPx / zoom}, scaledImageHeight: ${imageHeightInPx / zoom}\n" +
                        "scaledBitmapWidth: ${bitmapWidth / zoom}, scaledBitmapHeight: ${bitmapHeight / zoom}\n" +
                        "cropRect: $rectCrop\n, size: ${rectCrop.size}\n" +
                        "drawRect: $rectDraw, size: ${rectDraw.size}"
            )
        }
    }


}
