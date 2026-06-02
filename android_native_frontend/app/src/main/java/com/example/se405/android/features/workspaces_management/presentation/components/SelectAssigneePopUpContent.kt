package com.example.se405.android.features.workspaces_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.components.PopUpLayout
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.core.presentation.theme.Blue40
import com.example.se405.android.core.presentation.theme.BlueGrey80
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
@Composable
fun SelectAssigneePopUpContent(
    title: String,
    subtitle: String,
    candidates: List<AddableMember>,
    selectedAssigneeId: Uuid?,
    onSelect: (Uuid?) -> Unit,
    onCancel: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCandidates = remember(candidates, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) candidates
        else candidates.filter {
            it.displayName.lowercase().contains(q) || it.username.lowercase().contains(q)
        }
    }

    PopUpLayout {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = title, style = AppText.DisplayBold)
                Text(
                    text = subtitle,
                    style = AppText.Body2Regular,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or username") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                shape = RoundedCornerShape(12.dp),
            )

            // Member list
            if (candidates.isEmpty()) {
                Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp), contentAlignment = Alignment.Center,) {
                    Text(
                        text = "No available members to assign.",
                        style = AppText.Body2Regular,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            } else if (filteredCandidates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No results for \"$searchQuery\"",
                        style = AppText.Body2Regular,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredCandidates, key = { it.userId.toString() }) { member ->
                        val isSelected = member.userId == selectedAssigneeId
                        MemberAssigneeRow(
                            member = member,
                            isSelected = isSelected,
                            onClick = {
                                onSelect(member.userId)
                            }
                        )
                    }
                }
            }

            // Action buttons
            Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically,) {
                ButtonApp(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    type = ButtonType.OUTLINED,
                ) { Text("Cancel") }

                Spacer(modifier = Modifier.width(24.dp))
                ButtonApp(type = ButtonType.FILLED, modifier = Modifier.weight(1f), onClick = {
                        onSelect(null) // Unassign/Clear
                    }) { Text("Clear Assignee") }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun MemberAssigneeRow(
    member: AddableMember,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (isSelected) Blue40.copy(alpha = 0.08f) else Color.Transparent
    val borderColor = if (isSelected) Blue40 else BlueGrey80

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Avatar placeholder (initials)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSelected) Blue40 else BlueGrey80.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = member.displayName.firstOrNull()?.uppercase() ?: "?",
                style = AppText.BodyBold,
                color = Color.White,
            )
        }

        // Name + username
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.displayName,
                style = AppText.BodySemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "@${member.username}",
                style = AppText.Body2Regular,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Tick icon
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Blue40),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(14.dp),
                    tint = Color.White,
                )
            }
        }
    }
}
