package com.jeampol.comjeampolcontrolgastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeampol.comjeampolcontrolgastos.data.AppDatabase
import com.jeampol.comjeampolcontrolgastos.data.MovimientoConCategoria
import com.jeampol.comjeampolcontrolgastos.ui.navigation.Pestana
import com.jeampol.comjeampolcontrolgastos.ui.theme.screens.*
import com.jeampol.comjeampolcontrolgastos.ui.theme.theme.*
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", MODE_PRIVATE))
        setContent {
            ComjeampolcontrolgastosTheme {
                val authVm: AuthViewModel = viewModel()
                val usuario = authVm.usuario
                if (usuario == null) {
                    AuthScreen(authVm)
                } else {
                    ControlGastosScreen(
                        onCerrarSesion = { authVm.cerrarSesion() }
                    )
                }
            }
        }
    }
}

@Composable
fun ControlGastosScreen(onCerrarSesion: () -> Unit = {}) {
    val context = LocalContext.current
    val db = remember { AppDatabase.obtener(context) }
    val movimientos by remember { db.movimientoDao().obtenerTodos("local") }
        .collectAsState(initial = emptyList())

    var pestana by rememberSaveable { mutableStateOf(Pestana.INICIO) }
    var mostrarHistorialCompleto by rememberSaveable { mutableStateOf(false) }
    var mostrarRegistrar by rememberSaveable { mutableStateOf(false) }
    var mostrarGraficos by rememberSaveable { mutableStateOf(false) }
    var mostrarMapa by rememberSaveable { mutableStateOf(false) }

    if (mostrarRegistrar) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Fondo
        ) {
            RegistrarMovimientoScreen(
                onVolver = { mostrarRegistrar = false }
            )
        }
        return
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            mostrarHistorialCompleto -> {
                HistorialMovimientosScreen(
                    movimientos = movimientos,
                    onVolver = { mostrarHistorialCompleto = false }
                )
            }
            mostrarGraficos -> {
                GraficosScreen(
                    onVolver = { mostrarGraficos = false }
                )
            }
            mostrarMapa -> {
                MapaGastosScreen(
                    movimientos = movimientos,
                    onVolver = { mostrarMapa = false }
                )
            }
            else -> {
                when (pestana) {
                    Pestana.INICIO -> InicioOrbitalScreen(
                        movimientos = movimientos,
                        onAbrirRegistrarMovimiento = { mostrarRegistrar = true },
                        onNavegarPestana = { nuevaPestana -> pestana = nuevaPestana },
                        onAbrirMovimientos = { mostrarHistorialCompleto = true },
                        onAbrirGraficos = { mostrarGraficos = true },
                        onAbrirMapa = { mostrarMapa = true }
                    )
                    Pestana.RESUMEN -> {
                        Surface(modifier = Modifier.fillMaxSize(), color = Fondo) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding()
                                    .navigationBarsPadding()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Resumen de Gastos",
                                        color = Blanco,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { pestana = Pestana.INICIO }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Volver",
                                            tint = Blanco
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                val soloGastos = movimientos.filter { !it.tipo.equals("ingreso", ignoreCase = true) }
                                val porCategoria = soloGastos.groupBy { it.categoriaNombre.ifBlank { "General" } }
                                    .mapValues { (_, lista) -> lista.sumOf { it.monto } }
                                ResumenScreen(porCategoria, soloGastos.sumOf { it.monto })
                            }
                        }
                    }
                    Pestana.METAS -> {
                        Surface(modifier = Modifier.fillMaxSize(), color = Fondo) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding()
                                    .navigationBarsPadding()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Metas de Ahorro",
                                        color = Blanco,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { pestana = Pestana.INICIO }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Volver",
                                            tint = Blanco
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                MetasScreen()
                            }
                        }
                    }
                    Pestana.PERFIL -> {
                        Surface(modifier = Modifier.fillMaxSize(), color = Fondo) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding()
                                    .navigationBarsPadding()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Mi Perfil",
                                        color = Blanco,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { pestana = Pestana.INICIO }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Volver",
                                            tint = Blanco
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                PerfilScreen(onCerrarSesion)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistorialMovimientosScreen(
    movimientos: List<MovimientoConCategoria>,
    onVolver: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Fondo
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial de Movimientos",
                    color = Blanco,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Blanco
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(movimientos) { mov ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Tarjeta)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mov.categoriaNombre.ifBlank { "General" },
                                    color = Blanco,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tipo: ${mov.tipo.replaceFirstChar { it.uppercase() }}",
                                    color = Blanco.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "${if (mov.tipo.equals("ingreso", ignoreCase = true)) "+" else "-"} S/ ${mov.monto}",
                                color = if (mov.tipo.equals("ingreso", ignoreCase = true)) Verde else Rojo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}