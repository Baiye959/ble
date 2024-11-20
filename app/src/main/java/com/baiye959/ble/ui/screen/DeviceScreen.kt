package com.baiye959.ble.ui.screen

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.baiye959.ble.data.model.DeviceScreenMode
import com.baiye959.ble.ui.components.*
import com.baiye959.ble.viewmodel.DeviceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScreen(
    viewModel: DeviceViewModel,
    onBackPressed: () -> Unit
) {
    val currentMode by viewModel.currentMode.collectAsState()

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
                    if (currentMode == DeviceScreenMode.Debug) {
                        IconButton(onClick = { viewModel.clearMessages() }) {
                            Icon(Icons.Default.Delete, "清空记录")
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.setScreenMode(
                                if (currentMode == DeviceScreenMode.User)
                                    DeviceScreenMode.Debug
                                else
                                    DeviceScreenMode.User
                            )
                        }
                    ) {
                        Icon(
                            imageVector = if (currentMode == DeviceScreenMode.User)
                                Icons.Default.Build
                            else
                                Icons.Default.Person,
                            contentDescription = "切换模式"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (currentMode) {
            DeviceScreenMode.User -> UserScreen(viewModel, Modifier.padding(padding))
            DeviceScreenMode.Debug -> DebugScreen(viewModel, Modifier.padding(padding))
        }
    }
}

@Composable
fun UserScreen(
    viewModel: DeviceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.deviceUiState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        DeviceStatusCard(
            status = uiState.deviceStatus,
            getCurrentFps = uiState.getCurrentFps
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(uiState.availableCommands) { command ->
                CommandButton(
                    command = command,
                    onClick = { params ->
                        // 使用新的参数更新命令
                        val updatedCommand = command.copy(
                            message = command.message.copy(
                                params = params
                            )
                        )
                        viewModel.sendCommand(context, updatedCommand)
                    }
                )
            }
        }
    }

    toastMessage?.let { message ->
        CustomToast(
            message = message,
            onDismiss = { viewModel._toastMessage.value = null }
        )
    }
}

@Composable
fun DebugScreen(
    viewModel: DeviceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()
    var sendText by remember { mutableStateOf("") }
    val isHexMode by viewModel.isHexMode.collectAsState()
    val autoScroll by viewModel.autoScroll.collectAsState()

    LaunchedEffect(messages) {
        if (autoScroll && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
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