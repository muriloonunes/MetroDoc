package org.senai.metrodoc.features.report.presentation

import org.senai.metrodoc.features.report.model.*
import org.senai.metrodoc.features.report.presentation.ui.RightPanelTab

data class ReportCreatorState(
    val reportId: Long? = null,
    val isInitializing: Boolean = true,
    val pdfPath: String = "",
    val pdfName: String = "",
    val reportName: String = "",
    val cliente: String = "",
    val componente: String = "",
    val pdfItems: List<PdfItem> = emptyList(),
    val activePdfIndex: Int = 0,
    val pdf: Map<String, ByteArray> = mutableMapOf(),
    val zoomFactor: Float = 1.0f,
    val errorMessage: String? = null,
    val currentReport: ReportData? = null,
    val abaDireitaAtiva: RightPanelTab? = RightPanelTab.PREVIEW,
    val secoes: List<ReportSection> = emptyList(),
    val secoesAbertas: Set<String> = emptySet(),
    val showBackDialog: Boolean = false,
    val secaoAtivaId: String? = null,
    val isGeneratingPdf: Boolean = false,
    val processedPdfCount: Int = 0,
    val previewPdfBytes: ByteArray? = null,
    val isGeneratingPreview: Boolean = false,
    val reportSaveState: SavedState = SavedState.Unsaved,
    val showEditDialog: Boolean = false,
    val editingImage: Imagem? = null,
    val versions: List<ProjectVersion> = emptyList(),
) {
    val activePdfItem: PdfItem?
        get() = pdfItems.getOrNull(activePdfIndex)

    val sectionErrors: List<SectionError>
        get() {
            val item = activePdfItem
            return item?.errors
                ?: secoes.flatMap { secao ->
                    if (secao is ReportSection.Identificacao) {
                        currentReport?.getErrors(secao.id, secao.titulo) ?: emptyList()
                    } else secao.errors
                }
        }

    val canExport: Boolean
        get() {
            return if (pdfItems.isNotEmpty()) {
                pdfItems.all { it.isValid }
            } else {
                val reportValido = currentReport?.isValid ?: false
                val secoesValidas = secoes.all { it.isValid }
                reportValido && secoesValidas
            }
        }

    val canExportActive: Boolean
        get() {
            return activePdfItem?.isValid ?: canExport
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReportCreatorState

        if (reportId != other.reportId) return false
        if (isInitializing != other.isInitializing) return false
        if (activePdfIndex != other.activePdfIndex) return false
        if (zoomFactor != other.zoomFactor) return false
        if (showBackDialog != other.showBackDialog) return false
        if (isGeneratingPdf != other.isGeneratingPdf) return false
        if (processedPdfCount != other.processedPdfCount) return false
        if (isGeneratingPreview != other.isGeneratingPreview) return false
        if (showEditDialog != other.showEditDialog) return false
        if (pdfPath != other.pdfPath) return false
        if (pdfName != other.pdfName) return false
        if (reportName != other.reportName) return false
        if (cliente != other.cliente) return false
        if (componente != other.componente) return false
        if (pdfItems != other.pdfItems) return false
        if (pdf != other.pdf) return false
        if (errorMessage != other.errorMessage) return false
        if (currentReport != other.currentReport) return false
        if (abaDireitaAtiva != other.abaDireitaAtiva) return false
        if (secoes != other.secoes) return false
        if (secoesAbertas != other.secoesAbertas) return false
        if (secaoAtivaId != other.secaoAtivaId) return false
        if (!previewPdfBytes.contentEquals(other.previewPdfBytes)) return false
        if (reportSaveState != other.reportSaveState) return false
        if (editingImage != other.editingImage) return false
        if (versions != other.versions) return false
        if (canExport != other.canExport) return false
        if (canExportActive != other.canExportActive) return false
        if (activePdfItem != other.activePdfItem) return false
        if (sectionErrors != other.sectionErrors) return false

        return true
    }

    override fun hashCode(): Int {
        var result = reportId.hashCode()
        result = 31 * result + isInitializing.hashCode()
        result = 31 * result + activePdfIndex
        result = 31 * result + zoomFactor.hashCode()
        result = 31 * result + showBackDialog.hashCode()
        result = 31 * result + isGeneratingPdf.hashCode()
        result = 31 * result + processedPdfCount
        result = 31 * result + isGeneratingPreview.hashCode()
        result = 31 * result + showEditDialog.hashCode()
        result = 31 * result + pdfPath.hashCode()
        result = 31 * result + pdfName.hashCode()
        result = 31 * result + reportName.hashCode()
        result = 31 * result + cliente.hashCode()
        result = 31 * result + componente.hashCode()
        result = 31 * result + pdfItems.hashCode()
        result = 31 * result + pdf.hashCode()
        result = 31 * result + errorMessage.hashCode()
        result = 31 * result + currentReport.hashCode()
        result = 31 * result + abaDireitaAtiva.hashCode()
        result = 31 * result + secoes.hashCode()
        result = 31 * result + secoesAbertas.hashCode()
        result = 31 * result + secaoAtivaId.hashCode()
        result = 31 * result + (previewPdfBytes?.contentHashCode() ?: 0)
        result = 31 * result + reportSaveState.hashCode()
        result = 31 * result + editingImage.hashCode()
        result = 31 * result + versions.hashCode()
        result = 31 * result + canExport.hashCode()
        result = 31 * result + canExportActive.hashCode()
        result = 31 * result + activePdfItem.hashCode()
        result = 31 * result + sectionErrors.hashCode()
        return result
    }
}

sealed interface ReportCreatorIntent {
    val contentChanged: Boolean get() = false

    data class OnInit(val reportId: Long?, val pdfPath: String, val pdfName: String) : ReportCreatorIntent
    data class OnSelectPdfItem(val index: Int) : ReportCreatorIntent

    data class OnPdfLoaded(val pdfPath: String, val bytes: ByteArray) : ReportCreatorIntent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OnPdfLoaded

            if (pdfPath != other.pdfPath) return false
            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = pdfPath.hashCode()
            result = 31 * result + bytes.contentHashCode()
            return result
        }
    }

    data class OnSectionChange(val sectionId: String) : ReportCreatorIntent
    data class OnTabChange(val tab: RightPanelTab?) : ReportCreatorIntent
    data class OnZoomChange(val newZoom: Float) : ReportCreatorIntent

    data object OnBackClicked : ReportCreatorIntent
    data object OnBackDismissed : ReportCreatorIntent
    data object OnBackConfirmed : ReportCreatorIntent

    data class OnUpdateSection(val updatedSection: ReportSection) : ReportCreatorIntent {
        override val contentChanged: Boolean get() = true
    }

    data class OnRemoveSection(val sectionId: String) : ReportCreatorIntent {
        override val contentChanged: Boolean get() = true
    }

    data class OnMoveSection(val fromIndex: Int, val toIndex: Int) : ReportCreatorIntent {
        override val contentChanged: Boolean get() = true
    }

    data class OnAddSection(val section: ReportSection) : ReportCreatorIntent {
        override val contentChanged: Boolean get() = true
    }

    data class OnAddMeasurement(val sectionId: String) : ReportCreatorIntent {
        override val contentChanged: Boolean get() = true
    }

    data class OnReportNameChanged(val newName: String) : ReportCreatorIntent {
        override val contentChanged: Boolean get() = true
    }

    data class OnReportFieldChanged(val updatedData: ReportData) : ReportCreatorIntent {
        override val contentChanged: Boolean get() = true
    }

    data class OnEditImageClicked(val imagem: Imagem) : ReportCreatorIntent
    data object OnEditImageDismissed : ReportCreatorIntent
    data class OnEditImageConfirmed(val updatedImagem: Imagem) : ReportCreatorIntent {
        override val contentChanged: Boolean get() = true
    }

    data class OnRestoreVersion(val versionId: Long) : ReportCreatorIntent
    data class OnDeleteVersion(val versionId: Long) : ReportCreatorIntent
    data class OnRenameVersion(val versionId: Long, val newName: String) : ReportCreatorIntent

    data object OnSaveProject : ReportCreatorIntent

    data class OnGeneratePdf(val destinationPath: String) : ReportCreatorIntent
    data object OnGenerateAllPdfs : ReportCreatorIntent
    data object OnCancelGeneration : ReportCreatorIntent
}

sealed interface ReportCreatorEffect {
    data object NavigateBack : ReportCreatorEffect
    data class OnPdfGenerated(val bytes: ByteArray) : ReportCreatorEffect {
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

    data class OnBatchPdfsGenerated(val files: List<Pair<String, ByteArray>>) : ReportCreatorEffect
}