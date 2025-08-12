# TachiyomiAT - Guida Integrazione Componenti Migliorati

## Passaggi per Attivare i Nuovi Componenti

### 1. Modifica dei PageHolder esistenti

#### Per WebtoonPageHolder.kt:
```kotlin
// Sostituire la creazione di WebtoonTranslationsView con:
private fun addTranslationsView() {
    if (page.translation == null) return
    removeView(translationsView)
    
    // Usa il componente migliorato se abilitato nelle preferenze
    val useImproved = translationPreferences.useImprovedTranslationRendering().get()
    
    translationsView = if (useImproved) {
        ImprovedWebtoonTranslationsView(
            context, 
            translation = page.translation!!,
            font = font
        )
    } else {
        WebtoonTranslationsView(
            context, 
            translation = page.translation!!,
            font = font
        )
    }
    
    if (!showTranslations) translationsView?.hide()
    addView(translationsView, MATCH_PARENT, MATCH_PARENT)
}
```

#### Per PagerPageHolder.kt:
```kotlin
// Sostituire la creazione di PagerTranslationsView con:
private fun addTranslationsView() {
    if (page.translation == null) return
    removeView(translationsView)
    
    // Usa il componente migliorato se abilitato nelle preferenze
    val useImproved = translationPreferences.useImprovedTranslationRendering().get()
    
    translationsView = if (useImproved) {
        ImprovedPagerTranslationsView(
            context, 
            translation = page.translation!!,
            font = font
        ).apply {
            // Collega gli stati di scala e posizione
            scaleState = this@PagerPageHolder.scaleState
            viewTLState = this@PagerPageHolder.viewTLState
        }
    } else {
        PagerTranslationsView(
            context, 
            translation = page.translation!!,
            font = font
        ).apply {
            scaleState = this@PagerPageHolder.scaleState
            viewTLState = this@PagerPageHolder.viewTLState
        }
    }
    
    if (!showTranslations) translationsView?.hide()
    addView(translationsView, MATCH_PARENT, MATCH_PARENT)
}
```

### 2. Aggiunta delle Preferenze nell'UI

#### Nel file delle impostazioni traduzione (presumibilmente in presentation/settings):
```kotlin
// Aggiungere queste preferenze nel pannello di configurazione traduzione:

PreferenceCategory(
    title = stringResource(ATMR.strings.translation_rendering_settings)
) {
    SwitchPreference(
        preference = translationPreferences.useImprovedTranslationRendering(),
        title = stringResource(ATMR.strings.improved_translation_rendering),
        subtitle = stringResource(ATMR.strings.improved_translation_rendering_summary)
    )
    
    SliderPreference(
        value = translationPreferences.translationBackgroundOpacity().get(),
        title = stringResource(ATMR.strings.translation_background_opacity),
        min = 0f,
        max = 1f,
        onValueChanged = { translationPreferences.translationBackgroundOpacity().set(it) }
    )
    
    SwitchPreference(
        preference = translationPreferences.adaptiveTextSizing(),
        title = stringResource(ATMR.strings.adaptive_text_sizing),
        subtitle = stringResource(ATMR.strings.adaptive_text_sizing_summary)
    )
    
    SwitchPreference(
        preference = translationPreferences.smartTextAlignment(),
        title = stringResource(ATMR.strings.smart_text_alignment),
        subtitle = stringResource(ATMR.strings.smart_text_alignment_summary)
    )
}
```

### 3. Aggiunta delle Stringhe di Traduzione

#### Nel file i18n-at appropriato:
```kotlin
// Aggiungere nel file delle stringhe:
translation_rendering_settings("Impostazioni Rendering Traduzioni")
improved_translation_rendering("Rendering Traduzioni Migliorato") 
improved_translation_rendering_summary("Usa algoritmi avanzati per migliore posizionamento del testo")
translation_background_opacity("Opacità Sfondo Traduzioni")
adaptive_text_sizing("Dimensionamento Testo Adattivo")
adaptive_text_sizing_summary("Adatta automaticamente la dimensione del testo alla bolla")
smart_text_alignment("Allineamento Testo Intelligente")
smart_text_alignment_summary("Ottimizza l'allineamento basato sulla lunghezza del testo")
```

### 4. Import da Aggiungere

#### Nei file PageHolder:
```kotlin
import eu.kanade.translation.presentation.ImprovedWebtoonTranslationsView
import eu.kanade.translation.presentation.ImprovedPagerTranslationsView
import tachiyomi.domain.translation.TranslationPreferences
```

### 5. Test di Verifica

Dopo l'implementazione, testare:

1. **Funzionalità base**: Traduzioni funzionano con componenti esistenti
2. **Switch preferenze**: Il toggle attiva/disattiva i nuovi componenti  
3. **Miglioramenti visivi**: Testo meglio posizionato e dimensionato
4. **Performance**: Nessun rallentamento percettibile
5. **Zoom/Scale**: Comportamento corretto in modalità pager
6. **Orientamenti**: Funziona con testi verticali/orizzontali/ruotati

### 6. Debugging

Per abilitare il debug dei componenti, decommentare:
```kotlin
// In ImprovedTranslationBlock.kt, rimuovere commento da:
// .background(color = Color.Blue), // Per visualizzare i bounds del testo
```

### 7. Performance Optimization

Per ottimizzare le performance:
- I calcoli del font sono cachati automaticamente da SubcomposeLayout
- Il background viene renderizzato solo se `showBackground = true`
- Le dimensioni sono calcolate on-demand durante il layout

### 8. Fallback Strategy

I componenti sono progettati per fallback automatico:
- Se `useImprovedTranslationRendering()` è false, usa componenti esistenti
- Se c'è un errore nei nuovi componenti, il sistema continua a funzionare
- Compatibilità totale con traduzioni esistenti

Questo approccio garantisce:
- ✅ Sicurezza: nessuna breaking change
- ✅ Flessibilità: attivabile/disattivabile via preferenze  
- ✅ Performance: ottimizzato per mobile
- ✅ UX: miglioramenti significativi nella leggibilità
