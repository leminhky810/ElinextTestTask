package com.elinex.imagestesttask.presentation.helper

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
class GridSpacingItemDecoration(
    private val spanCount: Int,      // = 10
    private val spacing: Int         // = dpToPx(2)
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position / spanCount
        val row = position % spanCount

        outRect.left = if (column == 0) 0 else spacing
        outRect.top = if (row == 0) 0 else spacing
    }
}