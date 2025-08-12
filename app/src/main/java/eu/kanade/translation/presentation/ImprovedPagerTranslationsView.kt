package eu.kanade.translation.presentation

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import eu.kanade.translation.data.TranslationFont
import eu.kanade.translation.model.PageTranslation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.max

class ImprovedPagerTranslationsView :
    AbstractComposeView {

    private val translation: PageTranslation
    private val font: TranslationFont
    private val fontFamily: FontFamily

    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : super(context, attrs, defStyleAttr) {
        this.translation = PageTranslation.EMPTY
        this.font = TranslationFont.ANIME_ACE
        this.fontFamily = Font(
            resId = font.res,
            weight = FontWeight.Bold,
        ).toFontFamily()
    }

    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        translation: PageTranslation,
        font: TranslationFont? = null,
    ) : super(context, attrs, defStyleAttr) {
        this.translation = translation
        this.font = font ?: TranslationFont.ANIME_ACE
        this.fontFamily = Font(
            resId = this.font.res,
            weight = FontWeight.Bold,
        ).toFontFamily()
    }

    val scaleState = MutableStateFlow(1f)
    val viewTLState = MutableStateFlow(PointF())

    @Composable
    override fun Content() {
        val viewTL by viewTLState.collectAsState()
        val scale by scaleState.collectAsState()
        Box(
            modifier = Modifier
                .absoluteOffset(viewTL.x.pxToDp(), viewTL.y.pxToDp()),
        ) {
            // Background con trasparenza migliorata
            ImprovedTextBlockBackground(scale)
            // Contenuto del testo migliorato
            ImprovedTextBlockContent(scale)
        }
    }

    @Composable
    fun ImprovedTextBlockBackground(zoomScale: Float) {
        translation.blocks.forEach { block ->
            // Calcoli migliorati per lo sfondo
            val padX = block.symWidth * 1.3f  // Padding ridotto
            val padY = block.symHeight * 1.1f
            val bgX = max((block.x - padX / 2) * zoomScale, 0.0f)
            val bgY = max((block.y - padY / 2) * zoomScale, 0.0f)
            val bgWidth = (block.width + padX) * zoomScale
            val bgHeight = (block.height + padY) * zoomScale
            
            val isVertical = block.angle > 85 || block.angle < -85
            val adjustedAngle = when {
                isVertical -> 0f
                block.angle > 135 || block.angle < -135 -> block.angle + 180f
                else -> block.angle
            }
            
            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.CenterStart, true)
                    .offset(bgX.pxToDp(), bgY.pxToDp())
                    .requiredSize(bgWidth.pxToDp(), bgHeight.pxToDp())
                    .rotate(adjustedAngle)
                    .background(
                        color = Color.White.copy(alpha = 0.95f), // Trasparenza leggera
                        shape = RoundedCornerShape(6.dp) // Angoli più arrotondati
                    ),
            )
        }
    }

    @Composable
    fun ImprovedTextBlockContent(zoomScale: Float) {
        translation.blocks.forEach { block ->
            if (block.translation.isNotBlank()) { // Solo blocchi con traduzione
                ImprovedTranslationBlock(
                    block = block,
                    scaleFactor = zoomScale,
                    fontFamily = fontFamily,
                )
            }
        }
    }

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }
    
    fun updateTranslation(newTranslation: PageTranslation) {
        // Possibilità di aggiornare la traduzione dinamicamente
        // Implementazione futura per aggiornamenti in tempo reale
    }
}
