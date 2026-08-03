package org.senai.metrodoc.features.welcome.presentation.ui.components.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.common.ui.MetroDocTextField

@Composable
fun PaginaIdentificacao(
    reportData: ReportData,
    onDataChanged: (ReportData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val space = 8.dp
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(space)
    ) {
        MetroDocTextField(
            label = "Cliente / Projeto",
            value = reportData.cliente,
            onValueChange = { onDataChanged(reportData.copy(cliente = it)) }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space)
        ) {
            MetroDocTextField(
                label = "Componente avaliado",
                value = reportData.componente,
                onValueChange = { onDataChanged(reportData.copy(componente = it)) },
                modifier = Modifier.weight(1f)
            )
            MetroDocTextField(
                label = "Identificação no relatório CALYPSO",
                value = reportData.identificadorCalypso,
                onValueChange = { onDataChanged(reportData.copy(identificadorCalypso = it)) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space)
        ) {
            MetroDocTextField(
                label = "Máquina de medição",
                value = reportData.maquina,
                onValueChange = { onDataChanged(reportData.copy(maquina = it)) },
                modifier = Modifier.weight(1f)
            )
            MetroDocTextField(
                label = "Número da MMC",
                value = reportData.numeroMaquina,
                onValueChange = { onDataChanged(reportData.copy(numeroMaquina = it)) },
                modifier = Modifier.weight(1f)
            )
            MetroDocTextField(
                label = "Software",
                value = reportData.software,
                onValueChange = { onDataChanged(reportData.copy(software = it)) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space)
        ) {
            MetroDocTextField(
                label = "Operador",
                value = reportData.operador,
                onValueChange = { onDataChanged(reportData.copy(operador = it)) },
                modifier = Modifier.weight(1f)
            )
            MetroDocTextField(
                label = "Data/Hora da medição",
                value = reportData.dataHora,
                onValueChange = { onDataChanged(reportData.copy(dataHora = it)) },
                modifier = Modifier.weight(1f)
            )
            MetroDocTextField(
                label = "Quantidade de características",
                value = reportData.qtdCaracteristicas,
                onValueChange = { onDataChanged(reportData.copy(qtdCaracteristicas = it)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}