package com.homebax.axionis.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homebax.axionis.ui.theme.AxionisAITheme
import com.homebax.axionis.R
import com.homebax.axionis.data.ModelInfo
import com.homebax.axionis.data.DownloadState
import com.homebax.axionis.data.ModelCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val userName by viewModel.userName.collectAsState()
    val aiName by viewModel.aiName.collectAsState()

    val behavior by viewModel.behavior.collectAsState()
    val systemInstruction by viewModel.systemInstruction.collectAsState()
    val blockAi by viewModel.blockAi.collectAsState()
    val rolePlay by viewModel.rolePlay.collectAsState()
    val rpCharacterName by viewModel.rpCharacterName.collectAsState()
    val rpDescription by viewModel.rpDescription.collectAsState()
    val creativity by viewModel.creativity.collectAsState()

    val nsfwEnabled by viewModel.nsfwEnabled.collectAsState()
    val emojiEnabled by viewModel.emojiEnabled.collectAsState()
    val markdownEnabled by viewModel.markdownEnabled.collectAsState()
    val soundEffectsEnabled by viewModel.soundEffectsEnabled.collectAsState()

    val apiMode by viewModel.apiMode.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val apiProvider by viewModel.apiProvider.collectAsState()
    val apiModel by viewModel.apiModel.collectAsState()

    val downloadedModels by viewModel.downloadedModels.collectAsState()
    val downloadStates by viewModel.downloadStates.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Profile Section
            SettingsSection(title = stringResource(R.string.profile_section)) {
                SettingsTextField(
                    label = stringResource(R.string.user_name_label),
                    value = userName,
                    onValueChange = { viewModel.updateUserName(it) },
                    placeholder = "Uživatel"
                )
                SettingsTextField(
                    label = stringResource(R.string.ai_name_label),
                    value = aiName,
                    onValueChange = { viewModel.updateAiName(it) },
                    placeholder = "Karin"
                )
            }

            // Category-based Model Management
            SettingsSection(title = stringResource(R.string.models_section)) {
                ModelCategory.entries.forEach { category ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(category.label, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    
                    val categoryModels = viewModel.availableModels.filter { it.category == category }
                    categoryModels.forEach { model ->
                        val isDownloaded = downloadedModels.any { it.first.id == model.id }
                        val downloadedInfo = downloadedModels.find { it.first.id == model.id }
                        val state = downloadStates[model.id] ?: DownloadState.Idle
                        
                        ModelItem(
                            name = model.name,
                            size = if (isDownloaded) downloadedInfo?.second ?: model.sizeLabel else model.sizeLabel,
                            source = model.source,
                            onAction = { 
                                if (isDownloaded) viewModel.deleteModel(model) 
                                else if (state is DownloadState.Idle) viewModel.downloadModel(model)
                            },
                            actionLabel = when {
                                isDownloaded -> stringResource(R.string.delete)
                                state is DownloadState.Downloading -> "${(state.progress * 100).toInt()}%"
                                else -> stringResource(R.string.download)
                            },
                            isDelete = isDownloaded,
                            enabled = isDownloaded || state is DownloadState.Idle
                        )
                    }
                }
            }

            // General Section
            SettingsSection(title = stringResource(R.string.general_section)) {
                SettingsTextField(
                    label = stringResource(R.string.behavior_label),
                    value = behavior,
                    onValueChange = { viewModel.updateBehavior(it) },
                    placeholder = stringResource(R.string.behavior_placeholder)
                )
                
                SettingsTextField(
                    label = stringResource(R.string.system_instruction_label),
                    value = systemInstruction,
                    onValueChange = { viewModel.updateSystemInstruction(it) },
                    placeholder = stringResource(R.string.system_instruction_placeholder)
                )
            }

            // Role Play Section
            SettingsSection(title = stringResource(R.string.rp_section)) {
                Text(stringResource(R.string.rp_info), color = Color.Gray, fontSize = 12.sp)
                SettingsTextField(
                    label = stringResource(R.string.rp_character_name_label),
                    value = rpCharacterName,
                    onValueChange = { viewModel.updateRpCharacterName(it) },
                    placeholder = "Jméno postavy..."
                )
                SettingsTextField(
                    label = stringResource(R.string.rp_description_label),
                    value = rpDescription,
                    onValueChange = { viewModel.updateRpDescription(it) },
                    placeholder = "Popis postavy..."
                )
            }

            // Toggles Section
            SettingsSection(title = stringResource(R.string.toggles_section)) {
                SettingsSwitch(
                    label = stringResource(R.string.nsfw_label),
                    checked = nsfwEnabled,
                    onCheckedChange = { viewModel.updateNsfwEnabled(it) }
                )
                SettingsSwitch(
                    label = stringResource(R.string.emoji_label),
                    checked = emojiEnabled,
                    onCheckedChange = { viewModel.updateEmojiEnabled(it) }
                )
                SettingsSwitch(
                    label = stringResource(R.string.markdown_label),
                    checked = markdownEnabled,
                    onCheckedChange = { viewModel.updateMarkdownEnabled(it) }
                )
                SettingsSwitch(
                    label = stringResource(R.string.sound_effects_label),
                    checked = soundEffectsEnabled,
                    onCheckedChange = { viewModel.updateSoundEffectsEnabled(it) }
                )
            }

            // API Section
            SettingsSection(title = stringResource(R.string.api_section)) {
                SettingsSwitch(
                    label = stringResource(R.string.api_mode_label),
                    checked = apiMode,
                    onCheckedChange = { viewModel.updateApiMode(it) }
                )
                
                if (apiMode) {
                    SettingsTextField(
                        label = stringResource(R.string.api_key_label),
                        value = apiKey,
                        onValueChange = { viewModel.updateApiKey(it) },
                        placeholder = stringResource(R.string.api_key_placeholder)
                    )
                    
                    ProviderDropdown(
                        selectedProvider = apiProvider,
                        onProviderSelected = { viewModel.updateApiProvider(it) }
                    )
                    
                    ModelDropdown(
                        provider = apiProvider,
                        selectedModel = apiModel,
                        onModelSelected = { viewModel.updateApiModel(it) }
                    )
                }
            }

            // Protocols Section
            SettingsSection(title = stringResource(R.string.protocols_section)) {
                SettingsSwitch(
                    label = stringResource(R.string.block_ai_label),
                    checked = blockAi,
                    onCheckedChange = { viewModel.updateBlockAi(it) }
                )
                
                SettingsSwitch(
                    label = stringResource(R.string.role_play_label),
                    checked = rolePlay,
                    onCheckedChange = { viewModel.updateRolePlay(it) }
                )
            }

            // Parameters Section
            SettingsSection(title = stringResource(R.string.parameters_section)) {
                SettingsSlider(
                    label = stringResource(R.string.creativity_label),
                    value = creativity,
                    onValueChange = { viewModel.updateCreativity(it) },
                    valueRange = 0f..2f,
                    steps = 19
                )
            }

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(stringResource(R.string.footer_android_version, viewModel.androidVersion), color = Color.Gray, fontSize = 12.sp)
                Text(stringResource(R.string.footer_app_version, viewModel.appVersion), color = Color.Gray, fontSize = 12.sp)
                Text(stringResource(R.string.footer_copyright, viewModel.currentYear), color = Color.Gray, fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ModelItem(
    name: String,
    size: String,
    source: String,
    onAction: () -> Unit,
    actionLabel: String,
    isDelete: Boolean = false,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(size, color = Color.Gray, fontSize = 12.sp)
                Text(" • ", color = Color.Gray, fontSize = 12.sp)
                Text(source, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Button(
            onClick = onAction,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDelete) Color.Red.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                contentColor = if (isDelete) Color.Red else MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProviderDropdown(
    selectedProvider: String,
    onProviderSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val providers = listOf("OpenAI", "Anthropic", "Custom")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.provider_label), color = Color.White, fontSize = 14.sp)
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.Gray.copy(alpha = 0.5f)))
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedProvider)
                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                providers.forEach { provider ->
                    DropdownMenuItem(
                        text = { Text(provider, color = Color.White) },
                        onClick = {
                            onProviderSelected(provider)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModelDropdown(
    provider: String,
    selectedModel: String,
    onModelSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val models = when (provider) {
        "OpenAI" -> listOf("gpt-4o", "gpt-4o-mini", "gpt-3.5-turbo")
        "Anthropic" -> listOf("claude-3-5-sonnet", "claude-3-opus", "claude-3-haiku")
        else -> listOf("local-llama-3", "custom-endpoint")
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.api_model_label), color = Color.White, fontSize = 14.sp)
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.Gray.copy(alpha = 0.5f)))
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedModel)
                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                models.forEach { model ->
                    DropdownMenuItem(
                        text = { Text(model, color = Color.White) },
                        onClick = {
                            onModelSelected(model)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}

@Composable
fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, color = Color.White, fontSize = 14.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.Black
            )
        )
    }
}

@Composable
fun SettingsSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color.White, fontSize = 16.sp)
            Text(
                text = String.format("%.1f", value),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,navigation=buttons")
@Composable
fun SettingsScreenPreview() {
    AxionisAITheme {
        SettingsScreen(onBack = {})
    }
}
