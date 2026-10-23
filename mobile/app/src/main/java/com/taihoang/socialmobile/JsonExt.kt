package com.taihoang.socialmobile

import org.json.JSONObject

/**
 * Android's org.json turns a JSON null into the literal string "null" in optString(),
 * and the backend serialises absent values as null. Treat null like a missing key.
 */
internal fun JSONObject.optStringOrNull(name: String): String? =
    if (isNull(name)) null else optString(name).ifBlank { null }

internal fun JSONObject.optStringOr(name: String, fallback: String): String =
    optStringOrNull(name) ?: fallback
