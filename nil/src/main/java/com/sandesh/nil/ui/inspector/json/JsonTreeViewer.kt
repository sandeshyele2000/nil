package com.sandesh.nil.ui.inspector.json

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.sandesh.nil.ui.theme.NILColors
import com.sandesh.nil.utils.json.JsonTreeBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun JsonTreeViewer(
    json: String?,
    query: String = "",
    pathSearchMode: Boolean = false,
    activeMatchIndex: Int = 0,
    onMatchCountChanged: ((Int) -> Unit)? = null,
    enableInternalScroll: Boolean = false
) {
    val root by produceState<JsonNode?>(initialValue = null, key1 = json) {
        value = withContext(Dispatchers.Default) {
            JsonTreeBuilder.build(json)
        }
    }

    if (json.isNullOrBlank()) {
        Text("No JSON payload", style = MaterialTheme.typography.bodySmall)
        return
    }
    val rootNode = root ?: run {
        Text("Preparing JSON tree...", style = MaterialTheme.typography.bodySmall)
        return
    }

    val expanded = remember(json) {
        mutableStateMapOf<String, Boolean>().apply { put("root", true) }
    }
    val rows = remember(rootNode, expanded.toMap(), query, pathSearchMode) {
        val out = mutableListOf<JsonRenderRow>()
        val pathFilter = query.takeIf { pathSearchMode && it.isNotBlank() }
        appendRows(rootNode, path = "root", depth = 0, expanded = expanded, out = out, pathFilter = pathFilter)
        out
    }
    val matches = remember(query, rows) {
        if (query.isBlank()) emptyList()
        else rows.mapIndexedNotNull { index, row ->
            if (row.matchesQuery(query, pathSearchMode)) index else null
        }
    }
    LaunchedEffect(matches.size) {
        onMatchCountChanged?.invoke(matches.size)
    }

    val activeItemIndex = matches.getOrNull(activeMatchIndex.coerceAtLeast(0))
    val listState = rememberLazyListState()
    val horizontalScrollState = rememberScrollState()
    LaunchedEffect(activeItemIndex, enableInternalScroll) {
        if (enableInternalScroll && activeItemIndex != null) {
            listState.animateScrollToItem(activeItemIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
    ) {
        Box(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
            if (enableInternalScroll) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth()
                ) {
                    itemsIndexed(rows, key = { _, item -> item.path }) { index, row ->
                        JsonRow(
                            row = row,
                            isMatch = matches.contains(index),
                            isActive = activeItemIndex == index,
                            expanded = expanded
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth()
                ) {
                    rows.forEachIndexed { index, row ->
                        JsonRow(
                            row = row,
                            isMatch = matches.contains(index),
                            isActive = activeItemIndex == index,
                            expanded = expanded
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JsonRow(
    row: JsonRenderRow,
    isMatch: Boolean,
    isActive: Boolean,
    expanded: MutableMap<String, Boolean>
) {
    val bg = when {
        isActive -> NILColors.jsonActiveMatch()
        isMatch -> NILColors.jsonMatch()
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(6.dp))
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .animateContentSize()
            .clickable { expanded[row.path] = expanded[row.path] != true },
        verticalAlignment = Alignment.CenterVertically

    ) {
        Spacer(modifier = Modifier.width((row.depth * 12).dp))
        when (row.type) {
            JsonRowType.ObjectOpen -> {
                val isExpanded = expanded[row.path] == true
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = objectOrArrayLabel(
                        key = row.key,
                        openingBrace = "{",
                        isExpanded = isExpanded
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            JsonRowType.ArrayOpen -> {
                val isExpanded = expanded[row.path] == true
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = objectOrArrayLabel(
                        key = row.key,
                        openingBrace = "[",
                        isExpanded = isExpanded
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            JsonRowType.ObjectClose,
            JsonRowType.ArrayClose,
            JsonRowType.Value -> {
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (row.type == JsonRowType.Value) {
                        valueLabel(key = row.key, value = row.value.orEmpty())
                    } else {
                        buildAnnotatedString { append(row.value.orEmpty()) }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun appendRows(
    node: JsonNode,
    path: String,
    depth: Int,
    expanded: Map<String, Boolean>,
    out: MutableList<JsonRenderRow>,
    pathFilter: String? = null
) {
    if (pathFilter != null && !node.shouldIncludeForPathFilter(path, pathFilter)) return

    when (node) {
        is JsonNode.ObjectNode -> {
            val key = displayKey(node.key, depth)
            val isExpanded = if (pathFilter != null) true else expanded[path] == true
            out += JsonRenderRow(
                path = path,
                depth = depth,
                type = JsonRowType.ObjectOpen,
                key = key,
                value = null,
                isExpanded = isExpanded,
                searchText = "$key {"
            )

            if (isExpanded) {
                node.children.forEach { (childKey, child) ->
                    appendRows(
                        node = child.withKeyIfMissing(childKey),
                        path = "$path.$childKey",
                        depth = depth + 1,
                        expanded = expanded,
                        out = out,
                        pathFilter = pathFilter
                    )
                }
                out += JsonRenderRow(
                    path = "$path#close",
                    depth = depth,
                    type = JsonRowType.ObjectClose,
                    key = null,
                    value = "}",
                    isExpanded = false,
                searchText = "}"
                )
            }
        }

        is JsonNode.ArrayNode -> {
            val key = displayKey(node.key, depth)
            val isExpanded = if (pathFilter != null) true else expanded[path] == true
            out += JsonRenderRow(
                path = path,
                depth = depth,
                type = JsonRowType.ArrayOpen,
                key = key,
                value = null,
                isExpanded = isExpanded,
                searchText = "$key ["
            )

            if (isExpanded) {
                node.items.forEachIndexed { idx, child ->
                    val key = "[$idx]"
                    appendRows(
                        node = child.withKeyIfMissing(key),
                        path = "$path$key",
                        depth = depth + 1,
                        expanded = expanded,
                        out = out,
                        pathFilter = pathFilter
                    )
                }
                out += JsonRenderRow(
                    path = "$path#close",
                    depth = depth,
                    type = JsonRowType.ArrayClose,
                    key = null,
                    value = "]",
                    isExpanded = false,
                    searchText = "]"
                )
            }
        }

        is JsonNode.ValueNode -> {
            val key = displayKey(node.key, depth)
            out += JsonRenderRow(
                path = path,
                depth = depth,
                type = JsonRowType.Value,
                key = key,
                value = node.value,
                isExpanded = false,
                searchText = "$key ${node.value}"
            )
        }
    }
}

private fun JsonRenderRow.matchesQuery(query: String, pathSearchMode: Boolean): Boolean {
    val normalizedPath = path.removePrefix("root").replace("#close", "")
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) return false
    if (pathSearchMode) return normalizedPath.contains(normalizedQuery, ignoreCase = true)
    val tokens = normalizedQuery.split(Regex("\\s+")).filter { it.isNotBlank() }
    if (tokens.isEmpty()) return false
    return tokens.all { token ->
        val term = token.removePrefix("path:").removePrefix("p:")
        if (token.startsWith("path:", ignoreCase = true) || token.startsWith("p:", ignoreCase = true)) {
            normalizedPath.contains(term, ignoreCase = true)
        } else {
            searchText.contains(token, ignoreCase = true) || normalizedPath.contains(token, ignoreCase = true)
        }
    }
}

private fun JsonNode.shouldIncludeForPathFilter(path: String, filter: String): Boolean {
    val normalizedPath = path.removePrefix("root")
    if (normalizedPath.contains(filter, ignoreCase = true)) return true

    return when (this) {
        is JsonNode.ObjectNode -> children.any { (childKey, child) ->
            child.shouldIncludeForPathFilter("$path.$childKey", filter)
        }
        is JsonNode.ArrayNode -> items.anyIndexed { idx, child ->
            child.shouldIncludeForPathFilter("$path[$idx]", filter)
        }
        is JsonNode.ValueNode -> false
    }
}

private inline fun <T> List<T>.anyIndexed(predicate: (Int, T) -> Boolean): Boolean {
    for (index in indices) {
        if (predicate(index, this[index])) return true
    }
    return false
}

private fun displayKey(key: String?, depth: Int): String? {
    if (depth == 0 && key.isNullOrBlank()) return null
    return key ?: ""
}

@Composable
private fun objectOrArrayLabel(key: String?, openingBrace: String, isExpanded: Boolean) = buildAnnotatedString {
    if (!key.isNullOrBlank()) {
        withStyle(SpanStyle(color = NILColors.jsonKey())) { append("\"$key\"") }
        append(": ")
    }
    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
        append(openingBrace)
        if (!isExpanded) {
            append(" ... ")
            append(if (openingBrace == "{") "}" else "]")
        }
    }
}

@Composable
private fun valueLabel(key: String?, value: String) = buildAnnotatedString {
    if (!key.isNullOrBlank()) {
        withStyle(SpanStyle(color = NILColors.jsonKey())) {
            append("\"$key\"")
        }
        append(": ")
    }
    withStyle(SpanStyle(color = valueColor(value))) {
        append(value)
    }
}

private fun JsonNode.withKeyIfMissing(fallback: String): JsonNode = when (this) {
    is JsonNode.ObjectNode -> if (key == null) copy(key = fallback) else this
    is JsonNode.ArrayNode -> if (key == null) copy(key = fallback) else this
    is JsonNode.ValueNode -> if (key == null) copy(key = fallback) else this
}

@Composable
private fun valueColor(value: String) = when {
    value == "true" || value == "false" || value == "null" -> NILColors.jsonBoolNull()
    value.toDoubleOrNull() != null -> NILColors.jsonNumber()
    else -> NILColors.jsonString()
}

private data class JsonRenderRow(
    val path: String,
    val depth: Int,
    val type: JsonRowType,
    val key: String?,
    val value: String?,
    val isExpanded: Boolean,
    val searchText: String
)

private enum class JsonRowType {
    ObjectOpen,
    ObjectClose,
    ArrayOpen,
    ArrayClose,
    Value
}
