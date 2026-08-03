package org.senai.metrodoc.features.report.presentation

import androidx.compose.ui.graphics.ImageBitmap
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.ReportSection
import org.senai.metrodoc.features.report.presentation.ui.RightPanelTab

data class ReportCreatorState(
    val pdfPath: String = "",
    val pdfName: String = "",
    val pageCount: Int = 0,
    val currentPage: Int = 0,
    val currentPageImage: ImageBitmap? = null,
    val zoomFactor: Float = 1.0f,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val currentReport: ReportData? = null,
    val abaDireitaAtiva: RightPanelTab = RightPanelTab.PDF_ORIGINAL,
    val secoes: List<ReportSection> = emptyList(),
    val showBackDialog: Boolean = false,
    val secaoAtivaId: String? = null,
)

sealed interface ReportCreatorIntent {
    data class OnInit(val path: String, val name: String) : ReportCreatorIntent
    data class OnSectionChange(val sectionId: String) : ReportCreatorIntent
    data class OnTabChange(val tab: RightPanelTab) : ReportCreatorIntent
    data object OnNextPage : ReportCreatorIntent
    data object OnPreviousPage : ReportCreatorIntent
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
}

sealed interface ReportCreatorEffect {
    data object NavigateBack : ReportCreatorEffect
}