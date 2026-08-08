package org.senai.metrodoc.features.report.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import metrodoc.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.ui.MetroDocOutlinedButton
import org.senai.metrodoc.common.ui.MetroDocPrimaryButton
import org.senai.metrodoc.features.report.model.DrawShape
import org.senai.metrodoc.features.report.util.calculateFitRect
import org.senai.metrodoc.features.report.util.createDrawing
import org.senai.metrodoc.features.report.util.drawImageDrawing
import org.senai.metrodoc.features.report.util.showNativeColorPicker
import java.io.File
import kotlin.math.roundToInt

enum class ToolType {
    ARROW,
    CIRCLE,
    SQUARE
}

@Composable
fun EditImageDialog(
    onDismissRequest: () -> Unit,
    imagePath: String?,
    onConfirmEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageModel = imagePath?.takeIf { it.isNotBlank() }?.let(::File)
    var imageState by remember(imageModel) {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }
    var imageBounds by remember { mutableStateOf<Rect?>(null) }

    var selectedTool by remember { mutableStateOf(ToolType.CIRCLE) }
    var currentColor by remember { mutableStateOf(Color.Red) }
    val drawings = remember { mutableStateListOf<DrawShape>() }

    var isShiftPressed by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.88f)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Editar imagem",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Revise a imagem antes de confirmar as alterações.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnnotationToolbar(
                        selectedTool = selectedTool,
                        onToolSelected = {
                            selectedTool = it
                            focusRequester.requestFocus()
                        },
                        currentColor = currentColor,
                        onColorSelected = {
                            currentColor = it
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.close),
                                contentDescription = "Fechar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
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
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                        .focusRequester(focusRequester)
                        .focusable()
                        .onKeyEvent {
                            isShiftPressed = it.isShiftPressed
                            false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        imageModel == null -> {
                            Text(
                                text = "Nenhuma imagem selecionada.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        imageState is AsyncImagePainter.State.Error -> {
                            Text(
                                text = "Não foi possível carregar a imagem.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        else -> {
                            var dragStartOffset by remember { mutableStateOf<Offset?>(null) }
                            var dragCurrentOffset by remember { mutableStateOf<Offset?>(null) }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .onGloballyPositioned {
                                        val containerSize = it.size.toSize()

                                        imageState.painter?.intrinsicSize?.let { intrinsicSize ->
                                            imageBounds = calculateFitRect(intrinsicSize, containerSize)
                                        }
                                    }
                            ) {
                                AsyncImage(
                                    model = imageModel,
                                    contentDescription = "Imagem a ser editada",
                                    contentScale = ContentScale.Fit,
                                    onState = { imageState = it },
                                    modifier = Modifier
                                        .fillMaxSize(),
                                )

                                imageBounds?.let { bounds ->
                                    Canvas(
                                        modifier = Modifier
                                            .offset { IntOffset(bounds.left.roundToInt(), bounds.top.roundToInt()) }
                                            .size(
                                                width = with(LocalDensity.current) { bounds.width.toDp() },
                                                height = with(LocalDensity.current) { bounds.height.toDp() }
                                            )
                                            .clipToBounds()
                                            .pointerInput(selectedTool) {
                                                detectDragGestures(
                                                    onDragStart = { offset ->
                                                        focusRequester.requestFocus()
                                                        val clampedOffset = Offset(
                                                            x = offset.x.coerceIn(0f, bounds.width),
                                                            y = offset.y.coerceIn(0f, bounds.height)
                                                        )
                                                        dragStartOffset = clampedOffset
                                                        dragCurrentOffset = clampedOffset
                                                    },
                                                    onDrag = { change, _ ->
                                                        dragCurrentOffset = Offset(
                                                            x = change.position.x.coerceIn(0f, bounds.width),
                                                            y = change.position.y.coerceIn(0f, bounds.height)
                                                        )
                                                    },
                                                    onDragEnd = {
                                                        val start = dragStartOffset
                                                        val end = dragCurrentOffset

                                                        if (start != null && end != null) {
                                                            createDrawing(
                                                                start = start,
                                                                end = end,
                                                                tool = selectedTool,
                                                                color = currentColor,
                                                                nextBadgeNumber = drawings.size + 1,
                                                                isShiftPressed = isShiftPressed,
                                                            )?.let { shape ->
                                                                drawings.add(shape)
                                                            }
                                                        }
                                                        dragStartOffset = null
                                                        dragCurrentOffset = null
                                                    }
                                                )
                                            }
                                    ) {
                                        drawings.forEach { draw ->
                                            drawImageDrawing(draw)
                                        }

                                        val start = dragStartOffset
                                        val end = dragCurrentOffset
                                        if (start != null && end != null) {
                                            val previewShape = createDrawing(
                                                start = start,
                                                end = end,
                                                tool = selectedTool,
                                                color = currentColor,
                                                isShiftPressed = isShiftPressed,
                                                nextBadgeNumber = 0
                                            )

                                            if (previewShape != null) {
                                                drawImageDrawing(previewShape)
                                            }
                                        }
                                    }
                                }
                            }


                            if (imageState is AsyncImagePainter.State.Loading ||
                                imageState is AsyncImagePainter.State.Empty
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 3.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Carregando imagem",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetroDocOutlinedButton(
                        onClick = onDismissRequest,
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 6.dp
                        ),
                    ) {
                        Text("Cancelar")
                    }

                    MetroDocPrimaryButton(
                        onClick = onConfirmEdit,
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 6.dp
                        ),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.confirm),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

@Composable
fun AnnotationToolbar(
    selectedTool: ToolType,
    onToolSelected: (ToolType) -> Unit,
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { PlainTooltip { Text("Círculo") } },
                state = rememberTooltipState()
            ) {
                IconButton(
                    onClick = { onToolSelected(ToolType.CIRCLE) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (selectedTool == ToolType.CIRCLE) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            Color.Transparent
                        },
                        contentColor = if (selectedTool == ToolType.CIRCLE) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    ),
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.circle),
                        contentDescription = "Círculo",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { PlainTooltip { Text("Retângulo") } },
                state = rememberTooltipState()
            ) {
                IconButton(
                    onClick = { onToolSelected(ToolType.SQUARE) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (selectedTool == ToolType.SQUARE) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            Color.Transparent
                        },
                        contentColor = if (selectedTool == ToolType.SQUARE) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    ),
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.rectangle),
                        contentDescription = "Quadrado",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { PlainTooltip { Text("Seta") } },
                state = rememberTooltipState()
            ) {
                IconButton(
                    onClick = { onToolSelected(ToolType.ARROW) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (selectedTool == ToolType.ARROW) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            Color.Transparent
                        },
                        contentColor = if (selectedTool == ToolType.ARROW) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    ),
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_outward),
                        contentDescription = "Seta",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            VerticalDivider(
                modifier = Modifier.height(20.dp).padding(horizontal = 2.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { PlainTooltip { Text("Cor da marcação") } },
                state = rememberTooltipState()
            ) {
                //todo se possivel, trocar o color picker pra usar o dessa biblioteca https://github.com/skydoves/colorpicker-compose
                IconButton(
                    onClick = {
                        showNativeColorPicker(currentColor, onColorSelected)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(currentColor, shape = CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), CircleShape)
                    )
                }
            }
        }
    }
}