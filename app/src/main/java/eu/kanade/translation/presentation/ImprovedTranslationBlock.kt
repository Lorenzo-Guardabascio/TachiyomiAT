package eu.kanade.translation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.translation.model.TranslationBlock
import kotlin.math.max
import kotlin.math.min

@Composable
fun ImprovedTranslationBlock(
    modifier: Modifier = Modifier,
    block: TranslationBlock,
    scaleFactor: Float,
    fontFamily: FontFamily,
    showBackground: Boolean = true,
) {
    // Algoritmi migliorati per il calcolo delle posizioni e dimensioni
    val adjustedPadX = when {
        block.text.length > 50 -> block.symWidth * 1.8f // Testo lungo, più padding orizzontale
        block.text.length > 20 -> block.symWidth * 1.5f // Testo medio
        else -> block.symWidth * 1.2f // Testo corto, meno padding
    }
    
    val adjustedPadY = when {
        block.text.contains('\n') || block.translation.contains('\n') -> block.symHeight * 1.2f // Testo multilinea
        else -> block.symHeight * 0.8f // Testo singola linea
    }
    
    val xPx = max((block.x - adjustedPadX / 2) * scaleFactor, 0.0f)
    val yPx = max((block.y - adjustedPadY / 2) * scaleFactor, 0.0f)
    val width = ((block.width + adjustedPadX) * scaleFactor).pxToDp()
    val height = ((block.height + adjustedPadY) * scaleFactor).pxToDp()
    
    // Migliorata la detection dell'orientamento del testo
    val isVertical = block.angle > 85 || block.angle < -85
    val isRotated = kotlin.math.abs(block.angle) > 15 && !isVertical
    
    Box(
        modifier = modifier
            .wrapContentSize(Alignment.CenterStart, true)
            .offset(xPx.pxToDp(), yPx.pxToDp())
            .requiredSize(width, height),
    ) {
        // Background migliorato
        if (showBackground) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .rotate(if (isVertical) 0f else block.angle)
                    .background(
                        Color.White.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(
                            when {
                                isVertical -> 1.dp
                                isRotated -> 2.dp
                                else -> 2.dp
                            }
                        )
                    ),
            )
        }
        
        // Testo con algoritmo migliorato per il sizing
        val density = LocalDensity.current
        val fontSize = remember { mutableStateOf(16.sp) }
        
        SubcomposeLayout { constraints ->
            val maxWidthPx = with(density) { width.roundToPx() }
            val maxHeightPx = with(density) { height.roundToPx() }
            
            // Algoritmo di ricerca binaria migliorato con limiti più intelligenti
            val baseSize = min(maxWidthPx, maxHeightPx) / 20 // Dimensione base basata sulla box
            var low = max(8, baseSize / 2) // Minimo leggibile, adattivo
            var high = min(48, baseSize * 2) // Massimo ragionevole, adattivo
            var bestSize = low
            
            // Pre-calcolo per determinare se il testo necessita wrapping
            val textLength = block.translation.length
            val estimatedLines = when {
                textLength < 10 -> 1
                textLength < 30 -> 2
                else -> kotlin.math.ceil(textLength / 15.0).toInt()
            }
            
            while (low <= high) {
                val mid = (low + high) / 2
                val testResult = subcompose("size_test_$mid") {
                    Text(
                        text = block.translation,
                        fontSize = mid.sp,
                        fontFamily = fontFamily,
                        color = Color.Black,
                        overflow = TextOverflow.Visible,
                        textAlign = when {
                            isVertical -> TextAlign.Center
                            estimatedLines > 1 -> TextAlign.Start
                            else -> TextAlign.Center
                        },
                        maxLines = Int.MAX_VALUE,
                        softWrap = true,
                        lineHeight = (mid * 1.15f).sp, // Migliore spaziatura tra righe
                        modifier = Modifier
                            .width(width)
                            .rotate(if (isVertical) 0f else block.angle),
                    )
                }[0].measure(Constraints(maxWidth = maxWidthPx))
                
                // Controllo migliorato che considera sia larghezza che altezza
                val fitsWidth = testResult.width <= maxWidthPx
                val fitsHeight = testResult.height <= maxHeightPx
                
                if (fitsWidth && fitsHeight) {
                    bestSize = mid
                    low = mid + 1
                } else {
                    high = mid - 1
                }
            }
            
            fontSize.value = bestSize.sp
            
            // Layout finale
            val textPlaceable = subcompose("final_text") {
                Text(
                    text = block.translation,
                    fontSize = fontSize.value,
                    fontFamily = fontFamily,
                    color = Color.Black,
                    softWrap = true,
                    overflow = TextOverflow.Visible,
                    textAlign = when {
                        isVertical -> TextAlign.Center
                        estimatedLines > 1 -> TextAlign.Start
                        else -> TextAlign.Center
                    },
                    maxLines = Int.MAX_VALUE,
                    lineHeight = (fontSize.value.value * 1.15f).sp,
                    modifier = Modifier
                        .width(width)
                        .rotate(if (isVertical) 0f else block.angle)
                        .align(Alignment.Center),
                )
            }[0].measure(constraints)
            
            layout(textPlaceable.width, textPlaceable.height) {
                textPlaceable.place(0, 0)
            }
        }
    }
}
