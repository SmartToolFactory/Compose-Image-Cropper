@file:OptIn(ExperimentalMaterial3Api::class)

package com.smarttoolfactory.composeimagecropper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.smarttoolfactory.composeimagecropper.demo.CanvasDemo
import com.smarttoolfactory.composeimagecropper.demo.ImageCropDemo
import com.smarttoolfactory.composeimagecropper.demo.ImageScaleDemo
import com.smarttoolfactory.composeimagecropper.demo.ThumbnailDemo
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        HomeContent()
//                            ImageCropDemo()
                        
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
                1 -> ImageScaleDemo()
                2 -> ThumbnailDemo()
                else -> CanvasDemo()
            }
        }
    }
}

internal val tabList =
    listOf(
        "Image Cropping",
        "Images Scaling",
        "Image Thumbnail",
        "Native and Compose Canvas",
    )