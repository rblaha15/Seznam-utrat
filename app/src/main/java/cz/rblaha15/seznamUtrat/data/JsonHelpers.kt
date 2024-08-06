package cz.rblaha15.seznamUtrat.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T> String.fromJson(): T = Json.decodeFromString(this)
inline fun <reified T> T.toJson(): String = Json.encodeToString(this)