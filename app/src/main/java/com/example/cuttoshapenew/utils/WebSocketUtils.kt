package com.example.cuttoshapenew.utils

import com.example.cuttoshapenew.apiclients.ChatDetails
import kotlinx.serialization.json.Json


import android.util.Log
import com.example.cuttoshapenew.apiclients.ChatContent
import org.json.JSONObject

private const val PARSE_TAG = "WS_PARSE"

fun parseMessage(raw: String): ChatDetails? {
    return try {
        val o = JSONObject(raw)

        fun anyToString(key: String): String {
            if (!o.has(key) || o.isNull(key)) return ""
            val v = o.get(key)
            return when (v) {
                is Number -> v.toString()
                is Boolean -> v.toString()
                else -> v.toString()
            }
        }

        val id = when {
            o.has("id") && !o.isNull("id") -> anyToString("id")
            o.has("messageId") && !o.isNull("messageId") -> anyToString("messageId")
            else -> System.currentTimeMillis().toString()
        }

        val senderId = anyToString("senderId")
        val receiverId = anyToString("receiverId")

        val contentObj = o.optJSONObject("content")
        val contentText = when {
            contentObj != null -> contentObj.optString("content", "")
            o.has("content") -> o.optString("content", "")
            else -> ""
        }
        val productId = contentObj?.optString("productId") ?: ""

        val typeRaw = o.optString("type", "text")
        val typeNorm = if (typeRaw.equals("STRING", ignoreCase = true)) "text" else typeRaw
        val status = o.optString("status", "NEW")

        ChatDetails(
            id = id,
            senderId = senderId,
            receiverId = receiverId,
            content = ChatContent(content = contentText, productId = productId),
            type = typeNorm,
            status = status,
            createdAt = "",
            createdBy = "",
            updatedAt = "",
            updatedBy = "",
            deletedAt = "",
            deletedBy = ""
        )
    } catch (e: Exception) {
        Log.e(PARSE_TAG, "Failed to parse WS message: ${e.message}\nraw=$raw", e)
        null
    }
}
