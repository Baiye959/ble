package com.baiye959.ble.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay

@Composable
fun CustomToast(
    message: String,
    onDismiss: () -> Unit,
    durationMillis: Long = 2000
) {
    LaunchedEffect(message) {
        delay(durationMillis)
        onDismiss()
    }

    Popup(
        alignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}