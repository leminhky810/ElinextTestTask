package com.elinex.imagestesttask.presentation

import com.elinex.imagestesttask.presentation.home.HomeViewModelTest
import com.elinex.imagestesttask.presentation.imageDetail.ImageDetailViewModelTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for all presentation module tests (excluding UI components and Android-specific APIs)
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    // ViewModels
    MainActivityViewModelTest::class,
    HomeViewModelTest::class,
    ImageDetailViewModelTest::class

)
class PresentationTestSuite
