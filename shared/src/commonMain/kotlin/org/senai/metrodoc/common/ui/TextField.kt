package org.senai.metrodoc.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TitleEditorTextField(
    title: TextFieldValue,
    onTitleChange: (TextFieldValue) -> Unit,
    focusRequester: FocusRequester,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) =
    BasicTextField(
        value = title,
        onValueChange = onTitleChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { keyEvent ->
                keyEvent.type == KeyEventType.KeyDown && when (keyEvent.key) {
                    Key.Enter, Key.NumPadEnter -> {
                        onConfirm()
                        true
                    }

                    Key.Escape -> {
                        onCancel()
                        true
                    }

                    else -> false
                }
            }
    )

@Composable
fun MetroDocTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isRequired: Boolean = true,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier,
) {
    MetroDocTextFieldBase(
        label = label,
        textIsEmpty = value.isEmpty(),
        textIsBlank = value.isBlank(),
        placeholder = placeholder,
        isRequired = isRequired,
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        modifier = modifier,
        textFieldModifier = textFieldModifier
    ) { textStyle, cursorBrush, interactionSource, finalModifier ->
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            interactionSource = interactionSource,
            textStyle = textStyle,
            cursorBrush = cursorBrush,
            modifier = finalModifier,
            decorationBox = { innerTextField -> innerTextField() }
        )
    }
}

@Composable
fun MetroDocTextField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String = "",
    isRequired: Boolean = true,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier,
) {
    MetroDocTextFieldBase(
        label = label,
        textIsEmpty = value.text.isEmpty(),
        textIsBlank = value.text.isBlank(),
        placeholder = placeholder,
        isRequired = isRequired,
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        modifier = modifier,
        textFieldModifier = textFieldModifier
    ) { textStyle, cursorBrush, interactionSource, finalModifier ->
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            interactionSource = interactionSource,
            textStyle = textStyle,
            cursorBrush = cursorBrush,
            modifier = finalModifier,
            decorationBox = { innerTextField -> innerTextField() }
        )
    }
}

@Composable
private fun MetroDocTextFieldBase(
    label: String,
    textIsEmpty: Boolean,
    textIsBlank: Boolean,
    placeholder: String,
    isRequired: Boolean,
    enabled: Boolean,
    singleLine: Boolean,
    minLines: Int,
    modifier: Modifier,
    textFieldModifier: Modifier,
    content: @Composable (
        textStyle: TextStyle,
        cursorBrush: Brush,
        interactionSource: MutableInteractionSource,
        modifier: Modifier
    ) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isError = isRequired && textIsBlank && enabled
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    }

    val backgroundColor = when {
        !enabled -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f)
        isFocused -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = if (enabled) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )

    val cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (singleLine) Modifier.height(42.dp)
                    else Modifier.defaultMinSize(minHeight = (42 * minLines).dp)
                )
                .background(backgroundColor, RoundedCornerShape(4.dp))
                .border(
                    width = if (isFocused || isError) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(
                    horizontal = 10.dp,
                    vertical = if (singleLine) 0.dp else 8.dp
                ),
            contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
        ) {
            if (textIsEmpty && placeholder.isNotEmpty() && !isFocused) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            } else if (textIsEmpty && isError && !isFocused) {
                Text(
                    text = "Obrigatório",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }

            content(
                textStyle,
                cursorBrush,
                interactionSource,
                textFieldModifier.fillMaxWidth()
            )
        }
    }
}