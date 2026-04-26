package com.example.rentscope.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rentscope.R
import com.example.rentscope.ui.components.MascotOrb
import com.example.rentscope.ui.components.MascotState
import kotlinx.coroutines.delay

private val BrandBlue = Color(0xFF2F86D6)

@Composable
fun HomeScreen(
    padding: PaddingValues,
    onContinue: (String) -> Unit,
    onOpenAssistant: () -> Unit
) {
    val europe = stringResource(R.string.continent_europe)
    val asia = stringResource(R.string.continent_asia)
    val americas = stringResource(R.string.continent_americas)
    val africa = stringResource(R.string.continent_africa)
    val oceania = stringResource(R.string.continent_oceania)
    val antarctica = stringResource(R.string.continent_antarctica)

    var selectedContinent by remember { mutableStateOf(europe) }
    var homePositionInWindow by remember { mutableStateOf(Offset.Zero) }
    var mascotLookAtWindow by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(mascotLookAtWindow) {
        if (mascotLookAtWindow != null) {
            delay(1200)
            mascotLookAtWindow = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                homePositionInWindow = coordinates.positionInWindow()
            }
            .pointerInput(homePositionInWindow) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val touchStart = event.changes.firstOrNull { change ->
                            change.pressed && !change.previousPressed
                        }

                        if (touchStart != null) {
                            mascotLookAtWindow = Offset(
                                x = homePositionInWindow.x + touchStart.position.x,
                                y = homePositionInWindow.y + touchStart.position.y
                            )
                        }
                    }
                }
            }
            .padding(top = 112.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(14.dp))

        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF5FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MascotOrb(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(136.dp),
                    orbSize = 122.dp,
                    state = MascotState.IDLE,
                    lookAtWindow = mascotLookAtWindow
                )

                Spacer(Modifier.height(10.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        horizontalAlignment = Alignment.End
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(0.94f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.home_mascot_message),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        AssistantShortcutBubble(onClick = onOpenAssistant)
                    }
                }

                Spacer(Modifier.height(14.dp))

                ContinentStepHeader()

                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ContinentChip(
                            text = europe,
                            selected = selectedContinent == europe,
                            onClick = { selectedContinent = europe },
                            modifier = Modifier.weight(1f)
                        )
                        ContinentChip(
                            text = asia,
                            selected = selectedContinent == asia,
                            onClick = { selectedContinent = asia },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ContinentChip(
                            text = americas,
                            selected = selectedContinent == americas,
                            onClick = { selectedContinent = americas },
                            modifier = Modifier.weight(1f)
                        )
                        ContinentChip(
                            text = africa,
                            selected = selectedContinent == africa,
                            onClick = { selectedContinent = africa },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ContinentChip(
                            text = oceania,
                            selected = selectedContinent == oceania,
                            onClick = { selectedContinent = oceania },
                            modifier = Modifier.weight(1f)
                        )
                        ContinentChip(
                            text = antarctica,
                            selected = selectedContinent == antarctica,
                            onClick = { selectedContinent = antarctica },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { onContinue(selectedContinent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text(
                        text = stringResource(R.string.continue_button),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Spacer(Modifier.height(18.dp))
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ContinentStepHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = CircleShape,
            color = BrandBlue.copy(alpha = 0.14f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Public,
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_select_continent_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.home_select_continent_helper),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AssistantShortcutBubble(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = BrandBlue.copy(alpha = 0.12f),
        contentColor = BrandBlue,
        border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = stringResource(R.string.home_assistant_button),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ContinentChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = if (selected) BrandBlue else MaterialTheme.colorScheme.surface
    val content = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    val border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = container,
        contentColor = content,
        border = border,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Public,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = content
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
