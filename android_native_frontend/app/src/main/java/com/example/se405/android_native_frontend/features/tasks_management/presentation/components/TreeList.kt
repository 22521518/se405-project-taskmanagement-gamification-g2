package com.example.se405.android_native_frontend.features.tasks_management.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android_native_frontend.core.presentation.theme.Android_native_frontendTheme

data class TreeNode<T>(
    val id: Int,
    val name: String,
    val data: T,
    val children: List<TreeNode<T>> = emptyList(),
    var isExpanded: Boolean = true,
)

data class FlatNode<T>(
    val node: TreeNode<T>,
    val depth: Int,
    val isLeaf: Boolean = node.children.isEmpty(),
)

/**
 * Generic expandable/collapsible tree renderer.
 *
 * Callers own the tree data shape and provide branch/leaf content composables.
 */
@Composable
fun <T> TreeList(
    roots: List<TreeNode<T>>,
    modifier: Modifier = Modifier,
    indentDp: Int = 16,
    branchContent: @Composable RowScope.(node: TreeNode<T>, depth: Int, isExpanded: Boolean) -> Unit,
    leafContent: @Composable RowScope.(node: TreeNode<T>, depth: Int) -> Unit,
) {
    var visibleNodes by remember(roots) {
        mutableStateOf(flattenTree(roots))
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(
            items = visibleNodes,
            key = { flat -> "${flat.node.id}_${flat.depth}" }
        ) { flat ->

            if (flat.isLeaf) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .padding(start = (flat.depth * indentDp).dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leafContent(flat.node, flat.depth)
                }

            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .clickable {
                            flat.node.isExpanded = !flat.node.isExpanded
                            visibleNodes = flattenTree(roots)
                        }
                        .padding(start = (flat.depth * indentDp).dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    branchContent(flat.node, flat.depth, flat.node.isExpanded)
                }
            }
        }
    }
}

/**
 * Flattens nested tree data into visible rows based on current expansion flags.
 */
private  fun <T> flattenTree(
    nodes: List<TreeNode<T>>,
    depth: Int = 0,
): List<FlatNode<T>> {
    val result = mutableListOf<FlatNode<T>>()

    for (node in nodes) {
        result.add(FlatNode(node = node, depth = depth))
        if (node.isExpanded && node.children.isNotEmpty()) {
            result.addAll(flattenTree(node.children, depth + 1))
        }
    }

    return result
}

/**
 * Preview for TreeList.
 *
 * Usage guide:
 * - In production, map domain objects to `TreeNode<T>` outside this composable.
 * - Keep side effects (navigation, analytics) in callbacks passed to content lambdas.
 */
@Preview(showBackground = true)
@Composable
fun TreeListPreview() {
    val tree = PreviewTreeData.trees
    Android_native_frontendTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TreeList(
                roots = tree,
                branchContent = { node, _, expanded ->
                    Text(
                        text = if (expanded) "▼ ${node.name}" else "▶ ${node.name}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leafContent = { node, _ ->
                    Text(
                        text = node.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            )
        }
    }
}
