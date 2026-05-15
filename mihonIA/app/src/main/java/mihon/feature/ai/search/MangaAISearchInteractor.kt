package mihon.feature.ai.search

import android.util.Log
import mihon.feature.ai.GenesisAIClient

class MangaAISearchInteractor {

    private val aiClient = GenesisAIClient.getInstance()

    suspend fun searchManga(query: String): MangaAIResult {
        return try {
            val response = aiClient.searchManga(query)
            MangaAIResult.Success(response)
        } catch (e: Exception) {
            Log.e("MangaAISearch", "Error en búsqueda: ${e.message}")
            MangaAIResult.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun getMangaRecommendations(genre: String, count: Int = 5): MangaAIResult {
        val query = "Recomienda $count mangas del género $genre con breve descripción"
        return try {
            val response = aiClient.searchManga(query)
            MangaAIResult.Success(response)
        } catch (e: Exception) {
            Log.e("MangaAISearch", "Error en recomendaciones: ${e.message}")
            MangaAIResult.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun getLearningStats(): Map<String, Any> {
        return try {
            val stats = aiClient.getLearningStats()
            val extensions = aiClient.getAvailableExtensions()
            stats + mapOf("available_extensions" to extensions)
        } catch (e: Exception) {
            Log.e("MangaAISearch", "Error al obtener estadísticas: ${e.message}")
            emptyMap()
        }
    }
}

sealed class MangaAIResult {
    data class Success(val response: String) : MangaAIResult()
    data class Error(val message: String) : MangaAIResult()
    object Loading : MangaAIResult()
}
