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

@Composable
fun SmartTranslationBlock(
    modifier: Modifier = Modifier,
    block: TranslationBlock,
    scaleFactor: Float,
    fontFamily: FontFamily,

) {
    // Migliorato il calcolo del padding per una migliore disposizione del testo
    val padX = block.symWidth * 1.5f // Ridotto da 2 a 1.5 per un padding più preciso
    val padY = block.symHeight * 0.8f // Ridotto da 1 a 0.8 per un padding verticale più preciso
    val xPx = max((block.x - padX / 2) * scaleFactor, 0.0f)
    val yPx = max((block.y - padY / 2) * scaleFactor, 0.0f)
    val width = ((block.width + padX) * scaleFactor).pxToDp()
    val height = ((block.height + padY) * scaleFactor).pxToDp()
    val isVertical = block.angle > 85
    
    Box(
        modifier = modifier
            .wrapContentSize(Alignment.CenterStart, true)
            .offset(xPx.pxToDp(), yPx.pxToDp())
            .requiredSize(width, height),
    ) {
        val density = LocalDensity.current
        val fontSize = remember { mutableStateOf(16.sp) }
        SubcomposeLayout { constraints ->
            val maxWidthPx = with(density) { width.roundToPx() }
            val maxHeightPx = with(density) { height.roundToPx() }

            // Migliorato l'algoritmo di ricerca binaria per la dimensione del font
            var low = 6 // Aumentato il minimo da 1 a 6 per leggibilità
            var high = 72 // Ridotto il massimo da 100 a 72 per evitare font troppo grandi
            var bestSize = low

            while (low <= high) {
                val mid = ((low + high) / 2)
                val textLayoutResult = subcompose("test_$mid") {
                    Text(
                        text = block.translation,
                        fontSize = mid.sp,
                        fontFamily = fontFamily,
                        color = Color.Black,
                        overflow = TextOverflow.Visible,
                        textAlign = TextAlign.Center,
                        maxLines = Int.MAX_VALUE,
                        softWrap = true,
                        lineHeight = (mid * 1.1f).sp, // Aggiunto line height per miglior spaziatura
                        modifier = Modifier
                            .width(width)
                            .rotate(if (isVertical) 0f else block.angle)
                            .align(Alignment.Center),
                    )
                }[0].measure(Constraints(maxWidth = maxWidthPx))

                // Migliorato il controllo per assicurarsi che il testo si adatti sia in larghezza che in altezza
                if (textLayoutResult.height <= maxHeightPx && textLayoutResult.width <= maxWidthPx) {
                    bestSize = mid
                    low = mid + 1
                } else {
                    high = mid - 1
                }
            }
            fontSize.value = bestSize.sp

            // Layout finale con miglioramenti
            val textPlaceable = subcompose("final") {
                Text(
                    text = block.translation,
                    fontSize = fontSize.value,
                    fontFamily = fontFamily,
                    color = Color.Black,
                    softWrap = true,
                    overflow = TextOverflow.Visible,
                    textAlign = TextAlign.Center,
                    maxLines = Int.MAX_VALUE,
                    lineHeight = (fontSize.value.value * 1.1f).sp, // Migliorata la spaziatura tra le righe
                    modifier = Modifier
                        .width(width)
                        .rotate(if (isVertical) 0f else block.angle)
                        .align(Alignment.Center),
//                        .background(color = Color.Blue), // Debug
                )
            }[0].measure(constraints)

            layout(textPlaceable.width, textPlaceable.height) {
                textPlaceable.place(0, 0)
            }
        }
    }
}
