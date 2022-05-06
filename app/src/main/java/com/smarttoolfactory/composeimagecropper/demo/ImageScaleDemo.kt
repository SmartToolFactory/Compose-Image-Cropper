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
import com.smarttoolfactory.imagecropper.ImageScale
import com.smarttoolfactory.imagecropper.ScalableImage


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
//        .clipToBounds()
            .border(2.dp, Color.Red)
//            .size(300.dp, 500.dp)
            .size((1000/density).dp, height = (600/density).dp)
//            .fillMaxWidth()
//            .aspectRatio(3/4f)

        val imageBitmapLarge = ImageBitmap.imageResource(
            LocalContext.current.resources,
            R.drawable.rome
        )

        val imageBitmapSmall = ImageBitmap.imageResource(
            LocalContext.current.resources,
            R.drawable.landscape10
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "ScalableImage ImageScale.FillBounds")
        ScalableImage(
            modifier = modifier,
            bitmap = imageBitmapLarge,
            imageScale = ImageScale.FillBounds,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "ScalableImage ImageScale.Fit")
        ScalableImage(
            modifier = modifier,
            bitmap = imageBitmapLarge,
            imageScale = ImageScale.Fit,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "ScalableImage ImageScale.Crop")
        ScalableImage(
            modifier = modifier,
            bitmap = imageBitmapLarge,
            imageScale = ImageScale.Crop,
            contentDescription = null
        )


        ImageSamples(modifier = modifier, imageBitmap = imageBitmapLarge)
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

    Spacer(modifier = Modifier.height(10.dp))
    Text(text = "IMAGE ContentScale.None")
    Image(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.None
    )

    Spacer(modifier = Modifier.height(10.dp))
    Text(text = "IMAGE ContentScale.Fit")
    Image(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.Fit
    )

    Spacer(modifier = Modifier.height(10.dp))
    Text(text = "IMAGE ContentScale.Crop")
    Image(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.Crop
    )

    Spacer(modifier = Modifier.height(10.dp))
    Text(text = "IMAGE ContentScale.FillBounds")
    Image(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.FillBounds
    )

    Spacer(modifier = Modifier.height(10.dp))
    Text(text = "IMAGE ContentScale.FillWidth")
    Image(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.FillWidth
    )

    Spacer(modifier = Modifier.height(10.dp))
    Text(text = "IMAGE ContentScale.FillHeight")
    Image(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.FillHeight
    )

    Spacer(modifier = Modifier.height(10.dp))
    Text(text = "IMAGE ContentScale.Inside")
    Image(
        modifier = modifier,
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.Inside
    )
}