package com.lw.ai.glasses.ui.home

import Screen
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Battery2Bar
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Battery4Bar
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lw.ai.glasses.ui.base.screen.popup.CenteredFadeInPopup
import com.polidea.rxandroidble3.scan.ScanResult


@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToImage: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onNavigateToSetting: () -> Unit
) {
    var showScanningDevices by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        viewModel.onPermissionResult(allGranted)
        checkAndRequestManageStoragePermission(context)
    }

    LaunchedEffect(viewModel.permissionEvent) {
        viewModel.permissionEvent.collect { permissions ->
            if (permissions.isNotEmpty()) {
                permissionLauncher.launch(permissions.toTypedArray())
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            DeviceStatus(
                deviceName = uiState.connectedDeviceName,
                connectionState = uiState.connectionState,
                batteryLevel = uiState.batteryLevel
            )

            CompositionLocalProvider(LocalContentColor provides Color.Black) {
                IconButton(
                    onClick = {
                        showScanningDevices = !showScanningDevices
                        viewModel.startScanDevice()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "扫描设备"
                    )
                }
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 40.dp)
        ) {
            items(items = uiState.features, key = { it.id }) { feature ->
                FeatureCard(
                    feature = feature,
                    onClick = {
                        when (feature.route) {
                            Screen.Image.route -> {
                                onNavigateToImage()
                            }
                            Screen.Assistant.route -> {
                                onNavigateToAssistant()
                            }
                            Screen.Setting.route -> {
                                onNavigateToSetting()
                            }
                            else -> {
                            }
                        }
                    }
                )
            }
        }

    }

    CenteredFadeInPopup(
        visible = showScanningDevices,
        onDismissRequest = { showScanningDevices = false },
        modifier = Modifier.padding(horizontal = 40.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "扫描到的设备",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (uiState.scannedDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("未发现设备")
                    }
                } else {
                    LazyColumn {
                        items(
                            items = uiState.scannedDevices,
                            key = { result -> result.bleDevice.macAddress }) { result ->
                            ScannedDeviceItem(
                                scanResult = result,
                                onClick = {
                                    showScanningDevices = false
                                    viewModel.connectDevice(
                                        result.bleDevice.macAddress,
                                        result.bleDevice.name.toString()
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScannedDeviceItem(
    scanResult: ScanResult,
    onClick: () -> Unit
) {
    val device = scanResult.bleDevice
    val name = device.name ?: "未知设备"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "MAC: ${device.macAddress}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "RSSI: ${scanResult.rssi}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun DeviceStatus(
    deviceName: String?,
    connectionState: ConnectionState,
    batteryLevel: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.End) {
            val deviceNameToDisplay = deviceName ?: "点击右侧+连接设备"
            Text(
                text = deviceNameToDisplay,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            val (statusText, statusColor) = when (connectionState) {
                ConnectionState.CONNECTED -> "已连接" to MaterialTheme.colorScheme.primary
                ConnectionState.CONNECTING -> "正在连接..." to Color.Blue
                ConnectionState.DISCONNECTED -> "未连接" to Color.Gray
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor
            )
        }
        Icon(
            imageVector = getBatteryIcon(batteryLevel),
            contentDescription = "电量",
            modifier = Modifier
                .padding(start = 8.dp)
                .size(20.dp),
            tint = when (batteryLevel) {
                in 90..100 -> {
                    Color.Green
                }

                in 60..89 -> {
                    Color.Yellow
                }

                in 20..59 -> {
                    Color.Gray
                }

                else -> {
                    Color.Red
                }
            }
        )
        if (batteryLevel != -1) {
            Text(
                text = "$batteryLevel%",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun getBatteryIcon(batteryLevel: Int): ImageVector {
    return when {
        batteryLevel > 95 -> Icons.Filled.BatteryFull
        batteryLevel > 80 -> Icons.Filled.Battery6Bar
        batteryLevel > 60 -> Icons.Filled.Battery5Bar
        batteryLevel > 40 -> Icons.Filled.Battery4Bar
        batteryLevel > 20 -> Icons.Filled.Battery3Bar
        batteryLevel > 10 -> Icons.Filled.Battery2Bar
        else -> Icons.Default.BatteryAlert
    }
}


@Composable
fun FeatureCard(feature: Feature, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.name,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = feature.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (feature.badgeCount != null && feature.badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = feature.badgeCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


private fun checkAndRequestManageStoragePermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (!Environment.isExternalStorageManager()) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${context.packageName}")
                context.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                context.startActivity(intent)
            }
        }
    }
}