package com.jeampol.comjeampolcontrolgastos

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "movimientos_usuario")
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uid: String,
    val descripcion: String,
    val categoria: String,
    val monto: Double,
    val esIngreso: Boolean
)

@Dao
interface MovimientoDao {
    @Query("SELECT * FROM movimientos_usuario WHERE uid = :uid ORDER BY id DESC")
    fun observar(uid: String): Flow<List<MovimientoEntity>>

    @Insert
    suspend fun insertar(m: MovimientoEntity)
}

@Database(entities = [MovimientoEntity::class], version = 1, exportSchema = false)
abstract class GastosDatabase : RoomDatabase() {
    abstract fun dao(): MovimientoDao

    companion object {
        @Volatile private var INSTANCE: GastosDatabase? = null
        fun get(context: Context): GastosDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    GastosDatabase::class.java,
                    "gastos_usuario.db"
                ).build().also { INSTANCE = it }
            }
    }
}

class GastosViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = GastosDatabase.get(app).dao()

    fun movimientos(uid: String): Flow<List<MovimientoEntity>> = dao.observar(uid)

    fun agregar(uid: String, descripcion: String, categoria: String, monto: Double, esIngreso: Boolean) {
        viewModelScope.launch {
            dao.insertar(MovimientoEntity(
                uid = uid, descripcion = descripcion,
                categoria = categoria, monto = monto, esIngreso = esIngreso
            ))
        }
    }
}