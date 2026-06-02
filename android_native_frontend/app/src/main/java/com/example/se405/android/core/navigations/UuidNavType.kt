package com.example.se405.android.core.navigations

import android.os.Bundle
import androidx.navigation.NavType
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
val UuidType = object : NavType<Uuid>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Uuid? {
        return bundle.getString(key)?.let { Uuid.parse(it) }
    }

    override fun parseValue(value: String): Uuid {
        return Uuid.parse(value)
    }

    override fun put(bundle: Bundle, key: String, value: Uuid) {
        bundle.putString(key, value.toString())
    }

    override fun serializeAsValue(value: Uuid): String {
        return value.toString()
    }
}

@OptIn(ExperimentalUuidApi::class)
val UuidTypeMap: Map<KType, NavType<*>> = mapOf(
    typeOf<Uuid>() to UuidType
)
