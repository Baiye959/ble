package com.baiye959.ble.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.baiye959.ble.data.command.Command
import com.baiye959.ble.data.model.*

@Composable
fun DeviceStatusCard(status: DeviceStatus) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (status) {
                    DeviceStatus.Online -> Icons.Default.CheckCircle
                    DeviceStatus.Offline -> Icons.Default.Error
                    DeviceStatus.Connecting -> Icons.Default.Refresh
                    DeviceStatus.Rejected -> Icons.Default.Cancel
                },
                contentDescription = null,
                tint = when (status) {
                    DeviceStatus.Online -> Color.Green
                    DeviceStatus.Offline -> Color.Gray
                    DeviceStatus.Connecting -> Color.Blue
                    DeviceStatus.Rejected -> Color.Red
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "设备状态: ${status.name}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun CommandButton(
    command: Command,
    onClick: (Map<String, String>) -> Unit
) {
    var showDialog = remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showDialog.value = true },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = command.name)
            Text(
                text = command.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDialog.value) {
        CommandDialog(
            command = command,
            onDismiss = { showDialog.value = false },
            onConfirm = onClick
        )
    }
}

@Composable
fun MessageItem(
    message: String,
    modifier: Modifier = Modifier
) {
    val isSentMessage = message.contains("[发送]")
    Text(
        text = message,
        color = if (isSentMessage) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        modifier = modifier.padding(vertical = 4.dp)
    )
}