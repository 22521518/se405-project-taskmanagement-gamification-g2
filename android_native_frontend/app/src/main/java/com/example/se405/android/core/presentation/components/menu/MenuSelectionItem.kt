@file:OptIn(ExperimentalUuidApi::class)
package com.example.se405.android.core.presentation.components.menu

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class MenuSelectionItem<T>(val id: Uuid = Uuid.random(), val data: T, val selected: Boolean)