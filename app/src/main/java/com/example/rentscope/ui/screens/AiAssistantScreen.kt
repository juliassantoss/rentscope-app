package com.example.rentscope.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentscope.data.local.LastSearchManager
import com.example.rentscope.ui.components.MascotOrb
import com.example.rentscope.ui.viewmodel.AiViewModel

@Composable
fun AiAssistantScreen(
    padding: PaddingValues,
    aiViewModel: AiViewModel = viewModel()
) {
    var pergunta by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val lastSearch = LastSearchManager.get()

    val quickQuestions = remember(lastSearch) {
        listOf(
            "Como interpretar os resultados do RentScope?",
            "O que significam os pesos dos filtros?",
            "Como o score dos municípios é calculado?",
            "Este país parece adequado com as minhas preferências?",
            "Como usar o mapa coroplético?",
            "O que o histórico de preços me mostra?"
        )
    }

    val mascotMessage = when {
        aiViewModel.loading -> "Estou a analisar a tua pergunta..."
        aiViewModel.error != null -> "Algo correu mal. Vamos tentar outra vez?"
        aiViewModel.messages.size > 1 -> "Continua. Posso responder a mais dúvidas sobre o app e os locais."
        else -> "Pergunta qualquer coisa relacionada ao RentScope."
    }

    LaunchedEffect(aiViewModel.messages.size) {
        if (aiViewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(aiViewModel.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .animateContentSize(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MascotOrb(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(150.dp),
                    orbSize = 130.dp,
                    state = aiViewModel.mascotState
                )

                Spacer(modifier = Modifier.size(12.dp))

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                ) {
                    Text(
                        text = mascotMessage,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))


            }
        }

        if (aiViewModel.loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                QuickQuestionsRow(
                    questions = quickQuestions,
                    onQuestionClick = { pergunta = it }
                )
            }

            itemsIndexed(aiViewModel.messages) { _, message ->
                ChatBubble(
                    text = message.text,
                    isUser = message.isUser
                )
            }

            item {
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.ime))
            }
        }

        AnimatedVisibility(visible = aiViewModel.error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = aiViewModel.error ?: "",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = pergunta,
                onValueChange = { pergunta = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Pergunta sobre municípios, score, filtros, mapa, preços...")
                },
                minLines = 1,
                maxLines = 5
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { aiViewModel.clearConversation() }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Limpar conversa"
                    )
                }

                Button(
                    onClick = {
                        val texto = pergunta.trim()
                        if (texto.isNotBlank() && !aiViewModel.loading) {
                            aiViewModel.perguntar(texto)
                            pergunta = ""
                        }
                    },
                    enabled = pergunta.isNotBlank() && !aiViewModel.loading
                ) {
                    if (aiViewModel.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar"
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun QuickQuestionsRow(
    questions: List<String>,
    onQuestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "Sugestões rápidas",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.size(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            questions.forEach { question ->
                AssistChip(
                    onClick = { onQuestionClick(question) },
                    label = { Text(question) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    text: String,
    isUser: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isUser) "Tu" else "Assistente",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isUser) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Spacer(modifier = Modifier.size(4.dp))

                Text(
                    text = text,
                    color = if (isUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                if (!isUser) {
                    Spacer(modifier = Modifier.size(6.dp))
                    Row {
                        TextButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resposta IA")
                        }
                    }
                }
            }
        }
    }
}