package com.baiye959.ble.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.baiye959.ble.data.command.Command

@Composable
fun CommandDialog(
    command: Command,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, String>) -> Unit
) {
    var params by remember { mutableStateOf(command.message.params.mapValues { it.value.toString() }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(command.name) },
        text = {
            Column {
                params.forEach { (key, value) ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            params = params.toMutableMap().apply {
                                put(key, newValue)
                            }
                        },
                        label = { Text(key) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(params)
                    onDismiss()
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}