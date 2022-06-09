@file:OptIn(ExperimentalMaterialApi::class)

package com.smarttoolfactory.composeimagecropper.demo

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
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
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.smarttoolfactory.imagecropper.ImageWithConstraints
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImageCropDemo() {


    val bottomSheetScaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(
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

        sheetGesturesEnabled = true,
        sheetContent = {
            SheetContent()
        },
        drawerElevation = 16.dp,
        drawerGesturesEnabled = true,
        // This is the height in collapsed state
        sheetPeekHeight = 0.dp
    ) {
        MainContent(bottomSheetScaffoldState)
    }

}

@Composable
private fun SheetContent() {

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
private fun MainContent(bottomSheetScaffoldState: BottomSheetScaffoldState) {

    val imageBitmapLarge = ImageBitmap.imageResource(
        LocalContext.current.resources,
        R.drawable.landscape1
    )

    var imageBitmap by remember { mutableStateOf(imageBitmapLarge) }
    var croppedImage by remember { mutableStateOf<ImageBitmap?>(null) }

    val modifier = Modifier
        .background(Color.LightGray)
        .fillMaxWidth()
        .aspectRatio(3 / 4f)

    var contentScale by remember { mutableStateOf(ContentScale.Fit) }

    val coroutineScope = rememberCoroutineScope()

    var crop by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {

            if (!showDialog) {

                ContentScaleSelectionMenu(contentScale) {
                    contentScale = it
                }

                ImageCropper(
                    modifier = modifier,
                    imageBitmap = imageBitmap,
                    contentScale = contentScale,
                    contentDescription = "Image Cropper",
                    crop = crop,
                ) {
                    croppedImage = it
                    crop = false
                    showDialog = true
                }
            } else {
                croppedImage?.let {
                    Image(bitmap = it, contentDescription = null, modifier = Modifier.fillMaxSize())
                }
            }
        }

        BottomAppBar(
            modifier = Modifier.align(Alignment.BottomStart),
            icons = {

                IconButton(
                    onClick = {

                    }
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = "Localized description")
                }

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                            } else {
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Localized description",
                    )
                }

                IconButton(
                    onClick = { crop = true }) {
                    Icon(Icons.Filled.Crop, contentDescription = "Localized description")
                }
            },
            floatingActionButton = {
                ImageSelectionButton(
                    onImageSelected = { bitmap: ImageBitmap ->
                        imageBitmap = bitmap
                    }
                )
            }
        )
    }

    if (showDialog) {
        croppedImage?.let {
            ShowCroppedImageDialog(imageBitmap = it) {
                showDialog = !showDialog
                croppedImage = null
            }
        }
    }
}

@Composable
private fun ShowCroppedImageDialog(imageBitmap: ImageBitmap, onDismissRequest: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            ImageWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4 / 3f)
                    .border(2.dp, Color.Red),
                contentScale = ContentScale.Fit,
                imageBitmap = imageBitmap, contentDescription = "result"
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}