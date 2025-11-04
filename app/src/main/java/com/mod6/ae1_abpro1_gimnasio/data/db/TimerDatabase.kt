package com.mod6.ae1_abpro1_gimnasio.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mod6.ae1_abpro1_gimnasio.data.dao.TimerDao
import com.mod6.ae1_abpro1_gimnasio.data.model.CicloVidaEvento
import com.mod6.ae1_abpro1_gimnasio.data.model.Ejercicio

@Database(
    entities = [Ejercicio::class, CicloVidaEvento::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TimerDatabase : RoomDatabase() {
    abstract fun timerDao(): TimerDao
}