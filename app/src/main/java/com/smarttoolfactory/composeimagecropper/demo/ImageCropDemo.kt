@file:OptIn(ExperimentalMaterialApi::class)

package com.smarttoolfactory.composeimagecropper.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.composeimagecropper.ContentScaleSelectionMenu
import com.smarttoolfactory.composeimagecropper.ImageSelectionButton
import com.smarttoolfactory.composeimagecropper.R
import com.smarttoolfactory.imagecropper.ImageCropper

@Composable
fun ImageCropDemo() {

    val imageBitmapLarge = ImageBitmap.imageResource(
        LocalContext.current.resources,
        R.drawable.landscape1
    )

    var imageBitmap by remember { mutableStateOf(imageBitmapLarge) }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetElevation = 8.dp,
        sheetShape = RoundedCornerShape(
            bottomStart = 0.dp,
            bottomEnd = 0.dp,
            topStart = 16.dp,
            topEnd = 16.dp
        ),
        floatingActionButton = {
            ImageSelectionButton(
                onImageSelected = { bitmap: ImageBitmap ->
                    imageBitmap = bitmap
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        sheetGesturesEnabled = true,
        sheetContent = {
            SheetContent(
                imageBitmap

            )
        },
        drawerElevation = 16.dp,
        drawerGesturesEnabled = true,
        // This is the height in collapsed state
        sheetPeekHeight = 70.dp
    ) {
        MainContent(imageBitmap)
    }

}

@Composable
private fun SheetContent(
    imageBitmap: ImageBitmap,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .height(400.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Under Construction")
    }
}

@Composable
private fun MainContent(
    imageBitmap: ImageBitmap
) {

    val modifier = Modifier
        .background(Color.LightGray)
        .fillMaxWidth()
        .aspectRatio(3 / 4f)

    var contentScale by remember { mutableStateOf(ContentScale.Fit) }

    Column(modifier = Modifier.fillMaxSize()) {

        Spacer(modifier = Modifier.height(50.dp))
        ContentScaleSelectionMenu(contentScale) {
            contentScale = it
        }

        ImageCropper(
            modifier = modifier,
            imageBitmap = imageBitmap,
            contentScale = contentScale,
            contentDescription = "Image Cropper"
        )
    }
}