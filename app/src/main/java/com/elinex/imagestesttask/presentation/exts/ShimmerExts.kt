package com.elinex.imagestesttask.presentation.exts

import android.view.View
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout

fun ShimmerFrameLayout.resetShimmer() {
    val shimmer = Shimmer.AlphaHighlightBuilder()
        .setAutoStart(true)
        .setBaseAlpha(0.7f)
        .setHighlightAlpha(0.6f)
        .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
        .setDuration(1500L)
        .setRepeatCount(-1)
        .setRepeatDelay(500L)
        .setShape(Shimmer.Shape.LINEAR)    // LINEAR tương ứng với app:shimmer_shape="linear"
        .build()

    setShimmer(shimmer)
    visibility = View.VISIBLE
    startShimmer()
}

fun ShimmerFrameLayout.clearShimmer() {
    stopShimmer()
    setShimmer(null)
    visibility = View.GONE
}