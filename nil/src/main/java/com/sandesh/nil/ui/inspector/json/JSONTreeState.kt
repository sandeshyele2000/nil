package com.sandesh.nil.ui.inspector.json


import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf

class JsonTreeState {

    val expandedPaths = mutableStateMapOf<String, Boolean>()

    var query = mutableStateOf("")
        private set

    fun toggle(path: String) {
        expandedPaths[path] = !(expandedPaths[path] ?: false)
    }

    fun isExpanded(path: String): Boolean {
        return expandedPaths[path] ?: false
    }

    fun setQuery(q: String) {
        query.value = q
    }
}