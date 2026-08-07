package org.senai.metrodoc.features.welcome.presentation

import org.senai.metrodoc.common.database.dto.ProjectDto
import org.senai.metrodoc.features.report.model.MeasurementData
import org.senai.metrodoc.features.report.model.ReportData

data class WelcomeViewState(
    val recentProjects: List<ProjectDto> = emptyList(),
    val isLoadingRecentProjects: Boolean = false,
    val pdfPath: String = "",
    val pdfName: String = "",
    val errorMessage: String? = null,
    val isProcessingPdf: Boolean = false,
    val reportData: ReportData? = null,
    val editedReportData: ReportData? = null,
    val showReportDialog: Boolean = false,
) {
    val isFormValid: Boolean
        get() = editedReportData?.let {
            it.cliente.isNotBlank() &&
                    it.componente.isNotBlank() &&
                    it.identificadorCalypso.isNotBlank() &&
                    it.maquina.isNotBlank() &&
                    it.numeroMaquina.isNotBlank() &&
                    it.operador.isNotBlank() &&
                    it.dataHora.isNotBlank() &&
                    it.qtdCaracteristicas.isNotBlank() &&
                    it.software.isNotBlank()
        } ?: false
}

sealed interface WelcomeScreenIntent {
    data object OnOpenFileButtonClicked : WelcomeScreenIntent
    data class OnFileSelected(
        val path: String,
        val name: String,
    ) : WelcomeScreenIntent

    data class OnReportFieldChanged(val updatedData: ReportData) : WelcomeScreenIntent

    data class OnMeasurementChanged(val index: Int, val updatedMeasurement: MeasurementData) : WelcomeScreenIntent
    data object OnAddMeasurement : WelcomeScreenIntent

    data class OnProjectSelected(val id: Long) : WelcomeScreenIntent
    data object OnConfirmData: WelcomeScreenIntent
    data object OnDismissReportDialog : WelcomeScreenIntent
}

sealed interface WelcomeEffect {
    data object TriggerFilePicker : WelcomeEffect
    data class NavigateToRelatoryCreator(
        val reportId: Long?,
        val path: String,
        val pdfName: String
    ) : WelcomeEffect
}
