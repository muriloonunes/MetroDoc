package org.senai.metrodoc.features.welcome.presentation.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.Flow
import org.senai.metrodoc.features.welcome.presentation.WelcomeEffect
import org.senai.metrodoc.features.welcome.presentation.WelcomeScreenIntent
import org.senai.metrodoc.features.welcome.presentation.WelcomeViewState
import org.senai.metrodoc.features.welcome.presentation.ui.components.WelcomeContent
import org.senai.metrodoc.features.welcome.presentation.ui.components.dialog.ReportDataDialog

@Composable
fun WelcomeScreen(
    state: WelcomeViewState,
    onIntent: (WelcomeScreenIntent) -> Unit,
    onNavigateToRelatoryCreator: (String, String) -> Unit,
    effect: Flow<WelcomeEffect>,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        WelcomeContent(
            onIntent = onIntent,
            onNavigateToRelatoryCreator = onNavigateToRelatoryCreator,
            effect = effect
        )

        if (state.isProcessingPdf) {
            Dialog(
                onDismissRequest = { },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                Surface(
                    modifier = Modifier
                        .width(340.dp)
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 16.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Processando PDF...",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.pdfName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
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

