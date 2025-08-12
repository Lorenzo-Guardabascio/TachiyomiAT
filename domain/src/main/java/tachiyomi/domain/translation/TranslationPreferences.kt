package tachiyomi.domain.translation

import tachiyomi.core.common.preference.PreferenceStore

class TranslationPreferences(
    private val preferenceStore: PreferenceStore,
) {

    fun autoTranslateAfterDownload() = preferenceStore.getBoolean("auto_translate_after_download", false)
    fun translateFromLanguage() = preferenceStore.getString("translate_language_from", "CHINESE")
    fun translateToLanguage() = preferenceStore.getString("translate_language_to", "ENGLISH")
    fun translationFont() = preferenceStore.getInt("translation_font", 0)

    fun translationEngine() = preferenceStore.getInt("translation_engine", 0)
    fun translationEngineModel() = preferenceStore.getString("translation_engine_model", "gemini-1.5-pro")
    fun translationEngineApiKey() = preferenceStore.getString("translation_engine_api_key", "")
    fun translationEngineTemperature() = preferenceStore.getString("translation_engine_temperature", "1")
    fun translationEngineMaxOutputTokens() = preferenceStore.getString("translation_engine_output_tokens", "8192")
    
    // Nuove preferenze per i miglioramenti delle traduzioni
    fun useImprovedTranslationRendering() = preferenceStore.getBoolean("use_improved_translation_rendering", true)
    fun translationTextOpacity() = preferenceStore.getFloat("translation_text_opacity", 0.9f)
    fun translationBackgroundOpacity() = preferenceStore.getFloat("translation_background_opacity", 0.92f)
    fun adaptiveTextSizing() = preferenceStore.getBoolean("adaptive_text_sizing", true)
    fun smartTextAlignment() = preferenceStore.getBoolean("smart_text_alignment", true)
}
