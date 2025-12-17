package com.lw.ai.glasses.ui.translator

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lw.ai.glasses.ui.translate.Language
import com.lw.ai.glasses.ui.translate.TranslatorViewModel
import com.lw.top.lib_core.data.local.entity.TranslationEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    onNavigateBack: () -> Unit,
    viewModel: TranslatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var isSelectingSource by remember { mutableStateOf(true) }



    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空记录") },
            text = { Text("确定要删除所有的翻译历史记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("取消") }
            }
        )
    }

    if (showLanguageSheet) {
        LanguageSelectionSheet(
            languages = uiState.allLanguages,
            onDismissRequest = { showLanguageSheet = false },
            onLanguageSelected = { lang ->
                if (isSelectingSource) {
                    viewModel.setSourceLanguage(lang)
                } else {
                    viewModel.setTargetLanguage(lang)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI翻译") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "清空")
                    }
                }
            )
        },
        bottomBar = {
            RecordControlPanel(
                isRecording = uiState.isRecording,
                currentAmplitude = uiState.currentAmplitude,
                onStartRecording = { viewModel.startRecording() },
                onStopRecording = { viewModel.stopRecording() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LanguageTopBar(
                srcLang = uiState.srcLang,
                targetLang = uiState.targetLang,
                onSrcClick = {
                    isSelectingSource = true
                    showLanguageSheet = true
                },
                onTargetClick = {
                    isSelectingSource = false
                    showLanguageSheet = true
                },
                onSwapClick = { viewModel.swapLanguages() }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(uiState.messages) { message ->
                    TranslationItemCard(
                        item = message,
                        onPlayAudio = { audioPath ->
                            viewModel.playAudio(audioPath)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageTopBar(
    srcLang: Language?,
    targetLang: Language?,
    onSrcClick: () -> Unit,
    onTargetClick: () -> Unit,
    onSwapClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onSrcClick, modifier = Modifier.weight(1f)) {
            Text(
                srcLang?.name ?: "选择语言",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }

        IconButton(onClick = onSwapClick) {
            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = "交换",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        TextButton(onClick = onTargetClick, modifier = Modifier.weight(1f)) {
            Text(
                targetLang?.name ?: "选择语言",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionSheet(
    languages: List<Language>,
    onDismissRequest: () -> Unit,
    onLanguageSelected: (Language) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredLanguages = remember(searchQuery, languages) {
        if (searchQuery.isBlank()) {
            languages
        } else {
            languages.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.nameEn.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("选择语言", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, null)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索语言...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(filteredLanguages) { language ->
                    ListItem(
                        headlineContent = { Text(language.name) },
                        supportingContent = { Text(language.nameEn) },
                        modifier = Modifier.clickable {
                            onLanguageSelected(language)
                            onDismissRequest()
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
@Composable
fun RecordControlPanel(
    isRecording: Boolean,
    currentAmplitude: Float, // 新增：接收当前音量 (0.0 - 1.0)
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {

    val interactionSource = remember { MutableInteractionSource() }
    val currentStart by rememberUpdatedState(onStartRecording)
    val currentStop by rememberUpdatedState(onStopRecording)

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    currentStart()
                }
                is PressInteraction.Release -> {
                    currentStop()
                }
                is PressInteraction.Cancel -> {
                    currentStop()
                }
            }
        }
    }

    val pressScale by animateFloatAsState(
        targetValue = if (isRecording) 1.1f else 1.0f,
        label = "pressScale"
    )

    val volumeScale by animateFloatAsState(
        targetValue = if (isRecording) 1.0f + (currentAmplitude * 0.8f) else 1.0f,
        label = "volumeScale"
    )

    val containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val iconColor = if (isRecording) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(volumeScale)
                        .background(
                            color = containerColor.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(pressScale)
                    .background(containerColor, CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {  }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "录音",
                    tint = iconColor,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isRecording) "正在录音..." else "按住 说话",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TranslationItemCard(
    item: TranslationEntity,
    onPlayAudio: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = item.originalText ?: "...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (item.translatedText.isNullOrEmpty()) "翻译中..." else item.translatedText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                if (!item.audioPath.isNullOrEmpty()) {
                    IconButton(
                        onClick = { onPlayAudio(item.audioPath!!) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "播放",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}