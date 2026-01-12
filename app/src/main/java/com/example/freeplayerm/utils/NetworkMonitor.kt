package com.example.freeplayerm.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

// Tipos de conexión de red
sealed class NetworkStatus {
   data object Wifi : NetworkStatus()
   data object MobileData : NetworkStatus()
   data object Offline : NetworkStatus()
   data object Unknown : NetworkStatus()
}

// Información detallada de la conexión
data class NetworkInfo(
   val status: NetworkStatus,
   val isConnected: Boolean,
   val isWifi: Boolean,
   val isMobileData: Boolean,
   val isMetered: Boolean
) {
   companion object {
      val OFFLINE = NetworkInfo(
         status = NetworkStatus.Offline,
         isConnected = false,
         isWifi = false,
         isMobileData = false,
         isMetered = false
      )
   }
}

@Singleton
class NetworkMonitor @Inject constructor(
   @ApplicationContext private val context: Context
) {
   private val connectivityManager =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
   
   private val _networkInfo = MutableStateFlow(getCurrentNetworkInfo())
   val networkInfo: StateFlow<NetworkInfo> = _networkInfo.asStateFlow()
   
   // Flow reactivo para cambios de red
   val networkStatus: Flow<NetworkStatus> = callbackFlow {
      val callback = object : ConnectivityManager.NetworkCallback() {
         override fun onAvailable(network: Network) {
            Log.d(TAG, "Red disponible")
            updateNetworkInfo()
            trySend(_networkInfo.value.status)
         }
         
         override fun onLost(network: Network) {
            Log.d(TAG, "Red perdida")
            _networkInfo.value = NetworkInfo.OFFLINE
            trySend(NetworkStatus.Offline)
         }
         
         override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
         ) {
            Log.d(TAG, "Capacidades de red cambiaron")
            updateNetworkInfo()
            trySend(_networkInfo.value.status)
         }
      }
      
      val request = NetworkRequest.Builder()
         .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
         .build()
      
      connectivityManager.registerNetworkCallback(request, callback)
      
      // Emitir estado inicial
      trySend(getCurrentNetworkInfo().status)
      
      awaitClose {
         connectivityManager.unregisterNetworkCallback(callback)
      }
   }.distinctUntilChanged()
   
   // Verificación síncrona del estado actual
   fun getCurrentNetworkInfo(): NetworkInfo {
      val activeNetwork = connectivityManager.activeNetwork
      val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
      
      return when {
         capabilities == null -> NetworkInfo.OFFLINE
         
         capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkInfo(
            status = NetworkStatus.Wifi,
            isConnected = true,
            isWifi = true,
            isMobileData = false,
            isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
         )
         
         capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkInfo(
            status = NetworkStatus.MobileData,
            isConnected = true,
            isWifi = false,
            isMobileData = true,
            isMetered = true
         )
         
         capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkInfo(
            status = NetworkStatus.Wifi, // Ethernet = similar a WiFi
            isConnected = true,
            isWifi = true,
            isMobileData = false,
            isMetered = false
         )
         
         else -> NetworkInfo(
            status = NetworkStatus.Unknown,
            isConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
            isWifi = false,
            isMobileData = false,
            isMetered = true
         )
      }
   }
   
   private fun updateNetworkInfo() {
      _networkInfo.value = getCurrentNetworkInfo()
   }
   
   // Verificaciones rápidas
   fun isWifiConnected(): Boolean = _networkInfo.value.isWifi
   
   fun isMobileDataConnected(): Boolean = _networkInfo.value.isMobileData
   
   fun isConnected(): Boolean = _networkInfo.value.isConnected
   
   fun isMetered(): Boolean = _networkInfo.value.isMetered
   
   // Verificar si puede hacer streaming según preferencias
   fun canStream(soloWifiStreaming: Boolean): Boolean {
      val info = _networkInfo.value
      return when {
         !info.isConnected -> false
         !soloWifiStreaming -> true
         info.isWifi -> true
         else -> false
      }
   }
   
   companion object {
      private const val TAG = "NetworkMonitor"
   }
}