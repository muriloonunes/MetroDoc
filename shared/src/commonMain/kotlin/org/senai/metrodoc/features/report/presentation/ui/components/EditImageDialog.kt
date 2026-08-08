package org.senai.metrodoc.features.report.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import metrodoc.shared.generated.resources.Res
import metrodoc.shared.generated.resources.close
import metrodoc.shared.generated.resources.confirm
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.ui.MetroDocOutlinedButton
import org.senai.metrodoc.common.ui.MetroDocPrimaryButton
import org.senai.metrodoc.features.report.model.DrawShape
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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
    var selectedTool by remember { mutableStateOf(ToolType.CIRCLE) }
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
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Imagem a ser editada",
                                contentScale = ContentScale.Fit,
                                onState = { imageState = it },
                                modifier = Modifier
                                    .fillMaxSize()
                            )

                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(selectedTool) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                dragStartOffset = offset
                                                dragCurrentOffset = offset
                                            },
                                            onDrag = { change, _ ->
                                                dragCurrentOffset = change.position
                                            },
                                            onDragEnd = {
                                                val start = dragStartOffset
                                                val end = dragCurrentOffset

                                                if (start != null && end != null) {
                                                    createDrawing(
                                                        start = start,
                                                        end = end,
                                                        tool = selectedTool,
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
                                        isShiftPressed = isShiftPressed,
                                        nextBadgeNumber = 0
                                    )

                                    if (previewShape != null) {
                                        drawImageDrawing(previewShape)
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

private fun createDrawing(
    start: Offset,
    end: Offset,
    tool: ToolType,
    nextBadgeNumber: Int,
    isShiftPressed: Boolean,
): DrawShape? {
    return when (tool) {
        ToolType.CIRCLE -> {
            val finalEnd = if (isShiftPressed) {
                val dx = end.x - start.x
                val dy = end.y - start.y
                val sideLength = max(abs(dx), abs(dy))

                val signX = if (dx < 0) -1f else 1f
                val signY = if (dy < 0) -1f else 1f

                Offset(
                    x = start.x + (sideLength * signX),
                    y = start.y + (sideLength * signY)
                )
            } else {
                end
            }

            val topLeft = Offset(
                x = min(start.x, finalEnd.x),
                y = min(start.y, finalEnd.y)
            )
            val size = Size(
                width = abs(start.x - finalEnd.x),
                height = abs(start.y - finalEnd.y)
            )

            DrawShape.Circle(topLeft = topLeft, size = size)
        }

        ToolType.ARROW -> {
            null
        }

        ToolType.SQUARE -> {
            null
        }
    }
}

private fun DrawScope.drawImageDrawing(
    drawing: DrawShape,
) {
    when (drawing) {
        is DrawShape.Circle -> {
            drawOval(
                color = drawing.color,
                topLeft = drawing.topLeft,
                size = drawing.size,
                style = Stroke(drawing.strokeWidth)
            )
        }
    }
}
