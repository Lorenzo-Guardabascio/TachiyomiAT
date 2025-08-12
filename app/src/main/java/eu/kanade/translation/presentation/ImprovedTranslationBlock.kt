package eu.kanade.translation.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
) {
    // Calcoli migliorati per il posizionamento
    val adjustedPadX = block.symWidth * 1.5f  // Ridotto il padding orizzontale
    val adjustedPadY = block.symHeight * 1.2f // Ridotto il padding verticale
    
    // Posizionamento più preciso
    val xPx = max((block.x - adjustedPadX / 2) * scaleFactor, 0.0f)
    val yPx = max((block.y - adjustedPadY / 2) * scaleFactor, 0.0f)
    
    // Dimensioni ottimizzate
    val width = ((block.width + adjustedPadX) * scaleFactor).pxToDp()
    val height = ((block.height + adjustedPadY) * scaleFactor).pxToDp()
    
    val isVertical = block.angle > 85 || block.angle < -85
    val isUpsideDown = block.angle > 135 || block.angle < -135
    
    Box(
        modifier = modifier
            .wrapContentSize(Alignment.CenterStart, true)
            .offset(xPx.pxToDp(), yPx.pxToDp())
            .requiredSize(width, height),
    ) {
        val density = LocalDensity.current
        val fontSize = remember { mutableStateOf(12.sp) }
        
        SubcomposeLayout { constraints ->
            val maxWidthPx = with(density) { width.roundToPx() }
            val maxHeightPx = with(density) { height.roundToPx() }

            // Algoritmo migliorato per il calcolo della dimensione del font
            var low = 6  // Dimensione minima font
            var high = min(48, (maxHeightPx / 4).coerceAtLeast(8)) // Dimensione massima dinamica
            var bestSize = low

            // Ricerca binaria migliorata
            while (low <= high) {
                val mid = (low + high) / 2
                val testSize = mid.sp
                
                val textLayoutResult = subcompose("test_$mid") {
                    Text(
                        text = block.translation,
                        fontSize = testSize,
                        fontFamily = fontFamily,
                        color = Color.Black,
                        overflow = TextOverflow.Visible,
                        textAlign = TextAlign.Center,
                        maxLines = Int.MAX_VALUE,
                        softWrap = true,
                        modifier = Modifier.width(width)
                    )
                }[0].measure(Constraints(maxWidth = maxWidthPx, maxHeight = maxHeightPx))

                // Verifica sia altezza che larghezza con margine di sicurezza
                val fitsHeight = textLayoutResult.height <= (maxHeightPx * 0.95).toInt()
                val fitsWidth = textLayoutResult.width <= (maxWidthPx * 0.95).toInt()
                
                if (fitsHeight && fitsWidth) {
                    bestSize = mid
                    low = mid + 1
                } else {
                    high = mid - 1
                }
            }
            
            fontSize.value = bestSize.sp

            // Layout finale con posizionamento migliorato
            val textPlaceable = subcompose("final") {
                Text(
                    text = block.translation,
                    fontSize = fontSize.value,
                    fontFamily = fontFamily,
                    color = Color.Black,
                    softWrap = true,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center,
                    maxLines = if (isVertical) Int.MAX_VALUE else 3, // Limite linee per testo orizzontale
                    modifier = Modifier
                        .width(width)
                        .rotate(
                            when {
                                isVertical -> 0f
                                isUpsideDown -> block.angle + 180f
                                else -> block.angle
                            }
                        )
                )
            }[0].measure(constraints)

            layout(textPlaceable.width, textPlaceable.height) {
                // Posizionamento centrato
                val xOffset = (constraints.maxWidth - textPlaceable.width) / 2
                val yOffset = (constraints.maxHeight - textPlaceable.height) / 2
                textPlaceable.place(xOffset, yOffset)
            }
        }
    }
}
