package com.jeampol.comjeampolcontrolgastos.ui.theme.screens

import android.content.Intent
import android.graphics.Color as GColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeampol.comjeampolcontrolgastos.data.MovimientoConCategoria
import com.jeampol.comjeampolcontrolgastos.ui.navigation.Pestana
import com.jeampol.comjeampolcontrolgastos.ui.theme.theme.*
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.roundToInt

data class NodoOrbital(
    val titulo: String,
    val icono: ImageVector,
    val anguloGrados: Float,
    val onClick: () -> Unit
)

@Composable
fun InicioOrbitalScreen(
    movimientos: List<MovimientoConCategoria>,
    onAbrirRegistrarMovimiento: () -> Unit,
    onNavegarPestana: (Pestana) -> Unit,
    onAbrirMovimientos: () -> Unit,
    onAbrirGraficos: () -> Unit,
    onAbrirMapa: () -> Unit
) {
    val context = LocalContext.current
    val ingresos = movimientos.filter { it.tipo.equals("ingreso", ignoreCase = true) }.sumOf { it.monto }
    val gastos = movimientos.filter { !it.tipo.equals("ingreso", ignoreCase = true) }.sumOf { it.monto }
    val saldo = ingresos - gastos
    val moneda = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-PE")) }

    val nodos = listOf(
        NodoOrbital("EXPORTAR", Icons.Default.Share, -58f) {
            try {
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Formato A4
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint().apply {
                    color = GColor.BLACK
                    textSize = 10f
                }

                var yPos = 40f

                // --- ENCABEZADO ---
                paint.textSize = 18f
                paint.isFakeBoldText = true
                canvas.drawText("CONTROL DE GASTOS - REPORTE CON GRÁFICOS", 40f, yPos, paint)

                yPos += 18f
                paint.textSize = 10f
                paint.isFakeBoldText = false
                canvas.drawText("Fecha de emisión: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}", 40f, yPos, paint)

                yPos += 12f
                canvas.drawText("---------------------------------------------------------------------------------------------------------------------------------------------------------------------", 40f, yPos, paint)
                yPos += 20f

                // --- TARJETAS DE RESUMEN SUPERIOR ---
                paint.isFakeBoldText = true
                paint.textSize = 10f
                canvas.drawText("TOTAL INGRESOS", 40f, yPos, paint)
                canvas.drawText("TOTAL GASTOS", 220f, yPos, paint)
                canvas.drawText("SALDO DISPONIBLE", 400f, yPos, paint)

                yPos += 16f
                paint.textSize = 15f
                canvas.drawText("S/ $ingresos", 40f, yPos, paint)
                canvas.drawText("S/ $gastos", 220f, yPos, paint)
                canvas.drawText("S/ $saldo", 400f, yPos, paint)

                yPos += 20f
                canvas.drawText("---------------------------------------------------------------------------------------------------------------------------------------------------------------------", 40f, yPos, paint)
                yPos += 25f

                // --- GRÁFICO DE BARRAS ESTADÍSTICO A COLOR ---
                paint.isFakeBoldText = true
                paint.textSize = 11f
                canvas.drawText("GRÁFICO COMPARATIVO (INGRESOS VS GASTOS)", 40f, yPos, paint)
                yPos += 15f

                val maxValor = if (ingresos == 0.0 && gastos == 0.0) 1.0 else maxOf(ingresos, gastos)
                val anchoMaxBarra = 200f
                val altoBarra = 16f

                // Barra de Ingresos (Color Verde)
                paint.color = GColor.rgb(46, 125, 50) // Verde profesional
                val anchoIngresos = if (maxValor > 0) ((ingresos / maxValor) * anchoMaxBarra).toFloat() else 0f
                canvas.drawRect(40f, yPos, 40f + anchoIngresos, yPos + altoBarra, paint)

                paint.color = GColor.BLACK
                paint.textSize = 10f
                canvas.drawText("Ingresos: S/ $ingresos", 45f + anchoMaxBarra + 10f, yPos + 12f, paint)
                yPos += 25f

                // Barra de Gastos (Color Rojo)
                paint.color = GColor.rgb(198, 40, 40) // Rojo profesional
                val anchoGastos = if (maxValor > 0) ((gastos / maxValor) * anchoMaxBarra).toFloat() else 0f
                canvas.drawRect(40f, yPos, 40f + anchoGastos, yPos + altoBarra, paint)

                paint.color = GColor.BLACK
                canvas.drawText("Gastos: S/ $gastos", 45f + anchoMaxBarra + 10f, yPos + 12f, paint)

                yPos += 30f
                canvas.drawText("---------------------------------------------------------------------------------------------------------------------------------------------------------------------", 40f, yPos, paint)
                yPos += 22f

                // --- TABLA DETALLADA DE MOVIMIENTOS ---
                paint.isFakeBoldText = true
                paint.textSize = 11f
                canvas.drawText("DETALLE DE MOVIMIENTOS Y UBICACIONES (MAPA)", 40f, yPos, paint)

                yPos += 18f
                paint.textSize = 10f
                canvas.drawText("N°", 40f, yPos, paint)
                canvas.drawText("CATEGORÍA", 75f, yPos, paint)
                canvas.drawText("UBICACIÓN (MAPA)", 220f, yPos, paint)
                canvas.drawText("TIPO", 370f, yPos, paint)
                canvas.drawText("MONTO", 460f, yPos, paint)

                yPos += 6f
                canvas.drawText("---------------------------------------------------------------------------------------------------------------------------------------------------------------------", 40f, yPos, paint)
                yPos += 20f

                if (movimientos.isEmpty()) {
                    paint.isFakeBoldText = false
                    canvas.drawText("No hay movimientos registrados para mostrar en el reporte.", 40f, yPos, paint)
                } else {
                    paint.textSize = 10f
                    movimientos.forEachIndexed { index, mov ->
                        if (yPos > 780f) return@forEachIndexed

                        paint.isFakeBoldText = false

                        canvas.drawText("${index + 1}", 40f, yPos, paint)

                        val categoria = mov.categoriaNombre.ifBlank { "General" }
                        val catAcortada = if (categoria.length > 18) categoria.take(16) + "..." else categoria
                        canvas.drawText(catAcortada, 75f, yPos, paint)

                        val lugarInfo = if (mov.latitud != 0.0 && mov.longitud != 0.0) {
                            "Lat: ${String.format(Locale.US, "%.2f", mov.latitud)}, Lon: ${String.format(Locale.US, "%.2f", mov.longitud)}"
                        } else {
                            "Sin ubicación"
                        }
                        canvas.drawText(lugarInfo, 220f, yPos, paint)

                        val tipoTexto = mov.tipo.uppercase()
                        canvas.drawText(tipoTexto, 370f, yPos, paint)

                        paint.isFakeBoldText = true
                        val signo = if (tipoTexto == "INGRESO") "+" else "-"
                        canvas.drawText("$signo S/ ${mov.monto}", 460f, yPos, paint)

                        yPos += 15f
                        paint.isFakeBoldText = false
                        canvas.drawText("---------------------------------------------------------------------------------------------------------------------------------------------------------------------", 40f, yPos, paint)
                        yPos += 18f
                    }
                }

                pdfDocument.finishPage(page)

                // Guardar y abrir PDF
                val file = File(context.cacheDir, "Reporte_Grafico_${System.currentTimeMillis()}.pdf")
                pdfDocument.writeTo(FileOutputStream(file))
                pdfDocument.close()

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }

                context.startActivity(Intent.createChooser(intent, "Ver Reporte PDF con:"))

            } catch (e: Exception) {
                e.printStackTrace()
            }
        },
        NodoOrbital("RESUMEN", Icons.Default.PieChart, -35f) { onNavegarPestana(Pestana.RESUMEN) },
        NodoOrbital("METAS", Icons.Default.Star, -12f) { onNavegarPestana(Pestana.METAS) },
        NodoOrbital("MOVIMIENTOS", Icons.AutoMirrored.Filled.List, 11f) { onAbrirMovimientos() },
        NodoOrbital("GRAFICOS", Icons.Default.BarChart, 34f) { onAbrirGraficos() },
        NodoOrbital("MAPA", Icons.Default.LocationOn, 57f) { onAbrirMapa() },
        NodoOrbital("PERFIL", Icons.Default.Person, 80f) { onNavegarPestana(Pestana.PERFIL) }
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val density = LocalDensity.current
        val anchoPx = with(density) { maxWidth.toPx() }
        val altoPx = with(density) { maxHeight.toPx() }

        val centroX = anchoPx * 0.16f
        val centroY = altoPx * 0.46f
        val radioOrbitaPx = (anchoPx * 0.56f).coerceAtMost(altoPx * 0.31f)
        val radioCirculoInternoPx = radioOrbitaPx * 0.78f

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Verde.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(centroX, centroY),
                    radius = radioOrbitaPx * 1.25f
                ),
                radius = radioOrbitaPx * 1.25f,
                center = Offset(centroX, centroY)
            )
            drawCircle(
                color = Tarjeta.copy(alpha = 0.92f),
                radius = radioCirculoInternoPx,
                center = Offset(centroX, centroY)
            )
            drawCircle(
                color = Verde.copy(alpha = 0.25f),
                radius = radioCirculoInternoPx,
                center = Offset(centroX, centroY),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Verde.copy(alpha = 0.75f),
                radius = radioOrbitaPx,
                center = Offset(centroX, centroY),
                style = Stroke(width = 2.5.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(y = (-24).dp)
                .padding(start = 24.dp)
                .width(maxWidth * 0.56f)
        ) {
            Text(
                text = "SALDO DISPONIBLE",
                color = Blanco.copy(alpha = 0.75f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = moneda.format(saldo),
                color = Verde,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text("Ingresos", color = Blanco.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(
                        moneda.format(ingresos),
                        color = Blanco,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text("Gastos", color = Blanco.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(
                        moneda.format(gastos),
                        color = Rojo,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val tamanoBotonDp = 46.dp
        val radioBotonPx = with(density) { (tamanoBotonDp / 2).toPx() }

        nodos.forEach { nodo ->
            val rad = Math.toRadians(nodo.anguloGrados.toDouble())
            val x = centroX + radioOrbitaPx * cos(rad).toFloat() - radioBotonPx
            val y = centroY + radioOrbitaPx * sin(rad).toFloat() - radioBotonPx

            Row(
                modifier = Modifier.offset { IntOffset(x.roundToInt(), y.roundToInt()) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(tamanoBotonDp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Tarjeta)
                        .border(1.5.dp, Verde.copy(alpha = 0.55f), CircleShape)
                        .clickable { nodo.onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = nodo.icono,
                        contentDescription = nodo.titulo,
                        tint = Verde,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = nodo.titulo,
                    color = Blanco,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Button(
            onClick = onAbrirRegistrarMovimiento,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 36.dp, vertical = 22.dp)
                .fillMaxWidth()
                .height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Rojo, contentColor = Blanco)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("REGISTRAR MOVIMIENTO", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
    }
}