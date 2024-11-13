package com.baiye959.ble.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.baiye959.ble.viewmodel.DeviceViewModel


@Composable
fun MessageItem(message: String, modifier: Modifier = Modifier) {
    val isSentMessage = message.contains("[发送成功]")

    Text(
        text = message,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = if (isSentMessage) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScreen(
    viewModel: DeviceViewModel,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()
    var sendText by remember { mutableStateOf("") }
    val isHexMode by viewModel.isHexMode.collectAsState()
    val autoScroll by viewModel.autoScroll.collectAsState()

    // 自动滚动到底部
    LaunchedEffect(messages) {
        if (autoScroll && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设备通信") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.clearMessages() }
                    ) {
                        Text("清空记录")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 消息显示区域
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
//                items(messages.size) { index ->
//                    Text(
//                        text = messages[index],
//                        modifier = Modifier.padding(vertical = 4.dp)
//                    )
//                }
                items(messages.size) { index ->
                    MessageItem(
                        message = messages[index],
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (index < messages.size - 1) {
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }

            // 控制区域
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = autoScroll,
                        onCheckedChange = { viewModel.setAutoScroll(it) }
                    )
                    Text("自动滚动")

                    Spacer(modifier = Modifier.width(16.dp))

                    Checkbox(
                        checked = isHexMode,
                        onCheckedChange = { viewModel.setHexMode(it) }
                    )
                    Text("HEX")
                }
            }

            // 发送区域
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = sendText,
                    onValueChange = { sendText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入发送内容") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (sendText.isNotEmpty()) {
                            viewModel.sendMessage(context, sendText)
                            sendText = ""
                        }
                    }
                ) {
                    Text("发送")
                }
            }
        }
    }
}