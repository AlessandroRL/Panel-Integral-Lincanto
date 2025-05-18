package com.example.paneldecontrolreposteria.ui.asistente.voice

class TextInputInterpreter {

    fun interpretar(texto: String): Comando {
        val lower = texto.lowercase()

        return when {
            lower.contains("agrega") && lower.contains("pedido") -> Comando.AbrirAgregarPedido

            lower.contains("pedidos") -> Comando.AbrirSeccionPedidos

            lower.contains("ingredientes") -> Comando.AbrirSeccionIngredientes

            lower.contains("costos") || lower.contains("precio") -> Comando.AbrirSeccionCostos

            lower.contains("elimina") && lower.contains("ingrediente") -> {
                val nombre = extraerPalabraDespuesDe(texto, "ingrediente")
                if (nombre != null) Comando.EliminarIngrediente(nombre)
                else Comando.NoReconocido
            }

            lower.contains("edita") && lower.contains("producto") -> {
                val nombre = extraerPalabraDespuesDe(texto, "producto")
                if (nombre != null) Comando.EditarProducto(nombre)
                else Comando.NoReconocido
            }

            else -> Comando.NoReconocido
        }
    }

    private fun extraerPalabraDespuesDe(texto: String, palabraClave: String): String? {
        val palabras = texto.lowercase().split(" ")
        val indice = palabras.indexOf(palabraClave)
        return if (indice != -1 && indice + 1 < palabras.size) {
            palabras[indice + 1]
        } else null
    }
}