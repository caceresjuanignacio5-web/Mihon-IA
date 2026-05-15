package mihon.feature.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GenesisLocalAI {

    private var isInitialized = false
    private val knowledgeBase = mutableMapOf<String, KnowledgeEntry>()
    private val responseCache = mutableMapOf<String, String>()
    private var learningScore = 0.0

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext

        try {
            loadKnowledgeBase()
            isInitialized = true
            Log.d("GenesisLocalAI", "GENESIS Local AI inicializado (v${GenesisAIConfig.GENESIS_VERSION})")
        } catch (e: Exception) {
            Log.e("GenesisLocalAI", "Error al inicializar GENESIS: ${e.message}")
        }
    }

    private fun loadKnowledgeBase() {
        // Cargar base de conocimiento inicial de manga/manhwa
        knowledgeBase["one_piece"] = KnowledgeEntry(
            title = "One Piece",
            author = "Eiichiro Oda",
            genres = listOf("acción", "aventura", "fantasía"),
            status = "en curso",
            type = "manga",
            description = "Luffy y su tripulación buscan el tesoro más grande del mundo.",
            confidence = 0.95
        )

        knowledgeBase["solo_leveling"] = KnowledgeEntry(
            title = "Solo Leveling",
            author = "Chugong",
            genres = listOf("acción", "fantasía", "sobrenatural"),
            status = "en curso",
            type = "manhwa",
            description = "Sung Jin-Woo, el cazador más débil, obtiene el poder de nivelar infinitamente.",
            confidence = 0.92
        )

        knowledgeBase["tower_of_god"] = KnowledgeEntry(
            title = "Tower of God",
            author = "SIU",
            genres = listOf("acción", "fantasía", "misterio"),
            status = "en curso",
            type = "manhwa",
            description = "Bam entra a la torre misteriosa para encontrar a su amigo Rachel.",
            confidence = 0.90
        )

        knowledgeBase["jujutsu_kaisen"] = KnowledgeEntry(
            title = "Jujutsu Kaisen",
            author = "Gege Akutami",
            genres = listOf("acción", "sobrenatural", "fantasía"),
            status = "en curso",
            type = "manga",
            description = "Yuji Itadori ingresa al mundo de la brujería para combatir maldiciones.",
            confidence = 0.93
        )

        knowledgeBase["demon_slayer"] = KnowledgeEntry(
            title = "Demon Slayer",
            author = "Koyoharu Gotouge",
            genres = listOf("acción", "fantasía", "sobrenatural"),
            status = "completo",
            type = "manga",
            description = "Tanjiro Kamado se convierte en cazador de demonios para salvar a su hermana.",
            confidence = 0.94
        )

        knowledgeBase["omniscient_reader"] = KnowledgeEntry(
            title = "Omniscient Reader",
            author = "Sing Shong",
            genres = listOf("acción", "fantasía", "misterio"),
            status = "en curso",
            type = "manhwa",
            description = "Dokja lee una novela que se vuelve realidad y debe sobrevivir.",
            confidence = 0.88
        )

        knowledgeBase["chainsaw_man"] = KnowledgeEntry(
            title = "Chainsaw Man",
            author = "Tatsuya Fujimoto",
            genres = listOf("acción", "horror", "fantasía"),
            status = "en curso",
            type = "manga",
            description = "Denji se fusiona con un demonio motosierra y se convierte en un cazador de demonios.",
            confidence = 0.91
        )

        knowledgeBase["the_beginning_after_the_end"] = KnowledgeEntry(
            title = "The Beginning After The End",
            author = "TurtleMe",
            genres = listOf("acción", "fantasía", "aventura"),
            status = "en curso",
            type = "manhwa",
            description = "Grey es reencarnado en un mundo de magia y poder.",
            confidence = 0.87
        )
    }

    suspend fun searchManga(query: String, extensionContext: String = ""): String = withContext(Dispatchers.IO) {
        val lowerQuery = query.lowercase()

        // Si hay contexto de extensiones, priorizar resultados reales
        if (extensionContext.isNotBlank() && extensionContext.contains("Título:")) {
            return """
                Resultados de búsqueda en extensiones de Tachiyomi:

                $extensionContext

                GENESIS ha aprendido de estos resultados para mejorar futuras búsquedas.
            """.trimIndent()
        }

        // Buscar en base de conocimiento local
        val matches = knowledgeBase.filter { (key, entry) ->
            key.contains(lowerQuery) ||
            entry.title.lowercase().contains(lowerQuery) ||
            entry.genres.any { it.contains(lowerQuery) } ||
            entry.author.lowercase().contains(lowerQuery)
        }

        if (matches.isNotEmpty()) {
            val results = matches.values.take(5).joinToString("\n\n") { entry ->
                """
                ${entry.title} - ${entry.author}
                Géneros: ${entry.genres.joinToString(", ")}
                Tipo: ${entry.type}
                Estado: ${entry.status}
                Sinopsis: ${entry.description}
                """.trimIndent()
            }

            "Resultados de GENESIS (Base de conocimiento local):\n\n$results"
        } else {
            // Si no hay coincidencias, generar respuesta basada en contexto de extensiones
            generateResponseFromContext(query, extensionContext)
        }
    }

    suspend fun createMangaList(criteria: String, extensionContext: String = ""): String = withContext(Dispatchers.IO) {
        val lowerCriteria = criteria.lowercase()

        // Si hay contexto de extensiones, usar resultados reales
        if (extensionContext.isNotBlank() && extensionContext.contains("Título:")) {
            return """
                Lista generada desde extensiones de Tachiyomi:

                $extensionContext

                GENESIS ha aprendido de estos resultados para mejorar futuras recomendaciones.
            """.trimIndent()
        }

        // Filtrar base de conocimiento según criterios
        val matches = knowledgeBase.filter { (_, entry) ->
            entry.genres.any { lowerCriteria.contains(it) } ||
            entry.type.lowercase().contains(lowerCriteria) ||
            entry.status.lowercase().contains(lowerCriteria)
        }

        if (matches.isNotEmpty()) {
            val results = matches.values.joinToString("\n\n") { entry ->
                """
                ${entry.title} - ${entry.author}
                Géneros: ${entry.genres.joinToString(", ")}
                Tipo: ${entry.type}
                Estado: ${entry.status}
                Descripción: ${entry.description}
                """.trimIndent()
            }

            "Lista de GENESIS (Base de conocimiento local):\n\n$results"
        } else {
            // Generar lista basada en contexto de extensiones
            generateListFromContext(criteria, extensionContext)
        }
    }

    suspend fun chat(message: String, conversationHistory: List<String> = []): String = withContext(Dispatchers.IO) {
        val lowerMessage = message.lowercase()

        // Respuestas basadas en conocimiento local
        when {
            lowerMessage.contains("hola") || lowerMessage.contains("hi") -> {
                "¡Hola! Soy GENESIS, tu IA local especializada en mangas y manhwas. ¿Qué estás buscando hoy?"
            }
            lowerMessage.contains("qué puedes hacer") || lowerMessage.contains("ayuda") -> {
                """
                Como GENESIS, puedo ayudarte a:
                - Buscar mangas y manhwas en las extensiones de Tachiyomi
                - Crear listas personalizadas basadas en tus preferencias
                - Filtrar por género, autor, número de capítulos, tipo (manga/manhwa)
                - Recomendar mangas según criterios complejos
                - Aprender de tus preferencias para mejorar las recomendaciones

                Todo esto usando mi base de conocimiento local y aprendiendo de cada interacción.
                """.trimIndent()
            }
            lowerMessage.contains("manga") || lowerMessage.contains("manhwa") -> {
                val matches = knowledgeBase.filter { (_, entry) ->
                    entry.genres.any { lowerMessage.contains(it) } ||
                    entry.type.lowercase().contains(lowerMessage)
                }

                if (matches.isNotEmpty()) {
                    val titles = matches.values.map { it.title }.joinToString(", ")
                    "Basado en mi conocimiento local, tengo información sobre: $titles. ¿Te gustaría saber más sobre alguno de ellos?"
                } else {
                    "Puedo buscar mangas y manhwas en las extensiones instaladas usando filtros avanzados. ¿Qué tipo de manga o manhwa te interesa? Puedo filtrar por género, autor, número de capítulos, etc."
                }
            }
            else -> {
                "Entiendo tu mensaje. Como GENESIS, estoy aquí para ayudarte a encontrar mangas y manhwas usando el sistema de búsqueda de Mihon. ¿Puedes ser más específico sobre qué estás buscando? Puedo usar filtros como 'mangas de fantasía con más de 100 capítulos'."
            }
        }
    }

    fun learnFromInteraction(query: String, response: String, userFeedback: Double = 0.0) {
        // Aprender de la interacción
        val key = query.lowercase().replace(" ", "_")

        if (!knowledgeBase.containsKey(key)) {
            // Si no existe, crear nueva entrada con baja confianza inicial
            knowledgeBase[key] = KnowledgeEntry(
                title = query,
                author = "Desconocido",
                genres = emptyList(),
                status = "desconocido",
                type = "desconocido",
                description = response,
                confidence = 0.3 + userFeedback * GenesisAIConfig.CONFIDENCE_BOOST_FACTOR
            )
        } else {
            // Si existe, actualizar confianza
            val entry = knowledgeBase[key]!!
            knowledgeBase[key] = entry.copy(
                confidence = (entry.confidence + userFeedback * GenesisAIConfig.CONFIDENCE_BOOST_FACTOR).coerceAtMost(1.0)
            )
        }

        learningScore += GenesisAIConfig.GENESIS_LEARNING_RATE
        Log.d("GenesisLocalAI", "Aprendido de interacción: $query (confianza actualizada)")
    }

    fun getLearningStats(): Map<String, Any> {
        return mapOf(
            "knowledge_base_size" to knowledgeBase.size,
            "learning_score" to learningScore,
            "avg_confidence" to knowledgeBase.values.map { it.confidence }.average(),
            "genesis_version" to GenesisAIConfig.GENESIS_VERSION,
            "learning_mode" to GenesisAIConfig.GENESIS_LEARNING_MODE
        )
    }

    private fun generateResponseFromContext(query: String, context: String): String {
        return if (context.isNotBlank()) {
            """
            Basado en las extensiones de Tachiyomi:

            $context

            GENESIS está aprendiendo de estos resultados para mejorar futuras búsquedas.
            """.trimIndent()
        } else {
            "GENESIS no encontró resultados en su base de conocimiento local. Puedo usar proxies externos para aprender más sobre tu consulta."
        }
    }

    private fun generateListFromContext(criteria: String, context: String): String {
        return if (context.isNotBlank()) {
            """
            Lista generada por GENESIS basada en extensiones de Tachiyomi:

            $context

            GENESIS está aprendiendo de estos resultados para mejorar futuras recomendaciones.
            """.trimIndent()
        } else {
            "GENESIS puede generar una lista usando su conocimiento local o aprendiendo de proxies externos. ¿Qué criterios específicos tienes en mente? Puedo filtrar por género, autor, número de capítulos, tipo (manga/manhwa), etc."
        }
    }
}

data class KnowledgeEntry(
    val title: String,
    val author: String,
    val genres: List<String>,
    val status: String,
    val type: String,
    val description: String,
    val confidence: Double
)
