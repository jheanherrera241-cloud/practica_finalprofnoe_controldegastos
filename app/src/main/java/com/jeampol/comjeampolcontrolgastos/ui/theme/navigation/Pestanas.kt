package com.jeampol.comjeampolcontrolgastos.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jeampol.comjeampolcontrolgastos.ui.theme.theme.Tarjeta
import com.jeampol.comjeampolcontrolgastos.ui.theme.theme.Verde

enum class Pestana(val icono: ImageVector, val descripcion: String) {
    INICIO(Icons.Default.Home, "Inicio"),
    RESUMEN(Icons.Default.PieChart, "Resumen"),
    METAS(Icons.Default.Star, "Metas"),
    PERFIL(Icons.Default.Person, "Perfil")
}

@Composable
fun BarraInferior(actual: Pestana, onSeleccionar: (Pestana) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        shape = RoundedCornerShape(32.dp),
        color = Tarjeta,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Pestana.entries.forEach { p ->
                IconButton(onClick = { onSeleccionar(p) }) {
                    Icon(
                        imageVector = p.icono,
                        contentDescription = p.descripcion,
                        tint = if (p == actual) Verde else Color.LightGray
                    )
                }
            }
        }
    }
}