package com.elinex.imagestesttask.presentation.helper

import androidx.viewpager2.widget.ViewPager2
import com.elinex.imagestesttask.presentation.helper.CircleIndicatorWrapper

/**
 * Manages page indicator logic for ViewPager2 with CircleIndicatorWrapper.
 * 
 * This class encapsulates all indicator-related operations including:
 * - Setting up page change callbacks
 * - Handling page selection animations
 * - Managing indicator state
 * - Synchronizing indicator with ViewPager2 data changes
 * - Force updating indicator on first load
 * 
 * @param indicator CircleIndicatorWrapper instance to manage
 * @param viewPager ViewPager2 instance to monitor
 * 
 * @author Elinext Test Task Team
 * @since 1.0.0
 */
class PageIndicatorManager(
    private val indicator: CircleIndicatorWrapper,
    private val viewPager: ViewPager2
) {

    private var isUpdating: Boolean = false

    /**
     * Sets up the page change callback for automatic indicator updates.
     */
    fun setupPageChangeCallback() {
        android.util.Log.d("PageIndicatorManager", "setupPageChangeCallback: setting up callbacks")
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                android.util.Log.d("PageIndicatorManager", "onPageSelected: position=$position")
                if (!isUpdating) {
                    indicator.animateToPage(position)
                }
            }
            
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                android.util.Log.d("PageIndicatorManager", "onPageScrollStateChanged: state=$state")
                // Auto-update indicator when ViewPager2 data changes
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    syncIndicatorWithViewPager()
                }
            }
        })
        
        // Don't do initial sync here - wait for explicit data update
        android.util.Log.d("PageIndicatorManager", "setupPageChangeCallback: callbacks registered")
    }
    
    /**
     * Synchronizes the indicator with the current ViewPager2 state.
     */
    private fun syncIndicatorWithViewPager() {
        val totalPages = viewPager.adapter?.itemCount ?: 0
        val currentPage = viewPager.currentItem
        
        android.util.Log.d("PageIndicatorManager", "syncIndicatorWithViewPager: totalPages=$totalPages, currentPage=$currentPage, indicatorPageCount=${indicator.getPageCount()}, indicatorSelectedPage=${indicator.getSelectedPage()}")
        
        // Always update if we have pages and the indicator is not showing the correct count
        if (totalPages > 0 && totalPages != indicator.getPageCount()) {
            android.util.Log.d("PageIndicatorManager", "Updating page count from ${indicator.getPageCount()} to $totalPages")
            isUpdating = true
            indicator.updatePageCount(totalPages, currentPage)
            isUpdating = false
        } else if (totalPages > 0 && currentPage != indicator.getSelectedPage()) {
            android.util.Log.d("PageIndicatorManager", "Animating to page $currentPage")
            indicator.animateToPage(currentPage)
        } else if (totalPages == 0) {
            // Reset indicator when no pages
            android.util.Log.d("PageIndicatorManager", "Resetting indicator")
            indicator.reset()
        }
    }
    
    /**
     * Manually triggers indicator update when adapter data changes.
     * This should be called after submitList/submitImages on the adapter.
     */
    fun updateIndicatorOnDataChange() {
        // Add a small delay to ensure the adapter has processed the data
        viewPager.post {
            syncIndicatorWithViewPager()
        }
    }
    
    /**
     * Forces the indicator to update immediately.
     * 
     * This method is used to ensure the indicator shows correctly
     * on first load when data is already available.
     */
    fun forceIndicatorUpdate() {
        val totalPages = viewPager.adapter?.itemCount ?: 0
        val currentPage = viewPager.currentItem
        
        android.util.Log.d("PageIndicatorManager", "forceIndicatorUpdate: totalPages=$totalPages, currentPage=$currentPage")
        
        if (totalPages > 0) {
            indicator.forceUpdate(totalPages, currentPage)
        }
    }



    /**
     * Cleans up resources and unregisters callbacks.
     */
    fun cleanup() {
        viewPager.unregisterOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {})
    }
}
