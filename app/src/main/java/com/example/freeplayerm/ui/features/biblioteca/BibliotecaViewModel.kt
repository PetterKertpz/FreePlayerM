// app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/BibliotecaViewModel.kt
package com.example.freeplayerm.ui.features.biblioteca

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.example.freeplayerm.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BibliotecaEstado(
    val usuarioActual: UsuarioEntity? = null
)

@HiltViewModel
class BibliotecaViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(BibliotecaEstado())
    val estadoUi = _estadoUi.asStateFlow()

    fun cargarDatosDeUsuario(usuarioId: Int) {
        // La referencia a 'id' ahora se resuelve porque el import de UsuarioEntity es correcto
        if (_estadoUi.value.usuarioActual?.id == usuarioId) return

        viewModelScope.launch {
            if (usuarioId != -1) {
                val usuario = usuarioRepository.obtenerUsuarioPorId(usuarioId)
                _estadoUi.value = BibliotecaEstado(usuarioActual = usuario)
            }
        }
    }
}