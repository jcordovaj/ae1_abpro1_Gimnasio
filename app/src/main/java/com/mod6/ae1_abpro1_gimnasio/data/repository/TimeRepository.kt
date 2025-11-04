package com.mod6.ae1_abpro1_gimnasio.data.repository

import com.mod6.ae1_abpro1_gimnasio.data.dao.TimerDao
import com.mod6.ae1_abpro1_gimnasio.data.model.CicloVidaEvento
import com.mod6.ae1_abpro1_gimnasio.data.model.Ejercicio

class TimerRepository(private val dao: TimerDao) {
    suspend fun guardarEjercicio(duracionSegundos: Long) {
        val ejercicio = Ejercicio(duracionSegundos = duracionSegundos)
        dao.insertarEjercicio(ejercicio)
    }

    suspend fun auditarEvento(nombreEvento: String) {
        val evento = CicloVidaEvento(nombreEvento = nombreEvento)
        dao.insertarEvento(evento)
    }

    suspend fun obtenerHistorial(): List<Ejercicio> = dao.obtenerHistorialEjercicios()
    suspend fun obtenerAuditoria(): List<CicloVidaEvento> = dao.obtenerEventosCicloVida()
}