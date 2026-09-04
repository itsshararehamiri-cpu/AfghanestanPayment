package com.danesh.ui.datepicker

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.ui.theme.appTextStyle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private val WheelItemHeight = 48.dp
private const val VisibleItemCount = 3

@Composable
internal fun WheelPickerColumn(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val safeSelectedIndex = selectedIndex.coerceIn(items.indices)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = safeSelectedIndex,
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val centeredIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex.coerceIn(items.indices) }
    }

    LaunchedEffect(safeSelectedIndex, items.size) {
        val target = safeSelectedIndex.coerceIn(items.indices)
        if (listState.firstVisibleItemIndex != target || listState.firstVisibleItemScrollOffset != 0) {
            listState.animateScrollToItem(target)
        }
    }

    LaunchedEffect(listState, items.size) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { !it }
            .collect {
                val index = listState.firstVisibleItemIndex.coerceIn(items.indices)
                onSelectedIndexChange(index)
            }
    }

    Box(
        modifier = modifier.height(WheelItemHeight * VisibleItemCount),
        contentAlignment = Alignment.Center,
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = WheelItemHeight),
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(items) { index, label ->
                val isSelected = index == centeredIndex
                Text(
                    text = label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WheelItemHeight)
                        .padding(horizontal = 4.dp),
                    color = if (isSelected) {
                        Color.White
                    } else {
                        Color.White.copy(alpha = 0.35f)
                    },
                    style = appTextStyle(
                        base = MaterialTheme.typography.bodySmall,
                        fontSize = if (isSelected) 16.sp else 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        }
    }
}
