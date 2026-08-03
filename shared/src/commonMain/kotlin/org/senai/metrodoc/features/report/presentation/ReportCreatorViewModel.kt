package org.senai.metrodoc.features.report.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.senai.metrodoc.common.util.PdfGenerator
import org.senai.metrodoc.features.report.data.ReportRepository
import org.senai.metrodoc.features.report.model.MeasurementData
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.ReportSection
import org.senai.metrodoc.features.report.util.PdfRenderEngine

class ReportCreatorViewModel(
    private val renderEngine: PdfRenderEngine,
    private val reportRepository: ReportRepository,
    private val pdfGenerator: PdfGenerator,
) : ViewModel() {
    private val _state = MutableStateFlow(ReportCreatorState())
    val state = _state.asStateFlow()

    private val _effect = Channel<ReportCreatorEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var renderJob: Job? = null

    init {
        val currentReport = reportRepository.currentReport.value
        val initialSections = listOf(
            ReportSection.Introducao(),
            ReportSection.Identificacao(reportData = currentReport ?: ReportData()),
            ReportSection.ResultadosDimensionais(measurements = currentReport?.caracteristicas ?: emptyList())
        )

        _state.update {
            it.copy(
                currentReport = currentReport,
                secoes = initialSections
            )
        }
    }


    private fun loadPdf(path: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { renderEngine.loadPdf("") }
                .onSuccess { pages ->
                    _state.update { it.copy(pageCount = pages, currentPage = 0) }
                    renderPage(0)
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Erro ao carregar PDF")
                    }
                }
        }
    }

    fun renderPage(pageIndex: Int) {
        val pageCount = _state.value.pageCount
        if (pageIndex !in 0 until pageCount) return

        renderJob?.cancel()
        renderJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { renderEngine.renderPage(pageIndex) }
                .onSuccess { bitmap ->
                    _state.update {
                        it.copy(
                            currentPage = pageIndex,
                            currentPageImage = bitmap,
                            isLoading = false,
                            errorMessage = if (bitmap == null) "Falha ao renderizar página" else null
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Erro ao renderizar página")
                    }
                }
        }
    }

    fun handleIntent(intent: ReportCreatorIntent) {
        when (intent) {
            is ReportCreatorIntent.OnInit -> {
                _state.update {
                    it.copy(
                        pdfPath = intent.path,
                        pdfName = intent.name,
                        secaoAtivaId = it.secoes.first().id,
                    )
                }
                if (intent.path.isNotBlank()) {
                    loadPdf(intent.path)
                }
            }

            is ReportCreatorIntent.OnPdfLoaded -> {
                _state.update {
                    val pdf = it.pdf
                    it.copy(
                        pdf = pdf.plus(intent.pdfPath to intent.bytes),
                    )
                }
            }

            is ReportCreatorIntent.OnSectionChange -> {
                _state.update { it.copy(secaoAtivaId = intent.sectionId) }
            }

            is ReportCreatorIntent.OnTabChange -> {
                _state.update { it.copy(abaDireitaAtiva = intent.tab) }
            }

            ReportCreatorIntent.OnNextPage -> {
                val currentState = _state.value
                if (currentState.currentPage < currentState.pageCount - 1) {
                    renderPage(currentState.currentPage + 1)
                }
            }

            ReportCreatorIntent.OnPreviousPage -> {
                val currentState = _state.value
                if (currentState.currentPage > 0) {
                    renderPage(currentState.currentPage - 1)
                }
            }

            is ReportCreatorIntent.OnZoomChange -> {
                _state.update { it.copy(zoomFactor = intent.newZoom) }
            }

            ReportCreatorIntent.OnBackClicked -> {
                _state.update { it.copy(showBackDialog = true) }
            }

            ReportCreatorIntent.OnBackDismissed -> {
                _state.update { it.copy(showBackDialog = false) }
            }

            ReportCreatorIntent.OnBackConfirmed -> {
                _state.update { it.copy(showBackDialog = false) }
                reportRepository.clearReport()
                viewModelScope.launch {
                    _effect.send(ReportCreatorEffect.NavigateBack)
                }
            }

            is ReportCreatorIntent.OnUpdateSection -> {
                _state.update { currentState ->
                    val updatedList = currentState.secoes.map { section ->
                        if (section.id == intent.updatedSection.id) intent.updatedSection else section
                    }
                    currentState.copy(secoes = updatedList)
                }
            }

            is ReportCreatorIntent.OnRemoveSection -> {
                _state.update { currentState ->
                    val updatedList = currentState.secoes.filterNot { it.id == intent.sectionId }
                    currentState.copy(secoes = updatedList)
                }
            }

            is ReportCreatorIntent.OnMoveSection -> {
                _state.update { currentState ->
                    val list = currentState.secoes.toMutableList()
                    if (intent.fromIndex in list.indices && intent.toIndex in list.indices) {
                        val item = list.removeAt(intent.fromIndex)
                        list.add(intent.toIndex, item)
                    }
                    currentState.copy(secoes = list)
                }
            }

            is ReportCreatorIntent.OnAddSection -> {
                _state.update { currentState ->
                    currentState.copy(secoes = currentState.secoes + intent.section)
                }
            }

            is ReportCreatorIntent.OnAddMeasurement -> {
                _state.update { currentState ->
                    val updatedList = currentState.secoes.map { section ->
                        if (section.id == intent.sectionId && section is ReportSection.ResultadosDimensionais) {
                            section.copy(measurements = section.measurements + MeasurementData())
                        } else {
                            section
                        }
                    }
                    currentState.copy(secoes = updatedList)
                }
            }

            is ReportCreatorIntent.OnReportFieldChanged -> {
                _state.update { it.copy(currentReport = intent.updatedData) }
            }

            ReportCreatorIntent.OnGeneratePdf -> {
                val reportData = _state.value.currentReport ?: return
                val secoes = _state.value.secoes
                viewModelScope.launch {
                    try {
                        val bytes = pdfGenerator.generatePdfBytes(
                            reportData = reportData,
                            secoes = secoes
                        )
                        sendEffect(ReportCreatorEffect.OnPdfGenerated(bytes))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _state.update { it.copy(errorMessage = e.message ?: "Erro ao gerar PDF") }
                    }
                }
            }
        }
    }

    private fun sendEffect(effect: ReportCreatorEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    override fun onCleared() {
        renderJob?.cancel()
        super.onCleared()
        renderEngine.close()
    }
}