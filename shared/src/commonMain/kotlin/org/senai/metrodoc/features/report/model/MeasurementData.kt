package org.senai.metrodoc.features.report.model

import java.util.*

data class MeasurementData(
    val hash: String = UUID.randomUUID().toString(),
    val nome: String = "",
    val valorMedido: String = "",
    val unidade: String = "mm",
    val valorNominal: String = "",
    val tolSuperior: String = "-",
    val tolInferior: String = "-",
    val desvio: String = "",
    val isForaTolerancia: Boolean = false,
    val incluidaRelatorio: Boolean = true,
) {
    companion object {
        fun defineUnidadePadrao(data: List<MeasurementData>) = data.groupBy { it.unidade }
            .maxByOrNull { it.value.size }
            ?.key ?: "mm"
    }
}