package com.jeampol.comjeampolcontrolgastos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jeampol.comjeampolcontrolgastos.data.AppDatabase
import com.jeampol.comjeampolcontrolgastos.data.Categoria
import com.jeampol.comjeampolcontrolgastos.data.Movimiento
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MovimientoViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.obtener(app)
    private val movimientoDao = db.movimientoDao()
    private val categoriaDao = db.categoriaDao()

    private val _tipo = MutableStateFlow("gasto")
    val tipo: StateFlow<String> = _tipo.asStateFlow()

    fun cambiarTipo(nuevo: String) {
        _tipo.value = nuevo
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val categorias: StateFlow<List<Categoria>> = _tipo
        .flatMapLatest<String, List<Categoria>> { t -> categoriaDao.obtenerPorTipo(t) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun guardar(
        monto: Double,
        categoriaId: Long,
        nota: String,
        fecha: Long,
        latitud: Double? = null,
        longitud: Double? = null,
        alTerminar: () -> Unit
    ) {
        viewModelScope.launch {
            movimientoDao.insertar(
                Movimiento(
                    usuarioId = "local",
                    monto = monto,
                    tipo = _tipo.value,
                    categoriaId = categoriaId,
                    nota = nota,
                    fecha = fecha,
                    latitud = latitud,
                    longitud = longitud
                )
            )
            alTerminar()
        }
    }
}