package com.elinex.imagestesttask.presentation.home

import android.content.res.Configuration
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import coil3.ImageLoader
import com.elinex.imagestesttask.databinding.FragmentHomeBinding
import com.elinex.imagestesttask.domain.model.ImageModel
import com.elinex.imagestesttask.presentation.adapter.ImagePagerAdapter
import com.elinex.imagestesttask.presentation.base.ViewBindingFragment
import com.elinex.imagestesttask.presentation.exts.dpToPx
import com.elinex.imagestesttask.presentation.helper.PageIndicatorManager
import com.elinex.imagestesttask.presentation.helper.calculateGridConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : ViewBindingFragment<FragmentHomeBinding>(
    bindingInflater = FragmentHomeBinding::inflate
) {

    @Inject
    lateinit var imageLoader: dagger.Lazy<ImageLoader>

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var imagePagerAdapter: ImagePagerAdapter
    private lateinit var pageIndicatorManager: PageIndicatorManager


    companion object {
        /**
         * Spacing decoration value for grid items in pixels.
         * Used for calculating grid configuration and item spacing.
         */
        private const val DECORATION = 2
    }

    override fun setupViews() {
        setupViewPager()
        setupActionBar()
        observeViewModel()
    }

    /**
     * Handles configuration changes (e.g., orientation changes).
     * 
     * This method reinitializes the ViewPager2 and PageIndicatorManager
     * with the new grid configuration to ensure proper layout after
     * configuration changes.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val newGrid = requireContext().calculateGridConfig(
            dpToPx(DECORATION.plus(2), requireContext()),
            dpToPx(DECORATION, requireContext())
        )

        // Clean up existing page indicator manager
        if (::pageIndicatorManager.isInitialized) {
            pageIndicatorManager.cleanup()
        }

        // Update ViewPager2 adapter with new configuration
        imagePagerAdapter = ImagePagerAdapter(
            imageLoader = imageLoader.get(),
            onItemClick = { imageModel -> goToDetail(imageModel) },
            spanCount = newGrid.spanCount,
            itemWidth = newGrid.itemWidth
        )

        // Reapply all ViewPager2 settings
        binding.viewPagerImages.apply {
            adapter = imagePagerAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 1
            // Restore page transformer to prevent blinking
            setPageTransformer { page, position ->
                page.alpha = 1f
                page.translationX = 0f
            }
        }

        // Re-initialize page indicator manager
        pageIndicatorManager = PageIndicatorManager(
            indicator = binding.indicator,
            viewPager = binding.viewPagerImages
        )
        pageIndicatorManager.setupPageChangeCallback()

        // Restore current UI state if available
        viewModel.uiState.value.let { uiState ->
            if (!uiState.isLoading && !uiState.isEmpty) {
                imagePagerAdapter.submitImages(uiState.images)
                pageIndicatorManager.updateIndicatorOnDataChange()
            }
        }
    }

    /**
     * Sets up the ViewPager2 with ImagePagerAdapter and PageIndicatorManager.
     * 
     * This method initializes the carousel-like scrolling behavior with:
     * - Grid layout configuration based on screen size
     * - Page-based navigation with ViewPager2
     * - Circle indicator for page indication
     * - Page transformer to prevent blinking during transitions
     */
    private fun setupViewPager() {
        val config = context?.calculateGridConfig(
            dpToPx(DECORATION.plus(2), requireContext()),
            dpToPx(DECORATION, requireContext())
        ) ?: return

        // Initialize ViewPager2 adapter
        imagePagerAdapter = ImagePagerAdapter(
            imageLoader = imageLoader.get(),
            onItemClick = { imageModel -> goToDetail(imageModel) },
            spanCount = config.spanCount,
            itemWidth = config.itemWidth
        )

        binding.viewPagerImages.apply {
            adapter = imagePagerAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 1
            // Restore page transformer to prevent blinking
            setPageTransformer { page, position ->
                page.alpha = 1f
                page.translationX = 0f
            }
        }

        // Initialize and setup page indicator manager
        pageIndicatorManager = PageIndicatorManager(
            indicator = binding.indicator,
            viewPager = binding.viewPagerImages
        )
        pageIndicatorManager.setupPageChangeCallback()
        
        // Force initial indicator update if we have data
        viewModel.uiState.value.let { uiState ->
            if (!uiState.isLoading && !uiState.isEmpty) {
                android.util.Log.d("HomeFragment", "setupViewPager: forcing initial indicator update")
                pageIndicatorManager.forceIndicatorUpdate()
            }
        }
    }

    private fun setupActionBar() {
        binding.buttonAdd.setOnClickListener {
            viewModel.addNewImage()
        }

        binding.buttonRefresh.setOnClickListener {
            viewModel.refreshImages()
            binding.viewPagerImages.currentItem = 0
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    updateUI(uiState)
                }
            }
        }
    }

    /**
     * Updates the UI based on the current state.
     * 
     * Handles three states:
     * - Loading: Shows progress bar
     * - Empty: Shows empty state message
     * - Success: Shows ViewPager2 with images and forces indicator update
     */
    private fun updateUI(uiState: HomeUiState) {
        binding.apply {
            when {
                uiState.isLoading -> {
                    // Show loading state
                    progressBar.visibility = View.VISIBLE
                    textViewEmpty.visibility = View.GONE
                    viewPagerImages.visibility = View.GONE
                }

                uiState.isEmpty -> {
                    // Show empty state
                    progressBar.visibility = View.GONE
                    textViewEmpty.visibility = View.VISIBLE
                    viewPagerImages.visibility = View.GONE
                }

                else -> {
                    // Show images with integrated shimmer loading
                    progressBar.visibility = View.GONE
                    textViewEmpty.visibility = View.GONE
                    viewPagerImages.visibility = View.VISIBLE
                    imagePagerAdapter.submitImages(uiState.images)
                    // Force indicator update to ensure it shows on first load
                    pageIndicatorManager.forceIndicatorUpdate()
                }
            }
        }
    }

    private fun goToDetail(imageModel: ImageModel) {
        if (imageModel.url == null) return
        findNavController().navigate(
            HomeFragmentDirections.actionHomeToImageDetail(
                imageId = imageModel.id,
                imageUrl = imageModel.url
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::pageIndicatorManager.isInitialized) {
            pageIndicatorManager.cleanup()
        }
    }

}