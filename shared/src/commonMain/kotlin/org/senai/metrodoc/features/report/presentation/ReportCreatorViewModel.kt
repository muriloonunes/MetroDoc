package org.senai.metrodoc.features.report.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.senai.metrodoc.common.data.RoomProjectRepository
import org.senai.metrodoc.common.util.PdfGenerator
import org.senai.metrodoc.common.util.PdfRenderEngine
import org.senai.metrodoc.features.report.data.MemoryReportRepository
import org.senai.metrodoc.features.report.model.MeasurementData
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.ReportSection
import org.senai.metrodoc.features.report.presentation.ReportCreatorEffect.OnPdfGenerated
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class ReportCreatorViewModel(
    private val memoryReportRepository: MemoryReportRepository,
    private val roomProjectRepository: RoomProjectRepository,
    private val renderEngine: PdfRenderEngine,
    private val pdfGenerator: PdfGenerator,
) : ViewModel() {
    private val _state = MutableStateFlow(ReportCreatorState())
    val state = _state.asStateFlow()

    private val _effect = Channel<ReportCreatorEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var renderJob: Job? = null
    private var generatePdfJob: Job? = null
    private var previewJob: Job? = null

    init {
        val currentReport = memoryReportRepository.currentReport.value
        val initialSections = listOf(
            ReportSection.Introducao(),
            ReportSection.Identificacao(reportData = currentReport ?: ReportData()),
            ReportSection.ResultadosDimensionais(measurements = currentReport?.caracteristicas ?: emptyList()),
            ReportSection.InterpretacaoResultados(),
            ReportSection.Conclusao(),
        )

        _state.update {
            it.copy(
                currentReport = currentReport,
                secoes = initialSections
            )
        }

        viewModelScope.launch {
            _state.map { Triple(it.secoes, it.currentReport, it.isInitializing) }
                .distinctUntilChanged()
                .debounce(750L.milliseconds)
                .collect { (sections, reportData, isInitializing) ->
                    if (!isInitializing && reportData != null) {
                        generatePreview(sections, reportData)
                    }
                }
        }
    }

    fun handleIntent(intent: ReportCreatorIntent) {
        when (intent) {
            is ReportCreatorIntent.OnInit -> {
                val savedId = intent.reportId

                if (savedId != null) {
                    viewModelScope.launch {
                        val savedProject = roomProjectRepository.getProjectById(savedId)
                        if (savedProject != null) {
                            _state.update { currentState ->
                                currentState.copy(
                                    reportId = savedProject.projectId,
                                    isInitializing = false,
                                    pdfPath = savedProject.pdfPath,
                                    pdfName = savedProject.pdfName,
                                    reportName = savedProject.reportName,
                                    currentReport = savedProject.reportData,
                                    secoes = savedProject.secoes,
                                    secaoAtivaId = savedProject.secoes.firstOrNull()?.id
                                )
                            }
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            reportId = intent.reportId,
                            pdfPath = intent.pdfPath,
                            pdfName = intent.pdfName,
                            reportName = "Relatório ${_state.value.currentReport?.cliente ?: "Sem Cliente"} — ${intent.pdfName}",
                            secaoAtivaId = it.secoes.first().id,
                            isInitializing = false
                        )
                    }
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
                memoryReportRepository.clearReport()
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
                val list = _state.value.secoes.toMutableList()
                val conclusaoIndex = list.indexOfFirst { it is ReportSection.Conclusao }
                if (conclusaoIndex != -1) {
                    list.add(conclusaoIndex, intent.section)
                } else {
                    list.add(intent.section)
                }
                _state.update { currentState ->
                    currentState.copy(secoes = list)
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

            is ReportCreatorIntent.OnReportNameChanged -> {
                _state.update { it.copy(reportName = intent.newName) }
            }

            is ReportCreatorIntent.OnGeneratePdf -> {
                val reportData = _state.value.currentReport ?: return
                val secoes = _state.value.secoes
                val pdfPath = _state.value.pdfPath

                generatePdfJob?.cancel()

                generatePdfJob = viewModelScope.launch {
                    _state.update { it.copy(isGeneratingPdf = true) }
                    try {
                        val bytes = pdfGenerator.generatePdfBytes(
                            reportData = reportData,
                            secoes = secoes,
                            originalPdfPath = pdfPath,
                            renderEngine = renderEngine
                        )
                        sendEffect(OnPdfGenerated(bytes))
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        e.printStackTrace()
                        _state.update { it.copy(errorMessage = e.message ?: "Erro ao gerar PDF") }
                    } finally {
                        _state.update {
                            it.copy(isGeneratingPdf = false)
                        }
                    }
                }
            }

            ReportCreatorIntent.OnCancelGeneration -> {
                generatePdfJob?.cancel()
                generatePdfJob = null
                _state.update { it.copy(isGeneratingPdf = false) }
            }

            ReportCreatorIntent.OnSaveProject -> {
                viewModelScope.launch {
                    val newId = roomProjectRepository.saveProject(
                        projectId = _state.value.reportId,
                        projectName = _state.value.reportName,
                        pdfPath = state.value.pdfPath,
                        pdfName = _state.value.pdfName,
                        reportData = _state.value.currentReport ?: return@launch,
                        secoes = _state.value.secoes
                    )

                    _state.update { it.copy(reportId = newId) }
                }
            }
        }
    }

    private fun sendEffect(effect: ReportCreatorEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun generatePreview(
        secoes: List<ReportSection>,
        reportData: ReportData,
    ) {
        previewJob?.cancel()
        previewJob = viewModelScope.launch {
            _state.update { it.copy(isGeneratingPreview = true) }
            try {
                val bytes = pdfGenerator.generatePreviewPdfBytes(
                    reportData = reportData,
                    secoes = secoes,
                    renderEngine = renderEngine
                )
                _state.update {
                    it.copy(
                        previewPdfBytes = bytes,
                        isGeneratingPreview = false
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _state.update {
                    it.copy(
                        errorMessage = e.message ?: "Erro ao gerar PDF",
                        isGeneratingPreview = false
                    )
                }
            }
        }
    }

    override fun onCleared() {
        renderJob?.cancel()
        generatePdfJob?.cancel()
        previewJob?.cancel()
        renderEngine.close()
        super.onCleared()
    }
}