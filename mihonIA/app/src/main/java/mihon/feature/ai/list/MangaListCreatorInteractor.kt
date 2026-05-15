package mihon.feature.ai.list

import android.util.Log
import mihon.feature.ai.GenesisAIClient

class MangaListCreatorInteractor {
    
    private val aiClient = GenesisAIClient.getInstance()
    
    suspend fun createMangaList(criteria: String): MangaListResult {
        return try {
            val response = aiClient.createMangaList(criteria)
            MangaListResult.Success(response)
        } catch (e: Exception) {
            Log.e("MangaListCreator", "Error en creación de lista: ${e.message}")
            MangaListResult.Error(e.message ?: "Error desconocido")
        }
    }
    
    suspend fun createGenreList(genre: String, count: Int = 10): MangaListResult {
        val criteria = "Crea una lista de $count mangas del género $genre"
        return try {
            val response = aiClient.createMangaList(criteria)
            MangaListResult.Success(response)
        } catch (e: Exception) {
            Log.e("MangaListCreator", "Error en creación de lista por género: ${e.message}")
            MangaListResult.Error(e.message ?: "Error desconocido")
        }
    }
    
    suspend fun createAuthorList(author: String, count: Int = 5): MangaListResult {
        val criteria = "Crea una lista de $count mangas del autor $author"
        return try {
            val response = aiClient.createMangaList(criteria)
            MangaListResult.Success(response)
        } catch (e: Exception) {
            Log.e("MangaListCreator", "Error en creación de lista por autor: ${e.message}")
            MangaListResult.Error(e.message ?: "Error desconocido")
        }
    }
    
    suspend fun createCustomList(description: String): MangaListResult {
        val criteria = "Crea una lista de mangas personalizada: $description"
        return try {
            val response = aiClient.createMangaList(criteria)
            MangaListResult.Success(response)
        } catch (e: Exception) {
            Log.e("MangaListCreator", "Error en creación de lista personalizada: ${e.message}")
            MangaListResult.Error(e.message ?: "Error desconocido")
        }
    }
}

sealed class MangaListResult {
    data class Success(val response: String) : MangaListResult()
    data class Error(val message: String) : MangaListResult()
    object Loading : MangaListResult()
}
