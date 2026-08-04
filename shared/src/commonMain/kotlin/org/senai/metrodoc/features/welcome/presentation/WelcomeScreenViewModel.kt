package org.senai.metrodoc.features.welcome.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.senai.metrodoc.features.report.data.ReportRepository
import org.senai.metrodoc.features.report.model.MeasurementData
import org.senai.metrodoc.common.util.PdfParser

class WelcomeScreenViewModel(
    private val pdfParser: PdfParser,
    private val reportRepository: ReportRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(WelcomeViewState())
    val state = _state.asStateFlow()

    private val _effect = Channel<WelcomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

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

                reportRepository.setReport(finalData)
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
                        path = path,
                        pdfName = pdfName
                    )
                )
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
}