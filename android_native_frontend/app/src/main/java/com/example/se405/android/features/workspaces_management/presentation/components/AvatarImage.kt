package com.example.se405.android.features.workspaces_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.se405.android.R
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.core.presentation.theme.*

@Composable
fun ImageAvatar(
    imageUrl: String? = null,
    displayName: String? = null,
    size: Dp = 40.dp,
    isSelected: Boolean = false,
) {
    val placeholderColor =
        if (isSelected) Blue40 else Color.Gray

    if (imageUrl.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(placeholderColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = displayName?.firstOrNull()?.uppercase() ?: "?",
                style = AppText.BodyBold,
                color = Color.White,
            )
        }
    } else {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            loading = {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp)
                )
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(placeholderColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = displayName?.firstOrNull()?.uppercase() ?: "?",
                        style = AppText.BodyBold,
                        color = Color.White,
                    )
                }
            }
        )
    }
}

data class AvatarItem(
    val imageUrl: String? = null,
    val displayName: String,
)

@Composable
fun AvatarRow(
    avatars: List<AvatarItem>,
    size: Dp = 40.dp,
) {
    Row(
        modifier = Modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        avatars.take(3).forEachIndexed { index, avatar ->
            Box(
                modifier = Modifier.offset(x = (-index * 12).dp).border(width = 2.dp,color = Color.White,shape = CircleShape), contentAlignment = Alignment.Center
            ) {
                ImageAvatar(
                    imageUrl = avatar.imageUrl,
                    displayName = avatar.displayName,
                    size = size,
                )
            }
        }

        if (avatars.size > 3) {
            Box(
                modifier = Modifier.offset(x = (-3 * 12).dp).size(size).border(width = 2.dp,color = Color.White,shape = CircleShape)
                    .clip(CircleShape).background(Color.Gray),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+${avatars.size - 3}",
                    style = AppText.Body2Bold,
                    color = Color.White,
                )
            }
        }
    }
}