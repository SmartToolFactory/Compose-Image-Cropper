@file:OptIn(ExperimentalMaterial3Api::class)

package com.smarttoolfactory.composeimagecropper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.AsyncImagePainter.State.Empty.painter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.smarttoolfactory.composeimagecropper.demo.*
import com.smarttoolfactory.composeimagecropper.ui.theme.ComposeImageCropperTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeImageCropperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                   Column(modifier= Modifier.fillMaxSize()) {
//                       HomeContent()
                       ImageCropDemo()

                   }
                }
            }
        }
    }
}

@ExperimentalPagerApi
@Composable
private fun HomeContent() {

    val pagerState: PagerState = rememberPagerState(initialPage = 0)

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            ScrollableTabRow(
                modifier = Modifier.fillMaxWidth(),
                // Our selected tab is our current page
                selectedTabIndex = pagerState.currentPage,
                // Override the indicator, using the provided pagerTabIndicatorOffset modifier
                indicator = { tabPositions: List<TabPosition> ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(
                            tabPositions[pagerState.currentPage]
                        ),
                        height = 4.dp
                    )
                },
                edgePadding = 4.dp
            ) {
                // Add tabs for all of our pages
                tabList.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        }
    ) {

        HorizontalPager(
            modifier = Modifier.padding(it),
            state = pagerState,
            count = tabList.size
        ) { page: Int ->

            when (page) {

                0 -> ImageCropDemo()
                else -> CanvasDemo()
            }
        }
    }
}

internal val tabList =
    listOf(
        "Image Cropping",
        "Native and Compose Canvas",
    )