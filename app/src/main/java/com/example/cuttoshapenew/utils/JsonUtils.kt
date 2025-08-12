import com.example.cuttoshapenew.apiclients.MessageResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun parseConnectionsFromJson(json: String): List<MessageResponse> {
    try {
        // Use Gson to parse the JSON string into a List<MessageResponse>
        val gson = Gson()
        val type = object : TypeToken<List<MessageResponse>>() {}.type
        return gson.fromJson(json, type)
    } catch (e: Exception) {
        println("Error parsing JSON: ${e.message}")
        return emptyList() // Return empty list on failure
    }
}