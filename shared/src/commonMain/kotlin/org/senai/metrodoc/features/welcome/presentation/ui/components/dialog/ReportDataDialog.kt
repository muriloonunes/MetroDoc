package org.senai.metrodoc.features.welcome.presentation.ui.components.dialog

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.senai.metrodoc.common.ui.MetroDocOutlinedButton
import org.senai.metrodoc.common.ui.MetroDocPrimaryButton
import org.senai.metrodoc.features.report.model.MeasurementData
import org.senai.metrodoc.features.report.model.ReportData

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReportDataDialog(
    onDismissRequest: () -> Unit,
    onConfirmData: () -> Unit,
    onDataChanged: (ReportData) -> Unit,
    onMeasurementChanged: (index: Int, updated: MeasurementData) -> Unit,
    onAddMeasurement: () -> Unit,
    reportData: ReportData,
    isValid: Boolean,
) {
    var pagina by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.85f)
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
                        ) { pagina ->
                            Column {
                                Text(
                                    text = if (pagina == 0) "Dados Extraídos do Relatório" else "Medições Extraídas do Relatório",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (pagina == 0) "Confira os dados extraídos. Os campos destacados precisam ser preenchidos manualmente. Você também poderá alterar esses dados depois."
                                    else "Confira as medições extraídas do relatório. Você poderá alterar esses dados depois.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    StepIndicator(
                        currentStep = pagina,
                        onStepClick = { step ->
                            if (step == 0 || isValid) pagina = step
                        }
                    )
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
                    ) { pagina ->
                        when (pagina) {
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
                        MetroDocPrimaryButton(
                            onClick = {
                                if (pagina == 0) {
                                    pagina = 1
                                } else {
                                    onConfirmData()
                                }
                            },
                            enabled = if (pagina == 0) isValid else true,
                        ) {
                            Text(if (pagina == 0) "Continuar" else "Confirmar")
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