package com.elinex.imagestesttask.presentation.imageDetail

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil3.ImageLoader
import coil3.asDrawable
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import com.elinex.imagestesttask.R
import com.elinex.imagestesttask.databinding.FragmentImageDetailBinding
import com.elinex.imagestesttask.presentation.base.ViewBindingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ImageDetailFragment : ViewBindingFragment<FragmentImageDetailBinding>(
    bindingInflater = FragmentImageDetailBinding::inflate
) {

    @Inject
    lateinit var imageLoader: dagger.Lazy<ImageLoader>

    private val viewModel: ImageDetailViewModel by viewModels()

    override fun setupViews() {
        setupActionBar()
        observeViewModel()
    }

    private fun setupActionBar() {
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadImage(imageUrl: String?) {
        // Show loading state
        binding.progressBar.visibility = View.VISIBLE
        
        val request = ImageRequest.Builder(requireContext())
            .data(imageUrl)
            .crossfade(true)
            .scale(Scale.FIT)
            .target(
                onSuccess = { result ->
                    binding.progressBar.visibility = View.GONE
                    binding.imageView.setImageDrawable(result.asDrawable(resources))
                    binding.imageView.visibility = View.VISIBLE
                },
                onError = {
                    binding.progressBar.visibility = View.GONE
                    binding.imageView.setImageResource(android.R.drawable.ic_menu_report_image)
                    binding.imageView.visibility = View.VISIBLE
                }
            )
            .build()

        imageLoader.get().enqueue(request)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collect { uiState ->
                    binding.textViewImageId.text = getString(R.string.image_no, uiState.imageId.toString())
                    binding.textViewUrl.text = uiState.imageUrl
                    loadImage(uiState.imageUrl)
                }

            }
        }
    }
}