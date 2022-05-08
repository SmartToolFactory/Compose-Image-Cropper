package com.smarttoolfactory.composeimagecropper.demo

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttoolfactory.composeimagecropper.R
import com.smarttoolfactory.imagecropper.ImageWithConstraints

/**
 * This demo is for comparing results with [Image] and [ImageWithConstraints]
 */
@Composable
fun ImageScaleDemo() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val density = LocalDensity.current.density

        val modifier = Modifier
            .background(Color.LightGray)
            .border(2.dp, Color.Red)
            .size((800 / density).dp, height = (300 / density).dp)


        val imageBitmapLarge = ImageBitmap.imageResource(
            LocalContext.current.resources,
            R.drawable.landscape1
        )

        val imageBitmapSmall = ImageBitmap.imageResource(
            LocalContext.current.resources,
            R.drawable.landscape2
        )

        Spacer(modifier = Modifier.height(20.dp))
        ImageWitConstraintsSamples(modifier = modifier, imageBitmap = imageBitmapLarge)
        ImageSamples(modifier = modifier, imageBitmap = imageBitmapLarge)
    }
}

@Composable
private fun ImageWitConstraintsSamples(modifier: Modifier, imageBitmap: ImageBitmap) {

    Text(
        text = "Default Content Scale",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Red,
        modifier = Modifier.padding(8.dp)
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "ImageWithConstraints ContentScale.None")
    ImageWithConstraints(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.None
    ) {
        Spacer(
            modifier = Modifier
                .size(this.imageWidth, this.imageHeight)
                .border(2.dp, Color.Yellow)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "ImageWithConstraints ContentScale.Fit")
    ImageWithConstraints(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.Fit
    ) {
        Spacer(
            modifier = Modifier
                .size(this.imageWidth, this.imageHeight)
                .border(2.dp, Color.Yellow)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "ImageWithConstraints ContentScale.Crop")
    ImageWithConstraints(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.Crop
    ) {
        Spacer(
            modifier = Modifier
                .size(this.imageWidth, this.imageHeight)
                .border(2.dp, Color.Yellow)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "ImageWithConstraints ContentScale.FillBounds")
    ImageWithConstraints(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.FillBounds
    ) {
        Spacer(
            modifier = Modifier
                .size(this.imageWidth, this.imageHeight)
                .border(2.dp, Color.Yellow)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "ImageWithConstraints ContentScale.FillWidth")
    ImageWithConstraints(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.FillWidth
    ) {
        Spacer(
            modifier = Modifier
                .size(this.imageWidth, this.imageHeight)
                .border(2.dp, Color.Yellow)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "ImageWithConstraints ContentScale.FillHeight")
    ImageWithConstraints(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.FillHeight
    ) {
        Spacer(
            modifier = Modifier
                .size(this.imageWidth, this.imageHeight)
                .border(2.dp, Color.Yellow)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "ImageWithConstraints ContentScale.Inside")
    ImageWithConstraints(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.Inside
    ) {
        Spacer(
            modifier = Modifier
                .size(this.imageWidth, this.imageHeight)
                .border(2.dp, Color.Yellow)
        )
    }
}

@Composable
private fun ImageSamples(modifier: Modifier, imageBitmap: ImageBitmap) {
    /*
    IMAGE
 */
    Text(
        text = "Default Image Content Scale",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Red,
        modifier = Modifier.padding(8.dp)
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "IMAGE ContentScale.None")
    Image(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.None
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "IMAGE ContentScale.Fit")
    Image(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.Fit
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "IMAGE ContentScale.Crop")
    Image(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.Crop
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "IMAGE ContentScale.FillBounds")
    Image(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.FillBounds
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "IMAGE ContentScale.FillWidth")
    Image(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.FillWidth
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "IMAGE ContentScale.FillHeight")
    Image(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.FillHeight
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "IMAGE ContentScale.Inside")
    Image(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.Inside
    )
}