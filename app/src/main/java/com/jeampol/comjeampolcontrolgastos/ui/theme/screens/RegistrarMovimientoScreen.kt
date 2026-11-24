package com.jeampol.comjeampolcontrolgastos.ui.theme.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.jeampol.comjeampolcontrolgastos.MovimientoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegistrarMovimientoScreen(
    onVolver: () -> Unit,
    vm: MovimientoViewModel = viewModel()
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val tipo by vm.tipo.collectAsStateWithLifecycle()
    val categorias by vm.categorias.collectAsStateWithLifecycle()

    var monto by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var categoriaId by remember { mutableStateOf<Long?>(null) }
    var fecha by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var mostrarFecha by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Al cambiar entre gasto e ingreso, se borra la categoría elegida
    LaunchedEffect(tipo) { categoriaId = null }

    val formato = remember { SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-PE")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onVolver) { Text("← Volver") }

        Text(
            "Registrar movimiento",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Gasto o ingreso
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = tipo == "gasto",
                onClick = { vm.cambiarTipo("gasto") },
                label = { Text("Gasto") }
            )
            FilterChip(
                selected = tipo == "ingreso",
                onClick = { vm.cambiarTipo("ingreso") },
                label = { Text("Ingreso") }
            )
        }

        // Monto
        OutlinedTextField(
            value = monto,
            onValueChange = { monto = it },
            label = { Text("Monto") },
            prefix = { Text("S/ ") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        // Categorías
        Text("Categoría", style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categorias.forEach { cat ->
                FilterChip(
                    selected = categoriaId == cat.id,
                    onClick = { categoriaId = cat.id },
                    label = { Text(cat.nombre) }
                )
            }
        }

        // Fecha
        OutlinedButton(
            onClick = { mostrarFecha = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fecha: ${formato.format(Date(fecha))}")
        }

        // Nota
        OutlinedTextField(
            value = nota,
            onValueChange = { nota = it },
            label = { Text("Nota (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                val valor = monto.replace(',', '.').toDoubleOrNull()
                when {
                    valor == null || valor <= 0 -> error = "Escribe un monto mayor a 0"
                    categoriaId == null -> error = "Elige una categoría"
                    else -> {
                        error = null

                        // Verificamos permisos de ubicación para capturar coordenadas GPS
                        val tienePermiso = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED

                        if (tienePermiso) {
                            try {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    if (location != null) {
                                        vm.guardar(valor, categoriaId!!, nota.trim(), fecha, location.latitude, location.longitude, onVolver)
                                    } else {
                                        vm.guardar(valor, categoriaId!!, nota.trim(), fecha, null, null, onVolver)
                                    }
                                }.addOnFailureListener {
                                    vm.guardar(valor, categoriaId!!, nota.trim(), fecha, null, null, onVolver)
                                }
                            } catch (e: SecurityException) {
                                vm.guardar(valor, categoriaId!!, nota.trim(), fecha, null, null, onVolver)
                            }
                        } else {
                            // Si no hay permisos concedidos, guarda sin ubicación
                            vm.guardar(valor, categoriaId!!, nota.trim(), fecha, null, null, onVolver)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Guardar")
        }
    }

    // Calendario
    if (mostrarFecha) {
        val estado = rememberDatePickerState(initialSelectedDateMillis = fecha)
        DatePickerDialog(
            onDismissRequest = { mostrarFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { fecha = it }
                    mostrarFecha = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarFecha = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estado)
        }
    }
}