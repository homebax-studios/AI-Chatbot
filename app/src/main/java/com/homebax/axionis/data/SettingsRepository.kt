package com.homebax.axionis.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {

        val USER_NAME = stringPreferencesKey("user_name")
        val AI_NAME = stringPreferencesKey("ai_name")
        
        val BEHAVIOR = stringPreferencesKey("behavior")
        val SYSTEM_INSTRUCTION = stringPreferencesKey("system_instruction")
        val BLOCK_AI = booleanPreferencesKey("block_ai")
        val ROLE_PLAY = booleanPreferencesKey("role_play")
        val RP_CHARACTER_NAME = stringPreferencesKey("rp_character_name")
        val RP_DESCRIPTION = stringPreferencesKey("rp_description")
        
        val CREATIVITY = floatPreferencesKey("creativity")
        
        val NSFW_ENABLED = booleanPreferencesKey("nsfw_enabled")
        val EMOJI_ENABLED = booleanPreferencesKey("emoji_enabled")
        val MARKDOWN_ENABLED = booleanPreferencesKey("markdown_enabled")
        val SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
        
        val API_MODE = booleanPreferencesKey("api_mode")
        val API_KEY = stringPreferencesKey("api_key")
        val API_PROVIDER = stringPreferencesKey("api_provider")
        val API_MODEL = stringPreferencesKey("api_model")

        val SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
    }

    val userName: Flow<String> = context.settingsDataStore.data.map { it[USER_NAME] ?: "Uživatel" }
    val aiName: Flow<String> = context.settingsDataStore.data.map { it[AI_NAME] ?: "Karin" }

    val behavior: Flow<String> = context.settingsDataStore.data.map { it[BEHAVIOR] ?: "" }
    val systemInstruction: Flow<String> = context.settingsDataStore.data.map { it[SYSTEM_INSTRUCTION] ?: "" }
    val blockAi: Flow<Boolean> = context.settingsDataStore.data.map { it[BLOCK_AI] ?: true }
    val rolePlay: Flow<Boolean> = context.settingsDataStore.data.map { it[ROLE_PLAY] ?: false }
    val rpCharacterName: Flow<String> = context.settingsDataStore.data.map { it[RP_CHARACTER_NAME] ?: "" }
    val rpDescription: Flow<String> = context.settingsDataStore.data.map { it[RP_DESCRIPTION] ?: "" }

    val creativity: Flow<Float> = context.settingsDataStore.data.map { it[CREATIVITY] ?: 1.0f }

    val nsfwEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[NSFW_ENABLED] ?: false }
    val emojiEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[EMOJI_ENABLED] ?: true }
    val markdownEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[MARKDOWN_ENABLED] ?: true }
    val soundEffectsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[SOUND_EFFECTS_ENABLED] ?: true }

    val apiMode: Flow<Boolean> = context.settingsDataStore.data.map { it[API_MODE] ?: false }
    val apiKey: Flow<String> = context.settingsDataStore.data.map { it[API_KEY] ?: "" }
    val apiProvider: Flow<String> = context.settingsDataStore.data.map { it[API_PROVIDER] ?: "OpenAI" }
    val apiModel: Flow<String> = context.settingsDataStore.data.map { it[API_MODEL] ?: "gpt-4o" }
    
    val setupCompleted: Flow<Boolean> = context.settingsDataStore.data.map { it[SETUP_COMPLETED] ?: false }

    suspend fun updateUserName(value: String) = context.settingsDataStore.edit { it[USER_NAME] = value }
    suspend fun updateAiName(value: String) = context.settingsDataStore.edit { it[AI_NAME] = value }

    suspend fun updateBehavior(value: String) = context.settingsDataStore.edit { it[BEHAVIOR] = value }
    suspend fun updateSystemInstruction(value: String) = context.settingsDataStore.edit { it[SYSTEM_INSTRUCTION] = value }
    suspend fun updateBlockAi(value: Boolean) = context.settingsDataStore.edit { it[BLOCK_AI] = value }
    suspend fun updateRolePlay(value: Boolean) = context.settingsDataStore.edit { it[ROLE_PLAY] = value }
    suspend fun updateRpCharacterName(value: String) = context.settingsDataStore.edit { it[RP_CHARACTER_NAME] = value }
    suspend fun updateRpDescription(value: String) = context.settingsDataStore.edit { it[RP_DESCRIPTION] = value }

    suspend fun updateCreativity(value: Float) = context.settingsDataStore.edit { it[CREATIVITY] = value }

    suspend fun updateNsfwEnabled(value: Boolean) = context.settingsDataStore.edit { it[NSFW_ENABLED] = value }
    suspend fun updateEmojiEnabled(value: Boolean) = context.settingsDataStore.edit { it[EMOJI_ENABLED] = value }
    suspend fun updateMarkdownEnabled(value: Boolean) = context.settingsDataStore.edit { it[MARKDOWN_ENABLED] = value }
    suspend fun updateSoundEffectsEnabled(value: Boolean) = context.settingsDataStore.edit { it[SOUND_EFFECTS_ENABLED] = value }

    suspend fun updateApiMode(value: Boolean) = context.settingsDataStore.edit { it[API_MODE] = value }
    suspend fun updateApiKey(value: String) = context.settingsDataStore.edit { it[API_KEY] = value }
    suspend fun updateApiProvider(value: String) = context.settingsDataStore.edit { it[API_PROVIDER] = value }
    suspend fun updateApiModel(value: String) = context.settingsDataStore.edit { it[API_MODEL] = value }

    suspend fun updateSetupCompleted(value: Boolean) = context.settingsDataStore.edit { it[SETUP_COMPLETED] = value }

}
