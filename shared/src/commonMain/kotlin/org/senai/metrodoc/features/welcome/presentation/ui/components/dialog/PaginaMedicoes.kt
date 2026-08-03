package org.senai.metrodoc.features.welcome.presentation.ui.components.dialog

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.senai.metrodoc.common.theme.defaultScrollbarStyle
import org.senai.metrodoc.common.ui.MetroDocAddButton
import org.senai.metrodoc.common.ui.MetroDocOutlinedButton
import org.senai.metrodoc.features.report.model.MeasurementData
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.welcome.presentation.ui.components.dialog.components.MeasurementTableRow

@Composable
fun PaginaMedicoes(
    reportData: ReportData,
    onMeasurementChanged: (index: Int, updated: MeasurementData) -> Unit,
    onAddMeasurement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (reportData.caracteristicas.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        )
        {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Nenhuma medição encontrada no relatório",
                    style = MaterialTheme.typography.headlineSmall,
                )
                MetroDocAddButton(
                    text = "Adicionar Medição Manualmente",
                    onClick = onAddMeasurement
                )
            }
        }
    } else {
        val state = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        Column(modifier = modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Medições (${reportData.caracteristicas.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                MetroDocOutlinedButton(
                    onClick = {
                        onAddMeasurement()
                        coroutineScope.launch {
                            state.animateScrollToItem(reportData.caracteristicas.size)
                        }
                    }
                ) {
                    Text(text = "+ Adicionar Medição")
                }
            }
            TableGridHeader()
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = state,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 8.dp),
                ) {
                    itemsIndexed(
                        items = reportData.caracteristicas,
                        key = { index, _ -> index }
                    ) { index, caracteristica ->
                        MeasurementTableRow(
                            measurement = caracteristica,
                            onMeasurementChanged = { updated ->
                                onMeasurementChanged(index, updated)
                            }
                        )
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(state),
                    style = defaultScrollbarStyle()
                )
            }
        }
    }
}

@Composable
fun TableGridHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Spacer(modifier = Modifier.width(28.dp))

        HeaderLabel("Nome da Característica", modifier = Modifier.weight(2.5f))
        HeaderLabel("Unid.", modifier = Modifier.weight(0.8f))
        HeaderLabel("Medido*", modifier = Modifier.weight(1.2f))
        HeaderLabel("Nominal", modifier = Modifier.weight(1.2f))
        HeaderLabel("Tol. Sup.", modifier = Modifier.weight(1.1f))
        HeaderLabel("Tol. Inf.", modifier = Modifier.weight(1.1f))
        HeaderLabel("Desvio*", modifier = Modifier.weight(1.2f))
    }
}

@Composable
private fun HeaderLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 16.sp,
        modifier = modifier
    )
}