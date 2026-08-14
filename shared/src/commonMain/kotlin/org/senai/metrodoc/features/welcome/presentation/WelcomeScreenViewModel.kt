package org.senai.metrodoc.features.welcome.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.senai.metrodoc.common.data.RoomProjectRepository
import org.senai.metrodoc.common.util.PdfGenerator
import org.senai.metrodoc.common.util.PdfParser
import org.senai.metrodoc.features.report.data.MemoryReportRepository
import org.senai.metrodoc.features.report.model.MeasurementData
import org.senai.metrodoc.features.report.model.PdfItemStatus
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.ReportSection

class WelcomeScreenViewModel(
    private val roomProjectRepository: RoomProjectRepository,
    private val memoryReportRepository: MemoryReportRepository,
    private val pdfParser: PdfParser,
    private val pdfGenerator: PdfGenerator,
) : ViewModel() {
    private val _state = MutableStateFlow(WelcomeViewState())
    val state = _state.asStateFlow()

    private val _effect = Channel<WelcomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var generatePdfJob: Job? = null

    init {
        viewModelScope.launch {
            roomProjectRepository.getRecentProjects()
                .onStart {
                    _state.update { it.copy(isLoadingRecentProjects = true) }
                }
                .catch { e ->
                    e.printStackTrace()
                    _state.update { it.copy(isLoadingRecentProjects = false) }
                }
                .collect { projects ->
                    _state.update {
                        it.copy(
                            recentProjects = projects,
                            isLoadingRecentProjects = false
                        )
                    }
                }
        }
    }

    fun handleIntent(intent: WelcomeScreenIntent) {
        when (intent) {
            WelcomeScreenIntent.OnOpenFileButtonClicked -> {
                sendEffect(WelcomeEffect.TriggerFilePicker)
            }

            is WelcomeScreenIntent.OnFileSelected -> {
                _state.update {
                    it.copy(
                        pdfPath = intent.path,
                        pdfName = intent.name,
                        isBatchMode = false,
                        batchItems = emptyList()
                    )
                }
                readPdf(intent.path)
            }

            is WelcomeScreenIntent.OnMultipleFilesSelected -> {
                if (intent.files.isEmpty()) return
                if (intent.files.size == 1) {
                    val single = intent.files.first()
                    _state.update {
                        it.copy(
                            pdfPath = single.first,
                            pdfName = single.second,
                            isBatchMode = false,
                            batchItems = emptyList()
                        )
                    }
                    readPdf(single.first)
                } else {
                    readPdfsBatch(intent.files)
                }
            }

            is WelcomeScreenIntent.OnRemoveBatchItem -> {
                _state.update { currentState ->
                    val updated = currentState.batchItems.filterNot { it.id == intent.id }
                    currentState.copy(batchItems = updated)
                }
            }

            is WelcomeScreenIntent.OnProjectSelected -> {
                sendEffect(
                    WelcomeEffect.NavigateToRelatoryCreator(
                        reportId = intent.id,
                        path = "",
                        pdfName = ""
                    )
                )
            }

            is WelcomeScreenIntent.OnDeleteProject -> {
                viewModelScope.launch {
                    roomProjectRepository.deleteProjectById(intent.id)
                }
            }

            WelcomeScreenIntent.OnDeleteAllProjects -> {
                viewModelScope.launch {
                    roomProjectRepository.deleteAllProjects()
                }
            }

            is WelcomeScreenIntent.OnRequestDeleteProject -> {
                _state.update { it.copy(projectToDelete = intent.project) }
            }

            WelcomeScreenIntent.OnDismissDeleteProjectDialog -> {
                _state.update { it.copy(projectToDelete = null) }
            }

            WelcomeScreenIntent.OnConfirmDeleteProject -> {
                val targetProject = _state.value.projectToDelete
                if (targetProject != null) {
                    viewModelScope.launch {
                        roomProjectRepository.deleteProjectById(targetProject.id)
                        _state.update { it.copy(projectToDelete = null) }
                    }
                }
            }

            WelcomeScreenIntent.OnRequestDeleteAllProjects -> {
                _state.update { it.copy(showDeleteAllProjectsDialog = true) }
            }

            WelcomeScreenIntent.OnDismissDeleteAllProjectsDialog -> {
                _state.update { it.copy(showDeleteAllProjectsDialog = false) }
            }

            WelcomeScreenIntent.OnConfirmDeleteAllProjects -> {
                viewModelScope.launch {
                    roomProjectRepository.deleteAllProjects()
                    _state.update { it.copy(showDeleteAllProjectsDialog = false) }
                }
            }

            is WelcomeScreenIntent.OnReportFieldChanged -> {
                _state.update { it.copy(editedReportData = intent.updatedData) }
            }

            is WelcomeScreenIntent.OnMeasurementChanged -> {
                _state.update {
                    val currentReportData = it.editedReportData ?: return@update it
                    val updatedList = currentReportData.caracteristicas.toMutableList().apply {
                        this[intent.index] = intent.updatedMeasurement
                    }
                    it.copy(
                        editedReportData = currentReportData.copy(caracteristicas = updatedList)
                    )
                }
            }

            WelcomeScreenIntent.OnAddMeasurement -> {
                _state.update { currentState ->
                    val currentReportData = currentState.editedReportData ?: return@update currentState
                    val updatedList = currentReportData.caracteristicas + MeasurementData(
                        unidade = MeasurementData.defineUnidadePadrao(currentReportData.caracteristicas)
                    )
                    currentState.copy(
                        editedReportData = currentReportData.copy(caracteristicas = updatedList)
                    )
                }
            }

            is WelcomeScreenIntent.OnConfirmData -> {
                val finalData = _state.value.editedReportData ?: return
                if (_state.value.isBatchMode) {
                    val validBatchItems = _state.value.batchItems
                        .filter { it.status != PdfItemStatus.ERROR }
                        .map { item ->
                            val updatedData = item.reportData.copy(
                                cliente = finalData.cliente,
                                componente = finalData.componente
                            )
                            item.copy(reportData = updatedData)
                        }

                    if (validBatchItems.isEmpty()) return

                    memoryReportRepository.setProjectData(
                        cliente = finalData.cliente,
                        componente = finalData.componente,
                        pdfItems = validBatchItems
                    )

                    _state.update {
                        it.copy(
                            showReportDialog = false,
                            reportData = null,
                            editedReportData = null,
                            batchItems = emptyList(),
                            isBatchMode = false
                        )
                    }

                    sendEffect(
                        WelcomeEffect.NavigateToRelatoryCreator(
                            reportId = null,
                            path = "",
                            pdfName = ""
                        )
                    )
                } else {
                    val path = _state.value.pdfPath
                    val pdfName = _state.value.pdfName

                    memoryReportRepository.setReport(finalData)
                    _state.update {
                        it.copy(
                            showReportDialog = false,
                            reportData = null,
                            editedReportData = null,
                            pdfPath = "",
                            pdfName = ""
                        )
                    }
                    sendEffect(
                        WelcomeEffect.NavigateToRelatoryCreator(
                            reportId = null,
                            path = path,
                            pdfName = pdfName
                        )
                    )
                }
            }

            is WelcomeScreenIntent.OnRequestExportProject -> {
                viewModelScope.launch {
                    val projeto = roomProjectRepository.getProjectById(intent.project.id) ?: return@launch

                    val hasErrors = projeto.pdfItems.any { item ->
                        item.secoes.any { secao ->
                            if (secao is ReportSection.Identificacao) {
                                item.reportData.getErrors(secao.id, secao.titulo).isNotEmpty()
                            } else !secao.isValid
                        }
                    }

                    if (hasErrors) {
                        _state.update {
                            it.copy(
                                showProjectWithErrorsDialog = true,
                                projectWithErrorsId = intent.project.id
                            )
                        }
                        return@launch
                    }

                    if (projeto.pdfItems.size > 1) {
                        sendEffect(WelcomeEffect.TriggerBatchExportDirectoryPicker(intent.project.id))
                    } else {
                        sendEffect(
                            WelcomeEffect.TriggerSingleExportFileSaver(
                                projectId = intent.project.id,
                                suggestedName = "${intent.project.nomeProjeto}.pdf"
                            )
                        )
                    }
                }
            }

            is WelcomeScreenIntent.OnGeneratePdf -> {
                generatePdfJob?.cancel()
                generatePdfJob = viewModelScope.launch {
                    _state.update { it.copy(isGeneratingPdf = true) }
                    try {
                        val projeto = roomProjectRepository.getProjectById(intent.id) ?: return@launch
                        val firstItem = projeto.pdfItems.firstOrNull() ?: return@launch

                        val bytes = pdfGenerator.generatePdfBytes(
                            reportData = firstItem.reportData,
                            secoes = firstItem.secoes,
                            originalPdfPath = firstItem.pdfPath,
                        )
                        sendEffect(WelcomeEffect.OnPdfGenerated(bytes))
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

            is WelcomeScreenIntent.OnGenerateBatchPdfs -> {
                generatePdfJob?.cancel()
                generatePdfJob = viewModelScope.launch {
                    _state.update { it.copy(isGeneratingPdf = true, isBatchMode = true) }
                    try {
                        val projeto = roomProjectRepository.getProjectById(intent.projectId) ?: return@launch

                        val filesList = projeto.pdfItems.map { item ->
                            val bytes = pdfGenerator.generatePdfBytes(
                                reportData = item.reportData,
                                secoes = item.secoes,
                                originalPdfPath = item.pdfPath
                            )
                            Pair(item.pdfName, bytes)
                        }
                        sendEffect(WelcomeEffect.OnBatchPdfsGenerated(filesList))
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        e.printStackTrace()
                        _state.update { it.copy(errorMessage = e.message ?: "Erro ao gerar PDFs em lote") }
                    } finally {
                        _state.update { it.copy(isGeneratingPdf = false, isBatchMode = false) }
                    }
                }
            }

            WelcomeScreenIntent.OnDismissProjectWithErrorDialog -> {
                _state.update {
                    it.copy(
                        showProjectWithErrorsDialog = false,
                        projectWithErrorsId = null
                    )
                }
            }

            WelcomeScreenIntent.OnConfirmFixProjectWithError -> {
                val targetProjectId = _state.value.projectWithErrorsId
                _state.update {
                    it.copy(
                        showProjectWithErrorsDialog = false,
                        projectWithErrorsId = null
                    )
                }

                if (targetProjectId != null) {
                    sendEffect(
                        WelcomeEffect.NavigateToRelatoryCreator(
                            reportId = targetProjectId,
                            path = "",
                            pdfName = ""
                        )
                    )
                }
            }

            WelcomeScreenIntent.OnCancelGeneration -> {
                generatePdfJob?.cancel()
                generatePdfJob = null
                _state.update { it.copy(isGeneratingPdf = false) }
            }

            is WelcomeScreenIntent.OnDismissReportDialog -> {
                _state.update {
                    it.copy(
                        showReportDialog = false,
                        reportData = null,
                        editedReportData = null,
                        pdfPath = "",
                        pdfName = "",
                        batchItems = emptyList(),
                        isBatchMode = false
                    )
                }
            }
        }
    }

    private fun sendEffect(effect: WelcomeEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    fun readPdf(path: String) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessingPdf = true) }
            try {
                val data = pdfParser.parsePdf(path)
                _state.update {
                    it.copy(
                        reportData = data,
                        editedReportData = data,
                        showReportDialog = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = e.localizedMessage ?: "Erro ao ler o arquivo PDF.")
                }
            } finally {
                _state.update { it.copy(isProcessingPdf = false) }
            }
        }
    }

    private fun readPdfsBatch(files: List<Pair<String, String>>) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessingPdf = true) }
            try {
                val items = pdfParser.parsePdfsInBatch(files)
                val firstValidData = items.firstOrNull { it.status != PdfItemStatus.ERROR }?.reportData ?: ReportData()
                _state.update {
                    it.copy(
                        batchItems = items,
                        isBatchMode = true,
                        reportData = firstValidData,
                        editedReportData = firstValidData,
                        showReportDialog = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = e.localizedMessage ?: "Erro ao ler os arquivos PDF.")
                }
            } finally {
                _state.update { it.copy(isProcessingPdf = false) }
            }
        }
    }

    override fun onCleared() {
        generatePdfJob?.cancel()
        super.onCleared()
    }
}