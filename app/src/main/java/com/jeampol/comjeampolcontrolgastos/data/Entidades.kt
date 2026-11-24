package com.jeampol.comjeampolcontrolgastos.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categorias")
data class Categoria(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val icono: String,
    val tipo: String,
    val usuarioId: String = "local"
)

@Entity(
    tableName = "movimientos",
    foreignKeys = [ForeignKey(
        entity = Categoria::class,
        parentColumns = ["id"],
        childColumns = ["categoriaId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("categoriaId"), Index("fecha")]
)
data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monto: Double,
    val tipo: String,
    val categoriaId: Long,
    val nota: String = "",
    val fecha: Long,
    val usuarioId: String = "local",
    val latitud: Double? = null,
    val longitud: Double? = null
)