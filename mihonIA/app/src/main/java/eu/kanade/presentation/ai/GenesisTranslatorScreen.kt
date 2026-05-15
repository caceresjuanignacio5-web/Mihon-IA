package eu.kanade.presentation.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.Screen
import cafe.adriel.voyager.core.model.rememberScreenModel
import mihon.feature.ai.translator.TranslationState

class GenesisTranslatorScreen : Screen() {
    
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { GenesisTranslatorScreenModel() }
        val state by screenModel.state.collectAsState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Traductor GENESIS",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "Traduce capítulos de mangas y manhwas del inglés al español",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Input de manga ID y cantidad de capítulos
            OutlinedTextField(
                value = screenModel.mangaId,
                onValueChange = { screenModel.onMangaIdChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ID del manga") },
                label = { Text("ID del Manga") },
                singleLine = true
            )
            
            OutlinedTextField(
                value = screenModel.chapterCount,
                onValueChange = { screenModel.onChapterCountChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Número de capítulos") },
                label = { Text("Capítulos a traducir") },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de iniciar traducción
            Button(
                onClick = { screenModel.startTranslation() },
                modifier = Modifier.fillMaxWidth(),
                enabled = screenModel.mangaId.isNotBlank() && 
                          screenModel.chapterCount.isNotBlank() && 
                          state !is TranslationState.Loading
            ) {
                Icon(Icons.Default.Translate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Iniciar Traducción")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Estado de la traducción
            when (state) {
                is TranslationState.Idle -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Instrucciones:",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. Descarga el manga/manhwa que quieres traducir\n2. Ingresa el ID del manga\n3. Ingresa el número de capítulos a traducir\n4. GENESIS traducirá los capítulos uno por uno",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                is TranslationState.Loading -> {
                    val loadingState = state as TranslationState.Loading
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Traduciendo: ${loadingState.mangaTitle}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { loadingState.progress.toFloat() / loadingState.total },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${loadingState.progress}/${loadingState.total} capítulos",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = loadingState.currentChapter,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator()
                        }
                    }
                }
                is TranslationState.Success -> {
                    val successState = state as TranslationState.Success
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Traducción Completada",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${successState.mangaTitle}: ${successState.translatedCount}/${successState.totalCount} capítulos traducidos",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                is TranslationState.Error -> {
                    val errorState = state as TranslationState.Error
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Error en Traducción",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = errorState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de cancelar
            if (state is TranslationState.Loading) {
                Button(
                    onClick = { screenModel.cancelTranslation() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar Traducción")
                }
            }
        }
    }
}
