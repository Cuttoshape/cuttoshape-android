package com.example.cuttoshapenew.utils

import com.example.cuttoshapenew.apiclients.ChatDetails
import kotlinx.serialization.json.Json


fun parseMessage(json: String): ChatDetails {
    return Json.decodeFromString(json)
}