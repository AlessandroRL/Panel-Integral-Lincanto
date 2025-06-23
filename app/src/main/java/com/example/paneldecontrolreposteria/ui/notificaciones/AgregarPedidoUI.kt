package com.example.paneldecontrolreposteria.ui.notificaciones

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AgregarNotificacionUI(
    onGuardar: (LocalDateTime) -> Unit,
    onCancelar: () -> Unit,
    fechaInicial: LocalDateTime? = null
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.DarkGray
    val cardColor = if (isDarkTheme) Color.DarkGray else Color.White
    val gold = Color(0xFFC7A449)
    val secondaryVariant = if (isDarkTheme) Color.White else Color(0xFF705852)

    val context = LocalContext.current

    var allDay by remember { mutableStateOf(fechaInicial?.toLocalTime() == LocalTime.MIDNIGHT) }
    var fecha by remember { mutableStateOf(fechaInicial?.toLocalDate() ?: LocalDate.now()) }
    var horaInicio by remember { mutableStateOf(fechaInicial?.toLocalTime() ?: LocalTime.of(8, 0)) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                fecha = LocalDate.of(y, m + 1, d)
                showDatePicker = false
            },
            fecha.year, fecha.monthValue - 1, fecha.dayOfMonth
        ).show()
    }

    if (showTimePicker && !allDay) {
        TimePickerDialog(
            context,
            { _, h, m ->
                horaInicio = LocalTime.of(h, m)
                showTimePicker = false
            },
            horaInicio.hour, horaInicio.minute, false
        ).show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Todo el día", modifier = Modifier.weight(1f), color = textColor, style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = allDay, onCheckedChange = { allDay = it }, colors = SwitchDefaults.colors(
                        checkedThumbColor = gold,
                        checkedTrackColor = secondaryVariant
                    ))
                }

                Spacer(Modifier.height(16.dp))

                Column(modifier = Modifier.clickable { showDatePicker = true }) {
                    Text("Fecha", style = MaterialTheme.typography.bodyLarge, color = textColor)
                    Text(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), style = MaterialTheme.typography.bodyLarge, color = textColor)
                }

                if (!allDay) {
                    Spacer(Modifier.height(8.dp))
                    Column(modifier = Modifier.clickable { showTimePicker = true }) {
                        Text("Hora", style = MaterialTheme.typography.bodyLarge, color = textColor)
                        Text(horaInicio.format(DateTimeFormatter.ofPattern("hh:mm a")), style = MaterialTheme.typography.bodyLarge, color = textColor)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(
                        onClick = onCancelar,
                        border = BorderStroke(1.dp, Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", color = Color.Red, style = MaterialTheme.typography.bodyLarge)
                    }
                    Button(
                        onClick = {
                            val fechaHora = if (allDay) fecha.atStartOfDay() else LocalDateTime.of(fecha, horaInicio)
                            onGuardar(fechaHora)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = gold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar", color = textColor, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}