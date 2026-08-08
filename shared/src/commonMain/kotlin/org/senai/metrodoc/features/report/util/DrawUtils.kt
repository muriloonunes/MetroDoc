package org.senai.metrodoc.features.report.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import org.senai.metrodoc.features.report.model.DrawShape
import org.senai.metrodoc.features.report.presentation.ui.components.ToolType
import kotlin.math.*

fun createDrawing(
    start: Offset,
    end: Offset,
    tool: ToolType,
    nextBadgeNumber: Int,
    isShiftPressed: Boolean,
): DrawShape? {
    return when (tool) {
        ToolType.CIRCLE, ToolType.SQUARE -> {
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

            if (tool == ToolType.CIRCLE) {
                DrawShape.Circle(topLeft = topLeft, size = size)
            } else {
                DrawShape.Rectangle(topLeft = topLeft, size = size)
            }
        }

        ToolType.ARROW -> {
            val finalEnd = if (isShiftPressed) {
                // 1. Pega as distâncias
                val dx = (end.x - start.x).toDouble()
                val dy = (end.y - start.y).toDouble()

                // 2. Calcula a distância total e o ângulo atual
                val distance = hypot(dx, dy)
                val angle = atan2(dy, dx)

                // 3. Trava o ângulo no múltiplo mais próximo de 45° (PI / 4)
                val snapAngle = round(angle / (PI / 4)) * (PI / 4)

                // 4. Projeta o ponto final usando o novo ângulo travado
                Offset(
                    x = start.x + (distance * cos(snapAngle)).toFloat(),
                    y = start.y + (distance * sin(snapAngle)).toFloat()
                )
            } else {
                end
            }

            DrawShape.Arrow(start = start, end = finalEnd)
        }

    }
}

fun DrawScope.drawImageDrawing(
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

        is DrawShape.Rectangle -> {
            drawRect(
                color = drawing.color,
                topLeft = drawing.topLeft,
                size = drawing.size,
                style = Stroke(drawing.strokeWidth)
            )
        }

        is DrawShape.Arrow -> {
            drawLine(
                color = drawing.color,
                start = drawing.start,
                end = drawing.end,
                strokeWidth = drawing.strokeWidth,
                cap = StrokeCap.Round // Deixa as pontas arredondadas (mais bonito)
            )

            //cabeça da seta
            val dx = (drawing.end.x - drawing.start.x).toDouble()
            val dy = (drawing.end.y - drawing.start.y).toDouble()
            val angle = atan2(dy, dx)

            val arrowHeadLength = 40f // Tamanho da cabeça da seta
            val arrowHeadAngle = PI / 6 // Ângulo de abertura (30 graus)

            // Perna esquerda do "V"
            val leftX = drawing.end.x - arrowHeadLength * cos(angle - arrowHeadAngle).toFloat()
            val leftY = drawing.end.y - arrowHeadLength * sin(angle - arrowHeadAngle).toFloat()

            // Perna direita do "V"
            val rightX = drawing.end.x - arrowHeadLength * cos(angle + arrowHeadAngle).toFloat()
            val rightY = drawing.end.y - arrowHeadLength * sin(angle + arrowHeadAngle).toFloat()

            drawLine(
                color = drawing.color,
                start = drawing.end,
                end = Offset(leftX, leftY),
                strokeWidth = drawing.strokeWidth,
                cap = StrokeCap.Round
            )

            drawLine(
                color = drawing.color,
                start = drawing.end,
                end = Offset(rightX, rightY),
                strokeWidth = drawing.strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

fun calculateFitRect(srcSize: Size, dstSize: Size): Rect {
    val srcAspect = srcSize.width / srcSize.height
    val dstAspect = dstSize.width / dstSize.height

    val scale = if (srcAspect > dstAspect) {
        dstSize.width / srcSize.width
    } else {
        dstSize.height / srcSize.height
    }

    val scaledWidth = srcSize.width * scale
    val scaledHeight = srcSize.height * scale

    val left = (dstSize.width - scaledWidth) / 2f
    val top = (dstSize.height - scaledHeight) / 2f

    return Rect(left, top, left + scaledWidth, top + scaledHeight)
}