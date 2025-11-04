package com.mod6.ae1_abpro1_gimnasio.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "ejercicios")
data class Ejercicio(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val duracionSegundos: Long,
    val fechaFin: Date = Date()
)