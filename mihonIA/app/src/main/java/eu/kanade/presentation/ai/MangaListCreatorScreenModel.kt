package eu.kanade.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mihon.feature.ai.list.MangaListCreatorInteractor
import mihon.feature.ai.list.MangaListResult

class MangaListCreatorScreenModel : ViewModel() {
    
    private val listCreatorInteractor = MangaListCreatorInteractor()
    
    private val _state = MutableStateFlow<MangaListResult>(MangaListResult.Loading)
    val state: StateFlow<MangaListResult> = _state.asStateFlow()
    
    private val _criteria = MutableStateFlow("")
    val criteria: StateFlow<String> = _criteria.asStateFlow()
    
    fun onCriteriaChange(newCriteria: String) {
        _criteria.value = newCriteria
    }
    
    fun onCreateList() {
        viewModelScope.launch {
            _state.value = MangaListResult.Loading
            val result = listCreatorInteractor.createMangaList(_criteria.value)
            _state.value = result
        }
    }
}
