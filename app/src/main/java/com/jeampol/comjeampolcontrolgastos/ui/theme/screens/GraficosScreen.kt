package com.jeampol.comjeampolcontrolgastos.ui.theme.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeampol.comjeampolcontrolgastos.GraficosViewModel
import com.jeampol.comjeampolcontrolgastos.data.TotalCategoria

private val coloresGrafico = listOf(
    Color(0xFF42A5F5), Color(0xFFEF5350), Color(0xFF66BB6A),
    Color(0xFFFFA726), Color(0xFFAB47BC), Color(0xFF26C6DA),
    Color(0xFFFFEE58), Color(0xFF8D6E63), Color(0xFFEC407A)
)

@Composable
fun GraficosScreen(
    onVolver: () -> Unit,
    vm: GraficosViewModel = viewModel()
) {
    // Si tu ViewModel filtra por tipo individual, podemos recolectar ambos o combinarlos.
    // Como tu GraficosViewModel maneja un Flow por tipo, vamos a mostrar el tipo activo o
    // puedes unificarlo en el DAO para traer ambos. Aquí adaptamos la vista para mostrar
    // claramente el tipo actual con una paleta de colores independiente por categoría.
    val tipo by vm.tipo.collectAsStateWithLifecycle()
    val totales by vm.totales.collectAsStateWithLifecycle()

    GraficosContenido(
        tipo = tipo,
        totales = totales,
        onCambiarTipo = { vm.cambiarTipo(it) },
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraficosContenido(
    tipo: String,
    totales: List<TotalCategoria>,
    onCambiarTipo: (String) -> Unit,
    onVolver: () -> Unit
) {
    val suma = totales.sumOf { it.total }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(onClick = onVolver, modifier = Modifier.align(Alignment.Start)) {
            Text("← Volver")
        }

        Text(
            "Distribución por categoría",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Selector para alternar entre ver Ingresos o Gastos con colores dedicados
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = tipo == "gasto",
                onClick = { onCambiarTipo("gasto") },
                label = { Text("Gastos") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFEF5350), // Rojo suave para gastos
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = tipo == "ingreso",
                onClick = { onCambiarTipo("ingreso") },
                label = { Text("Ingresos") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF66BB6A), // Verde para ingresos
                    selectedLabelColor = Color.White
                )
            )
        }

        if (totales.isEmpty() || suma <= 0.0) {
            Spacer(Modifier.height(40.dp))
            Text("Aún no hay registros en esta sección")
        } else {
            // GRÁFICO CIRCULAR TIPO DONA
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val grosor = 36.dp.toPx()
                    val lado = size.minDimension - grosor
                    var inicio = -90f
                    totales.forEachIndexed { i, item ->
                        val barrido = (item.total / suma * 360.0).toFloat()
                        val colorAsociado = coloresGrafico[i % coloresGrafico.size]
                        drawArc(
                            color = colorAsociado,
                            startAngle = inicio,
                            sweepAngle = barrido,
                            useCenter = false,
                            topLeft = Offset(grosor / 2, grosor / 2),
                            size = Size(lado, lado),
                            style = Stroke(width = grosor)
                        )
                        inicio += barrido
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (tipo == "gasto") "Total Gastos" else "Total Ingresos", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "S/ " + "%.2f".format(suma),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (tipo == "gasto") Color(0xFFEF5350) else Color(0xFF66BB6A)
                    )
                }
            }

            // LEYENDA DETALLADA CON BARRAS DE OTRO COLOR POR CATEGORÍA
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                totales.forEachIndexed { i, item ->
                    val porcentaje = if (suma > 0) (item.total / suma * 100) else 0.0
                    val colorAsociado = coloresGrafico[i % coloresGrafico.size]

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(colorAsociado, CircleShape)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(item.nombre, fontWeight = FontWeight.SemiBold)
                                }
                                Text("S/ " + "%.2f".format(item.total) + " (${"%.0f".format(porcentaje)}%)")
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { if (suma > 0) (item.total / suma).toFloat().coerceIn(0f, 1f) else 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = colorAsociado,
                                trackColor = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}