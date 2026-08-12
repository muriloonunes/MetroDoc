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

    fun getErrors(
        sectionId: String,
        sectionTitle: String,
    ): List<SectionError> {
        val list = mutableListOf<SectionError>()
        if (cliente.isBlank()) list.add(SectionError(sectionId, sectionTitle, "Cliente / Projeto", "O nome do cliente ou do projeto é obrigatório"))
        if (componente.isBlank()) list.add(SectionError(sectionId, sectionTitle, "Componente Avaliado", "O nome do componente é obrigatório"))
        if (identificadorCalypso.isBlank()) list.add(SectionError(sectionId, sectionTitle, "Identificação no relatório CALYPSO", "A identificação é obrigatória"))
        if (maquina.isBlank()) list.add(SectionError(sectionId, sectionTitle, "Máquina de medição", "O nome da máquina é obrigatório"))
        if (numeroMaquina.isBlank()) list.add(SectionError(sectionId, sectionTitle, "Número da MMC", "O número da máquina é obrigatório"))
        if (software.isBlank()) list.add(SectionError(sectionId, sectionTitle, "Software", "O nome do software é obrigatório"))
        if (operador.isBlank()) list.add(SectionError(sectionId, sectionTitle, "Operador", "O nome do operador é obrigatório"))
        if (dataHora.isBlank()) list.add(SectionError(sectionId, sectionTitle, "Data e Hora", "A data e hora são obrigatórias"))
        if (qtdCaracteristicas.isBlank()) list.add(SectionError(sectionId, sectionTitle, "Quantidade de Características", "A quantidade de características é obrigatória"))

        return list
    }
}
