@file:OptIn(ExperimentalPagerApi::class)

package com.smarttoolfactory.composeimagecropper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.smarttoolfactory.composeimagecropper.demo.CanvasDemo
import com.smarttoolfactory.composeimagecropper.demo.ImageScaleDemo
import com.smarttoolfactory.composeimagecropper.demo.ThumbnailDemo
import com.smarttoolfactory.composeimagecropper.ui.theme.ComposeImageCropperTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeImageCropperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colors.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        HomeContent()
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

    ScrollableTabRow(
        backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        edgePadding = 8.dp,
        // Our selected tab is our current page
        selectedTabIndex = pagerState.currentPage,
        // Override the indicator, using the provided pagerTabIndicatorOffset modifier
        indicator = {}
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

    HorizontalPager(
        state = pagerState,
        count = tabList.size
    ) { page: Int ->

        when (page) {

            0 -> ImageScaleDemo()
            1 -> ThumbnailDemo()
            else -> CanvasDemo()
        }
    }
}

internal val tabList =
    listOf(
        "Images Scaling",
        "Image Thumbnail",
        "Native and Compose Canvas",
    )