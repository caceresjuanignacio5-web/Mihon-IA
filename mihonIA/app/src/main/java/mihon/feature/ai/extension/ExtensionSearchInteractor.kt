package mihon.feature.ai.extension

import android.util.Log
import eu.kanade.tachiyomi.extension.ExtensionManager
import eu.kanade.tachiyomi.source.CatalogueSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import tachiyomi.domain.source.service.SourceManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.concurrent.Executors

class ExtensionSearchInteractor {

    private val sourceManager: SourceManager = Injekt.get()
    private val extensionManager: ExtensionManager = Injekt.get()

    // Pool de hilos para búsqueda paralela (aceleración)
    private val searchDispatcher = Executors.newFixedThreadPool(8).asCoroutineDispatcher()

    suspend fun searchInExtensions(query: String, filters: MangaSearchFilters = MangaSearchFilters()): List<ExtensionSearchResult> {
        val results = mutableListOf<ExtensionSearchResult>()

        try {
            // Obtener todas las fuentes instaladas
            val sources = getEnabledSources()

            // Búsqueda paralela en todas las fuentes (aceleración)
            val searchResults = sources.map { source ->
                async(searchDispatcher) {
                    try {
                        // Usar el sistema de búsqueda real de Mihon
                        val page = source.getSearchManga(1, query, source.getFilterList())

                        // Convertir resultados a ExtensionSearchResult
                        page.mangas.map { manga ->
                            ExtensionSearchResult(
                                title = manga.title,
                                url = manga.url,
                                sourceName = source.name,
                                sourceId = source.id.toString(),
                                thumbnailUrl = manga.thumbnail_url,
                                genre = extractGenre(manga), // Extraer género si está disponible
                                author = extractAuthor(manga), // Extraer autor si está disponible
                                status = manga.status?.toString() ?: "desconocido",
                                type = detectMangaType(source.name),
                                chapterCount = manga.initial_chapter_count // Número de capítulos si está disponible
                            )
                        }.filter { result ->
                            // Aplicar filtros avanzados
                            applyFilters(result, filters)
                        }
                    } catch (e: Exception) {
                        Log.e("ExtensionSearch", "Error buscando en ${source.name}: ${e.message}")
                        emptyList<ExtensionSearchResult>()
                    }
                }
            }.awaitAll()

            // Combinar todos los resultados
            searchResults.forEach { results.addAll(it) }

            // Eliminar duplicados por URL
            val uniqueResults = results.distinctBy { it.url }

            Log.d("ExtensionSearch", "Búsqueda completada: ${uniqueResults.size} resultados únicos de ${sources.size} fuentes")
            return uniqueResults
        } catch (e: Exception) {
            Log.e("ExtensionSearch", "Error en búsqueda general: ${e.message}")
            return emptyList()
        }
    }

    suspend fun searchByGenre(genre: String, filters: MangaSearchFilters = MangaSearchFilters()): List<ExtensionSearchResult> {
        val query = genre
        val genreFilters = filters.copy(genre = genre)
        return searchInExtensions(query, genreFilters)
    }

    suspend fun searchByAuthor(author: String, filters: MangaSearchFilters = MangaSearchFilters()): List<ExtensionSearchResult> {
        val query = author
        val authorFilters = filters.copy(author = author)
        return searchInExtensions(query, authorFilters)
    }

    suspend fun searchByChapterCount(minChapters: Int, filters: MangaSearchFilters = MangaSearchFilters()): List<ExtensionSearchResult> {
        val chapterFilters = filters.copy(minChapters = minChapters)
        // Buscar con query vacío para obtener todos y filtrar por capítulos
        return searchInExtensions("", chapterFilters)
    }

    suspend fun searchWithComplexFilters(
        genre: String? = null,
        author: String? = null,
        minChapters: Int? = null,
        mangaType: String? = null
    ): List<ExtensionSearchResult> {
        val filters = MangaSearchFilters(
            genre = genre,
            author = author,
            minChapters = minChapters ?: 0,
            mangaType = mangaType
        )

        // Construir query basado en filtros
        val query = buildSearchQuery(genre, author)

        return searchInExtensions(query, filters)
    }

    private fun getEnabledSources(): List<CatalogueSource> {
        return sourceManager.getCatalogueSources()
            .filter { !it.isNsfw() } // Filtrar NSFW por defecto
    }

    private fun extractGenre(manga: tachiyomi.source.model.SManga): String {
        // Intentar extraer género de los metadatos del manga
        return manga.genre ?: "desconocido"
    }

    private fun extractAuthor(manga: tachiyomi.source.model.SManga): String {
        // Intentar extraer autor de los metadatos del manga
        return manga.author ?: "desconocido"
    }

    private fun detectMangaType(sourceName: String): String {
        val lowerName = sourceName.lowercase()
        return when {
            lowerName.contains("manhwa") || lowerName.contains("webtoon") -> "manhwa"
            lowerName.contains("manhua") -> "manhua"
            else -> "manga"
        }
    }

    private fun applyFilters(result: ExtensionSearchResult, filters: MangaSearchFilters): Boolean {
        // Filtrar por género
        if (filters.genre != null && !result.genre.lowercase().contains(filters.genre.lowercase())) {
            return false
        }

        // Filtrar por autor
        if (filters.author != null && !result.author.lowercase().contains(filters.author.lowercase())) {
            return false
        }

        // Filtrar por número mínimo de capítulos
        if (filters.minChapters > 0 && (result.chapterCount == null || result.chapterCount < filters.minChapters)) {
            return false
        }

        // Filtrar por tipo de manga
        if (filters.mangaType != null && result.type.lowercase() != filters.mangaType.lowercase()) {
            return false
        }

        return true
    }

    private fun buildSearchQuery(genre: String?, author: String?): String {
        val parts = mutableListOf<String>()
        genre?.let { parts.add(it) }
        author?.let { parts.add(it) }
        return parts.joinToString(" ")
    }

    fun getAvailableExtensions(): List<ExtensionInfo> {
        return sourceManager.getCatalogueSources().map { source ->
            ExtensionInfo(
                id = source.id.toString(),
                name = source.name,
                lang = source.lang,
                isNsfw = source.isNsfw()
            )
        }
    }
}

data class MangaSearchFilters(
    val genre: String? = null,
    val author: String? = null,
    val minChapters: Int = 0,
    val mangaType: String? = null, // "manga", "manhwa", "manhua"
    val status: String? = null
)

data class ExtensionSearchResult(
    val title: String,
    val url: String,
    val sourceName: String,
    val sourceId: String,
    val thumbnailUrl: String?,
    val genre: String,
    val author: String,
    val status: String,
    val type: String, // Manga, Manhwa, Manhua
    val chapterCount: Int? = null // Número de capítulos
)

data class ExtensionInfo(
    val id: String,
    val name: String,
    val lang: String,
    val isNsfw: Boolean
)
