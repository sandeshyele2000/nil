package com.sandesh.nil.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import com.sandesh.nil.ui.inspector.NILInspectorScreen
import com.sandesh.nil.ui.theme.NILTheme

class NILInspectorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NILTheme {
                NILInspectorScreen(
                    onBack = { finish() },
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                )
            }
        }
    }
}
