package com.example.rentscope.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rentscope.R
import com.example.rentscope.data.local.LanguageManager

data class LanguageOption(
    val code: String,
    val label: String
)

@Composable
fun LanguageScreen(
    padding: PaddingValues
) {
    val context = LocalContext.current
    val currentLanguage = LanguageManager.getSavedLanguage(context)

    var selectedLanguage by remember { mutableStateOf(currentLanguage) }

    val languages = listOf(
        LanguageOption("pt", "Português"),
        LanguageOption("en", "English")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.language_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.language_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        languages.forEach { language ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick = {
                    selectedLanguage = language.code
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = language.label,
                        style = MaterialTheme.typography.titleMedium
                    )

                    RadioButton(
                        selected = selectedLanguage == language.code,
                        onClick = {
                            selectedLanguage = language.code
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                val activity = context as? Activity ?: return@Button
                LanguageManager.setLanguage(activity, selectedLanguage)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.language_apply))
        }
    }
}