package org.senai.metrodoc.common.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

@Entity(
    tableName = "measurements",
    foreignKeys = [
        ForeignKey(
            entity = ReportDataEntity::class,
            parentColumns = ["id"],
            childColumns = ["reportId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MeasurementDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reportId: Long,
    val nome: String,
    val valorMedido: String,
    val unidade: String,
    val valorNominal: String,
    val tolSuperior: String,
    val tolInferior: String,
    val desvio: String,
    val isForaTolerancia: Boolean,
    val incluidaRelatorio: Boolean
)
