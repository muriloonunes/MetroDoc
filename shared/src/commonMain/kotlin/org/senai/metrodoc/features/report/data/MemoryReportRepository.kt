package org.senai.metrodoc.features.report.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.senai.metrodoc.features.report.model.PdfItem
import org.senai.metrodoc.features.report.model.ReportData

data class MemoryProjectData(
    val cliente: String = "",
    val componente: String = "",
    val pdfItems: List<PdfItem> = emptyList(),
)

interface MemoryReportRepository {
    val currentReport: StateFlow<ReportData?>
    val currentProjectData: StateFlow<MemoryProjectData?>
    fun setReport(data: ReportData)
    fun setProjectData(cliente: String, componente: String, pdfItems: List<PdfItem>)
    fun clearReport()
}

class InMemoryMemoryReportRepository: MemoryReportRepository {
    private val _currentReport = MutableStateFlow<ReportData?>(null)
    override val currentReport: StateFlow<ReportData?> = _currentReport.asStateFlow()

    private val _currentProjectData = MutableStateFlow<MemoryProjectData?>(null)
    override val currentProjectData: StateFlow<MemoryProjectData?> = _currentProjectData.asStateFlow()

    override fun setReport(data: ReportData) {
        _currentReport.value = data
    }

    override fun setProjectData(cliente: String, componente: String, pdfItems: List<PdfItem>) {
        _currentProjectData.value = MemoryProjectData(
            cliente = cliente,
            componente = componente,
            pdfItems = pdfItems
        )
    }

    override fun clearReport() {
        _currentReport.value = null
        _currentProjectData.value = null
    }
}