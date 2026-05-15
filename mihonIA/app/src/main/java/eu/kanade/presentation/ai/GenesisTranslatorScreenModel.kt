package eu.kanade.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mihon.feature.ai.translator.GenesisTranslatorInteractor
import mihon.feature.ai.translator.TranslationState

class GenesisTranslatorScreenModel : ViewModel() {
    
    private val translatorInteractor = GenesisTranslatorInteractor()
    
    private val _state = MutableStateFlow<TranslationState>(TranslationState.Idle)
    val state: StateFlow<TranslationState> = _state.asStateFlow()
    
    private val _mangaId = MutableStateFlow("")
    val mangaId: StateFlow<String> = _mangaId.asStateFlow()
    
    private val _chapterCount = MutableStateFlow("")
    val chapterCount: StateFlow<String> = _chapterCount.asStateFlow()
    
    fun onMangaIdChange(newId: String) {
        _mangaId.value = newId
    }
    
    fun onChapterCountChange(newCount: String) {
        _chapterCount.value = newCount
    }
    
    fun startTranslation() {
        viewModelScope.launch {
            val mangaIdLong = _mangaId.value.toLongOrNull() ?: 0L
            val chapterCountInt = _chapterCount.value.toIntOrNull() ?: 0
            
            if (mangaIdLong == 0L || chapterCountInt == 0) {
                _state.value = TranslationState.Error("ID del manga o número de capítulos inválido")
                return@launch
            }
            
            translatorInteractor.startTranslation(
                mangaId = mangaIdLong,
                chapterCount = chapterCountInt,
                sourceLanguage = "en",
                targetLanguage = "es"
            )
        }
    }
    
    fun cancelTranslation() {
        viewModelScope.launch {
            translatorInteractor.cancelTranslation()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        translatorInteractor.resetState()
    }
}
