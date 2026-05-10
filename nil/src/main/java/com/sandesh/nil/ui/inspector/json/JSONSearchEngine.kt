package com.sandesh.nil.ui.inspector.json

object JsonSearchEngine {
    fun matches(node: JsonNode, query: String, path: String = "root"): List<String> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase()
        val result = mutableListOf<String>()

        when (node) {
            is JsonNode.ValueNode -> {
                if (node.value.lowercase().contains(q) || (node.key?.lowercase()?.contains(q) == true)) {
                    result.add(path)
                }
            }

            is JsonNode.ObjectNode -> {
                node.children.forEach { (key, child) ->
                    val childPath = "$path.$key"
                    if (key.lowercase().contains(q)) {
                        result.add(childPath)
                    }
                    result += matches(child, query, childPath)
                }
            }

            is JsonNode.ArrayNode -> {
                node.items.forEachIndexed { index, child ->
                    result += matches(child, query, "$path[$index]")
                }
            }
        }
        return result
    }
}
