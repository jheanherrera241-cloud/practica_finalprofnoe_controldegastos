package com.jeampol.comjeampolcontrolgastos.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.jeampol.comjeampolcontrolgastos.ui.theme.theme.Blanco
import com.jeampol.comjeampolcontrolgastos.ui.theme.theme.Fondo
import com.jeampol.comjeampolcontrolgastos.ui.theme.theme.Rojo
import com.jeampol.comjeampolcontrolgastos.ui.theme.theme.Tarjeta
import com.jeampol.comjeampolcontrolgastos.ui.theme.theme.Verde
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ResumenScreen(porCategoria: Map<String, Double>, totalGastos: Double) {
    val moneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-PE"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Resumen de gastos", color = Blanco, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Tarjeta)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("TOTAL GASTADO", color = Color.LightGray, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text(moneda.format(totalGastos), color = Rojo, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (porCategoria.isEmpty()) {
            Text("Todavía no tienes gastos registrados.", color = Color.LightGray)
        } else {
            porCategoria.entries.sortedByDescending { it.value }.forEach { (categoria, monto) ->
                val fraccion = if (totalGastos > 0) (monto / totalGastos).toFloat().coerceIn(0f, 1f) else 0f
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Tarjeta)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(categoria, color = Blanco, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${moneda.format(monto)}  (${(fraccion * 100).toInt()}%)",
                                color = Color.LightGray,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { fraccion },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Verde,
                            trackColor = Color(0xFF101820)
                        )
                    }
                }
            }
        }
    }
}

// Modelo de datos para las metas
data class MetaAhorro(
    val id: Int,
    val titulo: String,
    val objetivo: Double,
    val actual: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetasScreen() {
    val moneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-PE"))

    var metas by remember {
        mutableStateOf(
            listOf(
                MetaAhorro(1, "Fondo de Emergencia", 1500.0, 600.0),
                MetaAhorro(2, "Viaje a Cusco", 800.0, 280.0),
                MetaAhorro(3, "Nueva Laptop", 2500.0, 1000.0)
            )
        )
    }

    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var mostrarDialogoAbonar by remember { mutableStateOf<MetaAhorro?>(null) }
    var metaAEditar by remember { mutableStateOf<MetaAhorro?>(null) }

    var tituloInput by remember { mutableStateOf("") }
    var objetivoInput by remember { mutableStateOf("") }
    var montoAbonoInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Metas y Presupuesto", color = Blanco, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Gestiona tus ahorros y objetivos", color = Color.LightGray, fontSize = 12.sp)
            }
            Button(
                onClick = {
                    metaAEditar = null
                    tituloInput = ""
                    objetivoInput = ""
                    mostrarDialogoCrear = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Verde, contentColor = Fondo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Nueva", fontWeight = FontWeight.Bold)
            }
        }

        if (metas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No tienes metas registradas aún.", color = Color.LightGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(metas, key = { it.id }) { meta ->
                    val fraccion = if (meta.objetivo > 0) (meta.actual / meta.objetivo).toFloat().coerceIn(0f, 1f) else 0f

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Tarjeta)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(meta.titulo, color = Blanco, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

                                Row {
                                    IconButton(onClick = {
                                        metaAEditar = meta
                                        tituloInput = meta.titulo
                                        objetivoInput = meta.objetivo.toString()
                                        mostrarDialogoCrear = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Verde, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = {
                                        metas = metas.filter { it.id != meta.id }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Rojo, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${moneda.format(meta.actual)} de ${moneda.format(meta.objetivo)} (${(fraccion * 100).toInt()}%)",
                                color = Color.LightGray,
                                fontSize = 13.sp
                            )

                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { fraccion },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Verde,
                                trackColor = Color(0xFF101820)
                            )

                            Spacer(Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = {
                                    montoAbonoInput = ""
                                    mostrarDialogoAbonar = meta
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Verde)
                            ) {
                                Text("+ Abonar a esta meta", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoCrear) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCrear = false },
            containerColor = Tarjeta,
            title = { Text(if (metaAEditar == null) "Nueva Meta de Ahorro" else "Editar Meta", color = Blanco, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tituloInput,
                        onValueChange = { tituloInput = it },
                        label = { Text("Nombre de la meta") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = objetivoInput,
                        onValueChange = { objetivoInput = it },
                        label = { Text("Monto objetivo (S/)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val obj = objetivoInput.replace(',', '.').toDoubleOrNull()
                        if (tituloInput.isNotBlank() && obj != null && obj > 0) {
                            if (metaAEditar == null) {
                                val nuevaMeta = MetaAhorro(
                                    id = (metas.maxOfOrNull { it.id } ?: 0) + 1,
                                    titulo = tituloInput.trim(),
                                    objetivo = obj,
                                    actual = 0.0
                                )
                                metas = metas + nuevaMeta
                            } else {
                                metas = metas.map {
                                    if (it.id == metaAEditar!!.id) it.copy(titulo = tituloInput.trim(), objetivo = obj) else it
                                }
                            }
                            mostrarDialogoCrear = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Verde, contentColor = Fondo)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCrear = false }) {
                    Text("Cancelar", color = Blanco)
                }
            }
        )
    }

    if (mostrarDialogoAbonar != null) {
        val metaActual = mostrarDialogoAbonar!!
        AlertDialog(
            onDismissRequest = { mostrarDialogoAbonar = null },
            containerColor = Tarjeta,
            title = { Text("Abonar a ${metaActual.titulo}", color = Blanco, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = montoAbonoInput,
                    onValueChange = { montoAbonoInput = it },
                    label = { Text("Monto a abonar (S/)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val abono = montoAbonoInput.replace(',', '.').toDoubleOrNull()
                        if (abono != null && abono > 0) {
                            metas = metas.map {
                                if (it.id == metaActual.id) it.copy(actual = it.actual + abono) else it
                            }
                            mostrarDialogoAbonar = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Verde, contentColor = Fondo)
                ) {
                    Text("Abonar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoAbonar = null }) {
                    Text("Cancelar", color = Blanco)
                }
            }
        )
    }
}

@Composable
fun PerfilScreen(onCerrarSesion: () -> Unit) {
    val correo = FirebaseAuth.getInstance().currentUser?.email ?: "Sin correo"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Perfil", color = Blanco, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Tarjeta)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("CORREO", color = Color.LightGray, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text(correo, color = Blanco, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Button(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Rojo, contentColor = Blanco)
        ) {
            Text("Cerrar sesión", fontWeight = FontWeight.Bold)
        }
    }
}