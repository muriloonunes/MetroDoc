package org.senai.metrodoc.common.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import metrodoc.shared.generated.resources.Res
import metrodoc.shared.generated.resources.add_circle
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.features.welcome.presentation.ui.components.dashedBorder

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MetroDocAddButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val buttonSize = ButtonDefaults.MediumContainerHeight

    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        border = null,
        interactionSource = interactionSource,
        contentPadding = ButtonDefaults.contentPaddingFor(buttonSize, hasStartIcon = true),
        modifier = modifier
            .heightIn(min = 50.dp)
            .dashedBorder(
                width = Dp.Hairline,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(10.dp),
                on = if (isHovered) 5.dp else 5.dp,
                off = if (isHovered) 0.dp else 10.dp
            ),
    ) {
        Icon(
            painter = painterResource(Res.drawable.add_circle),
            contentDescription = "Adicionar",
            modifier = Modifier.size(ButtonDefaults.iconSizeFor(buttonSize)),
        )
        Spacer(modifier = Modifier.width(ButtonDefaults.iconSpacingFor(buttonSize)))
        Text(
            text = text,
            style = ButtonDefaults.textStyleFor(buttonSize),
        )
    }
}

@Composable
fun MetroDocOutlinedButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        enabled = enabled,
        contentPadding = contentPadding,
        modifier = modifier,
        content = content,
    )
}

@Composable
fun MetroDocOutlinedIconButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    OutlinedIconButton(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        enabled = enabled,
        border = ButtonDefaults.outlinedButtonBorder(enabled),
        modifier = modifier,
        content = content
    )
}

@Composable
fun MetroDocPrimaryButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier,
        contentPadding = contentPadding,
        enabled = enabled,
        content = content,
    )
}