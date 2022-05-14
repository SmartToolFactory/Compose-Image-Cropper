package com.smarttoolfactory.composeimagecropper.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import com.smarttoolfactory.composeimagecropper.R
import com.smarttoolfactory.imagecropper.ImageCropper

@Composable
fun ImageCropDemo() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        val modifier = Modifier
            .background(Color.LightGray)
            .fillMaxWidth()
            .aspectRatio(4 / 3f)

        val bitmap1 = ImageBitmap.imageResource(
            LocalContext.current.resources,
            R.drawable.landscape1
        )

        ImageCropper(modifier, bitmap = bitmap1, contentDescription = null)
    }
}