package com.example.se405.android.features.workspaces_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.R
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData.projects
import com.example.se405.android.features.workspaces_management.presentation.screen.toGetProjectQueryTask
import com.example.se405.android.graphql.GetProjectQuery

@Composable
fun ProjectDetailMemberSection(members: List<GetProjectQuery.Member>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
            Text(text = "${members.size} Members", style = AppText.HeadBold, color = Color.Gray)
        }

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (member in members.filter { it.user != null }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 1.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = Color.Black.copy(alpha = 0.8f),
                            spotColor = Color.Black.copy(alpha = 0.16f),
                            clip = false
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
                        Text(text = member.user?.displayName ?: member.user?.username ?: "Unknown", style = AppText.HeadBold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(painter = painterResource(R.drawable.icon_email), contentDescription = "email: ", tint = Color.DarkGray)
                            Text(text = member.user?.email ?: "Unknown", style = AppText.Body2Regular, color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}

@Preview(backgroundColor = 0xfff, showBackground = true, name = "SubScreenHeader")
@Composable
fun ProjectDetailMemberSectionPreview() {
    CompositionLocalProvider(LocalPopupController provides PopupController()){
        Android_Theme {
            ProjectDetailMemberSection(members = projects.first().toGetProjectQueryTask().members)
        }
    }
}
