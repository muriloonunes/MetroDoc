package org.senai.metrodoc.features.report.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import org.senai.metrodoc.features.report.model.DrawShape
import org.senai.metrodoc.features.report.presentation.ui.components.ToolType
import javax.swing.JColorChooser
import kotlin.math.*

fun createDrawing(
    start: Offset,
    end: Offset,
    tool: ToolType,
    color: Color,
    textcolor: Color,
    width: DrawShape.StrokeWidth,
    nextBadgeNumber: Int,
    isShiftPressed: Boolean,
): DrawShape? {
    return when (tool) {
        ToolType.CIRCLE, ToolType.SQUARE, ToolType.TEXT -> {
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

            when (tool) {
                ToolType.CIRCLE -> {
                    DrawShape.Circle(topLeft = topLeft, size = size, color = color, strokeWidth = width.value)
                }
                ToolType.SQUARE -> {
                    DrawShape.Rectangle(topLeft = topLeft, size = size, color = color, strokeWidth = width.value)
                }
                else -> {
                    DrawShape.TextBox(
                        text = "",
                        topLeft = topLeft,
                        size = size,
                        color = color,
                        textColor = textcolor,
                        strokeWidth = width.value
                    )
                }
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

            DrawShape.Arrow(start = start, end = finalEnd, color = color, strokeWidth = width.value)
        }

        ToolType.NUMBER -> {
            DrawShape.NumberBadge(
                center = start,
                number = nextBadgeNumber,
                color = color,
                textColor = textcolor
            )
        }

        ToolType.ERASER -> {
            null
        }
    }
}

fun DrawScope.drawImageDrawing(
    drawing: DrawShape,
    textMeasurer: TextMeasurer,
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

        is DrawShape.NumberBadge -> {
            drawCircle(
                color = drawing.color,
                center = drawing.center,
                radius = drawing.radius,
            )

            val textLayoutResult = textMeasurer.measure(
                text = drawing.number.toString(),
                style = TextStyle(color = drawing.textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )
            val textSize = textLayoutResult.size
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = drawing.center.x - (textSize.width / 2),
                    y = drawing.center.y - (textSize.height / 2)
                )
            )
        }

        is DrawShape.TextBox -> {
            if (drawing.text.isEmpty()) {
                drawRect(
                    color = if (drawing.color.alpha < 0.5f) drawing.color.copy(alpha = 0.8f) else drawing.color,
                    topLeft = drawing.topLeft,
                    size = drawing.size,
                    style = Stroke(
                        width = drawing.strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
            } else {
                drawRect(
                    color = drawing.color,
                    topLeft = drawing.topLeft,
                    size = drawing.size
                )

                val safeWidth = (drawing.size.width - 16f).coerceAtLeast(0f)
                val safeHeight = (drawing.size.height - 16f).coerceAtLeast(0f)

                drawText(
                    textMeasurer = textMeasurer,
                    text = drawing.text,
                    topLeft = Offset(drawing.topLeft.x + 8f, drawing.topLeft.y + 8f),
                    style = TextStyle(color = drawing.textColor, fontSize = 20.sp),
                    size = Size(safeWidth, safeHeight),
                    overflow = TextOverflow.Clip
                )
            }
        }

        is DrawShape.ClearGroup, is DrawShape.Erased -> {
        }
    }
}

fun isPointInsideShape(
    clickOffset: Offset,
    shape: DrawShape,
): Boolean {
    return when (shape) {
        is DrawShape.Circle -> {
            Rect(shape.topLeft, shape.size).contains(clickOffset)
        }

        is DrawShape.Rectangle -> {
            Rect(shape.topLeft, shape.size).contains(clickOffset)
        }

        is DrawShape.TextBox -> {
            Rect(shape.topLeft, shape.size).contains(clickOffset)
        }

        is DrawShape.Arrow -> {
            val minX = min(shape.start.x, shape.end.x) - 10f
            val maxX = max(shape.start.x, shape.end.x) + 10f
            val minY = min(shape.start.y, shape.end.y) - 10f
            val maxY = max(shape.start.y, shape.end.y) + 10f

            clickOffset.x in minX..maxX && clickOffset.y in minY..maxY
        }

        is DrawShape.NumberBadge -> {
            val dx = clickOffset.x - shape.center.x
            val dy = clickOffset.y - shape.center.y
            val distance = sqrt((dx * dx) + (dy * dy))
            distance <= shape.radius
        }

        is DrawShape.ClearGroup, is DrawShape.Erased -> false
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

fun showNativeColorPicker(currentColor: Color, onColorSelected: (Color) -> Unit) {
    val initialAwtColor = java.awt.Color(currentColor.toArgb(), true)

    val selectedAwtColor = JColorChooser.showDialog(null, "Escolha uma cor", initialAwtColor)

    if (selectedAwtColor != null) {
        onColorSelected(Color(selectedAwtColor.rgb))
    }
}