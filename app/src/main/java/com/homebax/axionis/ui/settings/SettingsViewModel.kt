package com.homebax.axionis.ui.settings

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homebax.axionis.data.ModelInfo
import com.homebax.axionis.data.ModelManager
import com.homebax.axionis.data.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)
    private val modelManager = ModelManager(application)

    val userName = repository.userName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Uživatel")
    val aiName = repository.aiName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Karin")

    val behavior = repository.behavior.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val systemInstruction = repository.systemInstruction.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val blockAi = repository.blockAi.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val rolePlay = repository.rolePlay.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val rpCharacterName = repository.rpCharacterName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val rpDescription = repository.rpDescription.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    
    val creativity = repository.creativity.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val nsfwEnabled = repository.nsfwEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val emojiEnabled = repository.emojiEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val markdownEnabled = repository.markdownEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val soundEffectsEnabled = repository.soundEffectsEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val apiMode = repository.apiMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val apiKey = repository.apiKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val apiProvider = repository.apiProvider.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "OpenAI")
    val apiModel = repository.apiModel.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "gpt-4o")

    private val _downloadedModels = MutableStateFlow<List<Pair<ModelInfo, String>>>(emptyList())
    val downloadedModels = _downloadedModels.asStateFlow()

    val availableModels = modelManager.availableModels
    val downloadStates = modelManager.downloadStates

    init {
        refreshModels()
    }

    fun refreshModels() {
        _downloadedModels.value = modelManager.getDownloadedModels()
    }

    fun updateUserName(value: String) = viewModelScope.launch { repository.updateUserName(value) }
    fun updateAiName(value: String) = viewModelScope.launch { repository.updateAiName(value) }

    fun updateBehavior(value: String) = viewModelScope.launch { repository.updateBehavior(value) }
    fun updateSystemInstruction(value: String) = viewModelScope.launch { repository.updateSystemInstruction(value) }
    fun updateBlockAi(value: Boolean) = viewModelScope.launch { repository.updateBlockAi(value) }
    fun updateRolePlay(value: Boolean) = viewModelScope.launch { repository.updateRolePlay(value) }
    fun updateRpCharacterName(value: String) = viewModelScope.launch { repository.updateRpCharacterName(value) }
    fun updateRpDescription(value: String) = viewModelScope.launch { repository.updateRpDescription(value) }

    fun updateCreativity(value: Float) = viewModelScope.launch { repository.updateCreativity(value) }

    fun updateNsfwEnabled(value: Boolean) = viewModelScope.launch { repository.updateNsfwEnabled(value) }
    fun updateEmojiEnabled(value: Boolean) = viewModelScope.launch { repository.updateEmojiEnabled(value) }
    fun updateMarkdownEnabled(value: Boolean) = viewModelScope.launch { repository.updateMarkdownEnabled(value) }
    fun updateSoundEffectsEnabled(value: Boolean) = viewModelScope.launch { repository.updateSoundEffectsEnabled(value) }

    fun updateApiMode(value: Boolean) = viewModelScope.launch { repository.updateApiMode(value) }
    fun updateApiKey(value: String) = viewModelScope.launch { repository.updateApiKey(value) }
    fun updateApiProvider(value: String) = viewModelScope.launch { repository.updateApiProvider(value) }
    fun updateApiModel(value: String) = viewModelScope.launch { repository.updateApiModel(value) }

    fun downloadModel(model: ModelInfo) = viewModelScope.launch {
        modelManager.downloadModel(model)
        refreshModels()
    }

    fun deleteModel(model: ModelInfo) {
        modelManager.deleteModel(model)
        refreshModels()
    }

    val androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    val appVersion = "1.0.0 (1)" // Mocked, ideally from BuildConfig
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
}
