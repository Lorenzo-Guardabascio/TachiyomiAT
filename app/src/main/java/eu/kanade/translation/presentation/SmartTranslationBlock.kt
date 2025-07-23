package eu.kanade.translation.presentation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    val padX = block.symWidth * 2
    val padY = block.symHeight
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
        // Split translation into lines and render each line separately and clearly
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .rotate(if (isVertical) 0f else block.angle)
        ) {
            block.translation.lines().forEach { line ->
                Text(
                    text = line,
                    fontFamily = fontFamily,
                    fontSize = 18.sp,       // <<-- Set a constant font size
                    lineHeight = 22.sp,     // <<-- Even spacing between lines
                    color = Color.Black,    // or theme color
                    maxLines = 1,           // No wrapping within a line
                    overflow = TextOverflow.Ellipsis, // Truncate if too long
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(width - 8.dp) // leave a bit of padding
                        // (optional) .background(Color.White.copy(alpha=0.1f))
                )
            }
        }
    }
}
