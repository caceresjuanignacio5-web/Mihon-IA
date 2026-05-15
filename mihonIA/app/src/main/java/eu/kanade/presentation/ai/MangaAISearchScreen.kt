package eu.kanade.presentation.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import cafe.adriel.voyager.core.screen.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import eu.kanade.presentation.ai.components.MangaAIResultCard
import mihon.feature.ai.search.MangaAISearchInteractor
import mihon.feature.ai.search.MangaAIResult

class MangaAISearchScreen : Screen() {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { MangaAISearchScreenModel() }
        val state by screenModel.state.collectAsState()
        val learningStats by screenModel.learningStats.collectAsState()
        val extensionsCount by screenModel.extensionsCount.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "GENESIS AI - Búsqueda de Manga/Manhwa",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "IA Local v${learningStats["genesis_version"] ?: "3.0.0"} | Aprendizaje: ${learningStats["learning_score"] ?: 0.0}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Base de conocimiento: ${learningStats["knowledge_base_size"] ?: 0} mangas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Buscando en $extensionsCount extensiones de Tachiyomi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = screenModel.query,
                onValueChange = { screenModel.onQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Busca manga, manhwa o manhwa...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true
            )

            Button(
                onClick = { screenModel.onSearch() },
                modifier = Modifier.fillMaxWidth(),
                enabled = screenModel.query.isNotBlank() && state !is MangaAIResult.Loading
            ) {
                Text("Buscar con GENESIS AI (Local)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (state) {
                is MangaAIResult.Loading -> {
                    CircularProgressIndicator()
                }
                is MangaAIResult.Success -> {
                    val result = state as MangaAIResult.Success
                    MangaAIResultCard(result.response)
                }
                is MangaAIResult.Error -> {
                    val error = state as MangaAIResult.Error
                    Text(
                        text = "Error: ${error.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    Text(
                        text = "GENESIS es una IA local que aprende de cada interacción.\n\nBusca mangas y manhwas en las extensiones de Tachiyomi instaladas.\n\nUsa proxies externos solo para aprender, no para responder directamente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
