package com.mod6.ae1_abpro1_gimnasio.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "eventos_ciclo_vida")
data class CicloVidaEvento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombreEvento: String, // ej. "onStart"
    val timestamp: Date = Date()
)