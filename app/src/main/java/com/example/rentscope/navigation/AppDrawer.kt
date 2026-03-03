package com.example.rentscope.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppDrawer(
    onClose: () -> Unit,
    onLoginClick: () -> Unit,
    onItemClick: (String) -> Unit
) {
    ModalDrawerSheet {

        // Header azul
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF3F8EDC),
                            Color(0xFF2A6EBB)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "RS",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Bem-vindo",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Explore o mundo com RentScope",
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ✅ O "por menor" que tu pediu:
                TextButton(onClick = onLoginClick) {
                    Text(
                        text = "Fazer login",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Itens
        NavigationDrawerItem(
            label = { Text("Favoritos") },
            selected = false,
            onClick = { onItemClick("favoritos") },
            icon = { Icon(Icons.Default.StarBorder, null) }
        )

        NavigationDrawerItem(
            label = { Text("Histórico de Preços") },
            selected = false,
            onClick = { onItemClick("historico") },
            icon = { Icon(Icons.Default.AccessTime, null) }
        )

        NavigationDrawerItem(
            label = { Text("Países Disponíveis") },
            selected = false,
            onClick = { onItemClick("paises") },
            icon = { Icon(Icons.Default.LocationOn, null) }
        )

        NavigationDrawerItem(
            label = { Text("Comparar Regiões") },
            selected = false,
            onClick = { onItemClick("comparar") },
            icon = { Icon(Icons.Default.TrendingUp, null) }
        )

        NavigationDrawerItem(
            label = { Text("Configurações") },
            selected = false,
            onClick = { onItemClick("config") },
            icon = { Icon(Icons.Default.Settings, null) }
        )

        Spacer(modifier = Modifier.weight(1f))

        Divider()

        Text(
            text = "Versão 1.0.0",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
