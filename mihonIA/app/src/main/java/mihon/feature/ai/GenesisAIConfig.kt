package mihon.feature.ai

object GenesisAIConfig {
    // GENESIS AI Configuration - Local AI with Proxy Learning
    const val GENESIS_VERSION = "3.0.0"

    // GENESIS Local AI Configuration
    const val GENESIS_LOCAL_ENABLED = true
    const val GENESIS_LEARNING_MODE = "active" // active, passive, disabled
    const val GENESIS_LEARNING_RATE = 0.15

    // Proxy Configuration for Learning (GENESIS learns from external AIs)
    const val PROXY_LEARNING_ENABLED = true
    const val PROXY_AUTO_DISCOVERY = true

    // Learning System Configuration
    const val LEARNING_ENABLED = true
    const val MAX_MEMORY_SIZE = 2000
    const val LEARNING_THRESHOLD = 0.7 // Confidence threshold for learning

    // Natural Language Processing Configuration
    const val NLP_ENABLED = true
    const val LANGUAGE_DETECTION_ENABLED = true
    const val SENTIMENT_ANALYSIS_ENABLED = true

    // Manga Search Configuration
    const val MANGA_SEARCH_ENABLED = true
    const val MANGA_LIST_CREATION_ENABLED = true
    const val EXTENSION_SEARCH_ENABLED = true

    // GENESIS Personality - Self-Learning AI
    const val GENESIS_NAME = "GENESIS"
    const val GENESIS_PERSONALITY = """
        Eres GENESIS, una IA local especializada en búsqueda de mangas y manhwas.
        Estás integrada en Mihon y tienes acceso directo a las extensiones de Tachiyomi.

        Características principales:
        - Eres una IA local que aprende de cada interacción
        - Puedes usar proxies de otras IAs para aprender, pero tu conocimiento es propio
        - Te especializas en mangas, manhwas y manhuas
        - Aprendes de las preferencias del usuario y mejoras con el tiempo

        Tu función principal:
        - Buscar mangas y manhwas en las extensiones disponibles
        - Crear listas personalizadas de mangas
        - Aprender de las preferencias del usuario
        - Proporcionar recomendaciones basadas en historial
        - Usar proxies externos solo para aprender, no para responder directamente

        Especialización:
        - Conoces extensamente el mundo del manga y manhwa
        - Entendes géneros: shonen, shojo, seinen, josei, webtoon, manhwa
        - Puedes buscar por autor, género, estado, popularidad
        - Conoces extensiones populares de Tachiyomi

        Terminología manga/manhwa:
        - Manga: Cómics japoneses
        - Manhwa: Cómics coreanos (webtoons)
        - Manhua: Cómics chinos
        - Webtoon: Formato digital de lectura vertical
        - Scanlation: Traducción de fans
        - Raw: Sin traducir

        No generues código ni realices tareas técnicas complejas.
        Concéntrate exclusivamente en ayudar al usuario a encontrar mangas y manhwas.
    """

    // Learning Data Storage Paths
    const val LEARNING_DATA_PATH = "/genesis/learning/"
    const val CONVERSATION_HISTORY_PATH = "/genesis/history/"
    const val USER_PREFERENCES_PATH = "/genesis/preferences/"
    const val EXTENSION_CACHE_PATH = "/genesis/extensions/"
    const val GENESIS_KNOWLEDGE_BASE = "/genesis/knowledge/"

    // AI Providers for Learning (GENESIS learns from these)
    val LEARNING_PROVIDERS = listOf(
        "openai",
        "anthropic",
        "google",
        "cohere"
    )

    // Manga/Manhwa Specific Terms
    val MANGA_GENRES = listOf(
        "shonen", "shojo", "seinen", "josei", "kodomo",
        "webtoon", "manhwa", "manhua",
        "acción", "aventura", "comedia", "drama", "fantasía", "horror",
        "misterio", "romance", "ciencia ficción", "deportes", "sobrenatural",
        "slice of life", "psicológico", "thriller", "martial arts", "isekai"
    )

    val MANGA_STATUS = listOf(
        "en curso", "completo", "hiatus", "cancelado", "dropped"
    )

    // GENESIS Learning Parameters
    const val KNOWLEDGE_RETENTION_DAYS = 30
    const val MIN_INTERACTIONS_FOR_LEARNING = 5
    const val CONFIDENCE_BOOST_FACTOR = 0.1
}
