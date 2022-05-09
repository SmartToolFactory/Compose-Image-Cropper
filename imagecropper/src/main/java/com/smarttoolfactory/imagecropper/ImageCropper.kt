package com.smarttoolfactory.imagecropper

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale

@Composable
fun ImageCropper(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    contentDescription: String?,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality
) {
    ImageWithConstraints(
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment,
        contentDescription = contentDescription,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality,
        bitmap = bitmap,
    ) {
        val originalBitmapRect = this.rect
        var cropRect by remember { mutableStateOf(originalBitmapRect) }

        val scaledBitmap = remember(this) {
            // This bitmap is needed when we crop original bitmap due to scaling mode
            // and aspect ratio result of cropping
            // We might have center section of the image after cropping, and
            // because of that thumbLayout either should have rectangle and some
            // complex calculation for srcOffset and srcSide along side with touch offset
            // or we can create a new bitmap that only contains area bounded by rectangle
            bitmap.asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true).asImageBitmap()
        }

        androidx.compose.ui.graphics.Canvas(scaledBitmap).apply {

        }


    }
}

@Composable
private fun CropperImpl(modifier: Modifier) {

}