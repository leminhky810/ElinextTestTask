/**
 * Created by  on 28/8/25.
 * @author
 */
package com.elinex.imagestesttask.presentation.helper

import android.content.Context
import android.content.res.Configuration
import com.elinex.imagestesttask.core.COLUMN_EXPECTED
import com.elinex.imagestesttask.core.ROW_EXPECTED

object GridDefaults {
    const val PORTRAIT_ROW = ROW_EXPECTED
    const val PORTRAIT_COLUMN = COLUMN_EXPECTED
    const val LANDSCAPE_ROW = PORTRAIT_COLUMN
    const val LANDSCAPE_COLUMN = PORTRAIT_ROW
    const val ITEM_DECORATION = 2
}

data class GridConfig(val spanCount: Int, val itemWidth: Int)

fun Context.calculateGridConfig(
    recyclerHorizontalPadding: Int = 0,
    itemSpacing: Int = 0,
): GridConfig {
    val screenWidth = resources.displayMetrics.widthPixels

    val (row, column) = getGridByOrientation()

    val totalSpacing = itemSpacing * (column - 1)
    val availableWidth = screenWidth - recyclerHorizontalPadding - totalSpacing
    val itemWidth = availableWidth / column

    return GridConfig(row, itemWidth)
}

// Row-Column
fun Context.getGridByOrientation(): Pair<Int, Int> {
    val orientation = resources.configuration.orientation
    val grid = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        GridDefaults.LANDSCAPE_ROW to GridDefaults.LANDSCAPE_COLUMN
    } else {
        GridDefaults.PORTRAIT_ROW to GridDefaults.PORTRAIT_COLUMN
    }
    return grid
}