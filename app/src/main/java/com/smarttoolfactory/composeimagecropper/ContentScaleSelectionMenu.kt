package com.smarttoolfactory.composeimagecropper

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val contentScaleOptions =
    listOf("None", "Fit", "Crop", "FillBounds", "FillWidth", "FillHeight", "Inside")

@Composable
fun ContentScaleSelectionMenu(
    modifier: Modifier = Modifier,
    contentScale: ContentScale,
    onContentScaleChanged: (ContentScale) -> Unit
) {
    var index = when (contentScale) {
        ContentScale.None -> 0
        ContentScale.Fit -> 1
        ContentScale.Crop -> 2
        ContentScale.FillBounds -> 3
        ContentScale.FillWidth -> 4
        ContentScale.FillHeight -> 5
        else -> 6
    }

    Row(
        modifier = Modifier.padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ExposedSelectionMenu(
            modifier = modifier
                .fillMaxWidth(),
            index = index,
            title = "ContentScale",
            options = contentScaleOptions,
            onSelected = {
                index = it
                val scale = when (index) {
                    0 -> ContentScale.None
                    1 -> ContentScale.Fit
                    2 -> ContentScale.Crop
                    3 -> ContentScale.FillBounds
                    4 -> ContentScale.FillWidth
                    5 -> ContentScale.FillHeight
                    else -> ContentScale.Inside
                }

                onContentScaleChanged(scale)
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExposedSelectionMenu(
    modifier: Modifier = Modifier,
    index: Int,
    title: String? = null,
    textStyle: TextStyle = TextStyle(
        fontWeight = FontWeight.W600,
        fontSize = 14.sp
    ),
    colors: TextFieldColors = ExposedDropdownMenuDefaults.textFieldColors(
        backgroundColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedLabelColor = Color.DarkGray,
        unfocusedLabelColor = Color.DarkGray,
        trailingIconColor = Color.DarkGray,
        focusedTrailingIconColor = Color.DarkGray,
        textColor = Color.DarkGray,
    ),
    options: List<String>,
    onSelected: (Int) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[index]) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        TextField(
            modifier = modifier,
            readOnly = true,
            value = selectedOptionText,
            onValueChange = { },
            label = {
                title?.let {
                    Text(it)
                }
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = colors,
            textStyle = textStyle
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false

            }
        ) {
            options.forEachIndexed { index: Int, selectionOption: String ->
                DropdownMenuItem(
                    onClick = {
                        selectedOptionText = selectionOption
                        expanded = false
                        onSelected(index)
                    }
                ) {
                    Text(text = selectionOption)
                }
            }
        }
    }
}
