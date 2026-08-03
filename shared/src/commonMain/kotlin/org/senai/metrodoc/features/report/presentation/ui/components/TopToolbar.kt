package org.senai.metrodoc.features.report.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import metrodoc.shared.generated.resources.Res
import metrodoc.shared.generated.resources.back
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.ui.MetroDocPrimaryButton

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopToolbar(
    title: String,
    onBackClick: () -> Unit,
    onEmitReportClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(67.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
        }
        MetroDocPrimaryButton(
            onClick = onEmitReportClick,
        ) {
            Text(
                text = "Emitir Relatório",
            )
        }
    }
}