package com.jeampol.comjeampolcontrolgastos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Categoria::class, Movimiento::class],
    version = 2, // 👈 Subimos de 1 a 2 para aplicar las nuevas columnas
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoriaDao(): CategoriaDao
    abstract fun movimientoDao(): MovimientoDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        private val categoriasPorDefecto = listOf(
            Triple("Comida", "restaurant", "gasto"),
            Triple("Transporte", "directions_bus", "gasto"),
            Triple("Estudios", "school", "gasto"),
            Triple("Salud", "favorite", "gasto"),
            Triple("Ocio", "sports_esports", "gasto"),
            Triple("Hogar", "home", "gasto"),
            Triple("Otros gastos", "more_horiz", "gasto"),
            Triple("Sueldo", "payments", "ingreso"),
            Triple("Otros ingresos", "more_horiz", "ingreso")
        )

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "control_gastos.db"
                )
                    .fallbackToDestructiveMigration() // 👈 Evita cierres inesperados al cambiar la estructura
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            categoriasPorDefecto.forEach { (nombre, icono, tipo) ->
                                db.execSQL(
                                    "INSERT INTO categorias (nombre, icono, tipo, usuarioId) VALUES (?, ?, ?, 'local')",
                                    arrayOf(nombre, icono, tipo)
                                )
                            }
                        }
                    })
                    .build()
                    .also { instancia = it }
            }
    }
}