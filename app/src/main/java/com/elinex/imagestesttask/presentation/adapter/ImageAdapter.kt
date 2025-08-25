package com.elinex.imagestesttask.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.ImageLoader
import coil3.asDrawable
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import com.elinex.imagestesttask.databinding.ItemImageBinding
import com.elinex.imagestesttask.domain.model.ImageModel
import com.elinex.imagestesttask.presentation.exts.clearShimmer
import com.elinex.imagestesttask.presentation.exts.resetShimmer

/**
 * RecyclerView Adapter for displaying a 7×10 grid of images with shimmer loading animation.
 * 
 * This adapter manages the display of images in a grid layout with the following features:
 * - Efficient image loading using Coil library
 * - Shimmer loading animations for better UX
 * - Dynamic item width calculation for responsive design
 * - Click handling for image selection
 * - DiffUtil for efficient list updates
 * - Support for dummy items for grid padding
 * 
 * Key responsibilities:
 * - Binding image data to view holders
 * - Managing image loading states and animations
 * - Handling item click events
 * - Optimizing performance with DiffUtil
 * - Adapting to different screen sizes
 * 
 * The adapter uses ViewBinding for safe view access and integrates with
 * the Coil image loading library for efficient image caching and display.
 * 
 * @param imageLoader Coil ImageLoader for loading images from URLs
 * @param itemWidth Width of each grid item in pixels
 * @param onItemClick Callback function for item click events
 * 
 * @author Elinext Test Task Team
 * @since 1.0.0
 */
class ImageAdapter(
    private val imageLoader: ImageLoader,
    private var itemWidth: Int,
    private val onItemClick: (ImageModel) -> Unit
) : ListAdapter<ImageModel, ImageAdapter.ImageViewHolder>(ImageDiffCallback()) {

    fun updateItemWidth(newWidth: Int) {
        if (itemWidth != newWidth) {
            itemWidth = newWidth
            notifyItemRangeChanged(0, currentList.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(imageLoader, binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.layoutParams.width = itemWidth
        holder.bind(item)
    }

    /**
     * ViewHolder for individual image items with shimmer loading animation.
     * 
     * This ViewHolder manages the display of individual image items in the grid,
     * including image loading, shimmer animations, and click handling.
     * 
     * Key features:
     * - Uses Coil for efficient image loading and caching
     * - Implements shimmer loading animations
     * - Handles loading states and error states
     * - Provides smooth crossfade transitions
     * - Manages click interactions
     * 
     * @param imageLoader Coil ImageLoader for loading images
     * @param binding ViewBinding for safe view access
     * @param onItemClick Callback for item click events
     */
    class ImageViewHolder(
        private val imageLoader: ImageLoader,
        private val binding: ItemImageBinding,
        private val onItemClick: (ImageModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(imageModel: ImageModel) {
            binding.apply {

                // Set click listener for the entire item
                root.setOnClickListener {
                    onItemClick(imageModel)
                }

                val context = binding.root.context
                val shimmer = binding.shimmerContainer
                val imageUrl = imageModel.url

                shimmer.resetShimmer()
                shimmer.visibility = View.VISIBLE
                imageView.visibility = View.VISIBLE
                imageView.setImageDrawable(null)

                if (imageUrl.isNullOrEmpty()) {
                    return
                }
                val request = ImageRequest.Builder(itemView.context)
                    .data(imageUrl)
                    .crossfade(true)
                    .scale(Scale.FIT)
                    .target(
                        onSuccess = { result ->
                            shimmer.clearShimmer()
                            imageView.setImageDrawable(result.asDrawable(context.resources))
                            imageView.visibility = View.VISIBLE
                        },
                        onError = {
                            shimmer.clearShimmer()
                            imageView.setImageResource(android.R.drawable.ic_menu_report_image)
                            imageView.visibility = View.VISIBLE
                        }
                    )
                    .build()

                imageLoader.enqueue(request)
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     * 
     * This callback is used by RecyclerView to determine what has changed
     * in the data set, allowing for efficient updates without unnecessary
     * view recycling and rebinding.
     * 
     * The callback compares items by ID for structural changes and by
     * equality for content changes, ensuring optimal performance.
     */
    private class ImageDiffCallback : DiffUtil.ItemCallback<ImageModel>() {
        override fun areItemsTheSame(oldItem: ImageModel, newItem: ImageModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ImageModel, newItem: ImageModel): Boolean {
            // Add minimal debugging to understand what's causing the blinking
            val isSame = oldItem == newItem
            if (!isSame) {
                android.util.Log.d("ImageDiffCallback", "Content changed for id=${oldItem.id}: oldUrl=${oldItem.url}, newUrl=${newItem.url}")
            }
            return isSame
        }
    }
}