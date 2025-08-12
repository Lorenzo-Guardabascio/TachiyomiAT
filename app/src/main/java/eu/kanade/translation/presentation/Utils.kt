package eu.kanade.translation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun Float.pxToDp(): Dp {
    val density = LocalDensity.current.density
    return (this / density).dp
}

/**
 * Migliora la precisione del calcolo delle dimensioni
 */
@Composable
fun Float.pxToDpPrecise(): Dp {
    val density = LocalDensity.current.density
    return (this / density).coerceAtLeast(0f).dp
}

/**
 * Calcola l'angolo normalizzato per una migliore rotazione del testo
 */
fun normalizeAngle(angle: Float): Float {
    var normalizedAngle = angle % 360f
    if (normalizedAngle < 0) normalizedAngle += 360f
    return normalizedAngle
}

/**
 * Determina se il testo è verticale basandosi sull'angolo
 */
fun isVerticalText(angle: Float): Boolean {
    val normalized = normalizeAngle(angle)
    return (normalized > 85 && normalized < 95) || (normalized > 265 && normalized < 275)
}

/**
 * Determina se il testo è capovolto
 */
fun isUpsideDownText(angle: Float): Boolean {
    val normalized = normalizeAngle(angle)
    return normalized > 90 && normalized < 270
}

/**
 * Calcola la dimensione ottimale del font basata sulle dimensioni del contenitore
 */
fun calculateOptimalFontSize(
    containerWidth: Float,
    containerHeight: Float,
    textLength: Int,
    isVertical: Boolean = false,
    minSize: Float = 8f,
    maxSize: Float = 48f
): Float {
    val baseSize = if (isVertical) {
        min(containerHeight / textLength.coerceAtLeast(1), containerWidth * 0.8f)
    } else {
        min(containerHeight * 0.6f, containerWidth / textLength.coerceAtLeast(1) * 2)
    }
    
    return baseSize.coerceIn(minSize, maxSize)
}

/**
 * Calcola il padding dinamico basato sulla dimensione del testo
 */
fun calculateDynamicPadding(
    symWidth: Float,
    symHeight: Float,
    scaleFactor: Float,
    isVertical: Boolean = false
): Pair<Float, Float> {
    val basePadX = if (isVertical) symWidth * 2.0f else symWidth * 1.4f
    val basePadY = symHeight * 1.2f
    
    val scaledPadX = basePadX * scaleFactor.coerceIn(0.5f, 2.0f)
    val scaledPadY = basePadY * scaleFactor.coerceIn(0.5f, 2.0f)
    
    return Pair(scaledPadX, scaledPadY)
}
