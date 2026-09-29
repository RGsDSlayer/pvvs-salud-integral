package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.RibbonRed

data class ActiveAlarmEvent(
    val pickupId: Int = 0,
    val title: String,
    val message: String,
    val medicineName: String,
    val centerName: String,
    val pickupDateFormatted: String,
    val isOneDayBefore: Boolean = true
)

@Composable
fun ActiveAlarmDialog(
    alarmEvent: ActiveAlarmEvent,
    onDismiss: () -> Unit,
    onSkipAlarm: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_alarm")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alarm_scale"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pulsing Alarm Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(AlertAmber.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(AlertAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alarma Activa",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = alarmEvent.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = RibbonRed
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (alarmEvent.isOneDayBefore) {
                        "Alarma preventiva: 1 día antes a las 8:00 AM"
                    } else {
                        "Alarma del día de entrega a las 8:00 AM"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocalPharmacy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Tratamiento: ${alarmEvent.medicineName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = AlertAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Fecha de entrega: ${alarmEvent.pickupDateFormatted}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }

                        Text(
                            text = "📍 ${alarmEvent.centerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sound banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AlertAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = AlertAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reproduciendo sonido de alarma de recojo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Lleva contigo tu documento de identidad (DNI), carnet TARV y frasco anterior.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Saltar Alarma button as requested
                Button(
                    onClick = onSkipAlarm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_skip_alarm"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saltar Alarma de este Recojo", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_dismiss_alarm")
                ) {
                    Icon(imageVector = Icons.Default.AlarmOff, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Detener Sonido / Cerrar")
                }
            }
        },
        dismissButton = null
    )
}
