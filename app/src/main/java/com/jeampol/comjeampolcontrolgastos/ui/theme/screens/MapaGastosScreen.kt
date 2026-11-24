package com.jeampol.comjeampolcontrolgastos.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.jeampol.comjeampolcontrolgastos.data.MovimientoConCategoria
import com.jeampol.comjeampolcontrolgastos.ui.theme.theme.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MapaGastosScreen(
    movimientos: List<MovimientoConCategoria>,
    onVolver: () -> Unit
) {
    val moneda = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-PE")) }
    val movimientosConUbicacion = movimientos.filter { it.latitud != null && it.longitud != null }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)

                    val puntoInicial = if (movimientosConUbicacion.isNotEmpty()) {
                        GeoPoint(movimientosConUbicacion.first().latitud!!, movimientosConUbicacion.first().longitud!!)
                    } else {
                        GeoPoint(-12.0464, -77.0428) // Lima por defecto
                    }
                    controller.setCenter(puntoInicial)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                mapView.overlays.clear()

                movimientosConUbicacion.forEach { mov ->
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(mov.latitud!!, mov.longitud!!)
                        title = "${mov.categoriaNombre}: ${moneda.format(mov.monto)}"
                        snippet = mov.nota.ifBlank { "Gasto registrado" }
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(marker)
                }
                mapView.invalidate()
            }
        )

        Surface(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = Tarjeta,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Blanco
                    )
                }
                Text(
                    text = "Mapa de Gastos (${movimientosConUbicacion.size})",
                    color = Blanco,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }
    }
}