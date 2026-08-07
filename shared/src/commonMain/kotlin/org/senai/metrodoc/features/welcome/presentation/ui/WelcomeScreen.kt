package org.senai.metrodoc.features.welcome.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow
import org.senai.metrodoc.common.ui.MetroDocLoadingDialog
import org.senai.metrodoc.features.welcome.presentation.WelcomeEffect
import org.senai.metrodoc.features.welcome.presentation.WelcomeScreenIntent
import org.senai.metrodoc.features.welcome.presentation.WelcomeViewState
import org.senai.metrodoc.features.welcome.presentation.ui.components.WelcomeContent
import org.senai.metrodoc.features.welcome.presentation.ui.components.dialog.ReportDataDialog

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
            MetroDocLoadingDialog(
                loadingMessage = "Processando PDF",
                supportingMessage = state.pdfName,
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
            )
        }
    }
}

