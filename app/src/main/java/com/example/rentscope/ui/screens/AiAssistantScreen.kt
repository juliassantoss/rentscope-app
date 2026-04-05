package com.example.rentscope.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentscope.data.remote.dto.ai.AiQuestionRequest
import com.example.rentscope.ui.components.MascotOrb
import com.example.rentscope.ui.viewmodel.AiViewModel

@Composable
fun AiAssistantScreen(
    padding: PaddingValues,
    aiViewModel: AiViewModel = viewModel()
) {
    var pergunta by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Assistente IA",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        MascotOrb(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            orbSize = 180.dp,
            state = aiViewModel.mascotState
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pergunta,
            onValueChange = { pergunta = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Faça uma pergunta") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                aiViewModel.perguntar(
                    AiQuestionRequest(
                        pais = "Portugal",
                        municipio = "Aveiro",
                        pergunta = pergunta
                    )
                )
            },
            enabled = pergunta.isNotBlank() && !aiViewModel.loading
        ) {
            Text("Perguntar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            aiViewModel.loading -> {
                CircularProgressIndicator()
            }

            aiViewModel.error != null -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Erro: ${aiViewModel.error}",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            aiViewModel.resposta.isNotBlank() -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = aiViewModel.resposta,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}