package com.elinex.imagestesttask.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil3.ImageLoader
import com.elinex.imagestesttask.core.ITEMS_PER_PAGE
import com.elinex.imagestesttask.databinding.ItemImageBinding
import com.elinex.imagestesttask.databinding.ItemImagePageBinding
import com.elinex.imagestesttask.domain.model.ImageModel
import com.elinex.imagestesttask.presentation.helper.GridSpacingItemDecoration
import com.elinex.imagestesttask.presentation.exts.dpToPx
import com.elinex.imagestesttask.presentation.helper.GridDefaults

/**
 * Data class representing a page of images.
 *
 * @param pageIndex Index of the page
 * @param images List of images in this page
 */
data class PageData(
    val pageIndex: Int,
    val images: List<ImageModel>
)

/**
 * ViewPager2 Adapter for displaying pages of images in a grid layout.
 *
 * Each page contains a RecyclerView with a grid of images. This adapter
 * manages the creation and binding of individual page views.
 * 
 * Features:
 * - Efficient DiffUtil-based updates for both pages and individual images
 * - Grid layout with configurable span count and item width
 * - Page-based navigation with proper spacing and decorations
 * - URL change detection to prevent unnecessary updates
 *
 * @param imageLoader Coil ImageLoader for loading images
 * @param onItemClick Callback for image click events
 * @param spanCount Number of columns in the grid
 * @param itemWidth Width of each grid item
 *
 * @author Elinext Test Task Team
 * @since 1.0.0
 */
class ImagePagerAdapter(
    private val imageLoader: ImageLoader,
    private val onItemClick: (ImageModel) -> Unit,
    private val spanCount: Int,
    private val itemWidth: Int
) : ListAdapter<PageData, ImagePagerAdapter.PageViewHolder>(PageDiffCallback()) {

    fun submitImages(images: List<ImageModel>) {
        val pageDataList = createPageDataList(images)
        super.submitList(pageDataList)
    }

    private fun createPageDataList(images: List<ImageModel>): List<PageData> {
        if (images.isEmpty()) return emptyList()

        val pageCount = (images.size + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE
        return List(pageCount) { pageIndex ->
            val startIndex = pageIndex * ITEMS_PER_PAGE
            val endIndex = minOf(startIndex + ITEMS_PER_PAGE, images.size)
            PageData(
                pageIndex = pageIndex,
                images = images.subList(startIndex, endIndex)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ItemImagePageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val pageData = getItem(position)
        holder.bind(pageData.images)
    }

    /**
     * ViewHolder for individual pages containing a RecyclerView with grid layout.
     */
    inner class PageViewHolder(
        private val binding: ItemImagePageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val pageAdapter = PageImageAdapter(
            imageLoader = imageLoader,
            itemWidth = itemWidth,
            onItemClick = onItemClick
        )

        init {
            setupRecyclerView()
        }

        private fun setupRecyclerView() {
            val gridLayoutManager = GridLayoutManager(
                binding.root.context,
                spanCount,
                RecyclerView.HORIZONTAL,
                false
            )

            binding.recyclerViewPage.apply {
                layoutManager = gridLayoutManager
                adapter = pageAdapter
                clipToPadding = false
                itemAnimator = null
                setHasFixedSize(true)

                // Add spacing decoration
                addItemDecoration(
                    GridSpacingItemDecoration(
                        spanCount = spanCount,
                        spacing = dpToPx(GridDefaults.ITEM_DECORATION, context)
                    )
                )
            }
        }

        fun bind(images: List<ImageModel>) {
            pageAdapter.submitList(images)
        }
    }

    /**
     * Adapter for individual page content with DiffUtil support.
     */
    private class PageImageAdapter(
        private val imageLoader: ImageLoader,
        private val itemWidth: Int,
        private val onItemClick: (ImageModel) -> Unit
    ) : ListAdapter<ImageModel, ImageAdapter.ImageViewHolder>(ImageDiffCallback()) {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ImageAdapter.ImageViewHolder {
            val binding = ItemImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ImageAdapter.ImageViewHolder(imageLoader, binding, onItemClick)
        }

        override fun onBindViewHolder(holder: ImageAdapter.ImageViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemView.layoutParams.width = itemWidth
            holder.bind(item)
        }
    }

    /**
     * DiffUtil callback for efficient page updates.
     * 
     * Compares pages by index and content, including URL changes
     * to prevent unnecessary updates when only URLs are refreshed.
     */
    private class PageDiffCallback : DiffUtil.ItemCallback<PageData>() {
        override fun areItemsTheSame(oldItem: PageData, newItem: PageData): Boolean {
            return oldItem.pageIndex == newItem.pageIndex
        }

        override fun areContentsTheSame(oldItem: PageData, newItem: PageData): Boolean {
            // Compare images by their IDs for efficiency
            if (oldItem.images.size != newItem.images.size) {
                android.util.Log.d(
                    "PageDiffCallback",
                    "Page ${oldItem.pageIndex}: size changed from ${oldItem.images.size} to ${newItem.images.size}"
                )
                return false
            }

            // Compare IDs and URLs directly without creating intermediate collections
            for (i in oldItem.images.indices) {
                val oldImage = oldItem.images[i]
                val newImage = newItem.images[i]
                
                if (oldImage.id != newImage.id) {
                    android.util.Log.d(
                        "PageDiffCallback",
                        "Page ${oldItem.pageIndex}: ID changed at position $i from ${oldImage.id} to ${newImage.id}"
                    )
                    return false
                }
                
                // Check if URL changed (including null changes)
                if (oldImage.url != newImage.url) {
                    android.util.Log.d(
                        "PageDiffCallback",
                        "Page ${oldItem.pageIndex}: URL changed at position $i from ${oldImage.url} to ${newImage.url}"
                    )
                    return false
                }
            }
            return true
        }
    }

    /**
     * DiffUtil callback for efficient image list updates.
     * 
     * Uses direct equality comparison to detect any changes
     * in image properties including URL updates.
     */
    private class ImageDiffCallback : DiffUtil.ItemCallback<ImageModel>() {
        override fun areItemsTheSame(oldItem: ImageModel, newItem: ImageModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ImageModel, newItem: ImageModel): Boolean {
            return oldItem == newItem
        }
    }
}
