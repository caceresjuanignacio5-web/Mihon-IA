package eu.kanade.presentation.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
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
import eu.kanade.presentation.ai.components.MangaAIResultCard
import mihon.feature.ai.list.MangaListCreatorInteractor
import mihon.feature.ai.list.MangaListResult

class MangaListCreatorScreen : Screen() {
    
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { MangaListCreatorScreenModel() }
        val state by screenModel.state.collectAsState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Creador de Listas de Manga con GENESIS AI",
                style = MaterialTheme.typography.headlineMedium
            )
            
            OutlinedTextField(
                value = screenModel.criteria,
                onValueChange = { screenModel.onCriteriaChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Describe los criterios para la lista...") },
                leadingIcon = {
                    Icon(Icons.Default.Create, contentDescription = null)
                },
                singleLine = false,
                minLines = 3,
                maxLines = 5
            )
            
            Button(
                onClick = { screenModel.onCreateList() },
                modifier = Modifier.fillMaxWidth(),
                enabled = screenModel.criteria.isNotBlank() && state !is MangaListResult.Loading
            ) {
                Text("Crear Lista con GENESIS AI")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (state) {
                is MangaListResult.Loading -> {
                    CircularProgressIndicator()
                }
                is MangaListResult.Success -> {
                    val result = state as MangaListResult.Success
                    MangaAIResultCard(result.response)
                }
                is MangaListResult.Error -> {
                    val error = state as MangaListResult.Error
                    Text(
                        text = "Error: ${error.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    Text(
                        text = "Ingresa criterios para crear una lista de mangas personalizada",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
