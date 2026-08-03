package org.senai.metrodoc.common.mapper

import org.senai.metrodoc.common.database.entity.MeasurementDataEntity
import org.senai.metrodoc.common.database.entity.ReportDataEntity
import org.senai.metrodoc.features.report.model.MeasurementData
import org.senai.metrodoc.features.report.model.ReportData

fun ReportData.toEntity(): ReportDataEntity =
    ReportDataEntity(
        cliente = this.cliente,
        componente = this.componente,
        identificadorCalypso = this.identificadorCalypso,
        maquina = this.maquina,
        numeroMaquina = this.numeroMaquina,
        software = this.software,
        operador = this.operador,
        dataHora = this.dataHora,
        qtdCaracteristicas = this.qtdCaracteristicas,
    )

fun MeasurementData.toEntity(
    reportId: Long,
): MeasurementDataEntity =
    MeasurementDataEntity(
        reportId = reportId,
        nome = this.nome,
        valorMedido = this.valorMedido,
        unidade = this.unidade,
        valorNominal = this.valorNominal,
        tolSuperior = this.tolSuperior,
        tolInferior = this.tolInferior,
        desvio = this.desvio,
        isForaTolerancia = this.isForaTolerancia,
        incluidaRelatorio = this.incluidaRelatorio,
    )