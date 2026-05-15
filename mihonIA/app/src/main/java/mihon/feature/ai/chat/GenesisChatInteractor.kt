package mihon.feature.ai.chat

import android.util.Log
import mihon.feature.ai.GenesisAIClient

class GenesisChatInteractor {

    private val aiClient = GenesisAIClient.getInstance()
    private val conversationHistory = mutableListOf<String>()

    suspend fun sendMessage(message: String): ChatResult {
        return try {
            val response = aiClient.chat(message, conversationHistory)
            conversationHistory.add(message)
            conversationHistory.add(response)
            ChatResult.Success(response)
        } catch (e: Exception) {
            Log.e("GenesisChat", "Error al enviar mensaje: ${e.message}")
            ChatResult.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun clearHistory() {
        conversationHistory.clear()
    }

    fun getHistorySize(): Int = conversationHistory.size

    suspend fun getLearningStats(): Map<String, Any> {
        return try {
            aiClient.getLearningStats()
        } catch (e: Exception) {
            Log.e("GenesisChat", "Error al obtener estadísticas: ${e.message}")
            emptyMap()
        }
    }
}

sealed class ChatResult {
    data class Success(val response: String) : ChatResult()
    data class Error(val message: String) : ChatResult()
    object Loading : ChatResult()
}
