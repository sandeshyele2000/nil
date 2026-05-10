package com.sandesh.nil.ui.inspector.json

sealed class JsonNode {
    data class ObjectNode(
        val key: String? = null,
        val children: Map<String, JsonNode>
    ) : JsonNode()

    data class ArrayNode(
        val key: String? = null,
        val items: List<JsonNode>
    ) : JsonNode()

    data class ValueNode(
        val key: String? = null,
        val value: String
    ) : JsonNode()
}