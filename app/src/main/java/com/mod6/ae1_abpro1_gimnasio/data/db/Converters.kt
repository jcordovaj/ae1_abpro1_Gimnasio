package com.mod6.ae1_abpro1_gimnasio.data.db

import androidx.room.TypeConverter
import java.util.Date

/**
 * Esta clase convierte tipos de datos complejos que SQLite no procesa.
 * En este caso, convierte objetos de tipo Date a un Long (milisegundos) y viceversa.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}