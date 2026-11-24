package com.jeampol.comjeampolcontrolgastos.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class MovimientoConCategoria(
    val id: Long,
    val monto: Double,
    val tipo: String,
    val nota: String,
    val fecha: Long,
    val categoriaId: Long,
    val categoriaNombre: String,
    val categoriaIcono: String,
    val latitud: Double? = null,
    val longitud: Double? = null
)

@Dao
interface MovimientoDao {
    @Query("""
        SELECT m.id, m.monto, m.tipo, m.nota, m.fecha, m.categoriaId,
               c.nombre AS categoriaNombre, c.icono AS categoriaIcono,
               m.latitud, m.longitud
        FROM movimientos m
        INNER JOIN categorias c ON c.id = m.categoriaId
        WHERE m.usuarioId = :usuarioId
        ORDER BY m.fecha DESC
    """)
    fun obtenerTodos(usuarioId: String): Flow<List<MovimientoConCategoria>>

    @Query("""
        SELECT c.nombre AS nombre, SUM(m.monto) AS total
        FROM movimientos m
        INNER JOIN categorias c ON c.id = m.categoriaId
        WHERE m.usuarioId = :usuarioId AND m.tipo = :tipo
        GROUP BY c.nombre
    """)
    fun totalesPorCategoria(usuarioId: String, tipo: String): Flow<List<TotalCategoria>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(movimiento: Movimiento)
}

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categorias WHERE tipo = :tipo ORDER BY nombre ASC")
    fun obtenerPorTipo(tipo: String): Flow<List<Categoria>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(categoria: Categoria)
}