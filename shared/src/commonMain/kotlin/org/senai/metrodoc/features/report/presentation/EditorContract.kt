package org.senai.metrodoc.features.report.presentation

import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.ReportSection
import org.senai.metrodoc.features.report.presentation.ui.RightPanelTab

data class ReportCreatorState(
    val pdfPath: String = "",
    val pdfName: String = "",
    val pdf: Map<String, ByteArray> = mutableMapOf(),
    val zoomFactor: Float = 1.0f,
    val errorMessage: String? = null,
    val currentReport: ReportData? = null,
    val abaDireitaAtiva: RightPanelTab = RightPanelTab.PREVIEW,
    val secoes: List<ReportSection> = emptyList(),
    val showBackDialog: Boolean = false,
    val secaoAtivaId: String? = null,
    val isGeneratingPdf: Boolean = false,
    val previewPdfBytes: ByteArray? = null,
    val isGeneratingPreview: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReportCreatorState

        if (zoomFactor != other.zoomFactor) return false
        if (showBackDialog != other.showBackDialog) return false
        if (isGeneratingPdf != other.isGeneratingPdf) return false
        if (isGeneratingPreview != other.isGeneratingPreview) return false
        if (pdfPath != other.pdfPath) return false
        if (pdfName != other.pdfName) return false
        if (pdf != other.pdf) return false
        if (errorMessage != other.errorMessage) return false
        if (currentReport != other.currentReport) return false
        if (abaDireitaAtiva != other.abaDireitaAtiva) return false
        if (secoes != other.secoes) return false
        if (secaoAtivaId != other.secaoAtivaId) return false
        if (!previewPdfBytes.contentEquals(other.previewPdfBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = zoomFactor.hashCode()
        result = 31 * result + showBackDialog.hashCode()
        result = 31 * result + isGeneratingPdf.hashCode()
        result = 31 * result + isGeneratingPreview.hashCode()
        result = 31 * result + pdfPath.hashCode()
        result = 31 * result + pdfName.hashCode()
        result = 31 * result + pdf.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + (currentReport?.hashCode() ?: 0)
        result = 31 * result + abaDireitaAtiva.hashCode()
        result = 31 * result + secoes.hashCode()
        result = 31 * result + (secaoAtivaId?.hashCode() ?: 0)
        result = 31 * result + (previewPdfBytes?.contentHashCode() ?: 0)
        return result
    }
}

sealed interface ReportCreatorIntent {
    data class OnInit(val path: String, val name: String) : ReportCreatorIntent

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
    data class OnTabChange(val tab: RightPanelTab) : ReportCreatorIntent
    data class OnZoomChange(val newZoom: Float) : ReportCreatorIntent
    data object OnBackClicked : ReportCreatorIntent
    data object OnBackDismissed : ReportCreatorIntent
    data object OnBackConfirmed : ReportCreatorIntent

    data class OnUpdateSection(val updatedSection: ReportSection) : ReportCreatorIntent
    data class OnRemoveSection(val sectionId: String) : ReportCreatorIntent
    data class OnMoveSection(val fromIndex: Int, val toIndex: Int) : ReportCreatorIntent
    data class OnAddSection(val section: ReportSection) : ReportCreatorIntent
    data class OnAddMeasurement(val sectionId: String) : ReportCreatorIntent

    data class OnReportFieldChanged(val updatedData: ReportData) : ReportCreatorIntent

    data class OnGeneratePdf(val destinationPath: String) : ReportCreatorIntent
    data object OnCancelGeneration : ReportCreatorIntent
}

sealed interface ReportCreatorEffect {
    data object NavigateBack : ReportCreatorEffect
    data class OnPdfGenerated(val bytes: ByteArray) : ReportCreatorEffect {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OnPdfGenerated

            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }
}