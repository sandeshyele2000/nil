package com.sandesh.nil.ui.inspector.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.sandesh.nil.core.NIL
import com.sandesh.nil.ui.components.NILSearchBar
import com.sandesh.nil.ui.inspector.detail.DetailEmptyState
import com.sandesh.nil.ui.inspector.json.JsonTreeViewer
import com.sandesh.nil.ui.theme.NILColors
import com.sandesh.nil.utils.ShareFileUtil
import androidx.compose.ui.platform.LocalContext

@Composable
fun BodySearchScreen(
    title: String,
    body: String,
    onBack: () -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var currentMatch by rememberSaveable { mutableIntStateOf(0) }
    var jsonMatchCount by rememberSaveable { mutableIntStateOf(0) }
    var jsonPathMode by rememberSaveable { mutableStateOf(false) }
    val isJson = body.trim().startsWith("{") || body.trim().startsWith("[")
    val jsonTreeMaxChars = NIL.jsonTreeMaxChars()
    val canRenderJsonTree = isJson && body.length <= jsonTreeMaxChars
    val effectiveJsonMode = isJson && canRenderJsonTree

    val textMatches = remember(query, body, effectiveJsonMode) {
        if (effectiveJsonMode) emptyList() else SearchHighlighter.findMatches(body, query)
    }
    val totalMatches = if (effectiveJsonMode) jsonMatchCount else textMatches.size

    val lines = remember(body) { body.split('\n') }
    val lineMatchIndexes = remember(query, lines) {
        if (query.isBlank()) emptyList()
        else lines.mapIndexedNotNull { idx, line -> if (line.contains(query, ignoreCase = true)) idx else null }
    }
    val lineListState = rememberLazyListState()

    LaunchedEffect(currentMatch, lineMatchIndexes, effectiveJsonMode) {
        if (!effectiveJsonMode && lineMatchIndexes.isNotEmpty()) {
            lineListState.animateScrollToItem(lineMatchIndexes[currentMatch.coerceIn(0, lineMatchIndexes.lastIndex)])
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Analyse",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            NILSearchBar(
                value = query,
                onValueChange = {
                    query = it
                    currentMatch = 0
                },
                placeholder = if (jsonPathMode && isJson) "Search JSON path..." else "Search..."
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (isJson && !canRenderJsonTree) {
                DetailEmptyState(
                    label = "JSON too large for tree view (${body.length} chars > $jsonTreeMaxChars). Search raw text or export payload."
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        ShareFileUtil.shareTextFile(
                            context = context,
                            fileName = "analyse_payload.txt",
                            content = body,
                            mimeType = "text/plain"
                        )
                    }
                ) {
                    Text("Export Payload")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (effectiveJsonMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = !jsonPathMode,
                        onClick = {
                            jsonPathMode = false
                            currentMatch = 0
                        },
                        label = { Text("Text") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = jsonPathMode,
                        onClick = {
                            jsonPathMode = true
                            currentMatch = 0
                        },
                        label = { Text("Path") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (totalMatches > 0 && !(effectiveJsonMode && jsonPathMode)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        currentMatch =
                            if (currentMatch == 0) totalMatches - 1 else currentMatch - 1
                    }) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = "Up",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        "${currentMatch + 1}/$totalMatches",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = {
                        currentMatch =
                            if (currentMatch == totalMatches - 1) 0 else currentMatch + 1
                    }) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Down",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (effectiveJsonMode) {
                JsonTreeViewer(
                    json = body,
                    query = query,
                    pathSearchMode = jsonPathMode,
                    activeMatchIndex = currentMatch,
                    onMatchCountChanged = { count ->
                        jsonMatchCount = count
                        if (count in 1..currentMatch) currentMatch = 0
                    },
                    enableInternalScroll = true
                )
            } else {
                LazyColumn(state = lineListState, modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(lines) { index, line ->
                        val isLineMatched =
                            query.isNotBlank() && line.contains(query, ignoreCase = true)
                        val isActiveLine =
                            isLineMatched && lineMatchIndexes.getOrNull(currentMatch) == index

                        Text(
                            text = line.annotateQuery(query, NILColors.jsonMatch()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = when {
                                        isActiveLine -> NILColors.jsonActiveMatch()
                                        isLineMatched -> NILColors.jsonMatch()
                                        else -> androidx.compose.ui.graphics.Color.Transparent
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun String.annotateQuery(query: String, matchColor: androidx.compose.ui.graphics.Color) = buildAnnotatedString {
    if (query.isBlank()) {
        append(this@annotateQuery)
        return@buildAnnotatedString
    }
    val lower = this@annotateQuery.lowercase()
    val q = query.lowercase()
    var cursor = 0
    while (cursor < this@annotateQuery.length) {
        val match = lower.indexOf(q, cursor)
        if (match < 0) {
            append(this@annotateQuery.substring(cursor))
            break
        }
        append(this@annotateQuery.substring(cursor, match))
        withStyle(SpanStyle(background = matchColor)) {
            append(this@annotateQuery.substring(match, match + q.length))
        }
        cursor = match + q.length
    }
}
