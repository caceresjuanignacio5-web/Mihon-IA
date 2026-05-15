package eu.kanade.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mihon.feature.ai.chat.ChatResult
import mihon.feature.ai.chat.GenesisChatInteractor
import java.util.UUID

class GenesisChatScreenModel : ViewModel() {
    
    private val chatInteractor = GenesisChatInteractor()
    
    private val _state = MutableStateFlow<ChatResult>(ChatResult.Loading)
    val state: StateFlow<ChatResult> = _state.asStateFlow()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()
    
    fun onInputChange(newText: String) {
        _inputText.value = newText
    }
    
    fun sendMessage() {
        val message = _inputText.value
        if (message.isBlank()) return
        
        viewModelScope.launch {
            // Add user message
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                isUser = true,
                message = message
            )
            _messages.value = _messages.value + userMessage
            _inputText.value = ""
            
            _state.value = ChatResult.Loading
            
            val result = chatInteractor.sendMessage(message)
            
            when (result) {
                is ChatResult.Success -> {
                    val aiMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        isUser = false,
                        message = result.response
                    )
                    _messages.value = _messages.value + aiMessage
                    _state.value = result
                }
                is ChatResult.Error -> {
                    _state.value = result
                }
                else -> {}
            }
        }
    }
    
    fun clearChat() {
        viewModelScope.launch {
            chatInteractor.clearHistory()
            _messages.value = emptyList()
        }
    }
}
