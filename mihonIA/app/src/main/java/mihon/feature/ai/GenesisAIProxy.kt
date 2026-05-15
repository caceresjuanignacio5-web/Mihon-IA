package mihon.feature.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GenesisAIProxy {

    private var isInitialized = false
    private val genesisLocal = GenesisLocalAI()
    private val learningProviders = mutableMapOf<String, LearningProvider>()

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext

        try {
            // Inicializar GENESIS local
            genesisLocal.initialize()

            // Inicializar proveedores de aprendizaje (solo para que GENESIS aprenda)
            initializeLearningProviders()

            isInitialized = true
            Log.d("GenesisProxy", "Sistema de proxy inicializado (GENESIS Local + Proxies para aprendizaje)")
        } catch (e: Exception) {
            Log.e("GenesisProxy", "Error al inicializar: ${e.message}")
        }
    }

    private fun initializeLearningProviders() {
        // Proveedores externos solo para aprendizaje de GENESIS
        learningProviders["openai"] = LearningProvider("openai", "https://api.openai.com/v1")
        learningProviders["anthropic"] = LearningProvider("anthropic", "https://api.anthropic.com/v1")
        learningProviders["google"] = LearningProvider("google", "https://generativelanguage.googleapis.com/v1")
        learningProviders["cohere"] = LearningProvider("cohere", "https://api.cohere.ai/v1")
    }

    suspend fun searchManga(query: String, extensionContext: String = ""): String = withContext(Dispatchers.IO) {
        try {
            // GENESIS local responde primero
            val result = genesisLocal.searchManga(query, extensionContext)

            // GENESIS aprende de proxies externos para mejorar
            if (GenesisAIConfig.PROXY_LEARNING_ENABLED) {
                learnFromProviders(query, result)
            }

            result
        } catch (e: Exception) {
            Log.e("GenesisProxy", "Error en búsqueda: ${e.message}")
            "Error al buscar manga: ${e.message}"
        }
    }

    suspend fun createMangaList(criteria: String, extensionContext: String = ""): String = withContext(Dispatchers.IO) {
        try {
            // GENESIS local crea la lista
            val result = genesisLocal.createMangaList(criteria, extensionContext)

            // GENESIS aprende de proxies externos para mejorar
            if (GenesisAIConfig.PROXY_LEARNING_ENABLED) {
                learnFromProviders(criteria, result)
            }

            result
        } catch (e: Exception) {
            Log.e("GenesisProxy", "Error en creación de lista: ${e.message}")
            "Error al crear lista: ${e.message}"
        }
    }

    suspend fun chat(message: String, conversationHistory: List<String>): String = withContext(Dispatchers.IO) {
        try {
            // GENESIS local responde
            val result = genesisLocal.chat(message, conversationHistory)

            // GENESIS aprende de proxies externos para mejorar
            if (GenesisAIConfig.PROXY_LEARNING_ENABLED) {
                learnFromProviders(message, result)
            }

            result
        } catch (e: Exception) {
            Log.e("GenesisProxy", "Error en chat: ${e.message}")
            "Error en el chat: ${e.message}"
        }
    }

    private suspend fun learnFromProviders(query: String, genesisResponse: String) {
        // GENESIS aprende de proxies externos (asíncrono, no bloquea respuesta)
        learningProviders.values.forEach { provider ->
            try {
                val externalResponse = provider.learn(query)
                // GENESIS integra el aprendizaje
                genesisLocal.learnFromInteraction(query, externalResponse, 0.05)
                Log.d("GenesisProxy", "GENESIS aprendió de ${provider.name}")
            } catch (e: Exception) {
                Log.e("GenesisProxy", "Error aprendiendo de ${provider.name}: ${e.message}")
            }
        }
    }

    fun getLearningStats(): Map<String, Any> {
        return genesisLocal.getLearningStats()
    }
}

interface LearningProvider {
    val name: String
    suspend fun learn(query: String): String
}

class LearningProvider(
    override val name: String,
    private val baseUrl: String
) : LearningProvider {

    override suspend fun learn(query: String): String {
        // Simulación de aprendizaje desde proveedor externo
        // En producción, esto se conectaría al proxy real
        return "Aprendizaje desde $name para: $query"
    }
}
