package com.sandesh.nil.ui.inspector.detail

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sandesh.nil.model.NetworkEvent
import com.sandesh.nil.utils.CurlGenerator

@Composable
fun EventDetailScreen(
    event: NetworkEvent,
    onBack: () -> Unit,
    onAnalyse: (title: String, payload: String) -> Unit,
    modifier: Modifier
) {
    var requestHeadersExpanded by rememberSaveable { mutableStateOf(false) }
    var requestParamsExpanded by rememberSaveable { mutableStateOf(false) }
    var requestBodyExpanded by rememberSaveable { mutableStateOf(true) }
    var responseHeadersExpanded by rememberSaveable { mutableStateOf(false) }
    var responseBodyExpanded by rememberSaveable { mutableStateOf(true) }
    val clipboard = LocalClipboardManager.current
    val requestParams = remember(event.url) { formatRequestParams(event.url) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(onClick = {
                    clipboard.setText(AnnotatedString(CurlGenerator.fromEvent(event)))
                }) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy cURL",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Text(
                text = "Event Details",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = event.url,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CollapsibleSection(
                        title = "Request Headers",
                        expanded = requestHeadersExpanded,
                        onToggle = { requestHeadersExpanded = !requestHeadersExpanded },
                    ) {
                        Text(
                            text = event.requestHeaders.orEmpty(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                item {
                    CollapsibleSection(
                        title = "Request Params",
                        expanded = requestParamsExpanded,
                        onToggle = { requestParamsExpanded = !requestParamsExpanded },
                    ) {
                        Text(
                            text = requestParams,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                item {
                    CollapsibleSection(
                        title = "Request Body",
                        expanded = requestBodyExpanded,
                        onToggle = { requestBodyExpanded = !requestBodyExpanded },
                        onAnalyse = {
                            onAnalyse("Request Body", event.requestBody.orEmpty())
                        }
                    ) {
                        BodyViewer(
                            body = event.requestBody,
                            headers = event.requestHeaders
                        )
                    }
                }
                item {
                    CollapsibleSection(
                        title = "Response Headers",
                        expanded = responseHeadersExpanded,
                        onToggle = { responseHeadersExpanded = !responseHeadersExpanded },
                    ) {
                        Text(
                            text = event.responseHeaders.orEmpty(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                item {
                    CollapsibleSection(
                        title = "Response Body",
                        expanded = responseBodyExpanded,
                        onToggle = { responseBodyExpanded = !responseBodyExpanded },
                        onAnalyse = {
                            onAnalyse("Response Body", event.responseBody.orEmpty())
                        }
                    ) {
                        BodyViewer(
                            body = event.responseBody,
                            headers = event.responseHeaders
                        )
                    }
                }
            }
        }
    }
}

private fun formatRequestParams(url: String): String {
    val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return "No query params"
    val names = uri.queryParameterNames
    if (names.isEmpty()) return "No query params"

    return buildString {
        names.sorted().forEach { key ->
            val values = uri.getQueryParameters(key)
            if (values.isEmpty()) {
                append(key).append(" =").append('\n')
            } else {
                values.forEach { value ->
                    append(key).append(" = ").append(value).append('\n')
                }
            }
        }
    }.trimEnd()
}
