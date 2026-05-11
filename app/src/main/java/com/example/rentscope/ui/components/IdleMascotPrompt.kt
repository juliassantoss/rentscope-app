package com.example.rentscope.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rentscope.R
import kotlinx.coroutines.delay

/**
 * Overlay que mostra o mascote a perguntar "Posso ajudar?" depois de
 * alguns segundos de inatividade do utilizador no ecrã atual.
 *
 * Funciona como wrapper transparente: envolve o conteúdo da screen,
 * monitoriza eventos de input para detetar atividade, e mostra um
 * cartão flutuante no canto inferior quando o tempo de inatividade
 * é atingido. O cartão pode ser dispensado pelo utilizador, e nesse
 * caso só volta a aparecer depois de mudar de ecrã (ou seja, depois
 * do composable ser recriado pela navegação).
 *
 * @param idleTimeoutMs Tempo de inatividade até aparecer o pop-up (ms).
 * @param onOpenAssistant Acionado quando o utilizador clica em "Falar".
 */
@Composable
fun IdleMascotPrompt(
    modifier: Modifier = Modifier,
    idleTimeoutMs: Long = 8_000L,
    onOpenAssistant: () -> Unit,
    content: @Composable () -> Unit
) {
    var lastActivityAt by remember { mutableStateOf(System.currentTimeMillis()) }
    var dismissed by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(lastActivityAt, dismissed) {
        if (dismissed) return@LaunchedEffect
        delay(idleTimeoutMs)
        // Após o delay, confirma que ninguém tocou entretanto.
        if (!dismissed && System.currentTimeMillis() - lastActivityAt >= idleTimeoutMs) {
            visible = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // PASS_INITIAL para captar TODOS os toques sem consumir nem
                // bloquear interações da UI por baixo. Apenas regista atividade.
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.changes.any { it.pressed }) {
                            lastActivityAt = System.currentTimeMillis()
                            if (visible) {
                                visible = false
                            }
                        }
                    }
                }
            }
    ) {
        content()

        AnimatedVisibility(
            visible = visible && !dismissed,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 96.dp)
        ) {
            IdlePromptBubble(
                onDismiss = {
                    visible = false
                    dismissed = true
                },
                onOpenAssistant = {
                    visible = false
                    dismissed = true
                    onOpenAssistant()
                }
            )
        }
    }
}

@Composable
private fun IdlePromptBubble(
    onDismiss: () -> Unit,
    onOpenAssistant: () -> Unit
) {
    Card(
        modifier = Modifier.widthIn(max = 280.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.size(56.dp)) {
                    MascotOrb(
                        modifier = Modifier.fillMaxSize(),
                        orbSize = 56.dp,
                        state = MascotState.SPEAKING
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = stringResource(R.string.ai_assistant_name),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.idle_mascot_dismiss),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stringResource(R.string.idle_mascot_prompt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onOpenAssistant) {
                    Text(stringResource(R.string.idle_mascot_open_chat))
                }
            }
        }
    }
}
