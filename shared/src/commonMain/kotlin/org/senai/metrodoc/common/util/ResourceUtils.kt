package org.senai.metrodoc.common.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import metrodoc.shared.generated.resources.Res
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.senai.metrodoc.features.report.model.DrawShape
import org.senai.metrodoc.features.report.util.drawImageDrawing
import java.io.File
import java.util.*

object ResourceUtils {
    /**
     * Le um arquivo de imagem da pasta 'resources' (ex: composeResources/drawable/logo.png)
     * e converte para uma Data URI Base64 para ser injetada diretamente na tag <img> do HTML.
     */
    suspend fun getResourceAsBase64(resourcePath: String, mimeType: String = "image/png"): String {
        val resBytes = Res.readBytes(resourcePath)
        return "data:$mimeType;base64," + Base64.getEncoder().encodeToString(resBytes)
    }

    suspend fun getResourceAsString(resourcePath: String): String {
        return Res.readBytes(resourcePath).decodeToString().replace("\uFEFF", "").trim()
    }

    fun localFileToBase64(path: String, mimeType: String = "image/png"): String {
        val file = File(path)
        if (!file.exists()) return ""
        val bytes = file.readBytes()
        return "data:$mimeType;base64," + Base64.getEncoder().encodeToString(bytes)
    }

    fun localImageWithDrawingsToBase64(
        path: String,
        mimeType: String = "image/png",
        drawings: List<DrawShape>,
        uiCanvasSize: Size,
    ): String {
        val file = File(path)
        if (!file.exists()) return ""

        val bytes = file.readBytes()
        val skiaImg = Image.makeFromEncoded(bytes)
        val originalBitmap = skiaImg.toComposeImageBitmap()

        val targetBitmap = ImageBitmap(originalBitmap.width, originalBitmap.height)
        val canvas = Canvas(targetBitmap)

        val drawScope = CanvasDrawScope()
        val targetSize = Size(originalBitmap.width.toFloat(), originalBitmap.height.toFloat())

        val textMeasurer = TextMeasurer(
            defaultFontFamilyResolver = createFontFamilyResolver(),
            defaultDensity = Density(1f),
            defaultLayoutDirection = LayoutDirection.Ltr
        )

        drawScope.draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = targetSize
        ) {
            drawImage(originalBitmap)

            val scaleX = targetSize.width / uiCanvasSize.width
            val scaleY = targetSize.height / uiCanvasSize.height

            scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
                drawings.forEach { draw ->
                    if (draw !is DrawShape.ClearGroup) drawImageDrawing(
                        drawing = draw,
                        textMeasurer = textMeasurer,
                    )
                }
            }
        }

        val skiaBitmap = targetBitmap.asSkiaBitmap()
        val finalSkiaImage = Image.makeFromBitmap(skiaBitmap)

        val pngBytes = finalSkiaImage.encodeToData(EncodedImageFormat.PNG)?.bytes ?: return ""
        return "data:$mimeType;base64," + Base64.getEncoder().encodeToString(pngBytes)
    }
}