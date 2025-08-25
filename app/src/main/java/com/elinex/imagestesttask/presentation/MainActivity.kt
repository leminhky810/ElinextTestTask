package com.elinex.imagestesttask.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.elinex.imagestesttask.R
import com.elinex.imagestesttask.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Main Activity for the Images Test Task application.
 * 
 * This activity serves as the primary entry point for the user interface and handles:
 * - Application initialization and setup
 * - Permission management (notifications for Android 13+)
 * - Splash screen coordination
 * - Edge-to-edge display configuration
 * - Navigation setup for the main application flow
 * 
 * Key responsibilities:
 * - Manages the application lifecycle and initialization process
 * - Handles notification permission requests for Android 13+ devices
 * - Coordinates with the splash screen to provide smooth app startup
 * - Sets up edge-to-edge display for modern Android UI
 * - Manages window insets for proper content positioning
 * 
 * The activity uses:
 * - Hilt for dependency injection
 * - ViewBinding for safe view access
 * - ViewModel for state management
 * - Activity Result API for permission handling
 * - Coroutines for asynchronous operations
 * 
 * @author Elinext Test Task Team
 * @since 1.0.0
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()

    // Permission request launcher for notification permission
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setNotificationPermissionGranted(isGranted)
        viewModel.setNotificationPermissionRequested(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Install splash screen
        val splashScreen = installSplashScreen()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up splash screen to keep on screen until permission is handled
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value.shouldKeepSplashScreen() }
        
        setupWindowInsets()
        initializeApp()
    }
    
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun initializeApp() {
        lifecycleScope.launch {
            // Simulate app initialization (you can add actual initialization logic here)
            // For example, loading user preferences, checking database, etc.
            
            // Request notification permission
            requestNotificationPermissionIfNeeded()
            
            // Mark app as initialized
            viewModel.setAppInitialized(true)
        }
    }

    /**
     * Requests notification permission if needed (Android 13+).
     */
    private fun requestNotificationPermissionIfNeeded() {
        // Only request permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                // Permission already granted
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted, mark as requested and granted
                    viewModel.setNotificationPermissionGranted(true)
                    viewModel.setNotificationPermissionRequested(true)
                }
                
                // Should show rationale (user denied before)
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show explanation dialog before requesting permission
                    showNotificationPermissionRationale()
                }
                
                // First time requesting or user selected "Don't ask again"
                else -> {
                    // Request permission directly
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For older Android versions, mark as requested and granted (no permission needed)
            viewModel.setNotificationPermissionGranted(true)
            viewModel.setNotificationPermissionRequested(true)
        }
    }

    /**
     * Shows a rationale dialog explaining why notification permission is needed.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showNotificationPermissionRationale() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.notification_permission_title)
            .setMessage(R.string.notification_permission_rationale)
            .setPositiveButton(R.string.notification_permission_positive) { _, _ ->
                // User agreed to the rationale, now request permission
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton(R.string.notification_permission_negative) { dialog, _ ->
                // User declined, mark as requested but not granted
                viewModel.setNotificationPermissionGranted(false)
                viewModel.setNotificationPermissionRequested(true)
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}