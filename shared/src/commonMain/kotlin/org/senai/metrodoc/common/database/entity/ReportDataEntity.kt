package org.senai.metrodoc.common.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "reports")
data class ReportDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nomeRelatorio: String,
    val cliente: String,
    val componente: String,
    val identificadorCalypso: String,
    val maquina: String,
    val numeroMaquina: String,
    val software: String,
    val operador: String,
    val dataHora: String,
    val qtdCaracteristicas: String
)