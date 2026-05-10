package com.sandesh.nil.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandesh.nil.core.NIL
import com.sandesh.nil.model.NetworkEvent
import com.sandesh.nil.utils.BodyPrettyPrinter
import com.sandesh.nil.utils.CurlGenerator
import com.sandesh.nil.ui.theme.NILTheme

@Composable
fun NILInspectorScreen(
    modifier: Modifier = Modifier
) {
    NILTheme {
        NILInspectorScreenContent(modifier = modifier)
    }
}

@Composable
private fun NILInspectorScreenContent(
    modifier: Modifier = Modifier
) {
    val events by NIL.events.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var selectedEventId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedEvent = events.firstOrNull { it.id == selectedEventId }

    if (selectedEvent != null) {
        NILInspectorDetailScreen(
            event = selectedEvent,
            onBack = { selectedEventId = null },
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                NIL.setSearchQuery(it)
            },
            label = { Text("Search URL / method / body") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(events, key = { it.id }) { event ->
                EventListRow(
                    event = event,
                    onClick = { selectedEventId = event.id }
                )
            }
        }
    }
}

@Composable
private fun EventListRow(
    event: NetworkEvent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${event.method} ${event.statusCode ?: "ERR"}",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "${event.durationMs}ms",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Text(
                text = event.url,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NILInspectorDetailScreen(
    event: NetworkEvent,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    val prettyRequestBody = remember(event.id, event.requestBody, event.requestHeaders) {
        BodyPrettyPrinter.prettyPrint(
            body = event.requestBody,
            headers = event.requestHeaders
        )
    }
    val prettyResponseBody = remember(event.id, event.responseBody, event.responseHeaders) {
        BodyPrettyPrinter.prettyPrint(
            body = event.responseBody,
            headers = event.responseHeaders
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Back",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onBack)
            )
            Text(
                text = "Copy cURL",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    clipboard.setText(AnnotatedString(CurlGenerator.fromEvent(event)))
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = event.url,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Request Headers", style = MaterialTheme.typography.titleSmall)
                Text(event.requestHeaders.orEmpty(), style = MaterialTheme.typography.bodySmall)
            }
            item {
                Text("Request Body", style = MaterialTheme.typography.titleSmall)
                Text(prettyRequestBody.orEmpty(), style = MaterialTheme.typography.bodySmall)
            }
            item {
                Text("Response Headers", style = MaterialTheme.typography.titleSmall)
                Text(event.responseHeaders.orEmpty(), style = MaterialTheme.typography.bodySmall)
            }
            item {
                Text("Response Body", style = MaterialTheme.typography.titleSmall)
                Text(prettyResponseBody.orEmpty(), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
