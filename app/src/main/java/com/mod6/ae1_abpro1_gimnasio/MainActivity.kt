package com.mod6.ae1_abpro1_gimnasio

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.NumberPicker
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.appcompat.app.AlertDialog
import com.mod6.ae1_abpro1_gimnasio.data.db.TimerDatabase
import com.mod6.ae1_abpro1_gimnasio.data.repository.TimerRepository
import com.mod6.ae1_abpro1_gimnasio.presentation.TimerViewModel
import com.mod6.ae1_abpro1_gimnasio.presentation.TimerViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.graphics.toColorInt

class MainActivity : AppCompatActivity() {

    private val TAG = "CicloVidaApp"
    private lateinit var viewModel: TimerViewModel
    private lateinit var textViewTimer: TextView
    private lateinit var buttonToggleTimer: Button
    private lateinit var buttonReset: Button
    private lateinit var textViewAuditLog: TextView
    private lateinit var buttonSetCronometerMode: Button
    private lateinit var buttonSetTimerMode: Button
    private lateinit var buttonConfigureTime: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Mapear elementos del layout
        textViewTimer           = findViewById(R.id.textViewTimer)
        buttonToggleTimer       = findViewById(R.id.buttonToggleTimer)
        buttonReset             = findViewById(R.id.buttonReset)
        textViewAuditLog        = findViewById(R.id.textViewAuditLog)
        buttonSetCronometerMode = findViewById(R.id.buttonSetCronometerMode)
        buttonSetTimerMode      = findViewById(R.id.buttonSetTimerMode)
        buttonConfigureTime     = findViewById(R.id.buttonConfigureTime)

        // Inicializar componentes de Room y ViewModel
        val db = Room.databaseBuilder(
            applicationContext,
            TimerDatabase::class.java, "timer-db"
        ).build()
        val repository = TimerRepository(db.timerDao())
        val factory    = TimerViewModelFactory(repository)
        viewModel      = ViewModelProvider(this, factory).get(TimerViewModel::class.java)

        // Observers de LiveData

        // Tiempo
        viewModel.tiempoActualSegundos.observe(this, Observer { segundos ->
            textViewTimer.text = formatTime(segundos)
        })

        // Estado
        viewModel.isRunning.observe(this, Observer { isRunning ->
            buttonToggleTimer.text = if (isRunning) "Pausar" else "Iniciar"
            buttonReset.isEnabled  = !isRunning

            // Deshabilita configuración de modo/duración mientras opera
            buttonSetCronometerMode.isEnabled = !isRunning
            buttonSetTimerMode.isEnabled      = !isRunning
            buttonConfigureTime.isEnabled     = !isRunning
        })

        // Modo (controla visibilidad y otros detalles de UI)
        viewModel.isTimerMode.observe(this, Observer { isTimerMode ->
            // Muetra/Oculta el botón de configuración (sólo en Temporizador)
            buttonConfigureTime.visibility = if (isTimerMode) View.VISIBLE else View.GONE

            // Resalta el modo activo
            if (isTimerMode) {
                // Modo Temporizador activo
                buttonSetTimerMode.setBackgroundColor("#4CAF50".toColorInt())
                buttonSetCronometerMode.setBackgroundColor(Color.GRAY)
            } else {
                // Modo Cronómetro activo
                buttonSetTimerMode.setBackgroundColor(Color.GRAY)
                buttonSetCronometerMode.setBackgroundColor("#4CAF50".toColorInt())
            }

            // Resetea al cambiar de modo, sólo si no está corriendo el timer
            if (viewModel.isRunning.value != true) {
                viewModel.resetTimer()
            }
        })

        // Auditoría
        viewModel.auditLog.observe(this, Observer { eventos ->
            val logText = eventos.joinToString("\n") { evento ->
                val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val time = timeFormatter.format(evento.timestamp)
                "[$time] ${evento.nombreEvento}"
            }
            textViewAuditLog.text = logText
        })

        // Carga inicial del log
        viewModel.loadAuditLog()

        // Listeners

        // Botón Cronómetro
        buttonSetCronometerMode.setOnClickListener {
            viewModel.toggleMode(false)
            Toast.makeText(this, "Modo: Cronómetro",
                Toast.LENGTH_SHORT).show()
        }

        // Botón Temporizador
        buttonSetTimerMode.setOnClickListener {
            viewModel.toggleMode(true)
            Toast.makeText(this, "Modo: Temporizador",
                Toast.LENGTH_SHORT).show()
        }

        // Botón Configurar Tiempo (Abre el diálogo de NumberPicker)
        buttonConfigureTime.setOnClickListener {
            showTimeConfigurationDialog()
        }

        buttonToggleTimer.setOnClickListener {
            viewModel.toggleTimer()
        }

        buttonReset.setOnClickListener {
            viewModel.resetTimer()
        }

        // Establece modo por defecto al iniciar, en esta caso, "Temporizador"
        viewModel.toggleMode(true)

        logAndAudit("onCreate")
    }

    // Métodos para persistencia (Lifecycle)

    override fun onStart() {
        super.onStart()
        logAndAudit("onStart")
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isRunning.value == true) {
            viewModel.startOrResumeTimer()
        }
        logAndAudit("onResume")
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.isRunning.value == true) {
            viewModel.pauseTimer()
        }
        logAndAudit("onPause")
    }

    override fun onStop() {
        super.onStop()
        logAndAudit("onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        logAndAudit("onDestroy")
    }

    // Métodos Auxiliares

    private fun formatTime(totalSegundos: Long): String {
        val horas    = totalSegundos / 3600
        val minutos  = (totalSegundos % 3600) / 60
        val segundos = totalSegundos % 60
        return String.format("%02d:%02d:%02d", horas, minutos, segundos)
    }

    private fun logAndAudit(evento: String) {
        Log.d(TAG, "CONSOLA: Evento del ciclo de vida -> $evento")
        viewModel.auditarEventoCicloVida(evento)
    }

    // Diálogo config
    private fun showTimeConfigurationDialog() {
        val dialogView = LayoutInflater.from(this).
        inflate(R.layout.dialog_time_config, null)

        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.numberPickerMinutes)
        numberPicker.minValue = 1
        numberPicker.maxValue = 60
        numberPicker.value    = 5 // Valor por defecto

        AlertDialog.Builder(this)
            .setTitle("Configurar Temporizador (Minutos)")
            .setView(dialogView)
            .setPositiveButton("Establecer") { _, _ ->
                val minutes = numberPicker.value.toLong()
                val durationInSeconds = minutes * 60L

                viewModel.setTimerDuration(durationInSeconds)
                viewModel.resetTimer()
                Toast.makeText(this, "Temporizador fijado a $minutes minutos.",
                    Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}