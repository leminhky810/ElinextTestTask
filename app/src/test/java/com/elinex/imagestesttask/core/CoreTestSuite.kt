package com.elinex.imagestesttask.core

import com.elinex.imagestesttask.core.utils.PermissionUtilsTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for all core module tests
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    ConstantsTest::class,
    AppDispatchersTest::class,
    PermissionUtilsTest::class
)
class CoreTestSuite
