package com.smarttoolfactory.composeimagecropper.demo

import android.graphics.Bitmap
import android.graphics.PorterDuff
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
import com.smarttoolfactory.imagecropper.createPolygonPath
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

        val aspectRatio1 = imageBitmap1.width / imageBitmap1.height.toFloat()
        val aspectRatio2 = imageBitmap2.width / imageBitmap2.height.toFloat()

        Text("Native Canvas Clipping")
//        NativeCanvasSample1(
//            imageBitmap = imageBitmap1,
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(aspectRatio1)
//        )

//        Spacer(modifier = Modifier.height(50.dp))
        Text("Native Canvas BlendMode Clear")
        NativeCanvasSample2(
            imageBitmap = imageBitmap2,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio2)
        )
//
//        val dstBitmap = ImageBitmap.imageResource(id = R.drawable.landscape1)
//        val aspectRatioDst = dstBitmap.width / dstBitmap.height.toFloat()
//
//        Spacer(modifier = Modifier.height(50.dp))
//        Text("Compose Canvas BlendMode Clear")
//        ComposeCanvasSample(
//            imageBitmap = dstBitmap,
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(aspectRatioDst)
//        )
//
//        Spacer(modifier = Modifier.height(50.dp))
//        Text("Compose Canvas Path + BlendMode Clear")
//        ComposeCanvasSample2(
//            imageBitmap = dstBitmap,
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(aspectRatioDst)
//        )
    }
}


// FIXME This one does not refresh correctly, here is the question i asked on Stackoverflow
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

        val canvas: Canvas = remember {
            Canvas(imageBitmap)
        }

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
                color = android.graphics.Color.RED
//                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }
        }

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

            println("CANVAS DRAWING...")

            with(canvas.nativeCanvas) {
//                val checkPoint = saveLayer(nativeCanvas.clipBounds.toRectF(), paintClear)
                val checkPoint = saveLayer(null, null)
//                this.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY)
//                    this.drawBitmap(imageBitmap.asAndroidBitmap(), 0f, 0f, paintClear)
//                    this.drawRect(0f, 0f, canvasWidth, canvasHeight, paintBg2)
                this.drawCircle(offset.x, offset.y, 100f, paintClear)
                restoreToCount(checkPoint)

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

@Composable
fun ComposeCanvasSample2(modifier: Modifier, imageBitmap: ImageBitmap) {

    val path = remember {
        Path()
    }

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
                path.moveTo(offset.x, offset.y)
                it.consume()
            },
            onMove = {
                val position = it.position
                val offsetX = (position.x).coerceIn(0f, imageWidth.toFloat())
                val offsetY = (position.y).coerceIn(0f, imageHeight.toFloat())
                offset = Offset(offsetX, offsetY)
                path.lineTo(offset.x, offset.y)
                it.consume()
            },
            onUp = {
                val position = it.position
                val offsetX = (position.x).coerceIn(0f, imageWidth.toFloat())
                val offsetY = (position.y).coerceIn(0f, imageHeight.toFloat())
                offset = Offset(offsetX, offsetY)
                path.lineTo(offset.x, offset.y)
                path.close()
            },
            delayAfterDownInMillis = 20
        )

        println("⛺️ canvasModifier: $canvasModifier, offset: $offset")

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
//                drawCircle(
//                    center = offset,
//                    color = Color.Blue,
//                    radius = canvasHeight.coerceAtMost(canvasWidth) / 8f,
//                    blendMode = BlendMode.Clear
//                )

                drawPath(
                    color = Color.Green,
                    path = path,
//                    style = Stroke(width = 1.dp.toPx()),
                    blendMode = BlendMode.Clear
                )
                restoreToCount(checkPoint)
            }


        }
    }
}