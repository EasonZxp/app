package moe.crx.ovrport.utils

import com.reandroid.json.JSONArray
import com.reandroid.json.JSONObject

fun JSONObject?.named(name: String?): Boolean {
    return elem<String>("name") == name
}

fun JSONObject?.takeNodes(block: JSONArray?.() -> JSONArray? = { this }): JSONObject? {
    return take("nodes", block)
}

fun JSONObject?.takeAttributes(block: JSONArray?.() -> JSONArray? = { this }): JSONObject? {
    return take("attributes", block)
}

fun JSONObject?.takeNodesEach(
    condition: JSONObject?.() -> Boolean = { true },
    block: JSONObject?.() -> JSONObject? = { this }
): JSONObject? {
    return takeNodes {
        takeEach(condition, block)
    }
}

fun JSONObject?.takeAttributesEach(
    condition: JSONObject?.() -> Boolean = { true },
    block: JSONObject?.() -> JSONObject? = { this }
): JSONObject? {
    return takeAttributes {
        takeEach(condition, block)
    }
}

fun JSONObject?.attributeName(): String? {
    return elem<JSONArray>("attributes")
        .elemEach<JSONObject> { named("name") }
        .firstOrNull()
        .elem<String>("data")
}

fun createMetadata(name: String, value: String): JSONObject {
    return JSONObject()
        .put("node_type", "element")
        .put("name", "meta-data")
        .put(
            "attributes",
            JSONArray()
                .put(
                    JSONObject()
                        .put("name", "name")
                        .put("id", 16842755)
                        .put("uri", "http://schemas.android.com/apk/res/android")
                        .put("prefix", "android")
                        .put("value_type", "STRING")
                        .put("data", name)
                )
                .put(
                    JSONObject()
                        .put("name", "value")
                        .put("id", 16842788)
                        .put("uri", "http://schemas.android.com/apk/res/android")
                        .put("prefix", "android")
                        .put("value_type", "STRING")
                        .put("data", value)
                )
        )
}

fun createCategory(value: String): JSONObject {
    return JSONObject()
        .put("node_type", "element")
        .put("name", "category")
        .put(
            "attributes",
            JSONArray()
                .put(
                    JSONObject()
                        .put("name", "name")
                        .put("id", 16842755)
                        .put("uri", "http://schemas.android.com/apk/res/android")
                        .put("prefix", "android")
                        .put("value_type", "STRING")
                        .put("data", value)
                )
        )
}