package com.example.paneldecontrolreposteria.ui.notificaciones

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
    onCancelar: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.DarkGray
    val cardColor = if (isDarkTheme) Color.DarkGray else Color.White
    val gold = Color(0xFFC7A449)

    val context = LocalContext.current

    var allDay by remember { mutableStateOf(false) }
    var fecha by remember { mutableStateOf(LocalDate.now()) }
    var horaInicio by remember { mutableStateOf(LocalTime.of(8, 0)) }

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

    Scaffold(
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Todo el día", modifier = Modifier.weight(1f), color = textColor, style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = allDay, onCheckedChange = { allDay = it }, colors = SwitchDefaults.colors(checkedThumbColor = gold))
                    }

                    Spacer(Modifier.height(16.dp))

                    Column(modifier = Modifier.clickable { showDatePicker = true }) {
                        Text("Fecha", style = MaterialTheme.typography.labelMedium, color = textColor)
                        Text(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), style = MaterialTheme.typography.bodyLarge, color = textColor)
                    }

                    if (!allDay) {
                        Spacer(Modifier.height(8.dp))
                        Column(modifier = Modifier.clickable { showTimePicker = true }) {
                            Text("Hora", style = MaterialTheme.typography.labelMedium, color = textColor)
                            Text(horaInicio.format(DateTimeFormatter.ofPattern("hh:mm a")), style = MaterialTheme.typography.bodyLarge, color = textColor)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(
                            onClick = onCancelar,
                            border = BorderStroke(1.dp, Color.Red),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Cancelar", color = Color.Red)
                        }
                        Button(
                            onClick = {
                                val fechaHora = if (allDay) fecha.atStartOfDay() else LocalDateTime.of(fecha, horaInicio)
                                onGuardar(fechaHora)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = gold)
                        ) {
                            Text("Guardar", color = textColor)
                        }
                    }
                }
            }
        }
    }
}