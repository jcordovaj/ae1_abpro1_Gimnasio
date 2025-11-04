package com.mod6.ae1_abpro1_gimnasio.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mod6.ae1_abpro1_gimnasio.data.repository.TimerRepository

class TimerViewModelFactory(private val repository: TimerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}