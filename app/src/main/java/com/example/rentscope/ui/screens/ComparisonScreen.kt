package com.example.rentscope.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentscope.R
import com.example.rentscope.data.local.LastSearchData
import com.example.rentscope.data.local.LastSearchManager
import com.example.rentscope.data.remote.dto.score.ScoreMunicipioDto
import com.example.rentscope.ui.components.SkeletonBlock
import com.example.rentscope.ui.viewmodel.ScoreViewModel
import java.util.Locale

/**
 * Renders a side-by-side locality comparison using the current search context.
 *
 * @param padding Insets provided by the app scaffold.
 * @param vm View model that exposes the comparison dataset.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(
    padding: PaddingValues,
    vm: ScoreViewModel = viewModel()
) {
    val lastSearch = LastSearchManager.get()

    var leftExpanded by remember { mutableStateOf(false) }
    var rightExpanded by remember { mutableStateOf(false) }
    var leftQuery by rememberSaveable { mutableStateOf("") }
    var rightQuery by rememberSaveable { mutableStateOf("") }
    var leftSelected by remember { mutableStateOf<ScoreMunicipioDto?>(null) }
    var rightSelected by remember { mutableStateOf<ScoreMunicipioDto?>(null) }

    val municipios = remember(vm.municipios) {
        vm.municipios.sortedWith(
            compareByDescending<ScoreMunicipioDto> { it.score }
                .thenBy { it.municipioLocalidade }
        )
    }

    val leftOptions = remember(municipios, leftQuery, rightSelected?.codigoMunicipio) {
        municipios
            .asSequence()
            .filter { it.codigoMunicipio != rightSelected?.codigoMunicipio }
            .filter {
                leftQuery.isBlank() ||
                    it.municipioLocalidade.contains(leftQuery, ignoreCase = true)
            }
            .take(40)
            .toList()
    }

    val rightOptions = remember(municipios, rightQuery, leftSelected?.codigoMunicipio) {
        municipios
            .asSequence()
            .filter { it.codigoMunicipio != leftSelected?.codigoMunicipio }
            .filter {
                rightQuery.isBlank() ||
                    it.municipioLocalidade.contains(rightQuery, ignoreCase = true)
            }
            .take(40)
            .toList()
    }

    LaunchedEffect(
        lastSearch?.countryCode,
        lastSearch?.rendaMin,
        lastSearch?.rendaMax,
        lastSearch?.pesoRenda,
        lastSearch?.pesoEscolas,
        lastSearch?.pesoHospitais,
        lastSearch?.pesoCriminalidade
    ) {
        if (lastSearch != null && lastSearch.countryCode == "PT") {
            vm.carregarScores(
                rendaMin = lastSearch.rendaMin,
                rendaMax = lastSearch.rendaMax,
                pesoRenda = lastSearch.pesoRenda,
                pesoEscolas = lastSearch.pesoEscolas,
                pesoHospitais = lastSearch.pesoHospitais,
                pesoCriminalidade = lastSearch.pesoCriminalidade,
                limite = 400
            )
        }
    }

    LaunchedEffect(municipios) {
        if (municipios.isEmpty()) {
            leftSelected = null
            rightSelected = null
            return@LaunchedEffect
        }

        val preservedLeft = municipios.firstOrNull {
            it.codigoMunicipio == leftSelected?.codigoMunicipio
        } ?: municipios.firstOrNull()

        val preservedRight = municipios.firstOrNull {
            it.codigoMunicipio == rightSelected?.codigoMunicipio &&
                it.codigoMunicipio != preservedLeft?.codigoMunicipio
        } ?: municipios.firstOrNull {
            it.codigoMunicipio != preservedLeft?.codigoMunicipio
        }

        leftSelected = preservedLeft
        rightSelected = preservedRight
    }

    LaunchedEffect(leftSelected?.codigoMunicipio) {
        leftQuery = leftSelected?.municipioLocalidade.orEmpty()
    }

    LaunchedEffect(rightSelected?.codigoMunicipio) {
        rightQuery = rightSelected?.municipioLocalidade.orEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = stringResource(R.string.comparison_title),
            subtitle = stringResource(R.string.comparison_subtitle),
            icon = Icons.AutoMirrored.Filled.CompareArrows
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoBannerCard(
            title = stringResource(R.string.comparison_guide_title),
            message = stringResource(R.string.comparison_intro_body),
            icon = Icons.AutoMirrored.Filled.CompareArrows
        )

        Spacer(modifier = Modifier.height(14.dp))

        when {
            lastSearch == null -> {
                EmptyStateCard(
                    title = stringResource(R.string.comparison_no_search_title),
                    message = stringResource(R.string.comparison_no_search_message)
                )
            }

            lastSearch.countryCode != "PT" -> {
                EmptyStateCard(
                    title = stringResource(R.string.no_data_title),
                    message = stringResource(R.string.comparison_portugal_only)
                )
            }

            vm.isLoading -> {
                LoadingStateCard(
                    title = stringResource(R.string.comparison_loading_title),
                    message = stringResource(R.string.comparison_loading_message)
                )

                Spacer(modifier = Modifier.height(12.dp))

                ComparisonLoadingContent()
            }

            !vm.errorMessage.isNullOrBlank() -> {
                EmptyStateCard(
                    title = stringResource(R.string.error_loading_results_title),
                    message = vm.errorMessage ?: stringResource(R.string.unexpected_error)
                )
            }

            municipios.size < 2 -> {
                EmptyStateCard(
                    title = stringResource(R.string.no_data_title),
                    message = stringResource(R.string.comparison_not_enough_message)
                )
            }

            leftSelected == null || rightSelected == null -> {
                EmptyStateCard(
                    title = stringResource(R.string.no_data_title),
                    message = stringResource(R.string.comparison_not_enough_message)
                )
            }

            else -> {
                ComparisonBasisCard(data = lastSearch)

                Spacer(modifier = Modifier.height(14.dp))

                ComparisonSelectorsCard(
                    leftLabel = stringResource(R.string.comparison_select_left),
                    rightLabel = stringResource(R.string.comparison_select_right),
                    leftQuery = leftQuery,
                    onLeftQueryChange = {
                        leftQuery = it
                        leftExpanded = true
                    },
                    rightQuery = rightQuery,
                    onRightQueryChange = {
                        rightQuery = it
                        rightExpanded = true
                    },
                    leftExpanded = leftExpanded,
                    onLeftExpandedChange = { leftExpanded = it },
                    rightExpanded = rightExpanded,
                    onRightExpandedChange = { rightExpanded = it },
                    leftOptions = leftOptions,
                    rightOptions = rightOptions,
                    onLeftSelect = { municipio ->
                        leftSelected = municipio
                        leftExpanded = false
                    },
                    onRightSelect = { municipio ->
                        rightSelected = municipio
                        rightExpanded = false
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                ComparisonCardsLayout(
                    leftItem = leftSelected!!,
                    rightItem = rightSelected!!
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ComparisonLoadingContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(2) {
            SectionCard {
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.42f)
                        .height(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )
            }
        }
    }
}

@Composable
private fun ComparisonBasisCard(data: LastSearchData) {
    SectionCard {
        Text(
            text = stringResource(R.string.last_search_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.country_label, data.countryName),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        MetricChip(
            label = stringResource(R.string.rent_label_short),
            value = "${formatNullable(data.rendaMin)} - ${formatNullable(data.rendaMax)} / 20",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(
                R.string.weights_summary_string,
                format1(data.pesoRenda),
                format1(data.pesoEscolas),
                format1(data.pesoHospitais),
                format1(data.pesoCriminalidade)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComparisonSelectorsCard(
    leftLabel: String,
    rightLabel: String,
    leftQuery: String,
    onLeftQueryChange: (String) -> Unit,
    rightQuery: String,
    onRightQueryChange: (String) -> Unit,
    leftExpanded: Boolean,
    onLeftExpandedChange: (Boolean) -> Unit,
    rightExpanded: Boolean,
    onRightExpandedChange: (Boolean) -> Unit,
    leftOptions: List<ScoreMunicipioDto>,
    rightOptions: List<ScoreMunicipioDto>,
    onLeftSelect: (ScoreMunicipioDto) -> Unit,
    onRightSelect: (ScoreMunicipioDto) -> Unit
) {
    SectionCard {
        Text(
            text = stringResource(R.string.comparison_selectors_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.comparison_selectors_message),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        LocalitySelectorField(
            label = leftLabel,
            query = leftQuery,
            onQueryChange = onLeftQueryChange,
            expanded = leftExpanded,
            onExpandedChange = onLeftExpandedChange,
            options = leftOptions,
            onSelect = onLeftSelect
        )

        Spacer(modifier = Modifier.height(12.dp))

        LocalitySelectorField(
            label = rightLabel,
            query = rightQuery,
            onQueryChange = onRightQueryChange,
            expanded = rightExpanded,
            onExpandedChange = onRightExpandedChange,
            options = rightOptions,
            onSelect = onRightSelect
        )
    }
}

@Composable
private fun ComparisonCardsLayout(
    leftItem: ScoreMunicipioDto,
    rightItem: ScoreMunicipioDto
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compactLayout = maxWidth < 600.dp

        if (compactLayout) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ComparisonLocalityCard(
                    title = stringResource(R.string.comparison_select_left),
                    item = leftItem,
                    modifier = Modifier.fillMaxWidth()
                )
                ComparisonLocalityCard(
                    title = stringResource(R.string.comparison_select_right),
                    item = rightItem,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ComparisonLocalityCard(
                    title = stringResource(R.string.comparison_select_left),
                    item = leftItem,
                    modifier = Modifier.weight(1f)
                )
                ComparisonLocalityCard(
                    title = stringResource(R.string.comparison_select_right),
                    item = rightItem,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalitySelectorField(
    label: String,
    query: String,
    onQueryChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<ScoreMunicipioDto>,
    onSelect: (ScoreMunicipioDto) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(stringResource(R.string.comparison_search_placeholder)) },
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.comparison_no_matches)) },
                    onClick = { onExpandedChange(false) }
                )
            } else {
                options.forEach { municipio ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = municipio.municipioLocalidade,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = buildAreaSummary(municipio),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        onClick = { onSelect(municipio) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonLocalityCard(
    title: String,
    item: ScoreMunicipioDto,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.municipioLocalidade,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = buildAreaSummary(item),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        MetricChip(
            label = stringResource(R.string.comparison_score_title),
            value = format1(item.score),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        MetricChip(
            label = stringResource(R.string.rent_label_short),
            value = formatPricePerSquareMeter(item.valorMedioM2),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        MetricChip(
            label = stringResource(R.string.schools_label_short),
            value = item.totalEscolas.toString(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        MetricChip(
            label = stringResource(R.string.hospitals_label_short),
            value = item.totalHospitais.toString(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        MetricChip(
            label = stringResource(R.string.crime_label_short),
            value = item.totalCrimes.toString(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun buildAreaSummary(item: ScoreMunicipioDto): String {
    val parts = listOfNotNull(
        item.regiao?.takeIf { it.isNotBlank() },
        item.grandeRegiao?.takeIf { it.isNotBlank() }
    )
    return if (parts.isEmpty()) "-" else parts.joinToString(" / ")
}

private fun format1(value: Float): String = String.format(Locale.getDefault(), "%.1f", value)

private fun formatNullable(value: Float?): String = value?.let { format1(it) } ?: "-"

private fun formatPricePerSquareMeter(value: Float?): String {
    return value?.let { String.format(Locale.getDefault(), "%.1f €/m²", it) } ?: "-"
}
