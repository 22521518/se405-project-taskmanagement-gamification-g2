package com.example.se405.android.features.workspaces_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.R
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData.workspaces
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceRole


@Composable
fun WorkspaceDetailMemberSection(members: List<WorkspaceMember>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(text = "${members.size} Members", style = AppText.HeadRegular, color = Color.Gray)
            }
        }

        items(members.filter { it.user != null }) { member ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.Gray,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (member.role == WorkspaceRole.OWNER)
                            Text(
                                text = member.role.toString(),
                                style = AppText.CaptionBold,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        Text(
                            text = member.user?.displayName ?: member.user?.username
                            ?: "Unknown", style = AppText.HeadRegular, color = Color.Black
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(painter = painterResource(R.drawable.icon_email), contentDescription = "email: ", tint = Color.DarkGray)
                        Text(text = member.user?.email ?: "Unknown", style = AppText.Body2Regular, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}


@Preview(backgroundColor = 0xfff, showBackground = true, name = "SubScreenHeader")
@Composable
fun WorkspaceDetailMemberSectionPreview() {
    CompositionLocalProvider(LocalPopupController provides PopupController()){
        Android_Theme {
            WorkspaceDetailMemberSection(members = workspaces.first().members)
        }
    }
}
