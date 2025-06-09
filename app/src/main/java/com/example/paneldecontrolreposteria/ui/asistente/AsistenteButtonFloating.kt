package com.example.paneldecontrolreposteria.ui.asistente

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun AsistenteButtonFloating(
    currentTabIndex: Int,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val secondaryVariant = if (isDarkTheme) Color.White else Color(0xFF705852)
    if (currentTabIndex != 2) {
        FloatingActionButton(
            onClick = onMicClick,
            containerColor = secondaryVariant,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Activar asistente"
            )
        }
    }
}