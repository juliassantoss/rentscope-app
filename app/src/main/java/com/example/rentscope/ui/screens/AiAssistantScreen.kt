package com.example.rentscope.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentscope.R
import com.example.rentscope.data.local.LanguageManager
import com.example.rentscope.ui.components.MascotOrb
import com.example.rentscope.ui.components.MascotState
import com.example.rentscope.ui.viewmodel.AiViewModel

private val AssistantBlue = Color(0xFF0F6D90)

/**
 * Renders the assistant chat experience with contextual guidance and responsive feedback states.
 *
 * @param padding Insets provided by the app scaffold.
 * @param aiViewModel View model that owns the conversation state.
 */
@Composable
fun AiAssistantScreen(
    padding: PaddingValues,
    aiViewModel: AiViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentLanguage = LanguageManager.getSavedLanguage(context)
    val welcomeMessage = stringResource(R.string.ai_welcome_message)
    val errorReplyMessage = stringResource(R.string.ai_error_reply)
    val suggestionsTitle = stringResource(R.string.ai_suggestions_title)
    val inputPlaceholder = stringResource(R.string.ai_input_placeholder)
    val userLabel = stringResource(R.string.ai_user_label)
    val assistantLabel = stringResource(R.string.ai_assistant_name)
    val sendLabel = stringResource(R.string.ai_send)

    var question by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val firstMessage = aiViewModel.messages.firstOrNull()
    val remainingMessages = if (aiViewModel.messages.size > 1) {
        aiViewModel.messages.drop(1)
    } else {
        emptyList()
    }
    val quickQuestions = listOf(
        stringResource(R.string.ai_quick_question_results),
        stringResource(R.string.ai_quick_question_weights),
        stringResource(R.string.ai_quick_question_score),
        stringResource(R.string.ai_quick_question_country_fit),
        stringResource(R.string.ai_quick_question_map),
        stringResource(R.string.ai_quick_question_price_history)
    )

    LaunchedEffect(welcomeMessage, errorReplyMessage) {
        aiViewModel.syncLocalizedTexts(
            greeting = welcomeMessage,
            errorReply = errorReplyMessage
        )
    }

    LaunchedEffect(aiViewModel.messages.size, aiViewModel.loading) {
        val extraItems = if (aiViewModel.loading) 1 else 0
        val targetIndex = (aiViewModel.messages.lastIndex + extraItems).coerceAtLeast(0)
        if (aiViewModel.messages.isNotEmpty() || aiViewModel.loading) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (firstMessage != null) {
                item {
                    ChatBubble(
                        text = firstMessage.text,
                        isUser = firstMessage.isUser,
                        userLabel = userLabel,
                        assistantLabel = assistantLabel
                    )
                }
            }

            item {
                if (firstMessage == null) {
                    AssistantLoadingBubble(assistantLabel = assistantLabel)
                } else {
                    QuickQuestionsCard(
                        title = suggestionsTitle,
                        questions = quickQuestions,
                        onQuestionClick = { question = it }
                    )
                }
            }

            itemsIndexed(remainingMessages) { _, message ->
                ChatBubble(
                    text = message.text,
                    isUser = message.isUser,
                    userLabel = userLabel,
                    assistantLabel = assistantLabel
                )
            }

            if (aiViewModel.loading) {
                item {
                    AssistantLoadingBubble(assistantLabel = assistantLabel)
                }
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
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = aiViewModel.error ?: "",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp, max = 112.dp),
                    placeholder = {
                        Text(
                            text = inputPlaceholder,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    minLines = 1,
                    maxLines = 3,
                    shape = RoundedCornerShape(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        val text = question.trim()
                        if (text.isNotBlank() && !aiViewModel.loading) {
                            aiViewModel.perguntar(text, currentLanguage)
                            question = ""
                        }
                    },
                    enabled = question.isNotBlank() && !aiViewModel.loading,
                    modifier = Modifier.size(50.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = sendLabel
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickQuestionsCard(
    title: String,
    questions: List<String>,
    onQuestionClick: (String) -> Unit
) {
    SectionCard(contentPadding = PaddingValues(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            questions.forEach { prompt ->
                AssistChip(
                    onClick = { onQuestionClick(prompt) },
                    label = { Text(prompt) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AssistantBlue
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AssistantLoadingBubble(assistantLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        MascotOrb(
            modifier = Modifier.size(34.dp),
            orbSize = 32.dp,
            state = MascotState.THINKING
        )

        Spacer(modifier = Modifier.width(4.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(0.54f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = assistantLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                TypingDots()
            }
        }
    }
}

@Composable
private fun TypingDots() {
    val transition = rememberInfiniteTransition(label = "typing_dots")
    val firstAlpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 540, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "first_dot"
    )
    val secondAlpha by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 640, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "second_dot"
    )
    val thirdAlpha by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.84f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 740, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "third_dot"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        LoadingDot(firstAlpha)
        LoadingDot(secondAlpha)
        LoadingDot(thirdAlpha)
    }
}

@Composable
private fun LoadingDot(alpha: Float) {
    Surface(
        modifier = Modifier
            .size(8.dp)
            .graphicsLayer { this.alpha = alpha },
        shape = CircleShape,
        color = AssistantBlue.copy(alpha = 0.88f)
    ) {}
}

@Composable
private fun ChatBubble(
    text: String,
    isUser: Boolean,
    userLabel: String,
    assistantLabel: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            MascotOrb(
                modifier = Modifier.size(34.dp),
                orbSize = 32.dp
            )

            Spacer(modifier = Modifier.width(4.dp))
        }

        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = if (isUser) 18.dp else 6.dp,
                topEnd = if (isUser) 6.dp else 18.dp,
                bottomEnd = 18.dp,
                bottomStart = 18.dp
            ),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = if (isUser) userLabel else assistantLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUser) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = text,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
