package org.senai.metrodoc.common.mapper

import kotlinx.serialization.json.Json
import org.senai.metrodoc.common.database.dto.VersionDto
import org.senai.metrodoc.common.database.entity.MeasurementDataEntity
import org.senai.metrodoc.common.database.entity.ReportDataEntity
import org.senai.metrodoc.features.report.model.MeasurementData
import org.senai.metrodoc.features.report.model.ProjectVersion
import org.senai.metrodoc.features.report.model.ReportData

val metroDocJson = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "type"
}

fun ReportData.toEntity(
    id: Long = 0,
    projectId: Long = 0,
    nomeRelatorio: String,
    pdfName: String,
    pdfPath: String,
    lastModified: Long,
    secoesJson: String
): ReportDataEntity =
    ReportDataEntity(
        id = id,
        projectId = projectId,
        nomeRelatorio = nomeRelatorio,
        pdfName = pdfName,
        pdfPath = pdfPath,
        lastModified = lastModified,
        secoesJson = secoesJson,
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

fun ReportDataEntity.toDomain(
    measurementData: List<MeasurementData>
): ReportData =
    ReportData(
        cliente = this.cliente,
        componente = this.componente,
        identificadorCalypso = this.identificadorCalypso,
        maquina = this.maquina,
        numeroMaquina = this.numeroMaquina,
        software = this.software,
        operador = this.operador,
        dataHora = this.dataHora,
        qtdCaracteristicas = this.qtdCaracteristicas,
        caracteristicas = measurementData
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

fun MeasurementDataEntity.toDomain(): MeasurementData =
    MeasurementData(
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

fun VersionDto.toDomain(): ProjectVersion = ProjectVersion(
    id = this.id,
    nomeVersao = this.versionName,
    modificadoEm = this.createdAt
)