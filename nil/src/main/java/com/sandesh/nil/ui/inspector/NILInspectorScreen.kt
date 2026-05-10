package com.sandesh.nil.ui.inspector

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandesh.nil.core.NIL
import com.sandesh.nil.ui.inspector.detail.EventDetailScreen
import com.sandesh.nil.ui.inspector.list.EventListScreen
import com.sandesh.nil.ui.inspector.search.BodySearchScreen

@Composable
fun NILInspectorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val events by NIL.events.collectAsStateWithLifecycle()

    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var analyseTitle by rememberSaveable { mutableStateOf<String?>(null) }
    var analysePayload by rememberSaveable { mutableStateOf<String?>(null) }

    val selected = remember(selectedId, events) {
        events.firstOrNull { it.id == selectedId }
    }

    when {
        analyseTitle != null && analysePayload != null -> {
            BodySearchScreen(
                title = analyseTitle.orEmpty(),
                body = analysePayload.orEmpty(),
                onBack = {
                    analyseTitle = null
                    analysePayload = null
                },
                modifier = modifier
            )
        }

        selected != null -> {
            EventDetailScreen(
                event = selected,
                onBack = { selectedId = null },
                onAnalyse = { title, payload ->
                    analyseTitle = title
                    analysePayload = payload
                },
                modifier = modifier
            )
        }

        else -> {
            EventListScreen(
                events = events,
                onClick = { selectedId = it.id },
                onBack = onBack,
                modifier = modifier
            )
        }
    }
}
