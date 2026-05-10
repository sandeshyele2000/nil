package com.sandesh.nil.utils.json

import com.sandesh.nil.ui.inspector.json.JsonNode
import org.json.JSONArray
import org.json.JSONObject

object JsonTreeBuilder {
    fun build(input: String?): JsonNode? {
        if (input.isNullOrBlank()) return null
        val trimmed = input.trim()
        return try {
            when {
                trimmed.startsWith("{") -> parseObject(JSONObject(trimmed), key = null)
                trimmed.startsWith("[") -> parseArray(JSONArray(trimmed), key = null)
                else -> JsonNode.ValueNode(value = trimmed)
            }
        } catch (_: Exception) {
            JsonNode.ValueNode(value = input)
        }
    }

    private fun parseObject(obj: JSONObject, key: String?): JsonNode.ObjectNode {
        val children = buildMap {
            obj.keys().forEach { key ->
                put(key, parseAny(obj.get(key), key))
            }
        }
        return JsonNode.ObjectNode(key = key, children = children)
    }

    private fun parseArray(array: JSONArray, key: String?): JsonNode.ArrayNode {
        val items = buildList {
            for (index in 0 until array.length()) {
                add(parseAny(array.get(index), key = "[$index]"))
            }
        }
        return JsonNode.ArrayNode(key = key, items = items)
    }

    private fun parseAny(any: Any?, key: String?): JsonNode {
        return when (any) {
            null -> JsonNode.ValueNode(key = key, value = "null")
            is JSONObject -> parseObject(any, key)
            is JSONArray -> parseArray(any, key)
            else -> JsonNode.ValueNode(key = key, value = any.toString())
        }
    }
}
