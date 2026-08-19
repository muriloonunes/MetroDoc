package org.senai.metrodoc.features.welcome.presentation

import org.senai.metrodoc.common.database.dto.ProjectDto
import org.senai.metrodoc.features.report.model.MeasurementData
import org.senai.metrodoc.features.report.model.PdfItem
import org.senai.metrodoc.features.report.model.PdfItemStatus
import org.senai.metrodoc.features.report.model.ReportData

data class WelcomeViewState(
    val recentProjects: List<ProjectDto> = emptyList(),
    val isLoadingRecentProjects: Boolean = false,
    val pdfPath: String = "",
    val pdfName: String = "",
    val batchItems: List<PdfItem> = emptyList(),
    val isBatchMode: Boolean = false,
    val errorMessage: String? = null,
    val isProcessingPdf: Boolean = false,
    val processedPdfCount: Int = 0,
    val totalPdfCount: Int = 0,
    val reportData: ReportData? = null,
    val editedReportData: ReportData? = null,
    val showReportDialog: Boolean = false,
    val isGeneratingPdf: Boolean = false,
    val projectToDelete: ProjectDto? = null,
    val showProjectWithErrorsDialog: Boolean = false,
    val projectWithErrorsId: Long? = null,
    val showDeleteAllProjectsDialog: Boolean = false,
) {
    val isFormValid: Boolean
        get() = if (isBatchMode) {
            editedReportData?.let {
                it.cliente.isNotBlank() && it.componente.isNotBlank()
            } ?: false && batchItems.any { it.status != PdfItemStatus.ERROR }
        } else {
            editedReportData?.let {
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

    val pdfProcessingProgress: Float?
        get() = if (totalPdfCount > 0) processedPdfCount.toFloat() / totalPdfCount else null
}

sealed interface WelcomeScreenIntent {
    data object OnOpenFileButtonClicked : WelcomeScreenIntent
    data class OnFileSelected(
        val path: String,
        val name: String,
    ) : WelcomeScreenIntent

    data class OnMultipleFilesSelected(
        val files: List<Pair<String, String>>
    ) : WelcomeScreenIntent

    data class OnRemoveBatchItem(val id: String) : WelcomeScreenIntent

    data class OnReportFieldChanged(val updatedData: ReportData) : WelcomeScreenIntent

    data class OnMeasurementChanged(val index: Int, val updatedMeasurement: MeasurementData) : WelcomeScreenIntent
    data object OnAddMeasurement : WelcomeScreenIntent

    data class OnProjectSelected(val id: Long) : WelcomeScreenIntent
    data class OnDeleteProject(val id: Long) : WelcomeScreenIntent
    data object OnDeleteAllProjects : WelcomeScreenIntent

    data class OnRequestDeleteProject(val project: ProjectDto) : WelcomeScreenIntent
    data object OnConfirmDeleteProject : WelcomeScreenIntent
    data object OnDismissDeleteProjectDialog : WelcomeScreenIntent

    data object OnRequestDeleteAllProjects : WelcomeScreenIntent
    data object OnConfirmDeleteAllProjects : WelcomeScreenIntent
    data object OnDismissDeleteAllProjectsDialog : WelcomeScreenIntent

    data class OnRequestExportProject(val project: ProjectDto) : WelcomeScreenIntent
    data class OnGeneratePdf(val id: Long) : WelcomeScreenIntent
    data class OnGenerateBatchPdfs(val projectId: Long) : WelcomeScreenIntent
    data object OnCancelGeneration : WelcomeScreenIntent

    data object OnDismissProjectWithErrorDialog : WelcomeScreenIntent
    data object OnConfirmFixProjectWithError : WelcomeScreenIntent

    data object OnConfirmData : WelcomeScreenIntent
    data object OnDismissReportDialog : WelcomeScreenIntent
}

sealed interface WelcomeEffect {
    data object TriggerFilePicker : WelcomeEffect
    data class NavigateToRelatoryCreator(
        val reportId: Long?,
        val path: String,
        val pdfName: String,
    ) : WelcomeEffect

    data class TriggerSingleExportFileSaver(
        val projectId: Long,
        val suggestedName: String,
    ) : WelcomeEffect

    data class TriggerBatchExportDirectoryPicker(
        val projectId: Long,
    ) : WelcomeEffect

    data class OnPdfGenerated(val bytes: ByteArray) : WelcomeEffect {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OnPdfGenerated

            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }

    data class OnBatchPdfsGenerated(val files: List<Pair<String, ByteArray>>) : WelcomeEffect
}
