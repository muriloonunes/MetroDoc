package org.senai.metrodoc.features.welcome.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import org.senai.metrodoc.common.ui.ConfirmDialog
import org.senai.metrodoc.common.ui.MetroDocLoadingDialog
import org.senai.metrodoc.common.ui.MetroDocPrimaryButton
import org.senai.metrodoc.features.welcome.presentation.WelcomeEffect
import org.senai.metrodoc.features.welcome.presentation.WelcomeScreenIntent
import org.senai.metrodoc.features.welcome.presentation.WelcomeViewState
import org.senai.metrodoc.features.welcome.presentation.ui.components.WelcomeContent
import org.senai.metrodoc.features.welcome.presentation.ui.components.dialog.ConfirmDeleteAllProjectsDialog
import org.senai.metrodoc.features.welcome.presentation.ui.components.dialog.ConfirmDeleteProjectDialog
import org.senai.metrodoc.features.welcome.presentation.ui.components.dialog.ReportDataDialog

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WelcomeScreen(
    state: WelcomeViewState,
    onIntent: (WelcomeScreenIntent) -> Unit,
    onNavigateToRelatoryCreator: (Long?, String, String) -> Unit,
    effect: Flow<WelcomeEffect>,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        WelcomeContent(
            projetosRecentes = state.recentProjects,
            carregandoProjetos = state.isLoadingRecentProjects,
            onIntent = onIntent,
            onNavigateToRelatoryCreator = onNavigateToRelatoryCreator,
            effect = effect
        )

        if (state.isProcessingPdf) {
            val loadingMsg = if (state.isBatchMode) "Processando PDFs" else "Processando PDF"
            val supportingMessage = if (state.isBatchMode && state.processedPdfCount > 0) {
                "${state.processedPdfCount} de ${state.totalPdfCount} processados"
            } else state.pdfName
            MetroDocLoadingDialog(
                loadingMessage = loadingMsg,
                supportingMessage = supportingMessage,
                progress = state.pdfProcessingProgress,
            )
        }

        if (state.isGeneratingPdf) {
            val loadingMessage = if (state.isBatchMode) "Gerando PDFs" else "Gerando PDF"
            MetroDocLoadingDialog(
                loadingMessage = loadingMessage,
                isCancelable = true,
                onCancelLoading = {
                    onIntent(WelcomeScreenIntent.OnCancelGeneration)
                }
            )
        }

        if (state.showProjectWithErrorsDialog) {
            ConfirmDialog(
                title = "Relatório com pendências",
                description = "Este relatório possui campos obrigatórios em branco. Para emitir o PDF, é necessário corrigir as pendências no editor.",
                onDismiss = { onIntent(WelcomeScreenIntent.OnDismissProjectWithErrorDialog) },
                buttons = {
                    TextButton(
                        onClick = { onIntent(WelcomeScreenIntent.OnDismissProjectWithErrorDialog) },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        shape = ButtonDefaults.squareShape
                    ) {
                        Text(
                            text = "Cancelar",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    MetroDocPrimaryButton(
                        onClick = { onIntent(WelcomeScreenIntent.OnConfirmFixProjectWithError) },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "Abrir no editor",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        }

        if (!state.isProcessingPdf && state.showReportDialog && state.reportData != null) {
            ReportDataDialog(
                onDismissRequest = { onIntent(WelcomeScreenIntent.OnDismissReportDialog) },
                onConfirmData = {
                    onIntent(WelcomeScreenIntent.OnConfirmData)
                },
                onDataChanged = { updatedData ->
                    onIntent(WelcomeScreenIntent.OnReportFieldChanged(updatedData))
                },
                onMeasurementChanged = { index, updatedMeasurement ->
                    onIntent(WelcomeScreenIntent.OnMeasurementChanged(index, updatedMeasurement))
                },
                onAddMeasurement = { onIntent(WelcomeScreenIntent.OnAddMeasurement) },
                reportData = state.editedReportData ?: state.reportData,
                isValid = state.isFormValid,
                isBatchMode = state.isBatchMode,
                batchItems = state.batchItems,
                onRemoveBatchItem = { id ->
                    onIntent(WelcomeScreenIntent.OnRemoveBatchItem(id))
                }
            )
        }

        state.projectToDelete?.let {
            ConfirmDeleteProjectDialog(
                onConfirm = { onIntent(WelcomeScreenIntent.OnConfirmDeleteProject) },
                onDismiss = { onIntent(WelcomeScreenIntent.OnDismissDeleteProjectDialog) }
            )
        }

        if (state.showDeleteAllProjectsDialog) {
            ConfirmDeleteAllProjectsDialog(
                onConfirm = { onIntent(WelcomeScreenIntent.OnConfirmDeleteAllProjects) },
                onDismiss = { onIntent(WelcomeScreenIntent.OnDismissDeleteAllProjectsDialog) }
            )
        }
    }
}

