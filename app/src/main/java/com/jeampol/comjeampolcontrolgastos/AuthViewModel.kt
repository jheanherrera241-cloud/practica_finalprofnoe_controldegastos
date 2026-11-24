package com.jeampol.comjeampolcontrolgastos

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth

class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth

    var usuario by mutableStateOf(auth.currentUser); private set
    var cargando by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var mensaje by mutableStateOf<String?>(null); private set

    fun limpiarMensajes() { error = null; mensaje = null }

    fun iniciarSesion(correo: String, clave: String) {
        if (correo.isBlank() || clave.isBlank()) { error = "Completa correo y contraseña"; return }
        cargando = true; limpiarMensajes()
        auth.signInWithEmailAndPassword(correo.trim(), clave)
            .addOnCompleteListener { t ->
                cargando = false
                if (t.isSuccessful) usuario = auth.currentUser
                else { Log.e("AUTH", "Error: ${t.exception?.message}"); error = traducir(t.exception) }
            }
    }

    fun registrar(correo: String, clave: String, confirmar: String) {
        if (correo.isBlank() || clave.isBlank()) { error = "Completa todos los campos"; return }
        if (clave.length < 6) { error = "La contraseña debe tener al menos 6 caracteres"; return }
        if (clave != confirmar) { error = "Las contraseñas no coinciden"; return }
        cargando = true; limpiarMensajes()
        auth.createUserWithEmailAndPassword(correo.trim(), clave)
            .addOnCompleteListener { t ->
                cargando = false
                if (t.isSuccessful) usuario = auth.currentUser
                else { Log.e("AUTH", "Error: ${t.exception?.message}"); error = traducir(t.exception) }
            }
    }

    fun recuperar(correo: String) {
        if (correo.isBlank()) { error = "Escribe tu correo primero"; return }
        limpiarMensajes()
        auth.sendPasswordResetEmail(correo.trim())
            .addOnCompleteListener { t ->
                if (t.isSuccessful) mensaje = "Te enviamos un correo para restablecer la contraseña"
                else error = traducir(t.exception)
            }
    }

    fun cerrarSesion() { auth.signOut(); usuario = null }

    private fun traducir(e: Exception?): String = when (e) {
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
        is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo"
        is FirebaseAuthUserCollisionException -> "Ese correo ya está registrado"
        is FirebaseNetworkException -> "Sin conexión a internet"
        else -> e?.message ?: "Ocurrió un error"
    }
}