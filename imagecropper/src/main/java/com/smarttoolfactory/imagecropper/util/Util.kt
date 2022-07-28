package com.smarttoolfactory.imagecropper.util

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntRect
import com.smarttoolfactory.image.transform.TouchRegion
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun getScaledImageBitmap(
    imageWidth: Dp,
    imageHeight: Dp,
    rect: IntRect,
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

fun getTouchRegion(
    position: Offset,
    rect: Rect,
    threshold: Float
): TouchRegion {

    return when {

        position.x - rect.left in 0.0f..threshold &&
                position.y - rect.top in 0.0f..threshold -> TouchRegion.TopLeft

        rect.right - position.x in 0f..threshold &&
                position.y - rect.top in 0.0f..threshold -> TouchRegion.TopRight

        rect.right - position.x in 0f..threshold &&
                rect.bottom - position.y in 0.0f..threshold -> TouchRegion.BottomRight

        position.x - rect.left in 0.0f..threshold &&
                rect.bottom - position.y in 0.0f..threshold -> TouchRegion.BottomLeft


        rect.contains(offset = position) -> TouchRegion.Inside
        else -> TouchRegion.None
    }
}

/**
 * Rotates the given offset around the origin by the given angle in degrees.
 *
 * A positive angle indicates a counterclockwise rotation around the right-handed 2D Cartesian
 * coordinate system.
 *
 * See: [Rotation matrix](https://en.wikipedia.org/wiki/Rotation_matrix)
 */
fun Offset.rotateBy(angle: Float): Offset {
    val angleInRadians = angle * PI / 180
    return Offset(
        (x * cos(angleInRadians) - y * sin(angleInRadians)).toFloat(),
        (x * sin(angleInRadians) + y * cos(angleInRadians)).toFloat()
    )
}

private const val PI = Math.PI


