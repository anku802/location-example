package com.protuts.locationexample.ui

import android.location.Location
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<Location?>(null)

    val uiState = _uiState.asStateFlow()

    fun onLocationUpdate(location: Location) {
        _uiState.update {
            location
        }
    }

}