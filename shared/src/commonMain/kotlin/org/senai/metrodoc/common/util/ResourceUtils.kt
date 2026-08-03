package org.senai.metrodoc.common.util

import java.util.*

object ResourceUtils {
    /**
     * Le um arquivo de imagem da pasta 'resources' (ex: composeResources/drawable/logo.png)
     * e converte para uma Data URI Base64 para ser injetada diretamente na tag <img> do HTML.
     */
    fun getResourceAsBase64(resourcePath: String, mimeType: String = "image/png"): String {
        return runCatching {
            // Busca o arquivo no classpath do Java/Kotlin
            val inputStream = object {}.javaClass.classLoader.getResourceAsStream(resourcePath)
                ?: return ""

            val bytes = inputStream.readBytes()
            val base64 = Base64.getEncoder().encodeToString(bytes)
            "data:$mimeType;base64,$base64"
        }.getOrDefault("")
    }
}