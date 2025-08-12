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
    
    // Nuove preferenze per traduzioni migliorate
    fun useImprovedTranslationRendering() = preferenceStore.getBoolean("use_improved_translation_rendering", true)
    fun translationTextOpacity() = preferenceStore.getFloat("translation_text_opacity", 0.95f)
    fun translationBackgroundOpacity() = preferenceStore.getFloat("translation_background_opacity", 0.92f)
    fun translationPaddingMultiplier() = preferenceStore.getFloat("translation_padding_multiplier", 1.3f)
    fun translationMinFontSize() = preferenceStore.getInt("translation_min_font_size", 8)
    fun translationMaxFontSize() = preferenceStore.getInt("translation_max_font_size", 48)
}
