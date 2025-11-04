package com.mod6.ae1_abpro1_gimnasio.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mod6.ae1_abpro1_gimnasio.data.model.CicloVidaEvento
import com.mod6.ae1_abpro1_gimnasio.data.model.Ejercicio

@Dao
interface TimerDao {
    @Insert
    suspend fun insertarEjercicio(ejercicio: Ejercicio)

    @Insert
    suspend fun insertarEvento(evento: CicloVidaEvento)

    @Query("SELECT * FROM ejercicios ORDER BY fechaFin DESC")
    suspend fun obtenerHistorialEjercicios(): List<Ejercicio>

    @Query("SELECT * FROM eventos_ciclo_vida ORDER BY timestamp DESC")
    suspend fun obtenerEventosCicloVida(): List<CicloVidaEvento>
}