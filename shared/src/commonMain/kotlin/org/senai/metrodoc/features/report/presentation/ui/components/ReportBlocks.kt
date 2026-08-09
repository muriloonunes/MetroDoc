package org.senai.metrodoc.features.report.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import metrodoc.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.ui.MetroDocOutlinedButton
import org.senai.metrodoc.common.ui.MetroDocTextField
import org.senai.metrodoc.features.report.model.Imagem
import org.senai.metrodoc.features.report.model.ReportBlock
import org.senai.metrodoc.features.report.model.ReportBlock.Texto

@Composable
fun BlockWrapper(
    tituloBloco: String,
    index: Int,
    totalCount: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tituloBloco,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    if (index > 0) {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                            Icon(
                                painter = painterResource(Res.drawable.up),
                                contentDescription = "Subir Bloco",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (index < totalCount - 1) {
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                            Icon(
                                painter = painterResource(Res.drawable.down),
                                contentDescription = "Descer Bloco",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(
                            painter = painterResource(Res.drawable.remove),
                            contentDescription = "Remover Bloco",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun TextoBlocoEditor(
    block: Texto,
    onUpdate: (Texto) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (block.emTopicos) "Conteúdo (em Tópicos)" else "Conteúdo (Parágrafo)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = !block.emTopicos,
                    onClick = { onUpdate(block.copy(emTopicos = false)) },
                    label = { Text("Texto") },
                    shape = RoundedCornerShape(6.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilterChip(
                    selected = block.emTopicos,
                    onClick = { onUpdate(block.copy(emTopicos = true)) },
                    label = { Text("Tópicos") },
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }

        MetroDocTextField(
            label = "",
            value = block.conteudo,
            placeholder = if (block.emTopicos) "Digite cada tópico em uma linha (pressione Enter para novo tópico)" else "Digite seu texto aqui",
            singleLine = false,
            minLines = 4,
            onValueChange = { onUpdate(block.copy(conteudo = it)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun GaleriaImagemBlocoEditor(
    block: ReportBlock.GaleriaImagem,
    onUpdate: (ReportBlock.GaleriaImagem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val imagePickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        mode = FileKitMode.Multiple()
    ) { files ->
        if (!files.isNullOrEmpty()) {
            val novasImagens = files.map { file ->
                Imagem(nome = file.name, path = file.path)
            }
            onUpdate(block.copy(imagens = block.imagens + novasImagens))
        }
    }
    val temLegendasIndividuais = remember(block.imagens) {
        block.imagens.any { it.legenda.isNotBlank() }
    }
    val temLegendaGeral = block.legenda.isNotBlank()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Disposição:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                (1..4).forEach { col ->
                    FilterChip(
                        selected = block.colunas == col,
                        onClick = { onUpdate(block.copy(colunas = col)) },
                        label = { Text("$col col") },
                        modifier = Modifier.padding(end = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            }
            MetroDocOutlinedButton(
                onClick = { imagePickerLauncher.launch() },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text("+ Fotos")
            }
        }
        MetroDocTextField(
            label = "Legenda da Galeria (Opcional)",
            value = block.legenda,
            isRequired = false,
            placeholder = if (temLegendasIndividuais) "Bloqueada: limpe as legendas individuais abaixo para usar" else "Ex: Fotos das etapas de inspeção",
            enabled = (block.imagens.isNotEmpty() && !temLegendasIndividuais), // só tá ativa se nenhuma das fotos tiver legenda individual
            onValueChange = { onUpdate(block.copy(legenda = it)) },
            modifier = Modifier.fillMaxWidth()
        )
        if (block.imagens.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Nenhuma imagem adicionada nesta galeria.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                block.imagens.forEachIndexed { index, imagem ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.file),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = modifier.size(24.dp)
                            )
                            Text(
                                text = imagem.nome,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            MetroDocTextField(
                                label = "",
                                value = imagem.legenda,
                                placeholder = if (temLegendaGeral) "Bloqueada por legenda geral" else "Legenda desta foto",
                                enabled = !temLegendaGeral,
                                isRequired = false,
                                onValueChange = { novaLegenda ->
                                    val listaAtualizada = block.imagens.toMutableList()
                                    listaAtualizada[index] = imagem.copy(legenda = novaLegenda)
                                    onUpdate(block.copy(imagens = listaAtualizada))
                                },
                                modifier = Modifier.weight(1.5f)
                            )
                            IconButton(
                                onClick = {
                                    val listaAtualizada = block.imagens.toMutableList()
                                    listaAtualizada.removeAt(index)
                                    onUpdate(block.copy(imagens = listaAtualizada))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.remove),
                                    contentDescription = "Remover Foto",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}