@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.components.PopUpLayout
import com.example.se405.android.core.presentation.theme.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// ─── Data model ──────────────────────────────────────────────────────────────

/**
 * Model trung gian – cả WorkspaceMember lẫn GetUsersExcluding đều map về đây.
 */
data class AddableMember(
    val userId: Uuid,
    val displayName: String,
    val username: String,
    val avatarUrl: String? = null,
)

// ─── UI state ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalUuidApi::class)
sealed interface AddMembersUiState {
    object Loading : AddMembersUiState
    data class Editing(
        val selectedIds: Set<Uuid> = emptySet(),
    ) : AddMembersUiState
}

// ─── Core shared composable ───────────────────────────────────────────────────

/**
 * Nội dung popup thêm members dùng chung.
 *
 * @param title        Tiêu đề hiển thị, VD: "Add members to project"
 * @param subtitle     Dòng phụ, VD: tên project / workspace
 * @param candidates   Danh sách user có thể thêm vào (đã lọc sẵn từ VM)
 * @param isLoading    Trạng thái loading khi đang gọi API
 * @param onAdd        Callback khi nhấn Add, nhận danh sách userId đã chọn
 * @param onCancel     Callback khi nhấn Cancel
 */
@OptIn(ExperimentalUuidApi::class)
@Composable
fun AddMembersPopUpContent(
    title: String,
    subtitle: String,
    candidates: List<AddableMember>,
    isLoading: Boolean = false,
    onAdd: (List<Uuid>) -> Unit,
    onCancel: () -> Unit,
) {
    var uiState by remember { mutableStateOf<AddMembersUiState>(AddMembersUiState.Editing()) }
    var searchQuery by remember { mutableStateOf("") }

    // Sync loading flag từ bên ngoài vào local state
    LaunchedEffect(isLoading) {
        if (isLoading) uiState = AddMembersUiState.Loading
        // Khi isLoading = false và vẫn còn ở Loading thì không tự flip về Editing
        // – caller sẽ dismiss popup sau khi hoàn thành
    }

    val filteredCandidates = remember(candidates, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) candidates
        else candidates.filter {
            it.displayName.lowercase().contains(q) || it.username.lowercase().contains(q)
        }
    }

    PopUpLayout {
        when (uiState) {
            is AddMembersUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is AddMembersUiState.Editing -> {
                val state = uiState as AddMembersUiState.Editing

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Header ────────────────────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = title, style = AppText.DisplayBold)
                        Text(
                            text = subtitle,
                            style = AppText.Body2Regular,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }

                    // ── Search bar ────────────────────────────────────────────
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by name or username") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        shape = RoundedCornerShape(12.dp),
                    )

                    // ── Selected count badge ──────────────────────────────────
                    AnimatedVisibility(visible = state.selectedIds.isNotEmpty()) {
                        Text(
                            text = "${state.selectedIds.size} member(s) selected",
                            style = AppText.Body2Regular,
                            color = Blue40,
                        )
                    }

                    // ── Member list ───────────────────────────────────────────
                    if (candidates.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No available members to add.",
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
                                val isSelected = member.userId in state.selectedIds
                                MemberSelectRow(
                                    member = member,
                                    isSelected = isSelected,
                                    onClick = {
                                        uiState = if (isSelected) {
                                            state.copy(selectedIds = state.selectedIds - member.userId)
                                        } else {
                                            state.copy(selectedIds = state.selectedIds + member.userId)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // ── Action buttons ────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ButtonApp(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                            type = ButtonType.OUTLINED,
                        ) { Text("Cancel") }

                        Spacer(modifier = Modifier.width(24.dp))

                        ButtonApp(
                            type = ButtonType.FILLED,
                            modifier = Modifier.weight(1f),
                            enabled = state.selectedIds.isNotEmpty(),
                            onClick = {
                                uiState = AddMembersUiState.Loading
                                onAdd(state.selectedIds.toList())
                            },
                        ) { Text("Add (${state.selectedIds.size})") }
                    }
                }
            }
        }
    }
}

// ─── Member row ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun MemberSelectRow(
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
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Blue90)
                    .border(1.dp, BlueGrey80, CircleShape)
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Preview(name = "Add Members Popup",backgroundColor = 0xfff, showBackground = true,)
@Composable
private fun AddMembersPopUpContentPreview() {
    val previewMembers = listOf(
        AddableMember(
            userId = Uuid.random(),
            displayName = "Nguyen Van An",
            username = "an.nguyen"
        ),
        AddableMember(
            userId = Uuid.random(),
            displayName = "Tran Thi Mai",
            username = "mai.tran"
        ),
        AddableMember(
            userId = Uuid.random(),
            displayName = "Le Hoang Khang",
            username = "khang.le"
        ),
        AddableMember(
            userId = Uuid.random(),
            displayName = "Pham Minh Duc",
            username = "duc.pham"
        ),
        AddableMember(
            userId = Uuid.random(),
            displayName = "Vo Gia Bao",
            username = "bao.vo"
        ),
    )
    Android_Theme {
        AddMembersPopUpContent(
            title = "Add members to project",
            subtitle = "Mobile Banking App",
            candidates = previewMembers,
            onAdd = {},
            onCancel = {}
        )
    }
}