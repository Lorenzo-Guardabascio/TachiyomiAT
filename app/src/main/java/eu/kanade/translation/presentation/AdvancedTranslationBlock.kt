package eu.kanade.translation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.translation.model.TranslationBlock
import tachiyomi.domain.translation.TranslationPreferences
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import kotlin.math.max
import kotlin.math.min

@Composable
fun AdvancedTranslationBlock(
    modifier: Modifier = Modifier,
    block: TranslationBlock,
    scaleFactor: Float,
    fontFamily: FontFamily,
    translationPreferences: TranslationPreferences = Injekt.get(),
    visibilityState: TranslationVisibilityState? = null,
) {
    // Se la visibilità è controllata e le traduzioni sono nascoste, non mostrare nulla
    if (visibilityState != null && !visibilityState.shouldShowTranslations) {
        return
    }
    // Leggi le preferenze direttamente
    val textOpacity = translationPreferences.translationTextOpacity().get()
    val backgroundOpacity = translationPreferences.translationBackgroundOpacity().get()
    val paddingMultiplier = translationPreferences.translationPaddingMultiplier().get()
    val minFontSize = translationPreferences.translationMinFontSize().get()
    val maxFontSize = translationPreferences.translationMaxFontSize().get()
    val shadowEnabled = translationPreferences.translationShadowEnabled().get()
    val shadowBlur = translationPreferences.translationShadowBlur().get()

    // Calcoli ottimizzati per il posizionamento
    val adjustedPadX = block.symWidth * paddingMultiplier
    val adjustedPadY = block.symHeight * paddingMultiplier
    
    // Posizionamento più preciso senza clipping negativo
    val xPx = max((block.x - adjustedPadX / 2) * scaleFactor, 0.0f)
    val yPx = max((block.y - adjustedPadY / 2) * scaleFactor, 0.0f)
    
    // Dimensioni adattive
    val width = ((block.width + adjustedPadX) * scaleFactor).pxToDp()
    val height = ((block.height + adjustedPadY) * scaleFactor).pxToDp()
    
    // Deteczione orientamento migliorata
    val isVertical = block.angle > 75 || block.angle < -75
    val isUpsideDown = block.angle > 135 || block.angle < -135
    
    // Colori adattivi
    val textColor = Color.Black.copy(alpha = textOpacity)
    val backgroundColor = Color.White.copy(alpha = backgroundOpacity)
    val borderColor = Color.Black.copy(alpha = 0.3f)
    
    Box(
        modifier = modifier
            .wrapContentSize(Alignment.CenterStart, true)
            .offset(xPx.pxToDp(), yPx.pxToDp())
            .requiredSize(width, height),
    ) {
        val density = LocalDensity.current
        val fontSize = remember { mutableStateOf(minFontSize) }
        
        SubcomposeLayout { constraints ->
            val maxWidthPx = with(density) { width.roundToPx() }
            val maxHeightPx = with(density) { height.roundToPx() }

            // Algoritmo ottimizzato per il calcolo della dimensione del font
            var low = minFontSize
            var high = kotlin.math.min(maxFontSize, (maxHeightPx / 3).coerceAtLeast(minFontSize))
            var bestSize = low

            // Ricerca binaria con controlli più rigidi
            while (low <= high) {
                val mid = (low + high) / 2
                val testSize = mid.sp
                
                val textLayoutResult = subcompose("test_$mid") {
                    Text(
                        text = block.translation,
                        fontSize = testSize,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        overflow = TextOverflow.Visible,
                        textAlign = TextAlign.Center,
                        maxLines = if (isVertical) Int.MAX_VALUE else 4,
                        softWrap = true,
                        lineHeight = (mid * 1.15f).sp,
                        modifier = Modifier.width(width)
                    )
                }[0].measure(Constraints(maxWidth = maxWidthPx, maxHeight = maxHeightPx))

                // Controllo più conservativo per garantire che il testo si adatti
                val heightFits = textLayoutResult.height <= (maxHeightPx * 0.90).toInt()
                val widthFits = textLayoutResult.width <= (maxWidthPx * 0.95).toInt()
                
                if (heightFits && widthFits) {
                    bestSize = mid
                    low = mid + 1
                } else {
                    high = mid - 1
                }
            }
            
            fontSize.value = bestSize

            // Layout finale con styling migliorato
            val textPlaceable = subcompose("final") {
                Box(
                    modifier = Modifier
                        .then(
                            if (shadowEnabled) {
                                Modifier.shadow(
                                    elevation = shadowBlur.dp,
                                    shape = RoundedCornerShape(4.dp)
                                )
                            } else Modifier
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .background(backgroundColor)
                        .border(
                            width = 0.5.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 2.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = block.translation,
                        fontSize = fontSize.value.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        softWrap = true,
                        overflow = TextOverflow.Clip,
                        textAlign = TextAlign.Center,
                        maxLines = if (isVertical) Int.MAX_VALUE else 4,
                        lineHeight = (fontSize.value * 1.15f).sp,
                        modifier = Modifier
                            .width(width)
                            .rotate(
                                when {
                                    isVertical -> 0f
                                    isUpsideDown -> block.angle + 180f
                                    else -> block.angle
                                }
                            )
                            .graphicsLayer {
                                // Anti-aliasing per testo più pulito
                                this.alpha = textOpacity
                                this.compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                            }
                    )
                }
            }[0].measure(constraints)

            layout(textPlaceable.width, textPlaceable.height) {
                // Posizionamento centrato ottimizzato
                val xOffset = (constraints.maxWidth - textPlaceable.width) / 2
                val yOffset = (constraints.maxHeight - textPlaceable.height) / 2
                textPlaceable.place(xOffset.coerceAtLeast(0), yOffset.coerceAtLeast(0))
            }
        }
    }
}
