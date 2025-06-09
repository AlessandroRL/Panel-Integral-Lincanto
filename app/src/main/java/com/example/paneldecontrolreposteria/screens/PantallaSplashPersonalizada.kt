package com.example.paneldecontrolreposteria.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun PantallaSplashPersonalizada(onTerminar: () -> Unit) {
    var alphaTexto by remember { mutableFloatStateOf(0f) }

    val offsetLogoY by animateDpAsState(
        targetValue = 0.dp,
        animationSpec = tween(durationMillis = 1200),
        label = "logoOffset"
    )

    val alphaLogo by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1200),
        label = "logoAlpha"
    )

    LaunchedEffect(true) {
        delay(1400)
        alphaTexto = 1f
        delay(1800)
        onTerminar()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = offsetLogoY - 40.dp)
        ) {
            Image(
                painterResource(id = com.example.paneldecontrolreposteria.R.drawable.logo_recortado),
                contentDescription = "Logo L'incanto",
                modifier = Modifier
                    .size(180.dp)
                    .graphicsLayer {
                        alpha = alphaLogo
                    }
            )
            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = "Panel de control para gestión de repostería L'incanto",
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.graphicsLayer {
                    alpha = alphaTexto
                }
            )
        }
    }
}
