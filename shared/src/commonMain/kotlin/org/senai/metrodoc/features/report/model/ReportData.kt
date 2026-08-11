package org.senai.metrodoc.features.report.model

data class ReportData(
    val cliente: String = "",
    val componente: String = "",
    val identificadorCalypso: String = "",
    val maquina: String = "",
    val numeroMaquina: String = "",
    val software: String = "",
    val operador: String = "",
    val dataHora: String = "",
    val qtdCaracteristicas: String = "",
    val caracteristicas: List<MeasurementData> = emptyList(),
) {
    val isValid: Boolean
        get() {
            val campos = listOf(
                cliente, componente, identificadorCalypso, maquina,
                numeroMaquina, software, operador, dataHora, qtdCaracteristicas
            )

            return campos.all { it.isNotBlank() } && caracteristicas.isNotEmpty()
        }
}
