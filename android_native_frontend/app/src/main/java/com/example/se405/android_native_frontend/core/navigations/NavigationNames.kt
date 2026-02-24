package com.example.se405.android_native_frontend.navigation

import kotlinx.serialization.Serializable

/**
 * Example destination without arguments.
 *
 * This represents a simple screen that does not require
 * any navigation parameters.
 *
 * Being declared as an object ensures a single instance
 * and enforces that no arguments can be provided.
 */
@Serializable
object ScreenANav

/**
 * Example destination with arguments.
 *
 * @property arg1 A string parameter passed to the destination.
 * @property arg2 An integer parameter passed to the destination.
 *
 * This demonstrates how strongly-typed arguments can be enforced
 * at compile time using Kotlin Serialization.
 *
 * When navigating:
 *
 * navController.navigate(ScreenB("John", 25))
 *
 * Arguments are automatically serialized and restored
 * from the navigation back stack.
 */
@Serializable
data class ScreenBNav(
    val arg1: String,
    val arg2: Int
)