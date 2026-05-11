package com.sandesh.nil.ui.inspector.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sandesh.nil.core.NIL
import com.sandesh.nil.ui.inspector.json.JsonTreeViewer
import com.sandesh.nil.utils.BodyPrettyPrinter
import android.graphics.Color
import android.webkit.WebView

@Composable
fun BodyViewer(
    body: String?,
    headers: String?
) {
    val prettyBody = BodyPrettyPrinter.prettyPrint(body, headers).orEmpty()
    val isJson = prettyBody.trim().startsWith("{") || prettyBody.trim().startsWith("[")
    val isHtml = isHtmlBody(prettyBody, headers)
    val jsonTreeMaxChars = NIL.jsonTreeMaxChars()
    val canRenderJsonTree = prettyBody.length <= jsonTreeMaxChars
    var expanded by rememberSaveable(prettyBody) { mutableStateOf(false) }
    var htmlMode by rememberSaveable(prettyBody, headers) { mutableStateOf(false) }

    if (prettyBody.isBlank()) {
        DetailEmptyState(label = "No body available")
        return
    }

    if (isJson && canRenderJsonTree) {
        JsonTreeViewer(
            json = prettyBody,
            enableInternalScroll = false
        )
        return
    }
    if (isJson && !canRenderJsonTree) {
        DetailEmptyState(label = "JSON too large for tree view (${prettyBody.length} chars > $jsonTreeMaxChars).")
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (isHtml) {
        Column {
            Row {
                FilterChip(
                    selected = !htmlMode,
                    onClick = { htmlMode = false },
                    label = { Text("Raw") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = htmlMode,
                    onClick = { htmlMode = true },
                    label = { Text("HTML") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (htmlMode) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            setBackgroundColor(Color.TRANSPARENT)
                            loadDataWithBaseURL(null, prettyBody, "text/html", "utf-8", null)
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(null, prettyBody, "text/html", "utf-8", null)
                    },
                    modifier = Modifier
                        .height(260.dp)
                        .padding(bottom = 6.dp)
                )
            }
        }
        if (htmlMode) return
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

private fun isHtmlBody(body: String, headers: String?): Boolean {
    val lowerHeaders = headers.orEmpty().lowercase()
    if (lowerHeaders.contains("content-type: text/html")) return true
    val trimmed = body.trimStart().lowercase()
    return trimmed.startsWith("<!doctype html") || trimmed.startsWith("<html")
}
