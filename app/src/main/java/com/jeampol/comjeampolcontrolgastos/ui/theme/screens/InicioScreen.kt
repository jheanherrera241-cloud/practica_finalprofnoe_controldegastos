package com.jeampol.comjeampolcontrolgastos.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeampol.comjeampolcontrolgastos.GastosViewModel
import com.jeampol.comjeampolcontrolgastos.MovimientoEntity
import com.jeampol.comjeampolcontrolgastos.ui.theme.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun InicioScreen(
    uid: String,
    movimientos: List<MovimientoEntity>,
    gastosVm: GastosViewModel
) {
    var mostrarFormulario by remember { mutableStateOf(false) }
    var descripcion by remember { mutableStateOf("") }
    var montoTexto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Comida") }
    var esIngreso by remember { mutableStateOf(false) }

    val ingresos = movimientos.filter { it.esIngreso }.sumOf { it.monto }
    val gastos = movimientos.filter { !it.esIngreso }.sumOf { it.monto }
    val saldo = ingresos - gastos
    val moneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-PE"))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Tarjeta)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("SALDO DISPONIBLE", color = Blanco.copy(alpha = 0.7f), fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        moneda.format(saldo),
                        color = Verde,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Ingresos", color = Blanco.copy(alpha = 0.7f), fontSize = 13.sp)
                            Text(moneda.format(ingresos), color = Verde, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("Gastos", color = Blanco.copy(alpha = 0.7f), fontSize = 13.sp)
                            Text(moneda.format(gastos), color = Rojo, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = { mostrarFormulario = !mostrarFormulario },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Rojo, contentColor = Blanco)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Registrar movimiento", fontWeight = FontWeight.Bold)
            }
        }

        if (mostrarFormulario) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Tarjeta)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = montoTexto,
                            onValueChange = { montoTexto = it },
                            label = { Text("Monto") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = categoria,
                            onValueChange = { categoria = it },
                            label = { Text("Categoría") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("¿Es ingreso?", color = Blanco)
                            Switch(checked = esIngreso, onCheckedChange = { esIngreso = it })
                        }
                        Button(
                            onClick = {
                                val monto = montoTexto.replace(",", ".").toDoubleOrNull()
                                if (monto != null && monto > 0.0) {
                                    gastosVm.agregar(
                                        uid = uid,
                                        descripcion = descripcion.ifBlank { categoria },
                                        categoria = categoria.ifBlank { "General" },
                                        monto = monto,
                                        esIngreso = esIngreso
                                    )
                                    descripcion = ""
                                    montoTexto = ""
                                    mostrarFormulario = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Verde, contentColor = Fondo)
                        ) {
                            Text("Guardar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}