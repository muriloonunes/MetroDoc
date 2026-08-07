package org.senai.metrodoc.features.report.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import metrodoc.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.ui.MetroDocOutlinedIconButton
import org.senai.metrodoc.common.ui.MetroDocPrimaryButton
import org.senai.metrodoc.common.ui.MetroDocTextField
import org.senai.metrodoc.features.report.model.SavedState

@Composable
fun TopToolbar(
    title: String,
    savedState: SavedState,
    onUpdateTitle: (String) -> Unit,
    onBackClick: () -> Unit,
    onSave: () -> Unit,
    onEmitReportClick: () -> Unit,
    onFocusRoot: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val focusRequester = remember { FocusRequester() }

    var editarTitulo by remember { mutableStateOf(false) }
    var textFieldValue by remember(title, editarTitulo) {
        mutableStateOf(
            TextFieldValue(
                text = title,
                selection = TextRange(title.length)
            )
        )
    }

    LaunchedEffect(editarTitulo) {
        if (editarTitulo) {
            focusRequester.requestFocus()
        } else {
            onFocusRoot()
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.hoverable(interactionSource)
        ) {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { PlainTooltip { Text("Voltar") } },
                state = rememberTooltipState(),
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.back),
                        contentDescription = "Voltar",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (editarTitulo) {
                MetroDocTextField(
                    label = "",
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    singleLine = true,
                    modifier = Modifier.widthIn(max = 630.dp),
                    textFieldModifier = Modifier
                        .focusRequester(focusRequester)
                        .onPreviewKeyEvent { keyEvent ->
                            keyEvent.type == KeyEventType.KeyDown && when (keyEvent.key) {
                                Key.Enter, Key.NumPadEnter -> {
                                    val trimmed = textFieldValue.text.trim()
                                    if (trimmed.isNotEmpty()) {
                                        onUpdateTitle(trimmed)
                                    }
                                    editarTitulo = false
                                    true
                                }

                                Key.Escape -> {
                                    editarTitulo = false
                                    true
                                }

                                else -> false
                            }
                        }
                )
                IconButton(
                    onClick = {
                        editarTitulo = false
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.close),
                        contentDescription = "Cancelar",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    )
                }
                IconButton(
                    onClick = {
                        val trimmed = textFieldValue.text.trim()
                        if (trimmed.isNotEmpty()) {
                            onUpdateTitle(trimmed)
                        }
                        editarTitulo = false
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.confirm),
                        contentDescription = "Confirmar",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    )
                }
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                AnimatedVisibility(
                    visible = savedState == SavedState.Unsaved,
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(150))
                ) {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                        tooltip = { PlainTooltip { Text("Alterações não salvas") } },
                        state = rememberTooltipState(),
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(7.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }


            AnimatedVisibility(
                visible = isHovered && !editarTitulo,
                enter = fadeIn(animationSpec = tween(150)),
                exit = fadeOut(animationSpec = tween(150))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { editarTitulo = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.edit),
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SaveIconButton(
                savedState = savedState,
                onSave = onSave
            )
            Spacer(modifier = Modifier.width(4.dp))
            MetroDocPrimaryButton(
                onClick = onEmitReportClick,
            ) {
                Text(
                    text = "Emitir Relatório",
                )
            }
        }
    }
}

@Composable
fun SaveIconButton(
    savedState: SavedState,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tooltipText = when (savedState) {
        SavedState.Saved -> "Projeto salvo"
        SavedState.Unsaved -> "Salvar projeto"
        SavedState.Saving -> "Salvando..."
        SavedState.JustSaved -> "Salvo com sucesso!"
    }
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Left),
        tooltip = { PlainTooltip { Text(tooltipText) } },
        state = rememberTooltipState(),
    ) {
        MetroDocOutlinedIconButton(
            onClick = {
                onSave()
            },
            enabled = savedState == SavedState.Unsaved,
            modifier = modifier.size(36.dp)
        ) {
            AnimatedContent(
                targetState = savedState,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.8f))
                        .togetherWith(fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.8f))
                },
                label = "animacao_botao_salvar"
            ) { targetState ->
                when (targetState) {
                    SavedState.Saving -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    SavedState.JustSaved -> {
                        Icon(
                            painter = painterResource(Res.drawable.confirm),
                            contentDescription = "Salvo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    SavedState.Saved -> {
                        Icon(
                            painter = painterResource(Res.drawable.save),
                            contentDescription = "Salvo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    SavedState.Unsaved -> {
                        Icon(
                            painter = painterResource(Res.drawable.save),
                            contentDescription = "Salvar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}