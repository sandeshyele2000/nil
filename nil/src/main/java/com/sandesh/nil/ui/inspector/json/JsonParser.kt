package com.sandesh.nil.ui.inspector.json

import org.json.JSONArray
import org.json.JSONObject

object JsonParser {

    fun parse(input: String?): JsonNode? {
        if (input.isNullOrBlank()) return null

        return try {
            val trimmed = input.trim()

            when {
                trimmed.startsWith("{") -> parseObject(JSONObject(trimmed))
                trimmed.startsWith("[") -> parseArray(JSONArray(trimmed))
                else -> JsonNode.ValueNode(value = trimmed)
            }
        } catch (e: Exception) {
            JsonNode.ValueNode(value = input)
        }
    }

    private fun parseObject(obj: JSONObject): JsonNode.ObjectNode {
        val map = mutableMapOf<String, JsonNode>()

        obj.keys().forEach { key ->
            val value = obj.get(key)
            map[key] = when (value) {
                is JSONObject -> parseObject(value)
                is JSONArray -> parseArray(value)
                else -> JsonNode.ValueNode(value = value.toString())
            }
        }

        return JsonNode.ObjectNode(children = map)
    }

    private fun parseArray(arr: JSONArray): JsonNode.ArrayNode {
        val list = mutableListOf<JsonNode>()

        for (i in 0 until arr.length()) {
            val value = arr.get(i)

            list.add(
                when (value) {
                    is JSONObject -> parseObject(value)
                    is JSONArray -> parseArray(value)
                    else -> JsonNode.ValueNode(value = value.toString())
                }
            )
        }

        return JsonNode.ArrayNode(items = list)
    }
}