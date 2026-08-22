package com.homebax.axionis.ui.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homebax.axionis.data.DownloadState
import com.homebax.axionis.data.ModelCatalog
import com.homebax.axionis.data.ModelManager
import com.homebax.axionis.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SetupViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val modelManager =
        ModelManager(application)

    private val settingsRepository =
        SettingsRepository(application)

    // ============================================================
    // USER
    // ============================================================

    val userName =
        MutableStateFlow("")

    val aiName =
        MutableStateFlow("Karin")

    // ============================================================
    // SETUP MODELS
    // ============================================================

    /**
     * Model LLM, který se použije během prvního spuštění.
     */
    val defaultLlmModel =
        ModelCatalog.setupLlm

    /**
     * Model Speech-to-Text, který se použije během prvního spuštění.
     */
    val whisperModel =
        ModelCatalog.setupStt

    // ============================================================
    // DOWNLOAD STATE
    // ============================================================

    val downloadStates =
        modelManager.downloadStates

    // ============================================================
    // PERMISSIONS
    // ============================================================

    private val _permissionsGranted =
        MutableStateFlow(false)

    val permissionsGranted =
        _permissionsGranted.asStateFlow()

    fun updatePermissionsStatus(
        granted: Boolean
    ) {
        _permissionsGranted.value =
            granted
    }

    // ============================================================
    // DOWNLOAD
    // ============================================================

    fun startDownloads() {

        viewModelScope.launch {

            launch {
                modelManager.downloadModel(
                    whisperModel
                )
            }

            launch {
                modelManager.downloadModel(
                    defaultLlmModel
                )
            }
        }
    }

    // ============================================================
    // SETUP COMPLETE
    // ============================================================

    fun completeSetup() {

        viewModelScope.launch {

            settingsRepository.updateUserName(
                userName.value.ifBlank {
                    "Uživatel"
                }
            )

            settingsRepository.updateAiName(
                aiName.value.ifBlank {
                    "Karin"
                }
            )

            settingsRepository.updateSetupCompleted(
                true
            )
        }
    }

    // ============================================================
    // DELETE
    // ============================================================

    fun deleteModels() {

        modelManager.deleteModel(
            whisperModel
        )

        modelManager.deleteModel(
            defaultLlmModel
        )
    }

    // ============================================================
    // CHECK
    // ============================================================

    fun isAllDownloaded(): Boolean {

        val states =
            downloadStates.value

        val whisperDownloaded =
            states[whisperModel.id] is DownloadState.Completed

        val llmDownloaded =
            states[defaultLlmModel.id] is DownloadState.Completed

        return whisperDownloaded &&
                llmDownloaded
    }
}