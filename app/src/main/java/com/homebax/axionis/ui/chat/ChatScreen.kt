package com.homebax.axionis.ui.chat

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.homebax.axionis.data.chat.*
import com.homebax.axionis.ui.theme.AxionisAITheme
import com.homebax.axionis.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    onSettingsClick: () -> Unit,
    onMicClick: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val isTtsPlaying by viewModel.isTtsPlaying.collectAsState()
    val modelName by viewModel.activeModelName.collectAsState()
    val markdownEnabled by viewModel.markdownEnabled.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val compassHeading by viewModel.compassHeading.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    
    var activeSection by remember { mutableStateOf("Chat") }


    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        val isWide = maxWidth > 600.dp
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Nav Bar
            ChatTopBar(
                modelName = modelName,
                isOnline = isOnline,
                activeSection = activeSection,
                onSectionChange = { activeSection = it },
                onSettingsClick = onSettingsClick,
                modifier = Modifier.height(64.dp)
            )

            if (activeSection == "Chat") {
                // Chat Content
                ChatList(
                    messages = messages,
                    markdownEnabled = markdownEnabled,
                    modifier = Modifier.weight(1f).padding(horizontal = if (isWide) 64.dp else 0.dp),
                    onTtsClick = { viewModel.toggleTts(it) },
                    activeModelName = modelName,
                    compassHeading = compassHeading,
                    currentLocation = currentLocation
                )
            } else {
                // Images Content
                ImagesGallery(
                    messages = messages,
                    modifier = Modifier.weight(1f).padding(horizontal = if (isWide) 64.dp else 0.dp)
                )
            }

            // Interaction Widget
            InteractionWidget(
                inputText = inputText,
                currentMode = currentMode,
                isTtsPlaying = isTtsPlaying,
                onInputChange = { viewModel.onInputTextChanged(it) },
                onModeChange = { viewModel.onModeChanged(it) },
                onSendClick = { viewModel.sendMessage() },
                onStopTtsClick = { viewModel.stopTts() },
                onCancelTtsClick = { viewModel.cancelTts() },
                onMicClick = onMicClick,
                modifier = Modifier
                    .padding(5.dp)
                    .widthIn(max = if (isWide) 800.dp else 2000.dp)
                    .align(Alignment.CenterHorizontally)
                    .heightIn(max = 300.dp)
            )
        }
    }
}

@Composable
fun ImagesGallery(messages: List<ChatMessage>, modifier: Modifier = Modifier) {
    // Filter messages that have generated images
    val imageMessages = messages.filter { it.tool is ToolWidget.ImageGeneration || it.attachments.isNotEmpty() }
    
    if (imageMessages.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Žádné generované obrázky", color = Color.Gray)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = modifier.fillMaxSize().padding(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(imageMessages) { message ->
                ImageGalleryItem(message)
            }
        }
    }
}

@Composable
fun ImageGalleryItem(message: ChatMessage) {
    val tool = message.tool as? ToolWidget.ImageGeneration
    val uri = tool?.imageUri ?: message.attachments.firstOrNull() ?: ""
    
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
    ) {
        AsyncImage(
            model = uri,
            contentDescription = tool?.prompt ?: "Generated Image",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        
        if (tool != null) {
            Text(
                text = tool.prompt,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(8.dp),
                maxLines = 2
            )
        }
    }
}

@Composable
fun ChatTopBar(
    modelName: String,
    isOnline: Boolean,
    activeSection: String,
    onSectionChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = Color.Gray)
            }

            // Center: Model Name or Toggle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(modelName, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                ChatToggle(activeSection, onSectionChange)
            }

            // Status Indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) Color(0xFF4CAF50) else Color(0xFFF44336))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            )
        }
    }
}

@Composable
fun ChatToggle(activeSection: String, onSectionChange: (String) -> Unit) {
    val isChat = activeSection == "Chat"
    val indicatorOffset by animateDpAsState(
        targetValue = if (isChat) 0.dp else 60.dp,
        animationSpec = tween(300),
        label = "toggle"
    )

    Box(
        modifier = Modifier
            .width(120.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onSectionChange(if (isChat) "Images" else "Chat") }
    ) {
        // Slider
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(60.dp)
                .fillMaxHeight()
                .padding(2.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isChat) Color.Black else Color.Gray)
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("Images", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!isChat) Color.Black else Color.Gray)
            }
        }
    }
}

@Composable
fun ChatList(
    messages: List<ChatMessage>,
    markdownEnabled: Boolean,
    onTtsClick: (String) -> Unit,
    activeModelName: String,
    compassHeading: Float,
    currentLocation: ToolWidget.Location?,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(messages) { message ->
            if (message == messages.first()) {
                DateSeparator(stringResource(R.string.date_today))
            }

            if (message.tool !is ToolWidget.ImageGeneration) {
                MessageBubble(
                    message = message,
                    markdownEnabled = markdownEnabled,
                    activeModelName = activeModelName,
                    onCopyClick = {
                        scope.launch {
                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Axionis AI", message.content)))
                        }
                    },
                    onTtsClick = { onTtsClick(message.id) }
                )
            } else {
                // If it's an image generation, only show user text as bubble, AI response as image
                if (message.role == MessageRole.USER) {
                    MessageBubble(
                        message = message,
                        markdownEnabled = markdownEnabled,
                        activeModelName = activeModelName,
                        onCopyClick = {},
                        onTtsClick = {}
                    )
                }
            }

            message.tool?.let { tool ->
                ToolWidgetDisplay(tool, compassHeading, currentLocation)
            }
        }
    }
}

@Composable
fun DateSeparator(dateLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))
        Text(
            text = dateLabel,
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    markdownEnabled: Boolean,
    activeModelName: String,
    onCopyClick: () -> Unit,
    onTtsClick: () -> Unit
) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f)
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else Color.White

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = bgColor,
            contentColor = textColor,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (markdownEnabled || isUser) {
                    MarkdownText(text = message.content, color = textColor)
                } else {
                    Text(text = message.content, color = textColor)
                }
            }
        }

        if (!isUser) {
            Text(
                text = activeModelName,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
            
            Text(
                text = timeString,
                fontSize = 10.sp,
                color = Color.Gray
            )

            if (!isUser && message.processingTimeMs != null) {
                Text(
                    text = "${message.processingTimeMs / 1000f}s",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onCopyClick, modifier = Modifier.size(16.dp)) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", tint = Color.Gray, modifier = Modifier.size(12.dp))
            }

            if (!isUser) {
                IconButton(onClick = onTtsClick, modifier = Modifier.size(16.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Speech", tint = Color.Gray, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
fun ToolWidgetDisplay(tool: ToolWidget, compassHeading: Float = 0f, currentLocation: ToolWidget.Location? = null) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(24.dp) // Smoother corners
    ) {
        when (tool) {
            is ToolWidget.Weather -> {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.WbSunny, contentDescription = null, tint = Color(0xFFFFD600))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${stringResource(R.string.weather_title)}: ${tool.temp}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WeatherStat(stringResource(R.string.humidity), tool.humidity, Icons.Rounded.WaterDrop)
                        WeatherStat(stringResource(R.string.wind), tool.wind, Icons.Rounded.Air)
                        WeatherStat(stringResource(R.string.pressure), tool.pressure, Icons.Rounded.Compress)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(tool.hourlyForecast) { item ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(item.time, fontSize = 11.sp, color = Color.Gray)
                                Icon(Icons.Rounded.Cloud, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.LightGray)
                                Text(item.temp, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            is ToolWidget.Maps -> {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${stringResource(R.string.destination)}: ${tool.destination}", fontWeight = FontWeight.Bold)
                    Text(tool.routeInfo, fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val gmmIntentUri = Uri.parse("google.navigation:q=${tool.destination}&mode=${tool.travelMode.firstOrNull() ?: 'd'}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Rounded.Map, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.start_navigation))
                    }
                }
            }
            is ToolWidget.Time -> {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(tool.currentTime, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                        Text(tool.currentDate, fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }
            is ToolWidget.Location -> {
                val displayLat = currentLocation?.latitude ?: tool.latitude
                val displayLon = currentLocation?.longitude ?: tool.longitude
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.MyLocation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.gps_coordinates), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${stringResource(R.string.latitude)}: $displayLat", fontSize = 14.sp)
                    Text("${stringResource(R.string.longitude)}: $displayLon", fontSize = 14.sp)
                    tool.address?.let { Text(it, fontSize = 12.sp, color = Color.Gray) }
                }
            }
            is ToolWidget.Compass -> {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(120.dp)) {
                        drawCircle(color = Color.White.copy(alpha = 0.1f), radius = size.minDimension / 2)
                        rotate(degrees = -compassHeading) {
                            drawLine(
                                color = Color.Red,
                                start = center,
                                end = center.copy(y = center.y - size.minDimension / 2 + 10.dp.toPx()),
                                strokeWidth = 4.dp.toPx()
                            )
                            drawLine(
                                color = Color.White,
                                start = center,
                                end = center.copy(y = center.y + size.minDimension / 2 - 10.dp.toPx()),
                                strokeWidth = 4.dp.toPx()
                            )
                        }
                    }
                    Text("${compassHeading.toInt()}°", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            is ToolWidget.ImageGeneration -> {
                Column(modifier = Modifier.padding(16.dp)) {
                    AsyncImage(
                        model = tool.imageUri,
                        contentDescription = tool.prompt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(tool.prompt, color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun WeatherStat(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun InteractionWidget(
    inputText: String,
    currentMode: AIMode,
    isTtsPlaying: Boolean,
    onInputChange: (String) -> Unit,
    onModeChange: (AIMode) -> Unit,
    onSendClick: () -> Unit,
    onStopTtsClick: () -> Unit,
    onCancelTtsClick: () -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showModeMenu by remember { mutableStateOf(false) }
    
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { /* Handle URI */ }
    )
    
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { /* Handle URI */ }
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Slash Command Menu
        if (inputText.startsWith("/")) {
            SlashCommandMenu(
                input = inputText,
                onCommandClick = { onInputChange(it + " ") }
            )
        }
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp)),
            color = Color.White.copy(alpha = 0.05f)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (isTtsPlaying) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onStopTtsClick,
                            modifier = Modifier.weight(2f).fillMaxHeight(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Rounded.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.stop_start_tts), fontSize = 12.sp)
                        }
                        
                        Button(
                            onClick = onCancelTtsClick,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(stringResource(R.string.cancel_tts), fontSize = 12.sp)
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Attachment Buttons
                        Row(modifier = Modifier.padding(bottom = 4.dp)) {
                            MenuIconButton(
                                onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                icon = Icons.Rounded.Image
                            )
                            MenuIconButton(
                                onClick = { filePicker.launch(arrayOf("*/*")) },
                                icon = Icons.Rounded.AttachFile
                            )
                        }

                        // Text Input
                        TextField(
                            value = inputText,
                            onValueChange = onInputChange,
                            placeholder = { Text(stringResource(R.string.ask_ai), color = Color.Gray, fontSize = 15.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .onKeyEvent { 
                                    if (it.key == Key.Enter && it.type == KeyEventType.KeyDown) {
                                        if (it.isCtrlPressed) {
                                            onInputChange(inputText + "\n")
                                            true
                                        } else {
                                            onSendClick()
                                            true
                                        }
                                    } else false
                                },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = false,
                            maxLines = 10
                        )

                        // AI Mode & Send
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box {
                                IconButton(
                                    onClick = { showModeMenu = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Rounded.Build, contentDescription = null, size = 16.dp, tint = MaterialTheme.colorScheme.primary)
                                }
                                DropdownMenu(
                                    expanded = showModeMenu,
                                    onDismissRequest = { showModeMenu = false },
                                    modifier = Modifier
                                        .background(Color(0xFF1E1E1E))
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                ) {
                                    AIMode.entries.forEach { mode ->
                                        DropdownMenuItem(
                                            text = { 
                                                Text(
                                                    text = mode.label, 
                                                    color = if (mode == currentMode) MaterialTheme.colorScheme.primary else Color.White,
                                                    fontWeight = if (mode == currentMode) FontWeight.Bold else FontWeight.Normal
                                                ) 
                                            },
                                            onClick = {
                                                onModeChange(mode)
                                                showModeMenu = false
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                    }
                                }
                            }
                            
                            IconButton(
                                onClick = if (inputText.isEmpty()) onMicClick else onSendClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            ) {
                                val icon = if (inputText.isEmpty()) Icons.Rounded.Mic else Icons.AutoMirrored.Rounded.Send
                                Icon(icon, contentDescription = "Send", tint = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuIconButton(onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bgColor by animateColorAsState(if (isPressed) Color.White.copy(alpha = 0.15f) else Color.Transparent)
    
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bgColor),
        interactionSource = interactionSource
    ) {
        Icon(icon, contentDescription = null, size = 20.dp, tint = Color.Gray)
    }
}

@Composable
fun SlashCommandMenu(input: String, onCommandClick: (String) -> Unit) {
    val commands = listOf(
        "/cas", "/čas", "/time", "/poloha", "/location", "/kompas", "/compass", "/maps", "/mapy", "/pocasi", "/weather", "/image", "/generuj"
    )
    
    val filtered = commands.filter { it.contains(input.lowercase(), ignoreCase = true) }
    
    if (filtered.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 3.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                filtered.forEach { cmd ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onCommandClick(cmd) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Terminal, contentDescription = null, size = 16.dp, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(cmd, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun Icon(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    androidx.compose.material3.Icon(icon, contentDescription, modifier = Modifier.size(size), tint = tint)
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,navigation=buttons")
@Composable
fun ChatScreenPreview() {
    AxionisAITheme {
        ChatScreen(onSettingsClick = {}, onMicClick = {})
    }
}
