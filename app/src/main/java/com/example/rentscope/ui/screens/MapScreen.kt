package com.example.rentscope.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val BrandBlue = Color(0xFF2F86D6)

@Composable
fun MapScreen(
    padding: PaddingValues,
    countryCode: String,
    countryName: String,
    onConfigureFiltersClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ✅ Botão (única coisa "certa" do mockup)
        Button(
            onClick = onConfigureFiltersClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Tune, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text("Configurar Filtros", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(14.dp))

        // Header azul (título + país)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = BrandBlue)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Mapa de Acordo com as Preferências Acima",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$countryName",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ✅ Placeholder do mapa (vai integrar depois)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF2F4F7)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Mapa do $countryCode (placeholder)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5A6572)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Integração do mapa virá depois",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7A8694)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ✅ Legenda “Pior → Melhor” com a MESMA cor em tons diferentes
        LegendPiorMelhor(baseColor = BrandBlue)
    }
}

@Composable
private fun LegendPiorMelhor(baseColor: Color) {
    val pior = baseColor.copy(alpha = 0.25f)
    val melhor = baseColor.copy(alpha = 1.0f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = "Legenda",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Pior",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5A6572)
                )

                Spacer(Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Brush.horizontalGradient(listOf(pior, melhor)))
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Melhor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5A6572)
                )
            }
        }
    }
}
