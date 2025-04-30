package com.example.paneldecontrolreposteria.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Pedidos : BottomNavItem("pedidos", "Pedidos", Icons.AutoMirrored.Filled.List)
    object Produccion : BottomNavItem("produccion", "Producción", Icons.Default.Restaurant)
    object Asistente : BottomNavItem("asistente", "Asistente", Icons.Default.SmartToy)
}