package mihon.feature.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GenesisLearningSystem {
    
    private var isInitialized = false
    private val userPreferences = mutableMapOf<String, Any>()
    private val searchHistory = mutableListOf<SearchRecord>()
    private val conversationHistory = mutableListOf<ConversationRecord>()
    private val mangaListHistory = mutableListOf<MangaListRecord>()
    
    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext
        
        try {
            loadLearningData()
            isInitialized = true
            Log.d("GenesisLearning", "Sistema de aprendizaje inicializado")
        } catch (e: Exception) {
            Log.e("GenesisLearning", "Error al inicializar: ${e.message}")
        }
    }
    
    private fun loadLearningData() {
        // Cargar datos de aprendizaje desde almacenamiento local
        // Por ahora, inicializamos con datos vacíos
        userPreferences["preferred_genres"] = emptyList<String>()
        userPreferences["preferred_authors"] = emptyList<String>()
        userPreferences["reading_style"] = "standard"
    }
    
    fun enhanceQuery(query: String): String {
        // Aplicar aprendizaje del usuario para mejorar la consulta
        val preferredGenres = userPreferences["preferred_genres"] as? List<String> ?: emptyList()
        val preferredAuthors = userPreferences["preferred_authors"] as? List<String> ?: emptyList()
        
        var enhancedQuery = query
        
        // Si el usuario no especificó género, agregar sus preferencias
        if (!containsGenre(query) && preferredGenres.isNotEmpty()) {
            enhancedQuery += " (géneros preferidos: ${preferredGenres.joinToString(", ")})"
        }
        
        // Si el usuario no especificó autor, agregar sus preferencias
        if (!containsAuthor(query) && preferredAuthors.isNotEmpty()) {
            enhancedQuery += " (autores preferidos: ${preferredAuthors.joinToString(", ")})"
        }
        
        return enhancedQuery
    }
    
    fun enhanceMessage(message: String): String {
        // Aplicar aprendizaje del usuario para mejorar el mensaje
        val readingStyle = userPreferences["reading_style"] as? String ?: "standard"
        
        return when (readingStyle) {
            "detailed" -> "$message (proporciona información detallada)"
            "concise" -> "$message (sé conciso y directo)"
            else -> message
        }
    }
    
    fun learnFromSearch(query: String, result: String) {
        val record = SearchRecord(
            query = query,
            result = result,
            timestamp = System.currentTimeMillis()
        )
        
        searchHistory.add(record)
        
        // Limitar tamaño del historial
        if (searchHistory.size > GenesisAIConfig.MAX_MEMORY_SIZE) {
            searchHistory.removeAt(0)
        }
        
        // Extraer y aprender preferencias del usuario
        extractPreferences(query, result)
        
        Log.d("GenesisLearning", "Aprendido de búsqueda: $query")
    }
    
    fun learnFromConversation(userMessage: String, aiResponse: String) {
        val record = ConversationRecord(
            userMessage = userMessage,
            aiResponse = aiResponse,
            timestamp = System.currentTimeMillis()
        )
        
        conversationHistory.add(record)
        
        // Limitar tamaño del historial
        if (conversationHistory.size > GenesisAIConfig.MAX_MEMORY_SIZE) {
            conversationHistory.removeAt(0)
        }
        
        // Extraer y aprender preferencias del usuario
        extractPreferences(userMessage, aiResponse)
        
        Log.d("GenesisLearning", "Aprendido de conversación")
    }
    
    fun learnFromListCreation(criteria: String, result: String) {
        val record = MangaListRecord(
            criteria = criteria,
            result = result,
            timestamp = System.currentTimeMillis()
        )
        
        mangaListHistory.add(record)
        
        // Limitar tamaño del historial
        if (mangaListHistory.size > GenesisAIConfig.MAX_MEMORY_SIZE) {
            mangaListHistory.removeAt(0)
        }
        
        // Extraer y aprender preferencias del usuario
        extractPreferences(criteria, result)
        
        Log.d("GenesisLearning", "Aprendido de creación de lista")
    }
    
    private fun extractPreferences(query: String, result: String) {
        // Extraer géneros mencionados
        val genres = extractGenres(query)
        if (genres.isNotEmpty()) {
            val currentGenres = userPreferences["preferred_genres"] as? MutableList<String> ?: mutableListOf()
            genres.forEach { genre ->
                if (!currentGenres.contains(genre)) {
                    currentGenres.add(genre)
                }
            }
            userPreferences["preferred_genres"] = currentGenres
        }
        
        // Extraer autores mencionados
        val authors = extractAuthors(query)
        if (authors.isNotEmpty()) {
            val currentAuthors = userPreferences["preferred_authors"] as? MutableList<String> ?: mutableListOf()
            authors.forEach { author ->
                if (!currentAuthors.contains(author)) {
                    currentAuthors.add(author)
                }
            }
            userPreferences["preferred_authors"] = currentAuthors
        }
    }
    
    private fun containsGenre(query: String): Boolean {
        val genreKeywords = listOf("género", "genre", "tipo", "estilo", "shonen", "shojo", "seinen", "josei")
        return genreKeywords.any { query.lowercase().contains(it) }
    }
    
    private fun containsAuthor(query: String): Boolean {
        val authorKeywords = listOf("autor", "author", "creador", "mangaka", "escritor")
        return authorKeywords.any { query.lowercase().contains(it) }
    }
    
    private fun extractGenres(query: String): List<String> {
        val commonGenres = listOf(
            "acción", "aventura", "comedia", "drama", "fantasía", "horror", 
            "misterio", "romance", "ciencia ficción", "deportes", "sobrenatural",
            "shonen", "shojo", "seinen", "josei", "slice of life"
        )
        
        return commonGenres.filter { genre ->
            query.lowercase().contains(genre.lowercase())
        }
    }
    
    private fun extractAuthors(query: String): List<String> {
        // Extracción simple de nombres propios (muy básica)
        val words = query.split(" ")
        return words.filter { word ->
            word.length > 3 && word[0].isUpperCase()
        }
    }
    
    fun getStats(): Map<String, Any> {
        return mapOf(
            "search_history_size" to searchHistory.size,
            "conversation_history_size" to conversationHistory.size,
            "manga_list_history_size" to mangaListHistory.size,
            "user_preferences" to userPreferences,
            "learning_enabled" to GenesisAIConfig.LEARNING_ENABLED
        )
    }
    
    fun clearHistory() {
        searchHistory.clear()
        conversationHistory.clear()
        mangaListHistory.clear()
        userPreferences.clear()
        loadLearningData()
    }
}

data class SearchRecord(
    val query: String,
    val result: String,
    val timestamp: Long
)

data class ConversationRecord(
    val userMessage: String,
    val aiResponse: String,
    val timestamp: Long
)

data class MangaListRecord(
    val criteria: String,
    val result: String,
    val timestamp: Long
)
