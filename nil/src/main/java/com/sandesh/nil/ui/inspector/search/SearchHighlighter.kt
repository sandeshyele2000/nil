package com.sandesh.nil.ui.inspector.search


object SearchHighlighter {

    fun findMatches(text: String, query: String): List<IntRange> {
        if (query.isBlank()) return emptyList()

        val results = mutableListOf<IntRange>()
        val lower = text.lowercase()
        val q = query.lowercase()

        var start = 0

        while (true) {
            val index = lower.indexOf(q, start)
            if (index == -1) break

            results.add(index until index + q.length)
            start = index + q.length
        }

        return results
    }
}