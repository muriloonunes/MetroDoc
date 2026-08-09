package org.senai.metrodoc.common.database.entity

import androidx.room3.ColumnTypeConverter
import org.senai.metrodoc.common.mapper.metroDocJson
import org.senai.metrodoc.features.report.model.DrawShape

class DrawShapeConverters {
    @ColumnTypeConverter
    fun fromDrawShapeList(value: List<DrawShape>?): String? {
        if (value == null) return null

        return metroDocJson.encodeToString(value)
    }

    @ColumnTypeConverter
    fun toDrawShapeList(value: String?): List<DrawShape>? {
        if (value == null) return null

        return metroDocJson.decodeFromString(value)
    }
}