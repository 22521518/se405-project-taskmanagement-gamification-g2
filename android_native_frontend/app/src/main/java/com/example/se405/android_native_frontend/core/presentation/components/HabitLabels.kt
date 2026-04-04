@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.core.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.se405.android_native_frontend.R
import com.example.se405.android_native_frontend.core.presentation.theme.Android_native_frontendTheme
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Immutable
data class HabitLabel(
    val id: Uuid,
    val name: String,
    val description: String,
    val icon: Int,
)

val BuiltinLabels = listOf(
    HabitLabel(
        id = Uuid.fromLongs(1, 1),
        name = "Work",
        description = "Career, job-related tasks, and income-generating activities",
        icon = R.drawable.icon_work_habit_label
    ),
    HabitLabel(
        id = Uuid.fromLongs(2, 2),
        name = "Health",
        description = "Physical health, mental well-being, and self-care",
        icon = R.drawable.icon_strength_habit_label
    ),
    HabitLabel(
        id = Uuid.fromLongs(3, 3),
        name = "Personal",
        description = "Daily life, family, and personal responsibilities",
        icon = R.drawable.icon_discipline_habit_label
    ),
    HabitLabel(
        id = Uuid.fromLongs(4, 4),
        name = "Learning",
        description = "Studying, skill development, and knowledge growth",
        icon = R.drawable.icon_brain_habit_label
    ),
    HabitLabel(
        id = Uuid.fromLongs(5, 5),
        name = "Bad",
        description = "Bad habits that have a negative impact on your life",
        icon = R.drawable.icon_bad_habit_label
    ),
    HabitLabel(
        id = Uuid.fromLongs(6, 6),
        name = "Creative",
        description = "Creative work such as writing, design, and side projects",
        icon = R.drawable.icon_creativity_habit_label
    ),
    HabitLabel(
        id = Uuid.fromLongs(7, 7),
        name = "Networking",
        description = "Social activities and interactions with others",
        icon = R.drawable.icon_social_habit_label
    )
)

@Composable
fun BuiltinLabelIcon(
    label: HabitLabel,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified, // Keep the original color of the icons
) {
    Icon(
        painter = painterResource(id = label.icon),
        contentDescription = label.description,
        tint = tint,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun BuiltinLabelPreview() {
    Android_native_frontendTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BuiltinLabels.forEach { label ->
                BuiltinLabelIcon(label)
            }
        }
    }
}