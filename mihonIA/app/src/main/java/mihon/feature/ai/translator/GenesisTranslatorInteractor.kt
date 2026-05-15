package mihon.feature.ai.translator

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import tachiyomi.domain.chapter.interactor.GetChapters
import tachiyomi.domain.manga.interactor.GetManga
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class GenesisTranslatorInteractor {
    
    private val translator = GenesisTranslator()
    private val getManga: GetManga = Injekt.get()
    private val getChapters: GetChapters = Injekt.get()
    
    private val _translationState = MutableStateFlow<TranslationState>(TranslationState.Idle)
    val translationState: StateFlow<TranslationState> = _translationState.asStateFlow()
    
    suspend fun startTranslation(
        mangaId: Long,
        chapterCount: Int,
        sourceLanguage: String = "en",
        targetLanguage: String = "es"
    ) = withContext(Dispatchers.IO) {
        try {
            // Obtener información del manga
            val manga = getManga.await(mangaId)
            if (manga == null) {
                _translationState.value = TranslationState.Error("Manga no encontrado")
                return@withContext
            }
            
            _translationState.value = TranslationState.Loading(
                mangaTitle = manga.title,
                progress = 0,
                total = chapterCount,
                currentChapter = "Iniciando..."
            )
            
            // Iniciar traducción
            val result = translator.translateMangaChapters(
                mangaId = mangaId,
                chapterCount = chapterCount,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                onProgress = { progress, total, currentChapter ->
                    _translationState.value = TranslationState.Loading(
                        mangaTitle = manga.title,
                        progress = progress,
                        total = total,
                        currentChapter = currentChapter
                    )
                }
            )
            
            when (result) {
                is TranslationResult.Success -> {
                    _translationState.value = TranslationState.Success(
                        mangaTitle = manga.title,
                        translatedCount = result.translatedCount,
                        totalCount = result.totalCount
                    )
                    Log.d("GenesisTranslator", "Traducción completada: ${result.translatedCount}/${result.totalCount}")
                }
                is TranslationResult.Error -> {
                    _translationState.value = TranslationState.Error(result.message)
                    Log.e("GenesisTranslator", "Error en traducción: ${result.message}")
                }
                else -> {}
            }
        } catch (e: Exception) {
            Log.e("GenesisTranslatorInteractor", "Error: ${e.message}")
            _translationState.value = TranslationState.Error(e.message ?: "Error desconocido")
        }
    }
    
    suspend fun cancelTranslation() = withContext(Dispatchers.IO) {
        _translationState.value = TranslationState.Idle
        Log.d("GenesisTranslatorInteractor", "Traducción cancelada")
    }
    
    suspend fun getChapterCount(mangaId: Long): Int = withContext(Dispatchers.IO) {
        try {
            val chapters = getChapters.await(mangaId)
            chapters.size
        } catch (e: Exception) {
            Log.e("GenesisTranslatorInteractor", "Error al obtener capítulos: ${e.message}")
            0
        }
    }
    
    fun resetState() {
        _translationState.value = TranslationState.Idle
    }
}

sealed class TranslationState {
    object Idle : TranslationState()
    
    data class Loading(
        val mangaTitle: String,
        val progress: Int,
        val total: Int,
        val currentChapter: String
    ) : TranslationState()
    
    data class Success(
        val mangaTitle: String,
        val translatedCount: Int,
        val totalCount: Int
    ) : TranslationState()
    
    data class Error(val message: String) : TranslationState()
}
