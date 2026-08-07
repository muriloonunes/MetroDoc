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
                _state.update { it.copy(pdfPath = intent.path, pdfName = intent.name) }
                readPdf(intent.path)
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

            is WelcomeScreenIntent.OnGeneratePdf -> {
                generatePdfJob?.cancel()
                generatePdfJob = viewModelScope.launch {
                    _state.update { it.copy(isGeneratingPdf = true) }
                    try {
                        val projeto = roomProjectRepository.getProjectById(intent.id) ?: return@launch
                        val bytes = pdfGenerator.generatePdfBytes(
                            reportData = projeto.reportData,
                            secoes = projeto.secoes,
                            originalPdfPath = projeto.pdfPath,
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

            WelcomeScreenIntent.OnCancelGeneration -> {
                generatePdfJob?.cancel()
                generatePdfJob = null
            }

            is WelcomeScreenIntent.OnDismissReportDialog -> {
                _state.update {
                    it.copy(
                        showReportDialog = false,
                        reportData = null,
                        editedReportData = null,
                        pdfPath = "",
                        pdfName = ""
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

    override fun onCleared() {
        generatePdfJob?.cancel()
        super.onCleared()
    }
}