package eu.kanade.translation.presentation

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import tachiyomi.domain.translation.TranslationPreferences
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import kotlin.math.max

class ImprovedWebtoonTranslationsView :
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
    private val translationPreferences: TranslationPreferences = Injekt.get()

    @Composable
    override fun Content() {
        val viewTL by viewTLState.collectAsState()
        val scale by scaleState.collectAsState()
        val visibilityState = rememberTranslationVisibilityState()
        val scope = rememberCoroutineScope()
        val hideOnLongPress = translationPreferences.hideTranslationOnLongPress().get()
        val hideOnNavLongPress = translationPreferences.hideTranslationOnNavigationLongPress().get()
        val hideDuration = translationPreferences.longPressHideDuration().get()
        
        NavigationAwareTranslationContainer(
            modifier = Modifier.absoluteOffset(viewTL.x.pxToDp(), viewTL.y.pxToDp()),
            translationPreferences = translationPreferences,
            visibilityState = visibilityState,
            onNavigationLongPress = { isLeftZone ->
                // Callback per gestire azioni specifiche per zona sinistra/destra se necessario
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (hideOnLongPress && !hideOnNavLongPress) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        visibilityState.temporarilyHide(scope, hideDuration.toLong())
                                    }
                                )
                            }
                        } else Modifier
                    )
            ) {
                // Background ottimizzato per webtoon
                WebtoonTextBlockBackground(scale, if (hideOnLongPress || hideOnNavLongPress) visibilityState else null)
                // Contenuto ottimizzato per webtoon  
                WebtoonTextBlockContent(scale, if (hideOnLongPress || hideOnNavLongPress) visibilityState else null)
            }
        }
    }

    @Composable
    fun WebtoonTextBlockBackground(zoomScale: Float, visibilityState: TranslationVisibilityState? = null) {
        if (visibilityState != null && !visibilityState.shouldShowTranslations) {
            return
        }
        
        translation.blocks.forEach { block ->
            // Padding specifico per webtoon (spesso verticali)
            val padX = if (block.angle > 85 || block.angle < -85) {
                block.symWidth * 2.0f  // Padding maggiore per testo verticale
            } else {
                block.symWidth * 1.3f  // Padding normale per testo orizzontale
            }
            
            val padY = block.symHeight * 1.2f
            val bgX = max((block.x - padX / 2) * zoomScale, 0.0f)
            val bgY = max((block.y - padY / 2) * zoomScale, 0.0f)
            val bgWidth = (block.width + padX) * zoomScale
            val bgHeight = (block.height + padY) * zoomScale
            
            val isVertical = block.angle > 85 || block.angle < -85
            
            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.CenterStart, true)
                    .offset(bgX.pxToDp(), bgY.pxToDp())
                    .requiredSize(bgWidth.pxToDp(), bgHeight.pxToDp())
                    .rotate(if (isVertical) 0f else block.angle)
                    .background(
                        color = Color.White.copy(alpha = 0.92f), // Trasparenza per webtoon
                        shape = RoundedCornerShape(8.dp) // Angoli più arrotondati per webtoon
                    ),
            )
        }
    }

    @Composable
    fun WebtoonTextBlockContent(zoomScale: Float, visibilityState: TranslationVisibilityState? = null) {
        translation.blocks.forEach { block ->
            // Usa il nuovo componente avanzato per il rendering webtoon
            AdvancedTranslationBlock(
                block = block,
                scaleFactor = zoomScale,
                fontFamily = fontFamily,
                visibilityState = visibilityState
            )
        }
    }

    @Composable
    fun WebtoonTranslationBlock(
        modifier: Modifier = Modifier,
        block: eu.kanade.translation.model.TranslationBlock,
        scaleFactor: Float,
        fontFamily: FontFamily,
    ) {
        // Versione specializzata per webtoon con gestione migliorata del testo verticale
        val isVertical = block.angle > 85 || block.angle < -85
        
        val adjustedPadX = if (isVertical) {
            block.symWidth * 2.2f  // Più spazio per testo verticale
        } else {
            block.symWidth * 1.4f
        }
        val adjustedPadY = block.symHeight * 1.3f
        
        val xPx = max((block.x - adjustedPadX / 2) * scaleFactor, 0.0f)
        val yPx = max((block.y - adjustedPadY / 2) * scaleFactor, 0.0f)
        
        val width = ((block.width + adjustedPadX) * scaleFactor).pxToDp()
        val height = ((block.height + adjustedPadY) * scaleFactor).pxToDp()
        
        Box(
            modifier = modifier
                .wrapContentSize(Alignment.CenterStart, true)
                .offset(xPx.pxToDp(), yPx.pxToDp())
                .requiredSize(width, height),
        ) {
            ImprovedTranslationBlock(
                block = block,
                scaleFactor = 1f, // Già scalato nelle dimensioni
                fontFamily = fontFamily,
                modifier = Modifier.matchParentSize()
            )
        }
    }

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }
    
    fun updateTranslation(newTranslation: PageTranslation) {
        // Aggiornamento dinamico per webtoon
    }
}
