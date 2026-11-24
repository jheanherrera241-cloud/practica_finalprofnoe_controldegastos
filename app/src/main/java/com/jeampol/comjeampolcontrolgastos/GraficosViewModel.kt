package com.jeampol.comjeampolcontrolgastos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jeampol.comjeampolcontrolgastos.data.AppDatabase
import com.jeampol.comjeampolcontrolgastos.data.TotalCategoria
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class GraficosViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.obtener(app)
    private val movimientoDao = db.movimientoDao()

    private val _tipo = MutableStateFlow("gasto")
    val tipo: StateFlow<String> = _tipo.asStateFlow()

    fun cambiarTipo(nuevo: String) {
        _tipo.value = nuevo
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val totales: StateFlow<List<TotalCategoria>> = _tipo
        .flatMapLatest { t: String -> movimientoDao.totalesPorCategoria("local", t) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}