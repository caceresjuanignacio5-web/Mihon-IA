package mihon.feature.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mihon.feature.ai.extension.ExtensionSearchInteractor

class GenesisAIClient private constructor() {

    private var isInitialized = false
    private val learningSystem = GenesisLearningSystem()
    private val nlpProcessor = GenesisNLPProcessor()
    private val genesisLocal = GenesisLocalAI()
    private val extensionSearch = ExtensionSearchInteractor()

    companion object {
        @Volatile
        private var instance: GenesisAIClient? = null

        fun getInstance(): GenesisAIClient {
            return instance ?: synchronized(this) {
                instance ?: GenesisAIClient().also { instance = it }
            }
        }
    }

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext

        try {
            learningSystem.initialize()
            nlpProcessor.initialize()
            genesisLocal.initialize()
            isInitialized = true
            Log.d("GenesisAI", "GENESIS AI System inicializado (GENESIS Local + Extensiones)")
        } catch (e: Exception) {
            Log.e("GenesisAI", "Error al inicializar GENESIS AI: ${e.message}")
        }
    }

    suspend fun searchManga(query: String): String = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            initialize()
        }

        try {
            // Procesar la consulta con NLP para extraer filtros
            val processedQuery = nlpProcessor.processQuery(query)

            // Aplicar aprendizaje del usuario
            val enhancedQuery = learningSystem.enhanceQuery(processedQuery.processedQuery)

            // Buscar en extensiones de Mihon con filtros extraídos
            val extensionResults = extensionSearch.searchInExtensions(enhancedQuery, processedQuery.filters)

            // Generar contexto de extensiones
            val context = buildExtensionContext(extensionResults)

            // GENESIS local responde usando contexto de extensiones
            val aiResult = genesisLocal.searchManga(enhancedQuery, context)

            // Aprender de esta búsqueda
            learningSystem.learnFromSearch(query, aiResult)

            aiResult
        } catch (e: Exception) {
            Log.e("GenesisAI", "Error en búsqueda de manga: ${e.message}")
            "Error al buscar manga: ${e.message}"
        }
    }

    suspend fun createMangaList(criteria: String): String = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            initialize()
        }

        try {
            // Procesar criterios con NLP para extraer filtros
            val processedQuery = nlpProcessor.processQuery(criteria)

            // Buscar en extensiones con filtros extraídos
            val extensionResults = extensionSearch.searchInExtensions(processedQuery.processedQuery, processedQuery.filters)

            // Generar contexto de extensiones
            val context = buildExtensionContext(extensionResults)

            // GENESIS local crea la lista usando contexto de extensiones
            val result = genesisLocal.createMangaList(processedQuery.processedQuery, context)

            // Aprender de esta creación
            learningSystem.learnFromListCreation(criteria, result)

            result
        } catch (e: Exception) {
            Log.e("GenesisAI", "Error en creación de lista: ${e.message}")
            "Error al crear lista: ${e.message}"
        }
    }

    suspend fun chat(message: String, conversationHistory: List<String> = emptyList()): String = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            initialize()
        }

        try {
            // Procesar mensaje con NLP
            val processedMessage = nlpProcessor.processMessage(message)

            // Aplicar aprendizaje del usuario
            val enhancedMessage = learningSystem.enhanceMessage(processedMessage.processedMessage)

            // GENESIS local responde
            val result = genesisLocal.chat(enhancedMessage, conversationHistory)

            // Aprender de esta conversación
            learningSystem.learnFromConversation(message, result)

            result
        } catch (e: Exception) {
            Log.e("GenesisAI", "Error en chat: ${e.message}")
            "Error en el chat: ${e.message}"
        }
    }

    suspend fun getLearningStats(): Map<String, Any> = withContext(Dispatchers.IO) {
        val genesisStats = genesisLocal.getLearningStats()
        val learningStats = learningSystem.getStats()
        genesisStats + learningStats
    }

    suspend fun getAvailableExtensions(): List<mihon.feature.ai.extension.ExtensionInfo> = withContext(Dispatchers.IO) {
        extensionSearch.getAvailableExtensions()
    }

    private fun buildExtensionContext(results: List<mihon.feature.ai.extension.ExtensionSearchResult>): String {
        if (results.isEmpty()) {
            return "No se encontraron resultados en las extensiones instaladas."
        }

        return results.take(10).joinToString("\n\n") { result ->
            """
            Título: ${result.title}
            Fuente: ${result.sourceName}
            Género: ${result.genre}
            Autor: ${result.author}
            Estado: ${result.status}
            Tipo: ${result.type}
            ${if (result.chapterCount != null) "Capítulos: ${result.chapterCount}" else ""}
            """.trimIndent()
        }
    }
}
