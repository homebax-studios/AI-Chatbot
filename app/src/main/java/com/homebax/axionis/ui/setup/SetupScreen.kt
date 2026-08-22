package com.homebax.axionis.ui.setup

import android.os.Build
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homebax.axionis.data.DownloadState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.homebax.axionis.ui.theme.AxionisAITheme
import com.homebax.axionis.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SetupScreen(
    viewModel: SetupViewModel = viewModel(),
    onComplete: () -> Unit
) {
    val pageCount = 5
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()
    
    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.ACCESS_NETWORK_STATE 
    } else {
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            storagePermission
        )
    )

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted && pagerState.currentPage == 0) {
            pagerState.animateScrollToPage(1)
        }
    }

    val downloadStates by viewModel.downloadStates.collectAsState()
    val isAllDownloaded = viewModel.isAllDownloaded()

    LaunchedEffect(isAllDownloaded) {
        if (isAllDownloaded && pagerState.currentPage == 3) {
            pagerState.animateScrollToPage(4)
        }
    }

    Scaffold(
        bottomBar = {
            SetupBottomBar(
                currentPage = pagerState.currentPage,
                pageCount = pageCount,
                onNext = {
                    coroutineScope.launch {
                        if (pagerState.currentPage < pageCount - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            viewModel.completeSetup()
                            onComplete()
                        }
                    }
                },
                onBack = {
                    coroutineScope.launch {
                        if (pagerState.currentPage > 0) {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                }
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> WelcomePage(permissionsState)
                1 -> ProfilePage(viewModel)
                2 -> InfoPage()
                3 -> DownloadPage(viewModel, downloadStates)
                4 -> CompletionPage {
                    viewModel.completeSetup()
                    onComplete()
                }
            }
        }
    }
}

@Composable
fun ProfilePage(viewModel: SetupViewModel) {
    val userName by viewModel.userName.collectAsState()
    val aiName by viewModel.aiName.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Nastavení Profilu", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = userName,
            onValueChange = { viewModel.userName.value = it },
            label = { Text("Tvoje jméno") },
            placeholder = { Text("Uživatel") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = aiName,
            onValueChange = { viewModel.aiName.value = it },
            label = { Text("Jméno AI") },
            placeholder = { Text("Karin") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun SetupBottomBar(
    currentPage: Int,
    pageCount: Int,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            enabled = currentPage > 0,
            modifier = Modifier.background(if (currentPage > 0) MaterialTheme.colorScheme.surface else Color.Transparent, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = if (currentPage > 0) Color.White else Color.Transparent)
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(pageCount) { index ->
                val isSelected = index == currentPage
                val width by animateDpAsState(targetValue = if (isSelected) 24.dp else 8.dp, label = "dotWidth")
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f))
                )
            }
        }

        IconButton(
            onClick = onNext,
            modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.Black)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WelcomePage(permissionsState: com.google.accompanist.permissions.MultiplePermissionsState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.welcome_title), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.permissions_desc), textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(modifier = Modifier.height(48.dp))
        
        PermissionItem(stringResource(R.string.permission_mic), Icons.Default.Mic, permissionsState.permissions.any { it.permission == android.Manifest.permission.RECORD_AUDIO && it.status.isGranted })
        PermissionItem(stringResource(R.string.permission_location), Icons.Default.MyLocation, permissionsState.permissions.any { it.permission == android.Manifest.permission.ACCESS_FINE_LOCATION && it.status.isGranted })
        
        val isStorageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) true 
                               else permissionsState.permissions.any { it.permission == android.Manifest.permission.WRITE_EXTERNAL_STORAGE && it.status.isGranted }
        PermissionItem(stringResource(R.string.permission_storage), Icons.Default.Storage, isStorageGranted)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        if (!permissionsState.allPermissionsGranted) {
            Button(
                onClick = { permissionsState.launchMultiplePermissionRequest() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.grant_permissions), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InfoPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.info_title), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.info_desc),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            lineHeight = 26.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun PermissionItem(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isGranted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        if (isGranted) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Granted", tint = Color.Green)
        } else {
            Icon(Icons.Default.RadioButtonUnchecked, contentDescription = "Not Granted", tint = Color.Gray)
        }
    }
}

@Composable
fun DownloadPage(viewModel: SetupViewModel, downloadStates: Map<String, DownloadState>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.download_title), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.download_desc), textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(modifier = Modifier.height(48.dp))
        
        DownloadItem(viewModel.whisperModel.name, downloadStates[viewModel.whisperModel.id] ?: DownloadState.Idle)
        Spacer(modifier = Modifier.height(16.dp))
        DownloadItem(viewModel.defaultLlmModel.name, downloadStates[viewModel.defaultLlmModel.id] ?: DownloadState.Idle)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = { viewModel.startDownloads() },
            enabled = downloadStates.isEmpty() || downloadStates.values.any { it is DownloadState.Error },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.start_download), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DownloadItem(name: String, state: DownloadState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            when (state) {
                is DownloadState.Downloading -> Text("${(state.progress * 100).toInt()}%", color = MaterialTheme.colorScheme.primary)
                DownloadState.Completed -> Text(stringResource(R.string.completed), color = Color.Green)
                is DownloadState.Error -> Text("Chyba", color = MaterialTheme.colorScheme.error)
                DownloadState.Idle -> Text(stringResource(R.string.pending), color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { if (state is DownloadState.Downloading) state.progress else if (state == DownloadState.Completed) 1f else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.Gray.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun CompletionPage(onComplete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Celebration, contentDescription = null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.completion_title), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.completion_desc), textAlign = TextAlign.Center, fontSize = 18.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(64.dp))
        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(stringResource(R.string.launch_app), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,navigation=buttons")
@Composable
fun SetupScreenPreview() {
    AxionisAITheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SetupScreen(onComplete = {})
        }
    }
}
