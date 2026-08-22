package com.homebax.axionis.data

data class ModelInfo(
    val id: String,
    val name: String,

    /**
     * Přímá URL na soubor modelu.
     */
    val url: String,

    /**
     * Název souboru uloženého v telefonu.
     */
    val fileName: String,

    /**
     * Počet parametrů modelu, např. 0.5B, 1.5B, 7B.
     */
    val parameters: String,

    /**
     * Kvantizace, např. Q4_K_M.
     */
    val quantization: String?,

    /**
     * Maximální context length.
     */
    val contextLength: Int?,

    /**
     * Kategorie modelu.
     */
    val category: ModelCategory,

    /**
     * Zdroj modelu.
     */
    val source: String = "Hugging Face",

    /**
     * Volitelný SHA-256 hash pro kontrolu integrity.
     */
    val sha256: String? = null
)

enum class ModelCategory(
    val label: String
) {
    LLM("LLM"),
    STT("Speech to Text"),
    IMAGE("Generování obrázků"),
    VISION("Vision"),
    FILE("Analýza souborů")
}