// en: app/src/main/java/com/example/freeplayerm/ui/features/reproductor/ReproductorViewModel.kt
package com.example.freeplayerm.ui.features.reproductor

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ReproductorViewModel @Inject constructor(
    // Aquí más adelante inyectaremos los repositorios de música
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(ReproductorEstado())
    val estadoUi = _estadoUi.asStateFlow()

    fun enEvento(evento: ReproductorEvento) {
        when (evento) {
            is ReproductorEvento.SeleccionarCancion -> {
                _estadoUi.update {
                    it.copy(
                        cancionActual = evento.cancion,
                        estaReproduciendo = true,
                        progresoActualMs = 0L
                    )
                }
                // TODO: Aquí llamarías al servicio para que empiece a sonar la canción
            }

            ReproductorEvento.ReproducirPausar -> {
                _estadoUi.update {
                    it.copy(estaReproduciendo = !it.estaReproduciendo)
                }
                // TODO: Aquí pausarías o reanudarías la reproducción real
            }

            ReproductorEvento.AlternarFavorito -> {
                _estadoUi.update {
                    it.copy(esFavorita = !it.esFavorita)
                }
                // TODO: Aquí actualizarías la base de datos para marcar/desmarcar como favorita
            }

            // Lógica simulada para los otros botones
            ReproductorEvento.CambiarModoReproduccion -> {
                _estadoUi.update {
                    val nuevoModo = if (it.modoReproduccion == ModoReproduccion.EN_ORDEN) {
                        ModoReproduccion.ALEATORIO
                    } else {
                        ModoReproduccion.EN_ORDEN
                    }
                    it.copy(modoReproduccion = nuevoModo)
                }
            }

            ReproductorEvento.CambiarModoRepeticion -> {
                _estadoUi.update {
                    val nuevoModo = when (it.modoRepeticion) {
                        ModoRepeticion.NO_REPETIR -> ModoRepeticion.REPETIR_LISTA
                        ModoRepeticion.REPETIR_LISTA -> ModoRepeticion.REPETIR_CANCION
                        ModoRepeticion.REPETIR_CANCION -> ModoRepeticion.NO_REPETIR
                    }
                    it.copy(modoRepeticion = nuevoModo)
                }
            }

            // Los eventos restantes los implementaremos después
            else -> {}
        }
    }
}