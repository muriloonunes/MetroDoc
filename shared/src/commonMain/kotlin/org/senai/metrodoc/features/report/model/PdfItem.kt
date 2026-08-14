package org.senai.metrodoc.features.report.model

import java.util.*

enum class PdfItemStatus {
    PARSED,
    ERROR,
}

data class PdfItem(
    val id: String = UUID.randomUUID().toString(),
    val pdfPath: String = "",
    val pdfName: String = "",
    val reportData: ReportData = ReportData(),
    val secoes: List<ReportSection> = emptyList(),
    val status: PdfItemStatus = PdfItemStatus.PARSED,
    val errorMessage: String? = null,
    val isTouched: Boolean = false,
) {
    val errors: List<SectionError>
        get() = secoes.flatMap { secao ->
            if (secao is ReportSection.Identificacao) {
                reportData.getErrors(secao.id, secao.titulo)
            } else secao.errors
        }

    val isValid: Boolean get() = errors.isEmpty()
}
