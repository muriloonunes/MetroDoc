package org.senai.metrodoc.features.welcome.presentation.ui.components.dialog

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import metrodoc.shared.generated.resources.Res
import metrodoc.shared.generated.resources.close
import metrodoc.shared.generated.resources.confirm
import metrodoc.shared.generated.resources.warning
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.ui.MetroDocOutlinedButton
import org.senai.metrodoc.common.ui.MetroDocPrimaryButton
import org.senai.metrodoc.common.ui.MetroDocTextField
import org.senai.metrodoc.features.report.model.MeasurementData
import org.senai.metrodoc.features.report.model.PdfItem
import org.senai.metrodoc.features.report.model.PdfItemStatus
import org.senai.metrodoc.features.report.model.ReportData

@Composable
fun ReportDataDialog(
    onDismissRequest: () -> Unit,
    onConfirmData: () -> Unit,
    onDataChanged: (ReportData) -> Unit,
    onMeasurementChanged: (index: Int, updated: MeasurementData) -> Unit,
    onAddMeasurement: () -> Unit,
    reportData: ReportData,
    isValid: Boolean,
    isBatchMode: Boolean = false,
    batchItems: List<PdfItem> = emptyList(),
    onRemoveBatchItem: (String) -> Unit = {},
) {
    var pagina by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(pagina) {
        focusRequester.requestFocus()
    }

    val isPrimaryEnabled = if (isBatchMode) isValid else (pagina != 0 || isValid)
    val handlePrimaryClick = {
        if (isPrimaryEnabled) {
            if (!isBatchMode && pagina == 0) {
                pagina = 1
            } else {
                onConfirmData()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.88f)
                .focusRequester(focusRequester)
                .focusTarget()
                .onPreviewKeyEvent { keyEvent ->
                    keyEvent.type == KeyEventType.KeyDown && when (keyEvent.key) {
                        Key.Enter, Key.NumPadEnter -> {
                            if (isPrimaryEnabled) {
                                handlePrimaryClick()
                                true
                            } else {
                                false
                            }
                        }

                        Key.Escape -> {
                            if (pagina == 1) {
                                pagina = 0
                            } else {
                                onDismissRequest()
                            }
                            true
                        }

                        else -> false
                    }
                }
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = pagina,
                            transitionSpec = {
                                (fadeIn(tween(200)) + slideInVertically { it / 2 })
                                    .togetherWith(fadeOut(tween(150)) + slideOutVertically { -it / 2 })
                            },
                            label = "animacao_titulo"
                        ) { pag ->
                            Column {
                                Text(
                                    text = if (isBatchMode) {
                                        "Processamento em Lote (${batchItems.size} relatórios)"
                                    } else if (pag == 0) {
                                        "Dados Extraídos do Relatório"
                                    } else {
                                        "Medições Extraídas do Relatório"
                                    },
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isBatchMode) {
                                        "Defina os dados globais do cliente e componente. Todos os PDFs válidos serão importados para o projeto."
                                    } else if (pag == 0) {
                                        "Confira os dados extraídos. Os campos destacados precisam ser preenchidos manualmente."
                                    } else {
                                        "Confira as medições extraídas do relatório."
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (!isBatchMode) {
                        StepIndicator(
                            currentStep = pagina,
                            onStepClick = { step ->
                                if (step == 0 || isValid) pagina = step
                            }
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (isBatchMode) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetroDocTextField(
                                    label = "Cliente / Projeto",
                                    value = reportData.cliente,
                                    onValueChange = { onDataChanged(reportData.copy(cliente = it)) },
                                    modifier = Modifier.weight(1f)
                                )
                                MetroDocTextField(
                                    label = "Componente avaliado",
                                    value = reportData.componente,
                                    onValueChange = { onDataChanged(reportData.copy(componente = it)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )

                            Text(
                                text = "Fila de PDFs Importados",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            batchItems.forEach { item ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    border = BorderStroke(
                                        1.dp,
                                        if (item.status == PdfItemStatus.ERROR) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (item.status == PdfItemStatus.ERROR) {
                                                Icon(
                                                    painter = painterResource(Res.drawable.warning),
                                                    contentDescription = "Erro",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Icon(
                                                    painter = painterResource(Res.drawable.confirm),
                                                    contentDescription = "Sucesso",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = item.pdfName,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (item.status == PdfItemStatus.ERROR) {
                                                    Text(
                                                        text = item.errorMessage ?: "Erro ao parsear PDF",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                } else {
                                                    Text(
                                                        text = "${item.reportData.caracteristicas.size} características extraídas • Calypso ID: ${item.reportData.identificadorCalypso.ifBlank { "N/A" }}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                        IconButton(onClick = { onRemoveBatchItem(item.id) }) {
                                            Icon(
                                                painter = painterResource(Res.drawable.close),
                                                contentDescription = "Remover",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        AnimatedContent(
                            targetState = pagina,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally { width -> width / 3 } + fadeIn(tween(250)))
                                        .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut(tween(200)))
                                } else {
                                    (slideInHorizontally { width -> -width / 3 } + fadeIn(tween(250)))
                                        .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut(tween(200)))
                                }
                            },
                            label = "animacao_pagina"
                        ) { pag ->
                            when (pag) {
                                0 -> {
                                    PaginaIdentificacao(
                                        reportData = reportData,
                                        onDataChanged = onDataChanged,
                                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                                    )
                                }

                                1 -> {
                                    PaginaMedicoes(
                                        reportData = reportData,
                                        onMeasurementChanged = onMeasurementChanged,
                                        onAddMeasurement = onAddMeasurement
                                    )
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetroDocOutlinedButton(
                        onClick = onDismissRequest,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                    ) {
                        Text("Cancelar")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!isBatchMode) {
                            AnimatedVisibility(
                                visible = pagina == 1,
                                enter = fadeIn(tween(150)) + slideInHorizontally { 20 },
                                exit = fadeOut(tween(150)) + slideOutHorizontally { 20 }
                            ) {
                                MetroDocOutlinedButton(
                                    onClick = { pagina = 0 },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                ) {
                                    Text("Voltar")
                                }
                            }
                        }
                        MetroDocPrimaryButton(
                            onClick = handlePrimaryClick,
                            enabled = isPrimaryEnabled,
                        ) {
                            Text(
                                if (isBatchMode) "Criar Projeto em Lote"
                                else if (pagina == 0) "Continuar"
                                else "Confirmar"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: Int,
    onStepClick: (Int) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepChip(number = "1", label = "Identificação", isSelected = currentStep == 0, onClick = { onStepClick(0) })
        Text("—", color = MaterialTheme.colorScheme.outline)
        StepChip(number = "2", label = "Medições", isSelected = currentStep == 1, onClick = { onStepClick(1) })
    }
}

@Composable
private fun StepChip(
    number: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(durationMillis = 200),
        label = "chipContainerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "chipContentColor"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = number, style = MaterialTheme.typography.labelMedium)
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}