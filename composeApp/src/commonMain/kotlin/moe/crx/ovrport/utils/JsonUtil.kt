package moe.crx.ovrport.utils

import com.reandroid.json.JSONArray
import com.reandroid.json.JSONObject

inline fun <reified T> JSONObject?.elem(name: String): T? {
    return this?.opt(name).takeIf { it is T } as T?
}

inline fun <reified T> JSONArray?.elem(index: Int): T? {
    return this?.opt(index).takeIf { it is T } as T?
}

inline fun <reified T> JSONObject?.take(name: String, block: T?.() -> T? = { this }): JSONObject? {
    this?.put(name, block(elem<T>(name)))
    return this
}

inline fun <reified T> JSONArray?.take(index: Int, block: T?.() -> T? = { this }): JSONArray? {
    this?.put(index, block(elem<T>(index)))
    return this
}

inline fun <reified T> JSONObject?.takeEach(
    condition: T?.() -> Boolean = { true },
    block: T?.() -> T? = { this }
): JSONObject? {
    this?.keys()?.asSequence()?.toList()
        ?.forEach { i ->
            elem<T>(i).let {
                if (condition(it)) {
                    block(it)?.let { value -> put(i, value) }
                        ?: remove(i)
                }
            }
        }

    return this
}

inline fun <reified T> JSONArray?.takeEach(
    condition: T?.() -> Boolean = { true },
    block: T?.() -> T? = { this }
): JSONArray? {
    this?.length()?.let { (it - 1).downTo(0) }
        ?.forEach { i ->
            elem<T>(i).let {
                if (condition(it)) {
                    block(it)?.let { value -> put(i, value) }
                        ?: remove(i)
                }
            }
        }

    return this
}

inline fun <reified T> JSONObject?.elemEach(condition: T?.() -> Boolean = { true }): List<T> {
    return this?.keys()?.asSequence()?.toList()
        ?.mapNotNull { i -> elem<T>(i) }
        ?.filter { condition(it) }
        ?.toList()
        ?: listOf()
}

inline fun <reified T> JSONArray?.elemEach(condition: T?.() -> Boolean = { true }): List<T> {
    return this?.length()?.let { (it - 1).downTo(0) }
        ?.mapNotNull { i -> elem<T>(i) }
        ?.filter { condition(it) }
        ?.toList()
        ?: listOf()
}
