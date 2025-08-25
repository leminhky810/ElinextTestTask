package com.elinex.imagestesttask.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Base Fragment that accepts ViewBinding inflate function in constructor.
 * This eliminates the need for getViewBinding() method override.
 * 
 * Usage:
 * class HomeFragment : ViewBindingFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
 *     override fun setupViews() {
 *         binding.textView.text = "Hello World!"
 *     }
 * }
 */
abstract class ViewBindingFragment<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {

    private var _binding: VB? = null
    
    /**
     * Safe access to the binding. Only available between onCreateView and onDestroyView.
     */
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException(
            "Binding is only available between onCreateView and onDestroyView"
        )

    /**
     * Optional method for child fragments to set up their views.
     * Called after binding is initialized.
     */
    open fun setupViews() {}

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
