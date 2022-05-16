package com.smarttoolfactory.composeimagecropper.demo

import android.graphics.Bitmap
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttoolfactory.composeimagecropper.R
import com.smarttoolfactory.gesture.pointerMotionEvents
import kotlin.math.roundToInt

@Composable
fun CanvasDemo() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        val imageBitmap1 = ImageBitmap.imageResource(
            LocalContext.current.resources,
            R.drawable.landscape1
        ).asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true).asImageBitmap()

        val imageBitmap2 = ImageBitmap.imageResource(
            LocalContext.current.resources,
            R.drawable.landscape1
        ).asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true).asImageBitmap()

        Text("Native Canvas Clipping")
        NativeCanvasSample1(
            imageBitmap = imageBitmap1,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4 / 3f)
        )

        Spacer(modifier = Modifier.height(50.dp))
        Text("Native Canvas BlendMode Clear")
        NativeCanvasSample2(
            imageBitmap = imageBitmap2,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4 / 3f)
        )

        val dstBitmap = ImageBitmap.imageResource(id = R.drawable.landscape1)

        Spacer(modifier = Modifier.height(50.dp))
        Text("Compose Canvas BlendMode Clear")
        ComposeCanvasSample(
            imageBitmap = dstBitmap,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4 / 3f)
        )
    }
}


@Composable
fun NativeCanvasSample1(imageBitmap: ImageBitmap, modifier: Modifier) {
    BoxWithConstraints(modifier) {

        val imageWidth = constraints.maxWidth
        val imageHeight = constraints.maxHeight

        val bitmapWidth = imageBitmap.width
        val bitmapHeight = imageBitmap.height


        val canvas: androidx.compose.ui.graphics.Canvas = Canvas(imageBitmap)

        val imagePaint = remember {
            Paint().apply {
                blendMode = BlendMode.SrcIn
            }
        }

        val paint = remember {
            Paint().apply {
                color = Color(0xff29B6F6)
            }
        }

        canvas.apply {
            val nativeCanvas = this.nativeCanvas
            val canvasWidth = nativeCanvas.width.toFloat()
            val canvasHeight = nativeCanvas.height.toFloat()

            println(
                "🔥 Canvas Width: $canvasWidth, canvasHeight: $canvasHeight, " +
                        "imageWidth: $imageWidth, imageHeight: $imageHeight\n" +
                        "bitmapWidth: $bitmapWidth, bitmapHeight: $bitmapHeight\n" +
                        "rect: ${nativeCanvas.clipBounds.toComposeRect()}"
            )
            saveLayer(nativeCanvas.clipBounds.toComposeRect(), imagePaint)

            drawCircle(
                center = Offset(canvasWidth / 2, canvasHeight / 2),
                radius = canvasHeight / 2,
                paint = paint
            )
            drawImage(image = imageBitmap, topLeftOffset = Offset.Zero, imagePaint)
            restore()


        }

        Image(
            modifier = Modifier
                .background(Color.LightGray)
                .border(2.dp, Color.Red),
            bitmap = imageBitmap,
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )

    }
}

// FIXME This one does not refresh correctly as i asked on Stackoverflow
// https://stackoverflow.com/questions/72168588/jetpack-compose-androidx-compose-ui-graphics-canvas-not-refreshing-correctly-for
@Composable
fun NativeCanvasSample2(imageBitmap: ImageBitmap, modifier: Modifier) {


    BoxWithConstraints(modifier) {

        val imageWidth = constraints.maxWidth
        val imageHeight = constraints.maxHeight

        val bitmapWidth = imageBitmap.width
        val bitmapHeight = imageBitmap.height

        var offset by remember {
            mutableStateOf(Offset(bitmapWidth / 2f, bitmapHeight / 2f))
        }


        val canvasModifier = Modifier.pointerMotionEvents(
            Unit,
            onDown = {
                val position = it.position
                val offsetX = position.x * bitmapWidth / imageWidth
                val offsetY = position.y * bitmapHeight / imageHeight
                offset = Offset(offsetX, offsetY)
                it.consume()
            },
            onMove = {
                val position = it.position
                val offsetX = position.x * bitmapWidth / imageWidth
                val offsetY = position.y * bitmapHeight / imageHeight
                offset = Offset(offsetX, offsetY)
                it.consume()
            },
            delayAfterDownInMillis = 20
        )

        val canvas: androidx.compose.ui.graphics.Canvas = Canvas(imageBitmap)


        val paintBg = remember {
            Paint().apply {
                color = Color(0x55000000)
            }
        }

        val paintImage = remember {
            Paint()
        }

        val erasePaint = remember {
            Paint().apply {
                color = Color.Transparent
                blendMode = BlendMode.Clear
            }
        }

        val paint2 = remember {
            Paint().apply {
                color = Color.Blue
                blendMode = BlendMode.Clear
            }
        }

        canvas.apply {
            val nativeCanvas = this.nativeCanvas
            val canvasWidth = nativeCanvas.width.toFloat()
            val canvasHeight = nativeCanvas.height.toFloat()

            drawImage(imageBitmap, topLeftOffset = Offset.Zero, paintImage)

            val paint = remember {
                android.graphics.Paint().apply {
//                    color = android.graphics.Color.TRANSPARENT
//                    xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                }
            }

            val paintBg2 = remember {
                android.graphics.Paint().apply {
                    color = 0x55000000
                }
            }

            val paintClear = remember {
                android.graphics.Paint().apply {
                    color = android.graphics.Color.TRANSPARENT
//                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                }
            }


            with(canvas.nativeCanvas) {
//                val checkPoint = saveLayer(nativeCanvas.clipBounds.toRectF(), paintClear)
//                val checkPoint = saveLayer(null,null)
//                this.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY)
                this.drawBitmap(imageBitmap.asAndroidBitmap(), 0f, 0f, paintClear)
                this.drawRect(0f, 0f, canvasWidth, canvasHeight, paintBg2)
//                this.drawCircle(offset.x, offset.y, 100f, paintClear)
//                restoreToCount(checkPoint)
            }
        }


        Image(
            modifier = canvasModifier,
            bitmap = imageBitmap,
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )

        Text(
            "Offset: $offset",
            modifier = Modifier.align(Alignment.BottomEnd),
            color = Color.White,
            fontSize = 16.sp
        )

    }
}

@Composable
fun ComposeCanvasSample(modifier: Modifier, imageBitmap: ImageBitmap) {

    BoxWithConstraints(modifier) {
        val dstBitmap = imageBitmap

        val imageWidth = constraints.maxWidth
        val imageHeight = constraints.maxHeight

        val bitmapWidth = imageBitmap.width
        val bitmapHeight = imageBitmap.height

        var offset by remember {
            mutableStateOf(Offset(bitmapWidth / 2f, bitmapHeight / 2f))
        }

        val canvasModifier = Modifier.pointerMotionEvents(
            Unit,
            onDown = {
                val position = it.position
                val offsetX = (position.x).coerceIn(0f, imageWidth.toFloat())
                val offsetY = (position.y).coerceIn(0f, imageHeight.toFloat())
                offset = Offset(offsetX, offsetY)
                it.consume()
            },
            onMove = {
                val position = it.position
                val offsetX = (position.x).coerceIn(0f, imageWidth.toFloat())
                val offsetY = (position.y).coerceIn(0f, imageHeight.toFloat())
                offset = Offset(offsetX, offsetY)
                it.consume()
            },
            delayAfterDownInMillis = 20
        )

        Canvas(modifier = canvasModifier.fillMaxSize()) {
            val canvasWidth = size.width.roundToInt()
            val canvasHeight = size.height.roundToInt()

            drawImage(
                image = dstBitmap,
                srcSize = IntSize(dstBitmap.width, dstBitmap.height),
                dstSize = IntSize(canvasWidth, canvasHeight)
            )

            with(drawContext.canvas.nativeCanvas) {
                val checkPoint = saveLayer(null, null)

                // Destination
                drawRect(Color(0x55000000))

                // Source
                drawCircle(
                    center = offset,
                    color = Color.Blue,
                    radius = canvasHeight.coerceAtMost(canvasWidth) / 8f,
                    blendMode = BlendMode.Clear
                )
                restoreToCount(checkPoint)
            }
        }
    }
}