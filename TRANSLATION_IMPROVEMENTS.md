# TachiyomiAT - Miglioramenti Sistema di Traduzione

## Versione: 0.19.1-AT (Aggiornata da Mihon v0.19.1)

### Problemi Risolti

#### 1. Posizionamento del Testo
- **Prima**: Il testo tradotto spesso appariva disallineato rispetto al testo originale
- **Dopo**: Algoritmi migliorati per il calcolo delle posizioni con padding adattivo basato sulla lunghezza del testo

#### 2. Dimensionamento del Testo
- **Prima**: Testo troppo grande o troppo piccolo, non adattato alla bolla
- **Dopo**: Ricerca binaria migliorata per il sizing automatico con limiti più intelligenti (8-72sp)

#### 3. Gestione del Text Wrapping
- **Prima**: Problemi con il wrapping del testo e la gestione delle righe multiple
- **Dopo**: Algoritmo adattivo che considera la lunghezza del testo e applica allineamento intelligente

#### 4. Scaling e Zoom
- **Prima**: Le traduzioni non si scalavano correttamente con lo zoom della pagina
- **Dopo**: Sistema di scaling migliorato che mantiene la leggibilità a tutti i livelli di zoom

### Nuove Funzionalità

#### 1. ImprovedTranslationBlock
- Calcolo adattivo del padding basato sulla lunghezza del testo
- Migliore gestione dell'orientamento del testo (verticale, orizzontale, ruotato)
- Algoritmo di sizing intelligente che considera sia larghezza che altezza
- Spaziatura tra righe migliorata (line-height: 1.15)

#### 2. Background delle Traduzioni
- Trasparenza configurabile (default: 92% opacità)
- Bordi arrotondati adattivi all'orientamento del testo
- Padding ridotto per maggiore precisione

#### 3. Nuove Preferenze
```kotlin
useImprovedTranslationRendering()    // Abilita il nuovo sistema di rendering
translationTextOpacity()             // Opacità del testo (0.0-1.0)
translationBackgroundOpacity()       // Opacità dello sfondo (0.0-1.0)
adaptiveTextSizing()                 // Dimensionamento adattivo del testo
smartTextAlignment()                 // Allineamento intelligente del testo
```

#### 4. Componenti Migliorati

##### ImprovedWebtoonTranslationsView
- Calcolo del fattore di scala conservativo per mantenere le proporzioni
- Rendering ottimizzato per le webtoon

##### ImprovedPagerTranslationsView
- Gestione migliorata dello scaling e dello zoom
- Applicazione del fattore di scala al contenitore principale

### Algoritmi Tecnici

#### 1. Calcolo del Padding Adattivo
```kotlin
val adjustedPadX = when {
    block.text.length > 50 -> block.symWidth * 1.8f // Testo lungo
    block.text.length > 20 -> block.symWidth * 1.5f // Testo medio
    else -> block.symWidth * 1.2f // Testo corto
}
```

#### 2. Rilevamento dell'Orientamento
```kotlin
val isVertical = block.angle > 85 || block.angle < -85
val isRotated = kotlin.math.abs(block.angle) > 15 && !isVertical
```

#### 3. Sizing Adattivo del Font
```kotlin
val baseSize = min(maxWidthPx, maxHeightPx) / 20
var low = max(8, baseSize / 2)  // Minimo adattivo
var high = min(48, baseSize * 2) // Massimo adattivo
```

### Compatibilità
- Mantiene compatibilità completa con il sistema esistente
- I componenti originali rimangono funzionanti
- Le nuove funzionalità possono essere abilitate/disabilitate tramite preferenze

### Note di Implementazione
- Tutti i miglioramenti sono retrocompatibili
- Il sistema può essere attivato/disattivato tramite preferenze
- Performance ottimizzate tramite SubcomposeLayout per il calcolo delle dimensioni

### Test Consigliati
1. Testare con manga di diversi orientamenti (orizzontale, verticale)
2. Verificare il comportamento con testi lunghi e corti
3. Testare lo zoom in modalità pager
4. Verificare la leggibilità in modalità webtoon

### Aggiornamenti Futuri Suggeriti
1. Aggiungere supporto per stili di font personalizzati
2. Implementare cache per le dimensioni dei font calcolate
3. Aggiungere opzioni per il colore del testo personalizzabile
4. Implementare animazioni fluide per le transizioni
