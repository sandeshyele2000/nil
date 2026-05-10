package com.sandesh.nil.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sandesh.nil.ui.theme.NILTheme

class NILInspectorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NILTheme {
                NILInspectorScreen()
            }
        }
    }
}
