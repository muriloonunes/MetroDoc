package org.senai.metrodoc.features.report.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.senai.metrodoc.features.report.model.ReportData

interface ReportRepository {
    val currentReport: StateFlow<ReportData?>
    fun setReport(data: ReportData)
    fun clearReport()
}

class InMemoryReportRepository: ReportRepository {
    private val _currentReport = MutableStateFlow<ReportData?>(null)
    override val currentReport: StateFlow<ReportData?> = _currentReport.asStateFlow()

    override fun setReport(data: ReportData) {
        _currentReport.value = data
    }

    override fun clearReport() {
        _currentReport.value = null
    }
}