package eu.kanade.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mihon.feature.ai.search.MangaAISearchInteractor
import mihon.feature.ai.search.MangaAIResult

class MangaAISearchScreenModel : ViewModel() {

    private val searchInteractor = MangaAISearchInteractor()

    private val _state = MutableStateFlow<MangaAIResult>(MangaAIResult.Loading)
    val state: StateFlow<MangaAIResult> = _state.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _learningStats = MutableStateFlow<Map<String, Any>>(emptyMap())
    val learningStats: StateFlow<Map<String, Any>> = _learningStats.asStateFlow()

    private val _extensionsCount = MutableStateFlow(0)
    val extensionsCount: StateFlow<Int> = _extensionsCount.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val stats = searchInteractor.getLearningStats()
            _learningStats.value = stats
            val extensions = stats["available_extensions"] as? List<*> ?: emptyList<Any>()
            _extensionsCount.value = extensions.size
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onSearch() {
        viewModelScope.launch {
            _state.value = MangaAIResult.Loading
            val result = searchInteractor.searchManga(_query.value)
            _state.value = result
            loadStats() // Recargar estadísticas después de búsqueda
        }
    }
}
