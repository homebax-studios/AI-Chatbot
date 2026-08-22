package com.homebax.axionis.data

object ModelCatalog {

    // ============================================================
    // SETUP
    // ============================================================

    /**
     * Výchozí LLM model.
     *
     * Tento model se stáhne během prvního spuštění aplikace.
     */
    val setupLlm: ModelInfo =
        qwen25_05b_q4_k_m

    /**
     * Výchozí Speech-to-Text model.
     *
     * Tento model se stáhne během prvního spuštění aplikace.
     */
    val setupStt: ModelInfo =
        whisperSmallQ4Km


    // ============================================================
    // LLM MODELS
    // ============================================================

    val llmModels: List<ModelInfo> =
        listOf(
            qwen25_05b_q4_k_m
        )


    // ============================================================
    // STT MODELS
    // ============================================================

    val sttModels: List<ModelInfo> =
        listOf(
            whisperSmallQ4Km
        )


    // ============================================================
    // IMAGE MODELS
    // ============================================================

    val imageModels: List<ModelInfo> =
        emptyList()


    // ============================================================
    // VISION MODELS
    // ============================================================

    val visionModels: List<ModelInfo> =
        emptyList()


    // ============================================================
    // FILE MODELS
    // ============================================================

    val fileModels: List<ModelInfo> =
        emptyList()


    // ============================================================
    // ALL MODELS
    // ============================================================

    /**
     * Všechny modely dostupné v Settings.
     *
     * Setup používá pouze setupLlm a setupStt.
     * Settings používá tento kompletní seznam.
     */
    val availableModels: List<ModelInfo> =
        llmModels +
                sttModels +
                imageModels +
                visionModels +
                fileModels


    // ============================================================
    // MODEL DEFINITIONS
    // ============================================================

    /**
     * Qwen 2.5 0.5B Instruct
     *
     * GGUF Q4_K_M varianta.
     */
    private val qwen25_05b_q4_k_m =
        ModelInfo(
            id = "qwen2.5-0.5b-instruct-q4-k-m",

            name = "Qwen 2.5 0.5B Instruct",

            url = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf?download=true",

            fileName = "qwen2.5-0.5b-instruct-q4_k_m.gguf",

            parameters = "0.5B",

            quantization = "Q4_K_M",

            contextLength = 32768,

            category = ModelCategory.LLM,

            source = "Hugging Face"
        )


    /**
     * Whisper Small
     *
     * Poznámka:
     * Whisper není LLM. Je určený pro Speech-to-Text.
     *
     * Zde používáme GGUF variantu kompatibilní
     * s lokálním inference backendem, který budeš
     * následně připojovat k aplikaci.
     */
    private val whisperSmallQ4Km =
        ModelInfo(
            id = "whisper-small-q4-k-m",

            name = "Whisper Small",

            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small-q5_1.bin?download=true",

            fileName = "ggml-small-q5_1.bin",

            parameters = "244M",

            quantization = "Q5_1",

            contextLength = null,

            category = ModelCategory.STT,

            source = "Hugging Face"
        )
}