package com.sandesh.nil.ui.inspector.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sandesh.nil.ui.inspector.json.JsonTreeViewer
import com.sandesh.nil.utils.BodyPrettyPrinter

@Composable
fun BodyViewer(
    body: String?,
    headers: String?
) {
    val prettyBody = BodyPrettyPrinter.prettyPrint(body, headers).orEmpty()
    val isJson = prettyBody.trim().startsWith("{") || prettyBody.trim().startsWith("[")
    var expanded by rememberSaveable(prettyBody) { mutableStateOf(false) }

    if (prettyBody.isBlank()) {
        Text(
            text = "No body",
            style = MaterialTheme.typography.bodySmall
        )
        return
    }

    if (isJson) {
        JsonTreeViewer(
            json = prettyBody,
            enableInternalScroll = false
        )
        return
    }

    val preview = if (expanded || prettyBody.length <= 300) prettyBody else prettyBody.take(300)
    Text(
        text = preview,
        style = MaterialTheme.typography.bodySmall
    )

    if (prettyBody.length > 300) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (expanded) "Show less" else "Show more",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { expanded = !expanded }
        )
    }
}
