package com.smarttoolfactory.composeimagecropper.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isFinite
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttoolfactory.composeimagecropper.ContentScaleSelectionMenu
import com.smarttoolfactory.composeimagecropper.ImageSelectionButton
import com.smarttoolfactory.composeimagecropper.R
import com.smarttoolfactory.imagecropper.ImageWithThumbnail
import com.smarttoolfactory.imagecropper.ThumbnailPosition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThumbnailDemo() {

    val imageBitmapLarge = ImageBitmap.imageResource(
        LocalContext.current.resources,
        R.drawable.landscape2
    )

    var imageBitmap by remember { mutableStateOf(imageBitmapLarge) }

    Scaffold(
        floatingActionButton = {
            ImageSelectionButton {
                imageBitmap = it
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        content = { paddingValues: PaddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(10.dp)
            ) {

                ThumbnailDemoSamples(imageBitmap)
            }
        }
    )
}

@Composable
private fun ThumbnailDemoSamples(imageBitmap: ImageBitmap) {

    val modifier = Modifier
        .background(Color.LightGray)
        .border(2.dp, Color.Red)
        .fillMaxWidth()
        .aspectRatio(4 / 3f)

    var contentScale by remember { mutableStateOf(ContentScale.Fit) }
    ContentScaleSelectionMenu(contentScale = contentScale) {
        contentScale = it
    }

    Spacer(modifier = Modifier.height(20.dp))
    ThumbnailScaleModeCustomImageSample(modifier, imageBitmap, contentScale)
    ThumbnailCallbackSample(modifier, imageBitmap, contentScale)
    ThumbnailPositionChangeSample(modifier)
    ThumbnailScaleModeSample()
}


@Composable
private fun ThumbnailScaleModeCustomImageSample(
    modifier: Modifier,
    imageBitmap: ImageBitmap,
    contentScale: ContentScale
) {

    ExpandableColumnWithTitle(
        title = "Custom Image",
        color = Color.Red,
        initialExpandState = true
    ) {

        Text(
            "Open an image using FloatingActionButton or change ContentScale " +
                    "using dropdown menu."
        )

        ImageWithThumbnail(
            bitmap = imageBitmap,
            modifier = modifier,
            contentScale = contentScale,
            thumbnailZoom = 100,
            contentDescription = null
        ) {
            Box(
                modifier = Modifier
                    .size(imageWidth, imageHeight)
                    .border(4.dp, Color.Yellow)
            )
        }
    }
}

@Composable
private fun ThumbnailCallbackSample(
    modifier: Modifier,
    imageBitmap: ImageBitmap,
    contentScale: ContentScale
) {

    var center by remember { mutableStateOf(Offset.Unspecified) }
    var offset by remember { mutableStateOf(Offset.Unspecified) }

    ExpandableColumnWithTitle(
        title = "Callbacks",
        color = Color.Red,
        initialExpandState = true
    ) {
        Text(
            "Canvas is added as content to Thumbnail to get center of thumbnail and " +
                    "user's touch position with exact linear interpolation for any scaling mode of ScalableImage"
        )

        Text("Offset: $offset")

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.background(Color.LightGray)

        ) {

            ImageWithThumbnail(
                bitmap = imageBitmap,
                modifier = modifier,
                contentDescription = null,
                contentScale = contentScale,
                onThumbnailCenterChange = {
                    center = it
                },
                onTouchEvent = {
                    offset = it
                }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, Color.Yellow)
                ) {

                    if (center.isSpecified && center.isFinite) {
                        drawCircle(Color.Red, radius = 5.dp.toPx(), center = center)
                    }
                    if (offset.isSpecified && offset.isFinite) {
                        drawCircle(Color.Green, radius = 5.dp.toPx(), center = offset)
                    }
                }
            }
        }
    }
}

@Composable
private fun ThumbnailPositionChangeSample(
    modifier: Modifier
) {

    val imageBitmap = ImageBitmap.imageResource(
        LocalContext.current.resources,
        R.drawable.landscape4
    )

    ExpandableColumnWithTitle(
        title = "Thumbnail Position",
        color = Color.Red,
        initialExpandState = false
    ) {
        Text(
            "Change position of thumbnail from first one to second based on " +
                    "touch proximity to Thumbnail"
        )
        Text(text = "TopLeft-TopRight")
        ImageWithThumbnail(
            bitmap = imageBitmap,
            modifier = modifier,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "BottomRight-TopLeft")
        ImageWithThumbnail(
            bitmap = imageBitmap,
            modifier = modifier,
            thumbnailPosition = ThumbnailPosition.BottomRight,
            moveTo = ThumbnailPosition.TopLeft,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "TopRight-BottomLeft")
        ImageWithThumbnail(
            bitmap = imageBitmap,
            modifier = modifier,
            thumbnailPosition = ThumbnailPosition.TopRight,
            moveTo = ThumbnailPosition.BottomLeft,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "TopLeft not movable")
        ImageWithThumbnail(
            bitmap = imageBitmap,
            modifier = modifier,
            contentDescription = null,
            moveableThumbnail = false
        )

    }
}

@Composable
private fun ThumbnailScaleModeSample() {
    val modifier = Modifier
        .background(Color.LightGray)
        .border(2.dp, Color.Red)
        .fillMaxWidth()
        .aspectRatio(4 / 3f)

    val bitmap1 = ImageBitmap.imageResource(
        LocalContext.current.resources,
        R.drawable.landscape1
    )

    val bitmap2 = ImageBitmap.imageResource(
        LocalContext.current.resources,
        R.drawable.landscape2
    )

    val bitmap3 = ImageBitmap.imageResource(
        LocalContext.current.resources,
        R.drawable.landscape3
    )

    ExpandableColumnWithTitle(
        title = "Content Scale",
        color = Color.Red,
        initialExpandState = false
    ) {

        Text(
            "Demonstrates correct positions are returned even if image is " +
                    "scaled with ContentScale modes"
        )

        // Bitmap1 1920x1280
        Text(text = "ContentScale.None")
        ImageWithThumbnail(
            bitmap = bitmap1,
            modifier = modifier,
            contentScale = ContentScale.None,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.Fit")
        ImageWithThumbnail(
            bitmap = bitmap1,
            modifier = modifier,
            contentScale = ContentScale.Fit,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.Crop")
        ImageWithThumbnail(
            bitmap = bitmap1,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.FillBounds")
        ImageWithThumbnail(
            bitmap = bitmap1,
            modifier = modifier,
            contentScale = ContentScale.FillBounds,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.FillWidth")
        ImageWithThumbnail(
            bitmap = bitmap1,
            modifier = modifier,
            contentScale = ContentScale.FillWidth,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.FillHeight")
        ImageWithThumbnail(
            bitmap = bitmap1,
            modifier = modifier,
            contentScale = ContentScale.FillHeight,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.Inside")
        ImageWithThumbnail(
            bitmap = bitmap1,
            modifier = modifier,
            contentScale = ContentScale.Inside,
            contentDescription = null
        )

        // Bitmap2 480x270

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.None")
        ImageWithThumbnail(
            bitmap = bitmap2,
            modifier = modifier,
            contentScale = ContentScale.None,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.Fit")
        ImageWithThumbnail(
            bitmap = bitmap2,
            modifier = modifier,
            contentScale = ContentScale.Fit,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.Crop")
        ImageWithThumbnail(
            bitmap = bitmap2,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.FillBounds")
        ImageWithThumbnail(
            bitmap = bitmap2,
            modifier = modifier,
            contentScale = ContentScale.FillBounds,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.FillWidth")
        ImageWithThumbnail(
            bitmap = bitmap2,
            modifier = modifier,
            contentScale = ContentScale.FillWidth,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.FillHeight")
        ImageWithThumbnail(
            bitmap = bitmap2,
            modifier = modifier,
            contentScale = ContentScale.FillHeight,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.Inside")
        ImageWithThumbnail(
            bitmap = bitmap2,
            modifier = modifier,
            contentScale = ContentScale.Inside,
            contentDescription = null
        )

        // Bitmap3 1000x1000

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.FillBounds")
        ImageWithThumbnail(
            bitmap = bitmap3,
            modifier = modifier,
            contentScale = ContentScale.FillBounds,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.Fit")
        ImageWithThumbnail(
            bitmap = bitmap3,
            modifier = modifier,
            contentScale = ContentScale.Fit,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "ContentScale.Crop")
        ImageWithThumbnail(
            bitmap = bitmap3,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
    }

}

/**
 * Column with full width title and expand icon that can expand/shrink with [AnimatedVisibility].
 * @param title text on top of the column that is visible on both states.
 * @param color of [title].
 * @param initialExpandState whether this composable should be expanded initially.
 * @param content is the content that should be expended or hidden.
 */
@Composable
private fun ExpandableColumnWithTitle(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    title: String,
    color: Color,
    initialExpandState: Boolean = true,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initialExpandState) }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment
    ) {

        Row(
            modifier = Modifier
                .padding(5.dp)
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(vertical = 5.dp),
                text = title,
                fontSize = 22.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess
                else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = color
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column {
                content()
            }
        }
    }
}