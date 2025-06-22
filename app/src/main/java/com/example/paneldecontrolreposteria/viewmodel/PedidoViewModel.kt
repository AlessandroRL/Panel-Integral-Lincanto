package com.example.paneldecontrolreposteria.viewmodel

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paneldecontrolreposteria.model.Pedido
import com.example.paneldecontrolreposteria.receiver.NotificacionReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PedidoViewModel : ViewModel() {
    private val repository = PedidoRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos

    private val _productos = MutableStateFlow<List<String>>(emptyList())
    val productos: StateFlow<List<String>> = _productos

    init {
        obtenerPedidos()
    }

    fun obtenerPedidos() {
        firestore.collection("pedidos").get()
            .addOnSuccessListener { result ->
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                val ahora = LocalDateTime.now()

                val listaPedidos = result.documents.mapNotNull { doc ->
                    val pedido = doc.toObject(Pedido::class.java)
                    pedido?.id = doc.id

                    val notificacionesFiltradas = pedido?.notificaciones?.mapNotNull {
                        try {
                            val fecha = LocalDateTime.parse(it, formatter)
                            if (fecha.isAfter(ahora)) it else null
                        } catch (_: Exception) {
                            null
                        }
                    } ?: emptyList()

                    if (pedido != null && notificacionesFiltradas.size != pedido.notificaciones.size) {
                        firestore.collection("pedidos").document(pedido.id)
                            .update("notificaciones", notificacionesFiltradas)
                            .addOnFailureListener { e ->
                                Log.e("PedidoViewModel", "Error al limpiar notificaciones vencidas: ${e.message}")
                            }
                        pedido.notificaciones = notificacionesFiltradas
                    }

                    Log.d("PedidoViewModel", "Pedido obtenido: ${pedido?.id}")
                    pedido
                }

                _pedidos.value = listaPedidos
            }
            .addOnFailureListener { e ->
                Log.e("PedidoViewModel", "Error al obtener pedidos", e)
            }
    }

    fun agregarPedido(pedido: Pedido) {
        viewModelScope.launch {
            try {
                repository.agregarPedido(pedido)
                obtenerPedidos()
            } catch (e: Exception) {
                Log.e("PedidoViewModel", "Error al agregar pedido: ${e.message}")
            }
        }
    }

    fun editarPedido(pedido: Pedido, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.editarPedido(pedido)
            if (result) obtenerPedidos()
            onResult(result)
        }
    }

    fun eliminarPedido(pedidoId: String) {
        viewModelScope.launch {
            try {
                repository.eliminarPedido(pedidoId)
                obtenerPedidos()
            } catch (e: Exception) {
                Log.e("PedidoViewModel", "Error al eliminar pedido: ${e.message}")
            }
        }
    }

    fun obtenerNombresProductos(onResultado: (List<String>) -> Unit) {
        viewModelScope.launch {
            try {
                val productos = repository.obtenerProductos()
                val nombres = productos.map { it.nombre }
                onResultado(nombres)
            } catch (e: Exception) {
                Log.e("PedidoViewModel", "Error al obtener nombres de productos: ${e.message}")
                onResultado(emptyList())
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(value = "android.permission.SCHEDULE_EXACT_ALARM", conditional = true)
    @RequiresApi(Build.VERSION_CODES.M)
    fun programarNotificacion(context: Context, pedido: Pedido) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pedidoLimpio = limpiarNotificacionesExpiradas(pedido)

        pedidoLimpio.notificaciones.forEachIndexed { index, fechaStr ->
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                val fecha = LocalDateTime.parse(fechaStr, formatter)

                val intent = Intent(context, NotificacionReceiver::class.java).apply {
                    putExtra("titulo", "📦 Pedido para ${pedido.cliente}")
                    putExtra("mensaje", "Este pedido vence el ${pedido.fechaLimite}")
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    pedido.id.hashCode() + index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val triggerTime = fecha
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } catch (e: Exception) {
                Log.e("Notificacion", "Error al parsear fecha de notificación: $fechaStr", e)
            }
        }
    }

    fun limpiarNotificacionesExpiradas(pedido: Pedido): Pedido {
        val ahora = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

        val notificacionesValidas = pedido.notificaciones.filter {
            try {
                val fecha = LocalDateTime.parse(it, formatter)
                fecha.isAfter(ahora)
            } catch (_: Exception) {
                false
            }
        }

        return pedido.copy(notificaciones = notificacionesValidas)
    }
}