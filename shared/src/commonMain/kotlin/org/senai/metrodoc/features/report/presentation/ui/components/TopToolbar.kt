package org.senai.metrodoc.features.report.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import metrodoc.shared.generated.resources.Res
import metrodoc.shared.generated.resources.back
import metrodoc.shared.generated.resources.edit
import metrodoc.shared.generated.resources.save
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.ui.MetroDocOutlinedIconButton
import org.senai.metrodoc.common.ui.MetroDocPrimaryButton

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopToolbar(
    title: String,
    onBackClick: () -> Unit,
    onEmitReportClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(67.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp),
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
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.back),
                        contentDescription = "Voltar",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (isHovered) {
                Spacer(modifier = Modifier.width(12.dp))
                AnimatedVisibility(visible = true) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(IconButtonDefaults.smallContainerSize())
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.edit),
                            contentDescription = "Editar",
                            modifier = Modifier.size(IconButtonDefaults.smallIconSize)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        MetroDocOutlinedIconButton(
            onClick = {},
        ) {
            Icon(
                painter = painterResource(Res.drawable.save),
                contentDescription = "Salvar",
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        MetroDocPrimaryButton(
            onClick = onEmitReportClick,
        ) {
            Text(
                text = "Emitir Relatório",
            )
        }
    }
}