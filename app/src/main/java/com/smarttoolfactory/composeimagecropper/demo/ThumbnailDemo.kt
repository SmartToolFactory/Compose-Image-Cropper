package com.smarttoolfactory.composeimagecropper.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isFinite
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttoolfactory.composeimagecropper.R
import com.smarttoolfactory.imagecropper.ImageScale
import com.smarttoolfactory.imagecropper.ImageWithThumbnail
import com.smarttoolfactory.imagecropper.ThumbnailPosition

@Composable
fun ThumbnailDemo() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        val modifier = Modifier
            .background(Color.LightGray)
            .border(2.dp, Color.Red)
            .fillMaxWidth()
            .aspectRatio(4 / 3f)

        val imageBitmap1 = ImageBitmap.imageResource(
            LocalContext.current.resources,
            R.drawable.landscape1
        )

        val imageBitmap2 = ImageBitmap.imageResource(
            LocalContext.current.resources,
            R.drawable.landscape2
        )

        ThumbnailScaleModeSample()
        ThumbnailPositionChangeSample(modifier, imageBitmap1)
        ThumbnailCallbackSample()
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
        title = "Image Content Scale",
        color = Color.Red,
        initialExpandState = true
    ) {
        Column {
            Text(text = "ScalableImage ImageScale.FillBounds")
            ImageWithThumbnail(
                bitmap = bitmap1,
                modifier = modifier,
                imageScale = ImageScale.FillBounds,
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(30.dp))
            Text(text = "ScalableImage ImageScale.Fit")
            ImageWithThumbnail(
                bitmap = bitmap1,
                modifier = modifier,
                imageScale = ImageScale.Fit,
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(30.dp))
            Text(text = "ScalableImage ImageScale.Crop")
            ImageWithThumbnail(
                bitmap = bitmap1,
                modifier = modifier,
                imageScale = ImageScale.Crop,
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(30.dp))
            Text(text = "ScalableImage ImageScale.FillBounds")
            ImageWithThumbnail(
                bitmap = bitmap2,
                modifier = modifier,
                imageScale = ImageScale.FillBounds,
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(30.dp))
            Text(text = "ScalableImage ImageScale.Fit")
            ImageWithThumbnail(
                bitmap = bitmap2,
                modifier = modifier,
                imageScale = ImageScale.Fit,
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(30.dp))
            Text(text = "ScalableImage ImageScale.Crop")
            ImageWithThumbnail(
                bitmap = bitmap2,
                modifier = modifier,
                imageScale = ImageScale.Crop,
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(30.dp))
            Text(text = "ScalableImage ImageScale.FillBounds")
            ImageWithThumbnail(
                bitmap = bitmap3,
                modifier = modifier,
                imageScale = ImageScale.FillBounds,
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(30.dp))
            Text(text = "ScalableImage ImageScale.Fit")
            ImageWithThumbnail(
                bitmap = bitmap3,
                modifier = modifier,
                imageScale = ImageScale.Fit,
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(30.dp))
            Text(text = "ScalableImage ImageScale.Crop")
            ImageWithThumbnail(
                bitmap = bitmap3,
                modifier = modifier,
                imageScale = ImageScale.Crop,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ThumbnailPositionChangeSample(
    modifier: Modifier,
    imageBitmap: ImageBitmap
) {

    ExpandableColumnWithTitle(
        title = "Thumbnail Position",
        color = Color.Red,
        initialExpandState = true
    ) {
        Column {
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
}

@Composable
private fun ThumbnailCallbackSample() {

    val imageBitmap = ImageBitmap.imageResource(
        LocalContext.current.resources,
        R.drawable.landscape2
    )

    var center1 by remember { mutableStateOf(Offset.Unspecified) }
    var offset1 by remember { mutableStateOf(Offset.Unspecified) }

    var center2 by remember { mutableStateOf(Offset.Unspecified) }
    var offset2 by remember { mutableStateOf(Offset.Unspecified) }

    var center3 by remember { mutableStateOf(Offset.Unspecified) }
    var offset3 by remember { mutableStateOf(Offset.Unspecified) }

    val modifier = Modifier
        .border(2.dp, Color.Red)
        .fillMaxWidth()
        .aspectRatio(4 / 3f)

    ExpandableColumnWithTitle(
        title = "Callbacks",
        color = Color.Red,
        initialExpandState = true
    ) {
        Column {

            Text(
                "In this example Canvas is added as content to Thumbnail to get center of thumbnail and " +
                        "user's touch position with exact linear interpolation for any scaling mode of ScalableImage"
            )

            Text(text = "ScalableImage ImageScale.FillBounds")
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(Color.LightGray)
                    .border(2.dp, Color.Cyan)
            ) {

                ImageWithThumbnail(
                    bitmap = imageBitmap,
                    modifier = modifier,
                    contentDescription = null,
                    imageScale = ImageScale.FillBounds,
                    onThumbnailCenterChange = {
                        center1 = it
                    },
                    onTouchEvent = {
                        offset1 = it
                    }
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(3.dp, Color.Black)
                    ) {
                        if (center1.isSpecified && center1.isFinite) {
                            drawCircle(Color.Red, radius = 5.dp.toPx(), center = center1)
                        }
                        if (offset1.isSpecified && offset1.isFinite) {
                            drawCircle(Color.Green, radius = 5.dp.toPx(), center = offset1)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Text(text = "ScalableImage ImageScale.Fit")
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(Color.LightGray)
                    .border(2.dp, Color.Cyan)
            ) {

                ImageWithThumbnail(
                    bitmap = imageBitmap,
                    modifier = modifier,
                    contentDescription = null,
                    imageScale = ImageScale.Fit,
                    onThumbnailCenterChange = {
                        center2 = it
                    },
                    onTouchEvent = {
                        offset2 = it
                    }
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(3.dp, Color.Black)
                    ) {
                        if (center2.isSpecified && center2.isFinite) {
                            drawCircle(Color.Red, radius = 5.dp.toPx(), center = center2)
                        }
                        if (offset2.isSpecified && offset2.isFinite) {
                            drawCircle(Color.Green, radius = 5.dp.toPx(), center = offset2)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Text(text = "ScalableImage ImageScale.Crop")
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(Color.LightGray)
                    .border(2.dp, Color.Cyan)
            ) {

                ImageWithThumbnail(
                    bitmap = imageBitmap,
                    modifier = modifier,
                    contentDescription = null,
                    imageScale = ImageScale.Crop,
                    onThumbnailCenterChange = {
                        center3 = it
                    },
                    onTouchEvent = {
                        offset3 = it
                    }
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(3.dp, Color.Black)
                    ) {
                        if (center3.isSpecified && center3.isFinite) {
                            drawCircle(Color.Red, radius = 5.dp.toPx(), center = center3)
                        }
                        if (offset3.isSpecified && offset3.isFinite) {
                            drawCircle(Color.Green, radius = 5.dp.toPx(), center = offset3)
                        }
                    }
                }
            }
        }
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
            content()
        }
    }
}