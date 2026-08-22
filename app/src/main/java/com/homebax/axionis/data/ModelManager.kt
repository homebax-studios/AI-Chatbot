package com.homebax.axionis.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

sealed class DownloadState {

    data object Idle : DownloadState()

    data class Downloading(
        val progress: Float
    ) : DownloadState()

    data object Completed : DownloadState()

    data class Error(
        val message: String
    ) : DownloadState()
}


class ModelManager(
    private val context: Context
) {

    private val client = OkHttpClient()

    private val _downloadStates =
        MutableStateFlow<Map<String, DownloadState>>(emptyMap())

    val downloadStates =
        _downloadStates.asStateFlow()


    /**
     * ModelManager NEOBSAHUJE vlastní seznam modelů.
     *
     * Všechny modely získává z ModelCatalog.
     */
    val availableModels: List<ModelInfo>
        get() = ModelCatalog.availableModels


    init {
        refreshDownloadStates()
    }


    /**
     * Aktualizuje stav všech modelů.
     */
    fun refreshDownloadStates() {

        val states =
            mutableMapOf<String, DownloadState>()

        availableModels.forEach { model ->

            states[model.id] =
                if (isModelDownloaded(model)) {
                    DownloadState.Completed
                } else {
                    DownloadState.Idle
                }
        }

        _downloadStates.value = states
    }


    /**
     * Vrátí seznam stažených modelů
     * společně s jejich skutečnou velikostí.
     */
    fun getDownloadedModels():
            List<Pair<ModelInfo, String>> {

        return availableModels
            .filter { isModelDownloaded(it) }
            .map { model ->

                val file =
                    getModelFile(model)

                model to
                        formatFileSize(
                            file.length()
                        )
            }
    }


    /**
     * Určí umístění modelu v interním úložišti aplikace.
     *
     * Výsledná struktura:
     *
     * files/
     * └── models/
     *     ├── llm/
     *     ├── stt/
     *     ├── image/
     *     ├── vision/
     *     └── file/
     */
    fun getModelFile(
        model: ModelInfo
    ): File {

        return File(
            context.filesDir,
            "models/" +
                    model.category.name.lowercase() +
                    "/" +
                    model.fileName
        )
    }


    /**
     * Převede velikost souboru na čitelný formát.
     */
    private fun formatFileSize(
        size: Long
    ): String {

        if (size <= 0) {
            return "0 B"
        }

        val units =
            arrayOf(
                "B",
                "KB",
                "MB",
                "GB",
                "TB"
            )

        val digitGroups =
            (
                    Math.log10(size.toDouble()) /
                            Math.log10(1024.0)
                    )
                .toInt()
                .coerceIn(
                    0,
                    units.lastIndex
                )

        return String.format(
            Locale.US,
            "%.1f %s",
            size /
                    Math.pow(
                        1024.0,
                        digitGroups.toDouble()
                    ),
            units[digitGroups]
        )
    }


    /**
     * Stáhne model.
     */
    suspend fun downloadModel(
        model: ModelInfo
    ) {

        /*
         * Pokud už existuje kompletní model,
         * není potřeba ho znovu stahovat.
         */
        if (isModelDownloaded(model)) {

            setState(
                model.id,
                DownloadState.Completed
            )

            return
        }


        withContext(Dispatchers.IO) {

            try {

                setState(
                    model.id,
                    DownloadState.Downloading(0f)
                )


                val request =
                    Request.Builder()
                        .url(model.url)
                        .get()
                        .build()


                client
                    .newCall(request)
                    .execute()
                    .use { response ->


                        if (!response.isSuccessful) {

                            throw Exception(
                                "HTTP ${response.code}: ${response.message}"
                            )
                        }


                        val body =
                            response.body
                                ?: throw Exception(
                                    "Server neposlal žádná data."
                                )


                        val destination =
                            getModelFile(model)


                        /*
                         * Vytvoření složky:
                         *
                         * files/models/llm
                         * files/models/stt
                         * ...
                         */
                        destination
                            .parentFile
                            ?.mkdirs()


                        /*
                         * Model nejprve stáhneme
                         * do dočasného souboru.
                         *
                         * Díky tomu se po přerušeném
                         * downloadu nebude tvářit jako
                         * kompletní model.
                         */
                        val temporaryFile =
                            File(
                                destination.parentFile,
                                "${destination.name}.download"
                            )


                        /*
                         * Pokud existuje starý rozbitý
                         * temporary soubor, smažeme ho.
                         */
                        if (temporaryFile.exists()) {
                            temporaryFile.delete()
                        }


                        val totalBytes =
                            body.contentLength()


                        body
                            .byteStream()
                            .use { input ->

                                FileOutputStream(
                                    temporaryFile
                                ).use { output ->

                                    val buffer =
                                        ByteArray(
                                            64 * 1024
                                        )

                                    var downloaded =
                                        0L


                                    while (true) {

                                        val bytesRead =
                                            input.read(buffer)


                                        if (bytesRead == -1) {
                                            break
                                        }


                                        output.write(
                                            buffer,
                                            0,
                                            bytesRead
                                        )


                                        downloaded +=
                                            bytesRead


                                        /*
                                         * Pokud server poskytuje
                                         * Content-Length, můžeme
                                         * zobrazit procenta.
                                         */
                                        if (totalBytes > 0) {

                                            val progress =
                                                (
                                                        downloaded.toFloat() /
                                                                totalBytes.toFloat()
                                                        )
                                                    .coerceIn(
                                                        0f,
                                                        1f
                                                    )


                                            setState(
                                                model.id,
                                                DownloadState.Downloading(
                                                    progress
                                                )
                                            )
                                        }
                                    }


                                    output.flush()
                                }
                            }


                        /*
                         * Kontrola, že něco skutečně
                         * přišlo.
                         */
                        if (!temporaryFile.exists() ||
                            temporaryFile.length() <= 0L
                        ) {

                            throw Exception(
                                "Stažený soubor je prázdný."
                            )
                        }


                        /*
                         * Pokud starý model existuje,
                         * odstraníme ho.
                         */
                        if (destination.exists()) {
                            destination.delete()
                        }


                        /*
                         * Temporary -> finální soubor.
                         */
                        if (!temporaryFile.renameTo(destination)) {

                            throw Exception(
                                "Nepodařilo se dokončit instalaci modelu."
                            )
                        }
                    }


                setState(
                    model.id,
                    DownloadState.Completed
                )

            } catch (e: Exception) {

                setState(
                    model.id,
                    DownloadState.Error(
                        e.message
                            ?: "Neznámá chyba při stahování."
                    )
                )
            }
        }
    }


    /**
     * Smaže model.
     */
    fun deleteModel(
        model: ModelInfo
    ) {

        val file =
            getModelFile(model)


        if (file.exists()) {
            file.delete()
        }


        val temporaryFile =
            File(
                file.parentFile,
                "${file.name}.download"
            )


        if (temporaryFile.exists()) {
            temporaryFile.delete()
        }


        setState(
            model.id,
            DownloadState.Idle
        )
    }


    /**
     * Zkontroluje, zda je model kompletně stažený.
     */
    fun isModelDownloaded(
        model: ModelInfo
    ): Boolean {

        val file =
            getModelFile(model)


        return file.exists() &&
                file.isFile &&
                file.length() > 0L
    }


    /**
     * Aktualizuje stav jednoho modelu.
     */
    private fun setState(
        modelId: String,
        state: DownloadState
    ) {

        _downloadStates.value =
            _downloadStates.value +
                    (
                            modelId to state
                            )
    }
}