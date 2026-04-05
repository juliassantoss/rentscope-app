package com.example.rentscope.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentscope.R
import com.example.rentscope.ui.viewmodel.PaisesViewModel

@Composable
fun DebugPaisesScreen(
    vm: PaisesViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.carregarPaises()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.debug_countries_title), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        Text(stringResource(R.string.received_items, state.paises.size))

        Spacer(Modifier.height(12.dp))

        if (state.loading) {
            Row {
                CircularProgressIndicator()
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.loading))
            }
        }

        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.error_with_message, err))
            Spacer(Modifier.height(12.dp))
            Button(onClick = { vm.carregarPaises() }) {
                Text(stringResource(R.string.try_again))
            }
        }

        Spacer(Modifier.height(12.dp))

        if (!state.loading && state.error == null && state.paises.isEmpty()) {
            Text(stringResource(R.string.no_countries_returned))
        } else {
            LazyColumn {
                items(state.paises) { p ->
                    Text("${p.nome} (${p.codigo})", modifier = Modifier.padding(vertical = 6.dp))
                }
            }
        }
    }
}