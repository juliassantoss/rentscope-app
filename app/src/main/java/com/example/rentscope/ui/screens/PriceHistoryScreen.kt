package com.example.rentscope.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentscope.R
import com.example.rentscope.data.remote.dto.MunicipioDto
import com.example.rentscope.ui.components.PriceHistoryChart
import com.example.rentscope.ui.viewmodel.MunicipioViewModel
import com.example.rentscope.ui.viewmodel.PriceHistoryViewModel

@Composable
fun PriceHistoryScreen(
    padding: PaddingValues,
    vm: PriceHistoryViewModel = viewModel(),
    municipioVm: MunicipioViewModel = viewModel()
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedMunicipio by remember { mutableStateOf<MunicipioDto?>(null) }

    LaunchedEffect(Unit) {
        municipioVm.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.price_history),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box {
            OutlinedTextField(
                value = selectedMunicipio?.municipio_localidade ?: "",
                onValueChange = {},
                label = { Text(stringResource(R.string.search_municipality_label)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                municipioVm.municipios.forEach { municipio ->
                    DropdownMenuItem(
                        text = { Text(municipio.municipio_localidade) },
                        onClick = {
                            selectedMunicipio = municipio
                            expanded = false
                            vm.load(municipio.codigo_municipio)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            vm.loading -> {
                CircularProgressIndicator()
            }

            vm.error != null -> {
                Text("Erro ao carregar histórico")
            }

            selectedMunicipio == null -> {
                Text("Selecione um município")
            }

            vm.data.isEmpty() -> {
                Text("Sem dados disponíveis")
            }

            else -> {
                val chartData = vm.data
                    .sortedBy { it.trimestre }
                    .map { it.trimestre to it.valor_medio_m2 }

                PriceHistoryChart(data = chartData)
            }
        }
    }
}