package com.elinex.imagestesttask.presentation.helper

import android.content.Context
import android.util.AttributeSet
import android.view.View
import me.relex.circleindicator.CircleIndicator2

/**
 * Wrapper class for CircleIndicator2 that provides enhanced functionality and error handling.
 * 
 * This wrapper addresses common issues with CircleIndicator2:
 * - Prevents crashes from invalid page selections
 * - Provides better state management
 * - Handles edge cases gracefully
 * - Offers a cleaner API for common operations
 * 
 * @author Elinext Test Task Team
 * @since 1.0.0
 */
class CircleIndicatorWrapper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CircleIndicator2(context, attrs, defStyleAttr) {

    private var currentPageCount: Int = 0
    private var currentSelectedPage: Int = 0
    private var isAnimating: Boolean = false

    /**
     * Safely updates the indicator with new page count.
     * 
     * @param totalPages Total number of pages
     * @param selectedPage Currently selected page (will be clamped to valid range)
     */
    fun updatePageCount(totalPages: Int, selectedPage: Int = 0) {
        android.util.Log.d("CircleIndicatorWrapper", "updatePageCount: totalPages=$totalPages, selectedPage=$selectedPage, currentPageCount=$currentPageCount")
        
        if (totalPages < 0) return
        
        val safeSelectedPage = selectedPage.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
        
        if (currentPageCount != totalPages) {
            android.util.Log.d("CircleIndicatorWrapper", "Creating indicators: totalPages=$totalPages, safeSelectedPage=$safeSelectedPage")
            currentPageCount = totalPages
            currentSelectedPage = safeSelectedPage
            createIndicators(totalPages, safeSelectedPage)
        } else if (currentSelectedPage != safeSelectedPage) {
            android.util.Log.d("CircleIndicatorWrapper", "Animating to safeSelectedPage=$safeSelectedPage")
            currentSelectedPage = safeSelectedPage
            safeAnimatePageSelected(safeSelectedPage)
        }
    }

    /**
     * Safely animates to the specified page.
     * 
     * @param page The page to animate to
     */
    fun animateToPage(page: Int) {
        if (page < 0 || page >= currentPageCount || isAnimating) return
        
        isAnimating = true
        currentSelectedPage = page
        
        try {
            safeAnimatePageSelected(page)
        } catch (e: Exception) {
            // Handle any animation errors gracefully
            currentSelectedPage = page.coerceIn(0, currentPageCount - 1)
        } finally {
            isAnimating = false
        }
    }

    /**
     * Gets the current page count.
     */
    fun getPageCount(): Int = currentPageCount

    /**
     * Gets the currently selected page.
     */
    fun getSelectedPage(): Int = currentSelectedPage

    /**
     * Checks if the indicator is currently animating.
     */
    fun isAnimating(): Boolean = isAnimating

    /**
     * Resets the indicator state.
     */
    fun reset() {
        currentPageCount = 0
        currentSelectedPage = 0
        isAnimating = false
        createIndicators(0, 0)
    }
    
    /**
     * Forces the indicator to be visible and updates its state.
     */
    fun forceUpdate(totalPages: Int, selectedPage: Int = 0) {
        android.util.Log.d("CircleIndicatorWrapper", "forceUpdate: totalPages=$totalPages, selectedPage=$selectedPage")
        visibility = android.view.View.VISIBLE
        updatePageCount(totalPages, selectedPage)
    }

    /**
     * Safely animates to the selected page with bounds checking.
     */
    private fun safeAnimatePageSelected(position: Int) {
        if (position >= 0 && position < childCount) {
            animatePageSelected(position)
        }
    }
}
