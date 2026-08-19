package org.senai.metrodoc.features.report.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.senai.metrodoc.common.data.RoomProjectRepository
import org.senai.metrodoc.common.mapper.toDomain
import org.senai.metrodoc.common.util.PdfGenerator
import org.senai.metrodoc.common.util.PdfRenderEngine
import org.senai.metrodoc.features.report.data.MemoryReportRepository
import org.senai.metrodoc.features.report.model.*
import org.senai.metrodoc.features.report.presentation.ReportCreatorEffect.OnPdfGenerated
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
        val memoryBatch = memoryReportRepository.currentProjectData.value
        val memorySingle = memoryReportRepository.currentReport.value

        if (memoryBatch != null && memoryBatch.pdfItems.isNotEmpty()) {
            val initialItems = memoryBatch.pdfItems.mapIndexed { idx, item ->
                if (idx == 0) item.copy(isTouched = true) else item
            }
            val activeItem = initialItems.first()

            _state.update {
                it.copy(
                    cliente = memoryBatch.cliente,
                    componente = memoryBatch.componente,
                    pdfItems = initialItems,
                    activePdfIndex = 0,
                    pdfPath = activeItem.pdfPath,
                    pdfName = activeItem.pdfName,
                    reportName = "Projeto ${memoryBatch.cliente} — ${memoryBatch.componente}",
                    currentReport = activeItem.reportData,
                    secoes = activeItem.secoes,
                    secaoAtivaId = activeItem.secoes.firstOrNull()?.id,
                    secoesAbertas = activeItem.secoes.firstOrNull()?.id?.let { id -> setOf(id) } ?: emptySet()
                )
            }
        } else if (memorySingle != null) {
            val initialSections = listOf(
                ReportSection.Introducao(),
                ReportSection.Identificacao(),
                ReportSection.ResultadosDimensionais(measurements = memorySingle.caracteristicas),
                ReportSection.InterpretacaoResultados(),
                ReportSection.Conclusao(),
            )
            val initialItem = PdfItem(
                pdfPath = "",
                pdfName = memorySingle.componente,
                reportData = memorySingle,
                secoes = initialSections,
                isTouched = true
            )
            val initialSectionId = initialSections.firstOrNull()?.id

            _state.update {
                it.copy(
                    cliente = memorySingle.cliente,
                    componente = memorySingle.componente,
                    pdfItems = listOf(initialItem),
                    activePdfIndex = 0,
                    currentReport = memorySingle,
                    secoes = initialSections,
                    secaoAtivaId = initialSectionId,
                    secoesAbertas = initialSectionId?.let { id -> setOf(id) } ?: emptySet()
                )
            }
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

        viewModelScope.launch {
            _state.map { it.reportId }
                .distinctUntilChanged()
                .filterNotNull()
                .collectLatest { reportId ->
                    roomProjectRepository.getVersions(reportId)
                        .catch { e ->
                            e.printStackTrace()
                        }
                        .collect { versions ->
                            _state.update { state ->
                                state.copy(versions = versions.map { it.toDomain() })
                            }
                        }
                }
        }
    }

    fun handleIntent(intent: ReportCreatorIntent) {
        if (intent.contentChanged && _state.value.reportSaveState == SavedState.Saved) {
            _state.update { it.copy(reportSaveState = SavedState.Unsaved) }
        }

        when (intent) {
            is ReportCreatorIntent.OnInit -> {
                val savedId = intent.reportId

                if (savedId != null) {
                    viewModelScope.launch {
                        val savedProject = roomProjectRepository.getProjectById(savedId)
                        if (savedProject != null && savedProject.pdfItems.isNotEmpty()) {
                            val items = savedProject.pdfItems.mapIndexed { idx, item ->
                                if (idx == 0) item.copy(isTouched = true) else item
                            }
                            val firstItem = items.first()

                            _state.update { currentState ->
                                currentState.copy(
                                    reportId = savedProject.projectId,
                                    isInitializing = false,
                                    reportName = savedProject.nomeProjeto,
                                    cliente = savedProject.cliente,
                                    componente = savedProject.componente,
                                    pdfItems = items,
                                    activePdfIndex = 0,
                                    pdfPath = firstItem.pdfPath,
                                    pdfName = firstItem.pdfName,
                                    currentReport = firstItem.reportData,
                                    secoes = firstItem.secoes,
                                    secaoAtivaId = firstItem.secoes.firstOrNull()?.id,
                                    reportSaveState = SavedState.Saved
                                )
                            }
                        }
                    }
                } else if (_state.value.pdfItems.isEmpty() && intent.pdfPath.isNotBlank()) {
                    _state.update {
                        it.copy(
                            reportId = intent.reportId,
                            pdfPath = intent.pdfPath,
                            pdfName = intent.pdfName,
                            reportName = "Relatório ${intent.pdfName}",
                            secaoAtivaId = it.secoes.firstOrNull()?.id,
                            isInitializing = false,
                            reportSaveState = SavedState.Unsaved
                        )
                    }
                } else {
                    _state.update { it.copy(isInitializing = false) }
                }
            }

            is ReportCreatorIntent.OnSelectPdfItem -> {
                val index = intent.index
                if (index in _state.value.pdfItems.indices) {
                    _state.update { currentState ->
                        val updatedItems = currentState.pdfItems.mapIndexed { idx, item ->
                            if (idx == index) item.copy(isTouched = true) else item
                        }
                        val selectedItem = updatedItems[index]
                        currentState.copy(
                            activePdfIndex = index,
                            pdfItems = updatedItems,
                            pdfPath = selectedItem.pdfPath,
                            pdfName = selectedItem.pdfName,
                            currentReport = selectedItem.reportData,
                            secoes = selectedItem.secoes,
                            secaoAtivaId = selectedItem.secoes.firstOrNull()?.id,
                            secoesAbertas = selectedItem.secoes.firstOrNull()?.id?.let { setOf(it) } ?: emptySet()
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
                _state.update {
                    it.copy(
                        secaoAtivaId = intent.sectionId,
                        secoesAbertas = it.secoesAbertas + intent.sectionId
                    )
                }
            }

            is ReportCreatorIntent.OnTabChange -> {
                _state.update { it.copy(abaDireitaAtiva = intent.tab) }
            }

            is ReportCreatorIntent.OnZoomChange -> {
                _state.update { it.copy(zoomFactor = intent.newZoom) }
            }

            ReportCreatorIntent.OnBackClicked -> {
                if (_state.value.reportSaveState == SavedState.Saved || _state.value.reportSaveState == SavedState.JustSaved) {
                    viewModelScope.launch {
                        _effect.send(ReportCreatorEffect.NavigateBack)
                    }
                } else {
                    _state.update { it.copy(showBackDialog = true) }
                }
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
                    val updatedSecoes = currentState.secoes.map { section ->
                        if (section.id == intent.updatedSection.id) intent.updatedSection else section
                    }
                    val updatedReportData = if (intent.updatedSection is ReportSection.ResultadosDimensionais) {
                        currentState.currentReport?.copy(caracteristicas = intent.updatedSection.measurements)
                    } else {
                        currentState.currentReport
                    }

                    val updatedPdfItems = currentState.pdfItems.mapIndexed { idx, item ->
                        if (idx == currentState.activePdfIndex) {
                            item.copy(
                                secoes = updatedSecoes,
                                reportData = updatedReportData ?: item.reportData
                            )
                        } else item
                    }

                    currentState.copy(
                        secoes = updatedSecoes,
                        currentReport = updatedReportData,
                        pdfItems = updatedPdfItems
                    )
                }
            }

            is ReportCreatorIntent.OnRemoveSection -> {
                _state.update { currentState ->
                    val updatedSecoes = currentState.secoes.filterNot { it.id == intent.sectionId }
                    val updatedPdfItems = currentState.pdfItems.mapIndexed { idx, item ->
                        if (idx == currentState.activePdfIndex) item.copy(secoes = updatedSecoes) else item
                    }
                    currentState.copy(secoes = updatedSecoes, pdfItems = updatedPdfItems)
                }
            }

            is ReportCreatorIntent.OnMoveSection -> {
                _state.update { currentState ->
                    val list = currentState.secoes.toMutableList()
                    if (intent.fromIndex in list.indices && intent.toIndex in list.indices) {
                        val item = list.removeAt(intent.fromIndex)
                        list.add(intent.toIndex, item)
                    }
                    val updatedPdfItems = currentState.pdfItems.mapIndexed { idx, item ->
                        if (idx == currentState.activePdfIndex) item.copy(secoes = list) else item
                    }
                    currentState.copy(secoes = list, pdfItems = updatedPdfItems)
                }
            }

            is ReportCreatorIntent.OnAddSection -> {
                _state.update { currentState ->
                    val list = currentState.secoes.toMutableList()
                    val conclusaoIndex = list.indexOfFirst { it is ReportSection.Conclusao }
                    if (conclusaoIndex != -1) {
                        list.add(conclusaoIndex, intent.section)
                    } else {
                        list.add(intent.section)
                    }
                    val updatedPdfItems = currentState.pdfItems.mapIndexed { idx, item ->
                        if (idx == currentState.activePdfIndex) item.copy(secoes = list) else item
                    }
                    currentState.copy(secoes = list, pdfItems = updatedPdfItems)
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
                    val updatedPdfItems = currentState.pdfItems.mapIndexed { idx, item ->
                        if (idx == currentState.activePdfIndex) item.copy(secoes = updatedList) else item
                    }
                    currentState.copy(secoes = updatedList, pdfItems = updatedPdfItems)
                }
            }

            is ReportCreatorIntent.OnReportFieldChanged -> {
                _state.update { currentState ->
                    val updatedPdfItems = currentState.pdfItems.mapIndexed { idx, item ->
                        if (idx == currentState.activePdfIndex) item.copy(reportData = intent.updatedData) else item
                    }
                    currentState.copy(currentReport = intent.updatedData, pdfItems = updatedPdfItems)
                }
            }

            is ReportCreatorIntent.OnReportNameChanged -> {
                _state.update { it.copy(reportName = intent.newName) }
            }

            is ReportCreatorIntent.OnEditImageClicked -> {
                _state.update { it.copy(showEditDialog = true, editingImage = intent.imagem) }
            }

            ReportCreatorIntent.OnEditImageDismissed -> {
                _state.update { it.copy(showEditDialog = false, editingImage = null) }
            }

            is ReportCreatorIntent.OnEditImageConfirmed -> {
                _state.update { currentState ->
                    val updatedSections = currentState.secoes.map { section ->
                        when (section) {
                            is ReportSection.Introducao -> {
                                if (section.imagem.id == intent.updatedImagem.id) {
                                    section.copy(imagem = intent.updatedImagem)
                                } else section
                            }

                            is ReportSection.Customizada -> {
                                val updatedBlocks = section.blocos.map { block ->
                                    if (block is ReportBlock.GaleriaImagem) {
                                        val updatedImages = block.imagens.map { image ->
                                            if (image.id == intent.updatedImagem.id) {
                                                intent.updatedImagem
                                            } else image
                                        }
                                        block.copy(imagens = updatedImages)
                                    } else block
                                }
                                section.copy(blocos = updatedBlocks)
                            }

                            else -> section
                        }
                    }

                    val updatedPdfItems = currentState.pdfItems.mapIndexed { idx, item ->
                        if (idx == currentState.activePdfIndex) item.copy(secoes = updatedSections) else item
                    }

                    currentState.copy(
                        showEditDialog = false,
                        editingImage = null,
                        secoes = updatedSections,
                        pdfItems = updatedPdfItems
                    )
                }
            }

            ReportCreatorIntent.OnSaveProject -> {
                _state.update { it.copy(reportSaveState = SavedState.Saving) }
                viewModelScope.launch {
                    val itemsToSave = _state.value.pdfItems.ifEmpty {
                        val currentRep = _state.value.currentReport
                        if (currentRep != null) {
                            listOf(
                                PdfItem(
                                    pdfPath = state.value.pdfPath,
                                    pdfName = _state.value.pdfName,
                                    reportData = currentRep,
                                    secoes = _state.value.secoes
                                )
                            )
                        } else emptyList()
                    }

                    val newId = roomProjectRepository.saveProject(
                        projectId = _state.value.reportId,
                        projectName = _state.value.reportName.ifBlank { "Projeto Sem Nome" },
                        cliente = _state.value.cliente,
                        componente = _state.value.componente,
                        pdfItems = itemsToSave
                    )

                    _state.update { it.copy(reportId = newId, reportSaveState = SavedState.JustSaved) }
                    delay(3.seconds)
                    _state.update { it.copy(reportSaveState = SavedState.Saved) }
                }
            }

            is ReportCreatorIntent.OnRestoreVersion -> {
                viewModelScope.launch {
                    val projId = roomProjectRepository.restoreVersion(intent.versionId)
                    if (projId != 0L) {
                        val projetoRestaurado = roomProjectRepository.getProjectById(projId)
                        if (projetoRestaurado != null && projetoRestaurado.pdfItems.isNotEmpty()) {
                            val firstItem = projetoRestaurado.pdfItems.first()
                            _state.update { currentState ->
                                currentState.copy(
                                    reportId = projetoRestaurado.projectId,
                                    isInitializing = false,
                                    reportName = projetoRestaurado.nomeProjeto,
                                    cliente = projetoRestaurado.cliente,
                                    componente = projetoRestaurado.componente,
                                    pdfItems = projetoRestaurado.pdfItems,
                                    activePdfIndex = 0,
                                    pdfPath = firstItem.pdfPath,
                                    pdfName = firstItem.pdfName,
                                    currentReport = firstItem.reportData,
                                    secoes = firstItem.secoes,
                                    reportSaveState = SavedState.Saved
                                )
                            }
                        }
                    }
                }
            }

            is ReportCreatorIntent.OnDeleteVersion -> {
                viewModelScope.launch {
                    roomProjectRepository.deleteVersion(intent.versionId)
                }
            }

            is ReportCreatorIntent.OnRenameVersion -> {
                viewModelScope.launch {
                    roomProjectRepository.renameVersion(intent.versionId, intent.newName)
                }
            }

            is ReportCreatorIntent.OnGeneratePdf -> {
                val reportData = _state.value.currentReport ?: return
                val secoes = _state.value.secoes
                val pdfPath = _state.value.pdfPath

                generatePdfJob?.cancel()

                generatePdfJob = viewModelScope.launch {
                    _state.update { it.copy(isGeneratingPdf = true, processedPdfCount = 0) }
                    try {
                        val bytes = pdfGenerator.generatePdfBytes(
                            reportData = reportData,
                            secoes = secoes,
                            originalPdfPath = pdfPath,
                        )
                        sendEffect(OnPdfGenerated(bytes))
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        e.printStackTrace()
                        _state.update { it.copy(errorMessage = e.message ?: "Erro ao gerar PDF") }
                    } finally {
                        _state.update {
                            it.copy(isGeneratingPdf = false, processedPdfCount = 0)
                        }
                    }
                }
            }

            is ReportCreatorIntent.OnGenerateAllPdfs -> {
                generatePdfJob?.cancel()
                generatePdfJob = viewModelScope.launch {
                    _state.update { it.copy(isGeneratingPdf = true, processedPdfCount = 0) }
                    try {
                        val filesList = pdfGenerator.generateBatchPdfBytes(
                            items = _state.value.pdfItems,
                            onProgress = { processedCount ->
                                _state.update { it.copy(processedPdfCount = processedCount) }
                            }
                        )
                        sendEffect(ReportCreatorEffect.OnBatchPdfsGenerated(filesList))
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        e.printStackTrace()
                        _state.update { it.copy(errorMessage = e.message ?: "Erro ao gerar PDFs em lote") }
                    } finally {
                        _state.update { it.copy(isGeneratingPdf = false, processedPdfCount = 0) }
                    }
                }
            }

            ReportCreatorIntent.OnCancelGeneration -> {
                generatePdfJob?.cancel()
                generatePdfJob = null
                _state.update { it.copy(isGeneratingPdf = false) }
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