package org.senai.metrodoc.features.report.model

data class SectionError(
    val sectionId: String,
    val sectionTitle: String,
    val fieldName: String,
    val errorMessage: String = "Campo não preenchido",
)
