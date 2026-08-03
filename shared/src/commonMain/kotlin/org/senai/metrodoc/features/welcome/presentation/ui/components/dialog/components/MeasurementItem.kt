package org.senai.metrodoc.features.welcome.presentation.ui.components.dialog.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.senai.metrodoc.common.ui.MetroDocTextField
import org.senai.metrodoc.features.report.model.MeasurementData

@Composable
fun MeasurementTableRow(
    measurement: MeasurementData,
    onMeasurementChanged: (MeasurementData) -> Unit,
) {
    val isEnabled = measurement.incluidaRelatorio

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = if (isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLowest.copy(
            alpha = 0.5f
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Checkbox(
                checked = measurement.incluidaRelatorio,
                onCheckedChange = { checked ->
                    onMeasurementChanged(measurement.copy(incluidaRelatorio = checked))
                },
                modifier = Modifier.size(28.dp),
            )

            MetroDocTextField(
                label = "",
                value = measurement.nome,
                onValueChange = { onMeasurementChanged(measurement.copy(nome = it)) },
                isRequired = true,
                enabled = isEnabled,
                modifier = Modifier.weight(2.5f)
            )

            MetroDocTextField(
                label = "",
                value = measurement.unidade,
                onValueChange = { onMeasurementChanged(measurement.copy(unidade = it)) },
                enabled = isEnabled,
                modifier = Modifier.weight(0.8f)
            )

            MetroDocTextField(
                label = "",
                value = measurement.valorMedido,
                onValueChange = { onMeasurementChanged(measurement.copy(valorMedido = it)) },
                isRequired = true,
                enabled = isEnabled,
                modifier = Modifier.weight(1.2f)
            )

            MetroDocTextField(
                label = "",
                value = measurement.valorNominal,
                onValueChange = { onMeasurementChanged(measurement.copy(valorNominal = it)) },
                enabled = isEnabled,
                modifier = Modifier.weight(1.2f)
            )

            MetroDocTextField(
                label = "",
                value = measurement.tolSuperior,
                onValueChange = { onMeasurementChanged(measurement.copy(tolSuperior = it)) },
                enabled = isEnabled,
                isRequired = false,
                modifier = Modifier.weight(1.1f)
            )

            MetroDocTextField(
                label = "",
                value = measurement.tolInferior,
                onValueChange = { onMeasurementChanged(measurement.copy(tolInferior = it)) },
                enabled = isEnabled,
                isRequired = false,
                modifier = Modifier.weight(1.1f)
            )

            MetroDocTextField(
                label = "",
                value = measurement.desvio,
                onValueChange = { onMeasurementChanged(measurement.copy(desvio = it)) },
                isRequired = true,
                enabled = isEnabled,
                modifier = Modifier.weight(1.2f)
            )
        }
    }
}