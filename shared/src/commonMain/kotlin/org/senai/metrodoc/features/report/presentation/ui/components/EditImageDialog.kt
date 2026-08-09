package org.senai.metrodoc.features.report.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    SQUARE,
    NUMBER,
    TEXT
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
    var currentTextColor by remember { mutableStateOf(Color.White) }
    var currentStrokeWidth by remember { mutableStateOf(DrawShape.StrokeWidth.MEDIUM) }
    val drawings = remember { mutableStateListOf<DrawShape>() }
    val redoStack = remember { mutableStateListOf<DrawShape>() }
    val textMeasurer = rememberTextMeasurer()

    var activeTextEditor by remember { mutableStateOf<DrawShape.TextBox.TextEditState?>(null) }
    var activeTextValue by remember { mutableStateOf("") }
    val textFocusRequester = remember { FocusRequester() }

    var badgeNumber by remember { mutableIntStateOf(1) }

    var isShiftPressed by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun undo() {
        if (drawings.isNotEmpty()) {
            val lastItem = drawings.removeLast()

            if (lastItem is DrawShape.ClearGroup) {
                drawings.addAll(lastItem.shapes)
                redoStack.add(lastItem)
            } else {
                redoStack.add(lastItem)
            }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val itemToRestore = redoStack.removeLast()

            if (itemToRestore is DrawShape.ClearGroup) {
                drawings.removeAll(itemToRestore.shapes)
                drawings.add(itemToRestore)
            } else {
                drawings.add(itemToRestore)
            }
        }
    }

    fun clearAll() {
        if (drawings.isNotEmpty() && drawings.none { it is DrawShape.ClearGroup }) {
            val currentShapes = drawings.toList()
            drawings.clear()
            drawings.add(DrawShape.ClearGroup(shapes = currentShapes))
            redoStack.clear()
            badgeNumber = 1
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
                        currentTextColor = currentTextColor,
                        onTextColorSelected = {
                            currentTextColor = it
                        },
                        currentStrokeWidth = currentStrokeWidth,
                        onStrokeWidthSelected = {
                            currentStrokeWidth = it
                        },
                        canUndo = drawings.isNotEmpty(),
                        canRedo = redoStack.isNotEmpty(),
                        onUndo = { undo() },
                        onRedo = { redo() },
                        onClearAll = { clearAll() },
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

                            if (it.type == KeyEventType.KeyDown && (it.isCtrlPressed || it.isMetaPressed)) {
                                when (it.key) {
                                    Key.Z -> {
                                        undo()
                                        return@onKeyEvent true
                                    }

                                    Key.Y -> {
                                        redo()
                                        return@onKeyEvent true
                                    }
                                }
                            }

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
                                                when (selectedTool) {
                                                    ToolType.NUMBER -> {
                                                        detectTapGestures(
                                                            onTap = { offset ->
                                                                createDrawing(
                                                                    start = offset,
                                                                    end = offset,
                                                                    tool = selectedTool,
                                                                    color = currentColor,
                                                                    textcolor = currentTextColor,
                                                                    width = currentStrokeWidth,
                                                                    isShiftPressed = isShiftPressed,
                                                                    nextBadgeNumber = badgeNumber++
                                                                )?.let { shape ->
                                                                    drawings.add(shape)
                                                                    redoStack.clear()
                                                                }
                                                            }
                                                        )
                                                    }

                                                    else -> {
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
                                                                    if (selectedTool == ToolType.TEXT) {
                                                                        val previewShape = createDrawing(
                                                                            start = start,
                                                                            end = end,
                                                                            tool = selectedTool,
                                                                            color = currentColor,
                                                                            textcolor = currentTextColor,
                                                                            width = currentStrokeWidth,
                                                                            nextBadgeNumber = 0,
                                                                            isShiftPressed = isShiftPressed
                                                                        ) as? DrawShape.TextBox

                                                                        if (previewShape != null && previewShape.size.width > 20f && previewShape.size.height > 20f) {
                                                                            activeTextEditor =
                                                                                DrawShape.TextBox.TextEditState(
                                                                                    topLeft = previewShape.topLeft,
                                                                                    size = previewShape.size,
                                                                                    backgroundColor = previewShape.color,
                                                                                    textColor = previewShape.textColor,
                                                                                    strokeWidth = previewShape.strokeWidth
                                                                                )
                                                                            activeTextValue = ""
                                                                        }
                                                                    } else {
                                                                        createDrawing(
                                                                            start,
                                                                            end,
                                                                            selectedTool,
                                                                            currentColor,
                                                                            currentTextColor,
                                                                            currentStrokeWidth,
                                                                            badgeNumber++,
                                                                            isShiftPressed
                                                                        )?.let { shape ->
                                                                            drawings.add(shape)
                                                                            redoStack.clear()
                                                                        }
                                                                    }
                                                                }
                                                                dragStartOffset = null
                                                                dragCurrentOffset = null
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                    ) {
                                        drawings.forEach { draw ->
                                            drawImageDrawing(draw, textMeasurer)
                                        }

                                        val start = dragStartOffset
                                        val end = dragCurrentOffset
                                        if (start != null && end != null) {
                                            val previewShape = createDrawing(
                                                start = start,
                                                end = end,
                                                tool = selectedTool,
                                                color = currentColor,
                                                textcolor = currentTextColor,
                                                width = currentStrokeWidth,
                                                isShiftPressed = isShiftPressed,
                                                nextBadgeNumber = badgeNumber
                                            )

                                            if (previewShape != null) {
                                                drawImageDrawing(previewShape, textMeasurer)
                                            }
                                        }
                                    }
                                    activeTextEditor?.let { editor ->
                                        val density = LocalDensity.current
                                        val offsetX = with(density) { (editor.topLeft.x + bounds.left).toDp() }
                                        val offsetY = with(density) { (editor.topLeft.y + bounds.top).toDp() }
                                        val width = with(density) { editor.size.width.toDp() }
                                        val height = with(density) { editor.size.height.toDp() }

                                        LaunchedEffect(editor) {
                                            textFocusRequester.requestFocus()
                                        }

                                        Box(
                                            modifier = Modifier
                                                .offset(x = offsetX, y = offsetY)
                                                .size(width, height)
                                                .background(Color.Black.copy(alpha = 0.5f))
                                                .border(1.dp, editor.backgroundColor)
                                                .padding(8.dp)
                                        ) {
                                            BasicTextField(
                                                value = activeTextValue,
                                                onValueChange = { activeTextValue = it },
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .focusRequester(textFocusRequester)
                                                    .onPreviewKeyEvent { keyEvent ->
                                                        keyEvent.type == KeyEventType.KeyDown && when (keyEvent.key) {
                                                            Key.Escape -> {
                                                                activeTextEditor = null
                                                                focusRequester.requestFocus()
                                                                true
                                                            }

                                                            Key.Enter, Key.NumPadEnter -> {
                                                                if (keyEvent.isCtrlPressed || keyEvent.isMetaPressed) {
                                                                    if (activeTextValue.isNotBlank()) {
                                                                        drawings.add(
                                                                            DrawShape.TextBox(
                                                                                text = activeTextValue,
                                                                                topLeft = editor.topLeft,
                                                                                size = editor.size,
                                                                                color = editor.backgroundColor,
                                                                                textColor = editor.textColor,
                                                                                strokeWidth = editor.strokeWidth
                                                                            )
                                                                        )
                                                                    }
                                                                    activeTextEditor = null
                                                                    focusRequester.requestFocus()
                                                                }
                                                                false
                                                            }


                                                            else -> false
                                                        }
                                                    },
                                                textStyle = TextStyle(color = editor.textColor, fontSize = 16.sp),
                                                cursorBrush = SolidColor(editor.textColor)
                                            )
                                            Text(
                                                text = "Ctrl + Enter p/ salvar | Esc p/ cancelar",
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 10.sp,
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .offset(y = 24.dp) // Fica um pouco para baixo da caixa
                                            )
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
    currentTextColor: Color,
    onTextColorSelected: (Color) -> Unit,
    currentStrokeWidth: DrawShape.StrokeWidth,
    onStrokeWidthSelected: (DrawShape.StrokeWidth) -> Unit,
    canUndo: Boolean,
    onUndo: () -> Unit,
    canRedo: Boolean,
    onRedo: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showStrokeMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
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
                        containerColor = if (selectedTool == ToolType.CIRCLE) MaterialTheme.colorScheme.surface else Color.Transparent,
                        contentColor = if (selectedTool == ToolType.CIRCLE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.circle),
                        contentDescription = "Círculo",
                        modifier = Modifier.size(20.dp)
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
                        containerColor = if (selectedTool == ToolType.SQUARE) MaterialTheme.colorScheme.surface else Color.Transparent,
                        contentColor = if (selectedTool == ToolType.SQUARE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.rectangle),
                        contentDescription = "Retângulo",
                        modifier = Modifier.size(20.dp)
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
                        containerColor = if (selectedTool == ToolType.ARROW) MaterialTheme.colorScheme.surface else Color.Transparent,
                        contentColor = if (selectedTool == ToolType.ARROW) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_outward),
                        contentDescription = "Seta",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { PlainTooltip { Text("Numeração") } },
                state = rememberTooltipState()
            ) {
                IconButton(
                    onClick = { onToolSelected(ToolType.NUMBER) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (selectedTool == ToolType.NUMBER) MaterialTheme.colorScheme.surface else Color.Transparent,
                        contentColor = if (selectedTool == ToolType.NUMBER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.badge),
                        contentDescription = "Numeração",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { PlainTooltip { Text("Texto") } },
                state = rememberTooltipState()
            ) {
                IconButton(
                    onClick = { onToolSelected(ToolType.TEXT) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (selectedTool == ToolType.TEXT) MaterialTheme.colorScheme.surface else Color.Transparent,
                        contentColor = if (selectedTool == ToolType.TEXT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.insert_text),
                        contentDescription = "Texto",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            VerticalDivider(
                modifier = Modifier
                    .height(20.dp)
                    .padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { PlainTooltip { Text("Cor da marcação") } },
                state = rememberTooltipState()
            ) {
                IconButton(
                    onClick = {
                        showNativeColorPicker(currentColor, onColorSelected)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(currentColor, shape = CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), CircleShape)
                    )
                }
            }

            if (selectedTool == ToolType.TEXT || selectedTool == ToolType.NUMBER) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                    tooltip = { PlainTooltip { Text("Cor do texto") } },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        onClick = {
                            showNativeColorPicker(currentTextColor, onTextColorSelected)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.text_color),
                            contentDescription = "Cor do texto",
                            tint = currentTextColor,
                            modifier = Modifier.size(20.dp)
                                .border(width = 1.5.dp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            Box {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                    tooltip = { PlainTooltip { Text("Espessura do traço") } },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        onClick = { showStrokeMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.line_weight),
                            contentDescription = "Espessura da marcação",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showStrokeMenu,
                    onDismissRequest = { showStrokeMenu = false },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        StrokeOptionButton(
                            iconRes = Res.drawable.stroke_thin,
                            label = "Fina",
                            isSelected = currentStrokeWidth == DrawShape.StrokeWidth.THIN,
                            onClick = {
                                onStrokeWidthSelected(DrawShape.StrokeWidth.THIN)
                                showStrokeMenu = false
                            }
                        )

                        StrokeOptionButton(
                            iconRes = Res.drawable.stroke_medium,
                            label = "Média",
                            isSelected = currentStrokeWidth == DrawShape.StrokeWidth.MEDIUM,
                            onClick = {
                                onStrokeWidthSelected(DrawShape.StrokeWidth.MEDIUM)
                                showStrokeMenu = false
                            }
                        )

                        StrokeOptionButton(
                            iconRes = Res.drawable.stroke_thick,
                            label = "Grossa",
                            isSelected = currentStrokeWidth == DrawShape.StrokeWidth.THICK,
                            onClick = {
                                onStrokeWidthSelected(DrawShape.StrokeWidth.THICK)
                                showStrokeMenu = false
                            }
                        )
                    }
                }
            }

            VerticalDivider(
                modifier = Modifier
                    .height(20.dp)
                    .padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { PlainTooltip { Text("Desfazer") } },
                state = rememberTooltipState()
            ) {
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.undo),
                        contentDescription = "Desfazer (Ctrl+Z)",
                        tint = if (canUndo) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.38f
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { PlainTooltip { Text("Refazer (Ctrl+Y)") } },
                state = rememberTooltipState()
            ) {
                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.redo),
                        contentDescription = "Refazer",
                        tint = if (canRedo) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.38f
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { PlainTooltip { Text("Limpar todas as marcações") } },
                state = rememberTooltipState()
            ) {
                IconButton(
                    onClick = onClearAll,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.clear_all),
                        contentDescription = "Limpar tudo",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StrokeOptionButton(
    iconRes: org.jetbrains.compose.resources.DrawableResource,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            modifier = Modifier.size(18.dp)
        )
    }
}