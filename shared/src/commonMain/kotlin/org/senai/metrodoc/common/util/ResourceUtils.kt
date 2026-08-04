package org.senai.metrodoc.common.util

import metrodoc.shared.generated.resources.Res
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
}