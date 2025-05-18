package com.example.paneldecontrolreposteria.ui.asistente

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AsistenteButtonFloating(
    currentTabIndex: Int,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentTabIndex != 2) {
        FloatingActionButton(
            onClick = onMicClick,
            containerColor = MaterialTheme.colorScheme.primary,
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