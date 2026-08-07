package org.senai.metrodoc.features.welcome.presentation.ui.components.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.senai.metrodoc.common.ui.ConfirmDialog
import org.senai.metrodoc.common.ui.MetroDocOutlinedButton

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConfirmDeleteProjectDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmDialog(
        title = "Excluir projeto",
        description = "Tem certeza que deseja excluir esse projeto? Essa ação é irreversível.",
        onDismiss = onDismiss,
        buttons = {
            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                shape = ButtonDefaults.squareShape
            ) {
                Text(
                    text = "Cancelar",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            MetroDocOutlinedButton(
                onClick = onConfirm,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "Excluir",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConfirmDeleteAllProjectsDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmDialog(
        title = "Excluir todos os projetos",
        description = "Tem certeza que deseja excluir todos os projetos? Essa ação é irreversível.",
        onDismiss = onDismiss,
        buttons = {
            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                shape = ButtonDefaults.squareShape
            ) {
                Text(
                    text = "Cancelar",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            MetroDocOutlinedButton(
                onClick = onConfirm,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "Excluir todos",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}
