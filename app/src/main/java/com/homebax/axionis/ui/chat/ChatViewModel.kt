package com.homebax.axionis.ui.chat

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.homebax.axionis.data.MemoryManager
import com.homebax.axionis.data.MemoryTier
import com.homebax.axionis.data.SettingsRepository
import com.homebax.axionis.data.chat.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    private val memoryManager = MemoryManager(application)


    private val settingsRepository = SettingsRepository(application)

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    private val _compassHeading = MutableStateFlow(0f)
    val compassHeading = _compassHeading.asStateFlow()

    private val _currentLocation = MutableStateFlow<ToolWidget.Location?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    val emojiEnabled = settingsRepository.emojiEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val markdownEnabled = settingsRepository.markdownEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val soundEffectsEnabled = settingsRepository.soundEffectsEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val apiMode = settingsRepository.apiMode.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val apiProvider = settingsRepository.apiProvider.stateIn(viewModelScope, SharingStarted.Eagerly, "OpenAI")
    val apiModel = settingsRepository.apiModel.stateIn(viewModelScope, SharingStarted.Eagerly, "gpt-4o")

    val rpCharacterName = settingsRepository.rpCharacterName.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val rpDescription = settingsRepository.rpDescription.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val behavior = settingsRepository.behavior.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                content = "Ahoj! Jsem Axionis AI. Jak ti mohu dnes pomoci?",
                role = MessageRole.AI,
                timestamp = System.currentTimeMillis() - 3600000
            )
        )
    )
    val messages = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    private val _currentMode = MutableStateFlow(AIMode.NORMAL)
    val currentMode = _currentMode.asStateFlow()

    private val _isTtsPlaying = MutableStateFlow(false)
    val isTtsPlaying = _isTtsPlaying.asStateFlow()

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun onModeChanged(mode: AIMode) {
        _currentMode.value = mode
    }

    private var lastCommand: String? = null
    private var lastCommandTime: Long = 0

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return
        
        // Prevent multiple identical executions within 1 second
        if (text == lastCommand && System.currentTimeMillis() - lastCommandTime < 1000) return
        lastCommand = text
        lastCommandTime = System.currentTimeMillis()

        val userMessage = ChatMessage(content = text, role = MessageRole.USER)
        _messages.value = _messages.value + userMessage
        _inputText.value = ""

        // Handle Slash Commands
        if (text.startsWith("/")) {
            handleSlashCommand(text)
            return
        }

        viewModelScope.launch {
            // Add to temporary memory
            memoryManager.addTemporary(text)
            
            if (apiMode.value) {
                simulateCloudAiResponse(text)
            } else {
                simulateAiResponse(text)
            }
        }
    }

    private suspend fun simulateCloudAiResponse(userText: String) {
        val startTime = System.currentTimeMillis()
        delay(1500) // Cloud usually takes a bit longer

        var content = "Toto je odpověď z cloudu (${apiProvider.value} - ${apiModel.value}). " +
                "Na tvou zprávu: \"$userText\"."
        
        if (emojiEnabled.value) {
            content += " ☁️🚀"
        }
        
        if (markdownEnabled.value) {
            content += "\n\n**Důležité:** Vše funguje správně přes API."
        }

        val aiMessage = ChatMessage(
            content = content,
            role = MessageRole.AI,
            processingTimeMs = System.currentTimeMillis() - startTime
        )
        _messages.value = _messages.value + aiMessage
    }

    private suspend fun simulateAiResponse(userText: String) {
        val startTime = System.currentTimeMillis()
        delay(1000) // Simulate thinking

        val tool = detectTool(userText)
        var content = when {
            tool != null -> "Zde jsou informace, které jsi hledal."
            _currentMode.value == AIMode.ROLEPLAY && rpCharacterName.value.isNotEmpty() -> {
                "[${rpCharacterName.value}]: ${rpDescription.value}. Odpovídám na: \"$userText\"."
            }
            userText.contains("ahoj", ignoreCase = true) -> "Ahoj! Jsem připraven na tvé otázky. Moje paměť je aktivní."
            else -> "Rozumím. Ptal jsi se na: \"$userText\". Pracuji v režimu ${_currentMode.value.label}."
        }

        if (emojiEnabled.value) {
            content += " 🤖"
        }
        
        if (markdownEnabled.value) {
            content += "\n\n*Lokální zpracování je aktivní.*"
        }

        val aiMessage = ChatMessage(
            content = content,
            role = MessageRole.AI,
            processingTimeMs = System.currentTimeMillis() - startTime,
            tool = tool
        )
        _messages.value = _messages.value + aiMessage
        
        // Save to persistent memory if important
        if (userText.length > 50) {
            memoryManager.addPersistent("Uživatel se zajímal o: $userText")
        }
    }

    private fun detectSlashCommand(text: String): ToolWidget? {
        val parts = text.split(" ")
        val command = parts[0].lowercase().removeDiacritics()
        
        return when (command) {
            "/image", "/generuj" -> {
                val prompt = text.removePrefix(parts[0]).trim()
                ToolWidget.ImageGeneration(
                    imageUri = "https://picsum.photos/seed/${UUID.randomUUID()}/512/512",
                    prompt = prompt.ifBlank { "Krásná krajina" }
                )
            }
            "/cas", "/time" -> {
                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val df = SimpleDateFormat("d. MMMM yyyy", Locale.getDefault())
                ToolWidget.Time(
                    currentTime = sdf.format(Date()),
                    currentDate = df.format(Date())
                )
            }
            "/poloha", "/location" -> {
                startLocationUpdates()
                ToolWidget.Location(0.0, 0.0)
            }
            "/kompas", "/compass" -> {
                startCompass()
                ToolWidget.Compass(_compassHeading.value)
            }
            "/maps", "/mapy" -> {
                val query = text.removePrefix(parts[0]).trim()
                var destination = "Praha"
                var mode = "driving"
                
                if (query.isNotEmpty()) {
                    if (query.contains("-")) {
                        val qParts = query.split("-")
                        destination = qParts[0].trim()
                        val m = qParts[1].trim().lowercase()
                        mode = when {
                            m.contains("autem") || m.contains("driving") -> "driving"
                            m.contains("pěšky") || m.contains("walking") -> "walking"
                            m.contains("kolo") || m.contains("bicycling") -> "bicycling"
                            else -> "driving"
                        }
                    } else {
                        destination = query
                    }
                }
                ToolWidget.Maps(destination, "Režim: $mode", mode)
            }
            "/pocasi", "/weather" -> {
                ToolWidget.Weather(
                    temp = "22°C",
                    humidity = "45%",
                    wind = "12 km/h",
                    pressure = "1015 hPa",
                    hourlyForecast = listOf(
                        ForecastItem("16:00", "23°C", android.R.drawable.ic_menu_report_image),
                        ForecastItem("17:00", "22°C", android.R.drawable.ic_menu_report_image),
                        ForecastItem("18:00", "21°C", android.R.drawable.ic_menu_report_image),
                        ForecastItem("19:00", "20°C", android.R.drawable.ic_menu_report_image)
                    )
                )
            }
            else -> null
        }
    }

    private fun handleSlashCommand(text: String) {
        val tool = detectSlashCommand(text)
        if (tool != null) {
            val content = when (tool) {
                is ToolWidget.Time -> "Aktuální čas a datum:"
                is ToolWidget.Location -> "Tvoje aktuální poloha:"
                is ToolWidget.Compass -> "Kompas aktivován:"
                is ToolWidget.Maps -> {
                    val dest = (tool as ToolWidget.Maps).destination
                    val mode = (tool as ToolWidget.Maps).travelMode
                    "Otevírám Google Mapy pro cíl: $dest ($mode)"
                }
                is ToolWidget.Weather -> "Aktuální předpověď počasí:"
                is ToolWidget.ImageGeneration -> "Generuji obrázek pro: ${(tool as ToolWidget.ImageGeneration).prompt}"
            }
            val aiMessage = ChatMessage(
                content = content,
                role = MessageRole.AI,
                tool = tool
            )
            _messages.value = _messages.value + aiMessage
        } else {
            val command = text.split(" ")[0]
            _messages.value = _messages.value + ChatMessage(content = "Neznámý příkaz: $command", role = MessageRole.AI)
        }
    }

    private fun String.removeDiacritics(): String {
        return Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
    }

    private fun startLocationUpdates() {
        try {
            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
            val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(p0: com.google.android.gms.location.LocationResult) {
                    p0.lastLocation?.let {
                        _currentLocation.value = ToolWidget.Location(it.latitude, it.longitude)
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: SecurityException) {}
    }

    private fun startCompass() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
        
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        
        val heading = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        _compassHeading.value = (heading + 360) % 360
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }

    private fun detectTool(text: String): ToolWidget? {
        val lowerText = text.lowercase()
        return when {
            (lowerText.contains("počasí") || lowerText.contains("weather")) && !text.startsWith("/") -> {
                detectSlashCommand("/pocasi")
            }
            (lowerText.contains("mapa") || lowerText.contains("cesta") || lowerText.contains("map")) && !text.startsWith("/") -> {
                detectSlashCommand("/maps")
            }
            (lowerText.contains("čas") || lowerText.contains("datum") || lowerText.contains("time")) && !text.startsWith("/") -> {
                detectSlashCommand("/cas")
            }
            else -> null
        }
    }

    fun toggleTts(messageId: String) {
        _isTtsPlaying.value = !_isTtsPlaying.value
    }

    fun stopTts() {
        _isTtsPlaying.value = false
    }

    fun cancelTts() {
        _isTtsPlaying.value = false
        // Additional logic to clear TTS queue if any
    }

    fun addImportantMemory(content: String) {
        viewModelScope.launch {
            memoryManager.addImportant(content)
        }
    }
}
