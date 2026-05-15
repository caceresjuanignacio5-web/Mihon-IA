package mihon.feature.ai.translator

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import tachiyomi.domain.chapter.interactor.GetChapters
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class GenesisTranslator {
    
    private val getChapters: GetChapters = Injekt.get()
    
    suspend fun translateMangaChapters(
        mangaId: Long,
        chapterCount: Int,
        sourceLanguage: String = "en",
        targetLanguage: String = "es",
        onProgress: (Int, Int, String) -> Unit = { _, _, _ -> }
    ): TranslationResult = withContext(Dispatchers.IO) {
        try {
            Log.d("GenesisTranslator", "Iniciando traducción de $chapterCount capítulos del manga $mangaId")
            
            // Obtener capítulos del manga
            val chapters = getChapters.await(mangaId)
            val chaptersToTranslate = chapters.take(chapterCount)
            
            if (chaptersToTranslate.isEmpty()) {
                return@withContext TranslationResult.Error("No se encontraron capítulos para traducir")
            }
            
            Log.d("GenesisTranslator", "Se traducirán ${chaptersToTranslate.size} capítulos")
            
            // Traducir capítulos en paralelo (limitado para no sobrecargar)
            val translatedChapters = mutableListOf<TranslatedChapter>()
            val batchSize = 5 // Traducir 5 capítulos a la vez
            
            for (i in chaptersToTranslate.indices step batchSize) {
                val batch = chaptersToTranslate.subList(i, minOf(i + batchSize, chaptersToTranslate.size))
                
                val batchResults = batch.map { chapter ->
                    async {
                        try {
                            val translatedText = translateChapterText(chapter.name, sourceLanguage, targetLanguage)
                            TranslatedChapter(
                                chapterId = chapter.id,
                                originalText = chapter.name,
                                translatedText = translatedText,
                                status = "completado"
                            )
                        } catch (e: Exception) {
                            Log.e("GenesisTranslator", "Error traduciendo capítulo ${chapter.id}: ${e.message}")
                            TranslatedChapter(
                                chapterId = chapter.id,
                                originalText = chapter.name,
                                translatedText = chapter.name, // Mantener original si falla
                                status = "error: ${e.message}"
                            )
                        }
                    }
                }.awaitAll()
                
                translatedChapters.addAll(batchResults)
                
                // Reportar progreso
                val progress = translatedChapters.size
                onProgress(progress, chaptersToTranslate.size, "Traduciendo capítulo ${progress}/${chaptersToTranslate.size}")
            }
            
            TranslationResult.Success(
                translatedCount = translatedChapters.size,
                totalCount = chaptersToTranslate.size,
                chapters = translatedChapters
            )
        } catch (e: Exception) {
            Log.e("GenesisTranslator", "Error en traducción: ${e.message}")
            TranslationResult.Error(e.message ?: "Error desconocido")
        }
    }
    
    private suspend fun translateChapterText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): String = withContext(Dispatchers.IO) {
        // Simulación de traducción
        // En producción, esto usaría un servicio de traducción real (Google Translate, DeepL, etc.)
        // o un modelo de traducción local
        
        // Por ahora, simulamos traducción añadiendo prefijo
        "[ES] $text"
    }
    
    suspend fun getTranslationHistory(): List<TranslationHistory> = withContext(Dispatchers.IO) {
        // Simulación de historial de traducciones
        emptyList()
    }
}

data class TranslatedChapter(
    val chapterId: Long,
    val originalText: String,
    val translatedText: String,
    val status: String
)

data class TranslationHistory(
    val mangaId: Long,
    val mangaTitle: String,
    val translatedCount: Int,
    val totalCount: Int,
    val timestamp: Long,
    val status: String
)

sealed class TranslationResult {
    data class Success(
        val translatedCount: Int,
        val totalCount: Int,
        val chapters: List<TranslatedChapter>
    ) : TranslationResult()
    
    data class Error(val message: String) : TranslationResult()
    object InProgress : TranslationResult()
}
