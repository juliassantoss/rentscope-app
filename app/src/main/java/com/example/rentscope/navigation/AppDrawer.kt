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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentscope.R

@Composable
fun AppDrawer(
    isLoggedIn: Boolean,
    userEmail: String?,
    onClose: () -> Unit,
    onAuthClick: () -> Unit,
    onItemClick: (String) -> Unit
) {
    val drawerItemColors = NavigationDrawerItemDefaults.colors(
        unselectedContainerColor = Color.Transparent,
        unselectedIconColor = Color(0xFF334155),
        unselectedTextColor = Color(0xFF1F2937)
    )

    ModalDrawerSheet {
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
                .padding(18.dp)
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
                            contentDescription = stringResource(R.string.close),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isLoggedIn) {
                        stringResource(R.string.session_started)
                    } else {
                        stringResource(R.string.welcome)
                    },
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isLoggedIn && !userEmail.isNullOrBlank()) {
                        userEmail
                    } else {
                        stringResource(R.string.explore_rentscope)
                    },
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onAuthClick) {
                    Text(
                        text = if (isLoggedIn) {
                            stringResource(R.string.logout)
                        } else {
                            stringResource(R.string.login)
                        },
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.favorites)) },
            selected = false,
            onClick = { onItemClick(Routes.FAVORITES) },
            icon = { Icon(Icons.Default.StarBorder, contentDescription = null) },
            colors = drawerItemColors,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.search_history)) },
            selected = false,
            onClick = { onItemClick(Routes.HISTORY) },
            icon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
            colors = drawerItemColors,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.price_history)) },
            selected = false,
            onClick = { onItemClick(Routes.PRICE_HISTORY) },
            icon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null) },
            colors = drawerItemColors,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.ai_assistant_title)) },
            selected = false,
            onClick = { onItemClick(Routes.AI_ASSISTANT) },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
            colors = drawerItemColors,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider()

        Text(
            text = stringResource(R.string.version_label),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
