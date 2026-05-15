package eu.kanade.tachiyomi.ui.browse.genesis

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.ai.GenesisTranslatorScreen
import eu.kanade.presentation.ai.MangaAISearchScreen
import eu.kanade.presentation.ai.MangaListCreatorScreen
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.TabContent
import kotlinx.collections.immutable.persistentListOf
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun Screen.genesisTab(): TabContent {
    val navigator = LocalNavigator.currentOrThrow

    return TabContent(
        titleRes = MR.strings.browse, // Usar browse o crear nuevo string resource
        actions = persistentListOf(
            AppBar.Action(
                title = stringResource(MR.strings.action_search),
                icon = Icons.Outlined.Psychology,
                onClick = { navigator.push(MangaAISearchScreen()) },
            ),
        ),
        content = { contentPadding, _ ->
            // Pantalla principal de GENESIS AI
            GenesisMainScreen(
                contentPadding = contentPadding,
                onSearchClick = { navigator.push(MangaAISearchScreen()) },
                onListCreatorClick = { navigator.push(MangaListCreatorScreen()) },
                onTranslatorClick = { navigator.push(GenesisTranslatorScreen()) },
            )
        },
    )
}
