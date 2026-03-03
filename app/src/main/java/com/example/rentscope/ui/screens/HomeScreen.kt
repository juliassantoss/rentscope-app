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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val BrandBlue = Color(0xFF2F86D6)

@Composable
fun HomeScreen(
    padding: PaddingValues,
    onContinue: (String) -> Unit
) {
    var selectedContinent by remember { mutableStateOf("Europa") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(26.dp))

        Text(
            text = "Bem-vindo ao RentScope",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Descubra os melhores lugares para morar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(22.dp))

        // Card principal
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEFF5FF)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Public,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = BrandBlue
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Selecione um continente para começar\nsua busca",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        AlignStartTitle("Selecione o continente")

        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ContinentChip(
                    text = "Europa",
                    selected = selectedContinent == "Europa",
                    onClick = { selectedContinent = "Europa" },
                    modifier = Modifier.weight(1f)
                )
                ContinentChip(
                    text = "Ásia",
                    selected = selectedContinent == "Ásia",
                    onClick = { selectedContinent = "Ásia" },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ContinentChip(
                    text = "Américas",
                    selected = selectedContinent == "Américas",
                    onClick = { selectedContinent = "Américas" },
                    modifier = Modifier.weight(1f)
                )
                ContinentChip(
                    text = "África",
                    selected = selectedContinent == "África",
                    onClick = { selectedContinent = "África" },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ContinentChip(
                    text = "Oceania",
                    selected = selectedContinent == "Oceania",
                    onClick = { selectedContinent = "Oceania" },
                    modifier = Modifier.weight(1f)
                )
                ContinentChip(
                    text = "Antártida",
                    selected = selectedContinent == "Antártida",
                    onClick = { selectedContinent = "Antártida" },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = { onContinue(selectedContinent) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
        ) {
            Text("Continuar", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color.White
            )
        }

        Spacer(Modifier.height(14.dp))
    }
}

@Composable
private fun AlignStartTitle(text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
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
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        color = container,
        contentColor = content,
        border = border,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Public,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = content
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
