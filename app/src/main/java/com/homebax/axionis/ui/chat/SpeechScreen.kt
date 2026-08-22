package com.homebax.axionis.ui.chat

import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homebax.axionis.ui.theme.AxionisAITheme
import com.homebax.axionis.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeechScreen(
    viewModel: ChatViewModel = viewModel(),
    onBackToChat: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val modelName by viewModel.activeModelName.collectAsState()
    val markdownEnabled by viewModel.markdownEnabled.collectAsState()
    val compassHeading by viewModel.compassHeading.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val soundEffectsEnabled by viewModel.soundEffectsEnabled.collectAsState()
    var isMicOn by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Audio effect: Bubbling water
    DisposableEffect(isMicOn, soundEffectsEnabled) {
        var mediaPlayer: MediaPlayer? = null
        if (soundEffectsEnabled) {
            try {
                // Try sound_bubbling as requested
                var resId = context.resources.getIdentifier("sound_bubbling", "raw", context.packageName)
                if (resId == 0) {
                    // Fallback to bubbling_water
                    resId = context.resources.getIdentifier("bubbling_water", "raw", context.packageName)
                }
                
                if (resId != 0) {
                    mediaPlayer = MediaPlayer.create(context, resId)
                    if (isMicOn) {
                        mediaPlayer?.isLooping = true
                        mediaPlayer?.start()
                    }
                }
            } catch (_: Exception) {
            }
        }
        
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            SpeechTopBar(modelName = modelName)

            // Chat History
            ChatList(
                messages = messages,
                markdownEnabled = markdownEnabled,
                onTtsClick = { viewModel.toggleTts(it) },
                activeModelName = modelName,
                compassHeading = compassHeading,
                currentLocation = currentLocation,
                modifier = Modifier.weight(1f)
            )

            // Visual Aura and Controls
            SpeechInteractionArea(
                isMicOn = isMicOn,
                onToggleMic = { isMicOn = !isMicOn },
                onStop = onBackToChat
            )
        }
    }
}

@Composable
fun SpeechTopBar(modelName: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        color = Color.Transparent,
        contentColor = Color.White
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = modelName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SpeechInteractionArea(
    isMicOn: Boolean,
    onToggleMic: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)),
        color = Color.White.copy(alpha = 0.03f)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            VisualAura(isMoving = isMicOn)

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Text(
                    text = if (isMicOn) stringResource(R.string.listening) else stringResource(R.string.mic_off),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpeechControlButton(
                        icon = if (isMicOn) Icons.Rounded.Mic else Icons.Rounded.MicOff,
                        label = stringResource(R.string.turn_off_mic),
                        onClick = onToggleMic,
                        containerColor = if (isMicOn) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    )

                    SpeechControlButton(
                        icon = Icons.Rounded.Stop,
                        label = stringResource(R.string.cancel),
                        onClick = onStop,
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SpeechControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(containerColor)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = label, color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun VisualAura(isMoving: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .size(280.dp)
            .graphicsLayer {
                scaleX = if (isMoving) pulse else 1f
                scaleY = if (isMoving) pulse else 1f
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize().blur(40.dp)) {
            val brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00E5FF).copy(alpha = 0.8f),
                    Color(0xFF651FFF).copy(alpha = 0.6f),
                    Color.Transparent
                ),
                center = center,
                radius = size.width / 2
            )
            
            drawCircle(brush = brush, radius = size.width / 2.5f, center = center)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val rayCount = 12
            val radius = size.width / 2.8f
            val rayLength = 30.dp.toPx()
            
            if (isMoving) {
                for (i in 0 until rayCount) {
                    val angle = (i * 2 * Math.PI / rayCount) + phase
                    val startX = center.x + radius * cos(angle).toFloat()
                    val startY = center.y + radius * sin(angle).toFloat()
                    
                    val endRadius = radius + rayLength * pulse
                    val endX = center.x + endRadius * cos(angle).toFloat()
                    val endY = center.y + endRadius * sin(angle).toFloat()
                    
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = center.copy(x = startX, y = startY),
                        end = center.copy(x = endX, y = endY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,navigation=buttons")
@Composable
fun SpeechScreenPreview() {
    AxionisAITheme {
        SpeechScreen(onBackToChat = {})
    }
}
