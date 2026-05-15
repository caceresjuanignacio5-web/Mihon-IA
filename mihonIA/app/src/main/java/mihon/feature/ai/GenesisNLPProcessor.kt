package mihon.feature.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GenesisNLPProcessor {

    private var isInitialized = false
    private val languagePatterns = mapOf(
        "search" to listOf("buscar", "search", "find", "encontrar", "busca", "encuentra", "dame", "quiero"),
        "list" to listOf("lista", "list", "crear lista", "make list", "recomienda", "recommend", "todos", "todas"),
        "genre" to listOf("género", "genre", "tipo", "estilo", "shonen", "shojo", "seinen", "josei"),
        "author" to listOf("autor", "author", "creador", "mangaka", "escritor"),
        "action" to listOf("acción", "action"),
        "comedy" to listOf("comedia", "comedy"),
        "drama" to listOf("drama"),
        "fantasy" to listOf("fantasía", "fantasy"),
        "romance" to listOf("romance"),
        "horror" to listOf("horror", "terror"),
        "scifi" to listOf("ciencia ficción", "scifi", "science fiction"),
        // Manga/Manhwa specific
        "manga" to listOf("manga", "japonés", "japan"),
        "manhwa" to listOf("manhwa", "coreano", "korea", "webtoon"),
        "manhua" to listOf("manhua", "chino", "china"),
        "webtoon" to listOf("webtoon", "vertical", "scroll"),
        "status" to listOf("estado", "status", "en curso", "completo", "finalizado", "hiatus"),
        // Chapter filters
        "chapters" to listOf("capítulos", "capitulos", "chapters", "caps", "episodios"),
        "more_than" to listOf("más de", "mas de", "more than", "greater than", ">"),
        "less_than" to listOf("menos de", "less than", "<"),
        "at_least" to listOf("al menos", "at least", "minimo", "mínimo")
    )

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext

        try {
            // Cargar patrones de lenguaje específicos de manga
            isInitialized = true
            Log.d("GenesisNLP", "Procesador NLP inicializado (Especializado en Manga/Manhwa)")
        } catch (e: Exception) {
            Log.e("GenesisNLP", "Error al inicializar: ${e.message}")
        }
    }

    fun processQuery(query: String): ProcessedQuery {
        // Procesar la consulta para extraer intención, filtros y entidades específicas de manga
        val intent = detectIntent(query)
        val filters = extractFilters(query)
        val mangaType = detectMangaType(query)

        // Normalizar la consulta
        var processedQuery = query.lowercase().trim()

        // Agregar contexto basado en la intención detectada
        when (intent) {
            "search" -> processedQuery = "Busca $mangaType: $processedQuery"
            "list" -> processedQuery = "Crea lista de $mangaType: $processedQuery"
            "recommend" -> processedQuery = "Recomienda $mangaType: $processedQuery"
        }

        return ProcessedQuery(
            originalQuery = query,
            processedQuery = processedQuery,
            intent = intent,
            filters = filters,
            mangaType = mangaType
        )
    }

    fun processMessage(message: String): ProcessedMessage {
        // Procesar el mensaje para chat especializado en manga
        val intent = detectIntent(message)
        val sentiment = detectSentiment(message)
        val mangaType = detectMangaType(message)
        val filters = extractFilters(message)

        var processedMessage = message

        // Ajustar el mensaje según el sentimiento
        when (sentiment) {
            "positive" -> processedMessage = "$processedMessage (tono positivo)"
            "negative" -> processedMessage = "$processedMessage (tono negativo, sé empático)"
            "neutral" -> processedMessage
        }

        // Agregar contexto de tipo de manga si es relevante
        if (mangaType != "manga") {
            processedMessage = "$processedMessage (especializado en $mangaType)"
        }

        return ProcessedMessage(
            originalMessage = message,
            processedMessage = processedMessage,
            intent = intent,
            sentiment = sentiment,
            mangaType = mangaType,
            filters = filters
        )
    }

    private fun detectIntent(query: String): String {
        val lowerQuery = query.lowercase()

        for ((intent, patterns) in languagePatterns) {
            if (patterns.any { lowerQuery.contains(it) }) {
                return intent
            }
        }

        return "general"
    }

    private fun extractFilters(query: String): mihon.feature.ai.extension.MangaSearchFilters {
        val lowerQuery = query.lowercase()

        // Extraer género
        val genre = extractGenre(lowerQuery)

        // Extraer autor
        val author = extractAuthor(lowerQuery)

        // Extraer número de capítulos
        val chapterFilter = extractChapterFilter(lowerQuery)

        // Extraer tipo de manga
        val mangaType = detectMangaType(query)

        return mihon.feature.ai.extension.MangaSearchFilters(
            genre = genre,
            author = author,
            minChapters = chapterFilter,
            mangaType = mangaType
        )
    }

    private fun extractGenre(query: String): String? {
        val mangaGenres = GenesisAIConfig.MANGA_GENRES
        for (genre in mangaGenres) {
            if (query.contains(genre.lowercase())) {
                return genre
            }
        }
        return null
    }

    private fun extractAuthor(query: String): String? {
        // Extracción simple de nombres propios
        val words = query.split(" ")
        val potentialAuthors = words.filter { word ->
            word.length > 3 && word[0].isUpperCase()
        }
        return potentialAuthors.firstOrNull()
    }

    private fun extractChapterFilter(query: String): Int {
        // Extraer filtros de capítulos como "más de 100 capítulos"
        val chapterPattern = Regex("(más de|mas de|more than|at least|al menos|minimo|mínimo)\\s*(\\d+)")
        val match = chapterPattern.find(query)

        if (match != null) {
            val number = match.groupValues[2].toIntOrNull() ?: 0
            return number
        }

        // Buscar números en el contexto de capítulos
        val chapterKeywords = listOf("capítulos", "capitulos", "chapters", "caps")
        for (keyword in chapterKeywords) {
            val index = query.indexOf(keyword)
            if (index >= 0) {
                // Buscar número cerca de la palabra capítulos
                val afterKeyword = query.substring(index + keyword.length).trim()
                val numberMatch = Regex("\\d+").find(afterKeyword)
                if (numberMatch != null) {
                    return numberMatch.value.toIntOrNull() ?: 0
                }
            }
        }

        return 0
    }

    private fun detectMangaType(query: String): String? {
        val lowerQuery = query.lowercase()

        return when {
            lowerQuery.contains("manhwa") || lowerQuery.contains("webtoon") ||
            lowerQuery.contains("coreano") || lowerQuery.contains("korea") -> "manhwa"
            lowerQuery.contains("manhua") || lowerQuery.contains("chino") ||
            lowerQuery.contains("china") -> "manhua"
            lowerQuery.contains("manga") -> "manga"
            else -> null
        }
    }

    private fun detectSentiment(message: String): String {
        val lowerMessage = message.lowercase()

        val positiveWords = listOf("gracias", "thanks", "excelente", "great", "bueno", "good", "genial", "awesome", "me gusta", "like", "recomendado", "recomendado")
        val negativeWords = listOf("mal", "bad", "terrible", "horrible", "no me gusta", "don't like", "odio", "hate", "frustrado", "frustrated", "aburrido", "boring")

        val positiveCount = positiveWords.count { lowerMessage.contains(it) }
        val negativeCount = negativeWords.count { lowerMessage.contains(it) }

        return when {
            positiveCount > negativeCount -> "positive"
            negativeCount > positiveCount -> "negative"
            else -> "neutral"
        }
    }

    fun analyzeContext(conversationHistory: List<String>): Map<String, Any> {
        val recentMessages = conversationHistory.takeLast(5)

        return mapOf(
            "message_count" to recentMessages.size,
            "avg_message_length" to recentMessages.map { it.length }.average(),
            "dominant_intent" to recentMessages.map { detectIntent(it) }.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: "general",
            "manga_type_preference" to detectMangaTypePreference(recentMessages)
        )
    }

    private fun detectMangaTypePreference(messages: List<String>): String {
        val mangaCount = messages.count { it.lowercase().contains("manga") }
        val manhwaCount = messages.count { it.lowercase().contains("manhwa") || it.lowercase().contains("webtoon") }
        val manhuaCount = messages.count { it.lowercase().contains("manhua") }

        return when {
            manhwaCount > mangaCount && manhwaCount > manhuaCount -> "manhwa"
            manhuaCount > mangaCount && manhuaCount > manhwaCount -> "manhua"
            else -> "manga"
        }
    }
}

data class ProcessedQuery(
    val originalQuery: String,
    val processedQuery: String,
    val intent: String,
    val filters: mihon.feature.ai.extension.MangaSearchFilters,
    val mangaType: String
)

data class ProcessedMessage(
    val originalMessage: String,
    val processedMessage: String,
    val intent: String,
    val sentiment: String,
    val mangaType: String,
    val filters: mihon.feature.ai.extension.MangaSearchFilters
)
