package com.example.freeplayerm.utils

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.example.freeplayerm.ui.features.library.TipoDeCuerpoBiblioteca

/**
 * 👆 UTILIDADES DE GESTOS HORIZONTALES PARA NAVEGACIÓN
 *
 * Proporciona funcionalidades para:
 * - Detectar swipes horizontales
 * - Navegar entre secciones de la biblioteca
 * - Determinar la siguiente/anterior sección
 */

// ═══════════════════════════════════════════════════════════════════════════════
// CONSTANTES
// ═══════════════════════════════════════════════════════════════════════════════

/** Umbral mínimo de distancia (en píxeles) para considerar un swipe válido */
private const val SWIPE_THRESHOLD = 100f

/**
 * Orden lógico de navegación entre secciones El usuario puede deslizar izquierda/derecha para
 * moverse en este orden
 */
val ORDEN_NAVEGACION_SECCIONES =
   listOf(
      TipoDeCuerpoBiblioteca.CANCIONES,
      TipoDeCuerpoBiblioteca.ALBUMES,
      TipoDeCuerpoBiblioteca.ARTISTAS,
      TipoDeCuerpoBiblioteca.GENEROS,
      TipoDeCuerpoBiblioteca.LISTAS,
   )

// ═══════════════════════════════════════════════════════════════════════════════
// FUNCIONES HELPER
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Obtiene la siguiente sección en el orden de navegación
 *
 * @param seccionActual La sección actual
 * @return La siguiente sección, o null si está al final
 */
fun obtenerSiguienteSeccion(seccionActual: TipoDeCuerpoBiblioteca): TipoDeCuerpoBiblioteca? {
   // Si la sección actual no está en el orden principal, no navegar
   if (seccionActual !in ORDEN_NAVEGACION_SECCIONES) return null

   val indiceActual = ORDEN_NAVEGACION_SECCIONES.indexOf(seccionActual)
   val siguienteIndice = indiceActual + 1

   return if (siguienteIndice < ORDEN_NAVEGACION_SECCIONES.size) {
      ORDEN_NAVEGACION_SECCIONES[siguienteIndice]
   } else {
      null // Ya está en la última sección
   }
}

/**
 * Obtiene la sección anterior en el orden de navegación
 *
 * @param seccionActual La sección actual
 * @return La sección anterior, o null si está al inicio
 */
fun obtenerSeccionAnterior(seccionActual: TipoDeCuerpoBiblioteca): TipoDeCuerpoBiblioteca? {
   // Si la sección actual no está en el orden principal, no navegar
   if (seccionActual !in ORDEN_NAVEGACION_SECCIONES) return null

   val indiceActual = ORDEN_NAVEGACION_SECCIONES.indexOf(seccionActual)
   val anteriorIndice = indiceActual - 1

   return if (anteriorIndice >= 0) {
      ORDEN_NAVEGACION_SECCIONES[anteriorIndice]
   } else {
      null // Ya está en la primera sección
   }
}

/**
 * Verifica si se puede navegar con gestos desde la sección actual Secciones de detalle (como
 * CANCIONES_DE_ALBUM) no permiten navegación por gestos
 */
fun puedeNavegarConGestos(seccion: TipoDeCuerpoBiblioteca): Boolean {
   return seccion in ORDEN_NAVEGACION_SECCIONES
}

// ═══════════════════════════════════════════════════════════════════════════════
// MODIFIER EXTENSION
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Modifier que detecta gestos horizontales de swipe para navegación
 *
 * @param habilitado Si la detección de gestos está habilitada
 * @param onSwipeLeft Callback cuando se detecta un swipe hacia la izquierda
 * @param onSwipeRight Callback cuando se detecta un swipe hacia la derecha
 *
 * Uso:
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .detectarSwipeHorizontal(
 *             habilitado = true,
 *             onSwipeLeft = { /* navegar a siguiente */ },
 *             onSwipeRight = { /* navegar a anterior */ }
 *         )
 * )
 * ```
 */
fun Modifier.detectarSwipeHorizontal(
   habilitado: Boolean = true,
   onSwipeLeft: () -> Unit = {},
   onSwipeRight: () -> Unit = {},
): Modifier {
   if (!habilitado) return this

   return this.pointerInput(Unit) {
      var dragDistanciaTotal = 0f
      var yaEjecuto = false

      detectHorizontalDragGestures(
         onDragStart = { _ ->
            dragDistanciaTotal = 0f
            yaEjecuto = false
         },
         onDragEnd = {
            dragDistanciaTotal = 0f
            yaEjecuto = false
         },
         onDragCancel = {
            dragDistanciaTotal = 0f
            yaEjecuto = false
         },
         onHorizontalDrag = { _, dragAmount ->
            if (!yaEjecuto) {
               dragDistanciaTotal += dragAmount

               when {
                  // Swipe hacia la izquierda (siguiente sección)
                  dragDistanciaTotal < -SWIPE_THRESHOLD -> {
                     onSwipeLeft()
                     yaEjecuto = true
                  }
                  // Swipe hacia la derecha (sección anterior)
                  dragDistanciaTotal > SWIPE_THRESHOLD -> {
                     onSwipeRight()
                     yaEjecuto = true
                  }
               }
            }
         },
      )
   }
}

/**
 * Versión alternativa que detecta swipes con acumulación de distancia Más precisa para gestos
 * largos
 */
fun Modifier.detectarSwipeHorizontalAcumulado(
   habilitado: Boolean = true,
   onSwipeLeft: () -> Unit = {},
   onSwipeRight: () -> Unit = {},
): Modifier {
   if (!habilitado) return this

   return this.pointerInput(Unit) {
      var dragDistanciaTotal = 0f
      var yaEjecuto = false

      detectHorizontalDragGestures(
         onDragStart = { _ ->
            dragDistanciaTotal = 0f
            yaEjecuto = false
         },
         onDragEnd = {
            // Reset al finalizar el gesto
            dragDistanciaTotal = 0f
            yaEjecuto = false
         },
         onDragCancel = {
            dragDistanciaTotal = 0f
            yaEjecuto = false
         },
         onHorizontalDrag = { _, dragAmount ->
            if (!yaEjecuto) {
               dragDistanciaTotal += dragAmount

               when {
                  // Swipe hacia la izquierda (siguiente)
                  dragDistanciaTotal < -SWIPE_THRESHOLD -> {
                     onSwipeLeft()
                     yaEjecuto = true
                  }
                  // Swipe hacia la derecha (anterior)
                  dragDistanciaTotal > SWIPE_THRESHOLD -> {
                     onSwipeRight()
                     yaEjecuto = true
                  }
               }
            }
         },
      )
   }
}

// ═══════════════════════════════════════════════════════════════════════════════
// FUNCIONES DE NAVEGACIÓN
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Maneja la navegación por swipe hacia la izquierda
 *
 * @param seccionActual La sección desde donde se hace el swipe
 * @return La siguiente sección a navegar, o null si no se puede
 */
fun manejarSwipeIzquierda(seccionActual: TipoDeCuerpoBiblioteca): TipoDeCuerpoBiblioteca? {
   if (!puedeNavegarConGestos(seccionActual)) return null
   return obtenerSiguienteSeccion(seccionActual)
}

/**
 * Maneja la navegación por swipe hacia la derecha
 *
 * @param seccionActual La sección desde donde se hace el swipe
 * @return La sección anterior a navegar, o null si no se puede
 */
fun manejarSwipeDerecha(seccionActual: TipoDeCuerpoBiblioteca): TipoDeCuerpoBiblioteca? {
   if (!puedeNavegarConGestos(seccionActual)) return null
   return obtenerSeccionAnterior(seccionActual)
}
