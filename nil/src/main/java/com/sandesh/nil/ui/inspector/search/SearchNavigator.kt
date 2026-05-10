package com.sandesh.nil.ui.inspector.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchNavigator(
    total: Int,
    current: Int,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    if (total == 0) return
    Row {
        Text(
            text = "Prev",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onPrev)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text("${current + 1}/$total")
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Next",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onNext)
        )
    }
}
