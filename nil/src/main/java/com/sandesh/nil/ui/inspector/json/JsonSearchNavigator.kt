package com.sandesh.nil.ui.inspector.json

class JsonSearchNavigator(
    private val matches: List<String>
) {
    private var index: Int = 0

    fun currentIndex(): Int = index
    fun total(): Int = matches.size
    fun currentPath(): String? = matches.getOrNull(index)

    fun next(): String? {
        if (matches.isEmpty()) return null
        index = (index + 1) % matches.size
        return matches[index]
    }

    fun previous(): String? {
        if (matches.isEmpty()) return null
        index = if (index == 0) matches.lastIndex else index - 1
        return matches[index]
    }

    fun reset() {
        index = 0
    }
}
