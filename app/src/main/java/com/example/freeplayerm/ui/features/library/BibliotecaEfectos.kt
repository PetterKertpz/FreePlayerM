package com.example.freeplayerm.ui.features.library

sealed class EfectoNavegacion {
   data object AbrirPerfil : EfectoNavegacion()
   data object AbrirConfiguraciones : EfectoNavegacion()
   // Agregar más efectos según necesites
}