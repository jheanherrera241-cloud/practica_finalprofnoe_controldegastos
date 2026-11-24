package com.jeampol.comjeampolcontrolgastos.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jeampol.comjeampolcontrolgastos.AuthViewModel

@Composable
fun AuthScreen(vm: AuthViewModel) {
    var esRegistro by remember { mutableStateOf(false) }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (esRegistro) "Crear cuenta" else "Iniciar sesión",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = correo, onValueChange = { correo = it },
            label = { Text("Correo") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = clave, onValueChange = { clave = it },
            label = { Text("Contraseña") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        if (esRegistro) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmar, onValueChange = { confirmar = it },
                label = { Text("Confirmar contraseña") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
        }

        vm.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        vm.mensaje?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (esRegistro) vm.registrar(correo, clave, confirmar)
                else vm.iniciarSesion(correo, clave)
            },
            enabled = !vm.cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (vm.cargando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text(if (esRegistro) "Registrarme" else "Entrar")
        }

        if (!esRegistro) {
            TextButton(onClick = { vm.recuperar(correo) }) { Text("¿Olvidaste tu contraseña?") }
        }
        TextButton(onClick = { esRegistro = !esRegistro; vm.limpiarMensajes() }) {
            Text(if (esRegistro) "Ya tengo cuenta" else "Crear una cuenta")
        }
    }
}