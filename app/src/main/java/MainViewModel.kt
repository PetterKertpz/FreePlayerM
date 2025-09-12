package com.example.freeplayerm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UsuarioRepository // <-- Importante añadir
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val usuarioRepository: UsuarioRepository // <-- Inyectamos el UsuarioRepository
) : ViewModel() {

    val usuarioActual: StateFlow<UsuarioEntity?> = sessionRepository.idDeUsuarioActivo
        .flatMapLatest { id ->
            // flatMapLatest reacciona a cada nuevo ID emitido por el Flow de sesión.
            if (id != null) {
                // Si hay un ID, usamos el UsuarioRepository para obtener el Flow del usuario.
                usuarioRepository.obtenerUsuarioPorIdFlow(id)
            } else {
                // Si no hay ID (sesión cerrada), emitimos un Flow con valor nulo.
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // El valor inicial es nulo mientras se determina la sesión.
        )
}