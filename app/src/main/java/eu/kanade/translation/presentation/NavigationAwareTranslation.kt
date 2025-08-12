package eu.kanade.translation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tachiyomi.domain.translation.TranslationPreferences
import kotlin.math.abs

data class NavigationZoneConfig(
    val zoneWidthPercent: Float = 0.2f, // 20% della larghezza schermo
    val enabled: Boolean = true,
    val hideDuration: Long = 2000L
)

@Composable
fun NavigationAwareTranslationContainer(
    modifier: Modifier = Modifier,
    translationPreferences: TranslationPreferences,
    visibilityState: TranslationVisibilityState,
    onNavigationLongPress: (isLeftZone: Boolean) -> Unit = { },
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val navigationEnabled = translationPreferences.hideTranslationOnNavigationLongPress().get()
    val zoneWidth = translationPreferences.navigationZoneWidth().get()
    val hideDuration = translationPreferences.longPressHideDuration().get()
    
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val navigationZoneWidth = screenWidth * zoneWidth
    
    var isLongPressing by remember { mutableStateOf(false) }
    var pressStartTime by remember { mutableStateOf(0L) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(navigationEnabled) {
                if (navigationEnabled) {
                    detectTapGestures(
                        onPress = { offset ->
                            val isInNavigationZone = offset.x <= navigationZoneWidth || 
                                                   offset.x >= (screenWidth - navigationZoneWidth)
                            
                            if (isInNavigationZone) {
                                pressStartTime = System.currentTimeMillis()
                                isLongPressing = true
                                
                                // Aspetta per la durata della pressione lunga
                                val longPressDelay = 500L // milliseconds
                                scope.launch {
                                    delay(longPressDelay)
                                    if (isLongPressing) {
                                        // È una pressione lunga nelle zone di navigazione
                                        val isLeftZone = offset.x <= navigationZoneWidth
                                        onNavigationLongPress(isLeftZone)
                                        visibilityState.temporarilyHide(scope, hideDuration.toLong())
                                    }
                                }
                                
                                // Aspetta il rilascio
                                awaitRelease()
                                isLongPressing = false
                            }
                        }
                    )
                }
            }
    ) {
        // Overlay di debug per le zone di navigazione (opzionale, rimuovibile)
        if (navigationEnabled && false) { // Disabilitato per production
            Row(modifier = Modifier.fillMaxSize()) {
                // Zona sinistra
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(zoneWidth)
                        .background(Color.Red.copy(alpha = 0.1f))
                )
                // Zona centrale
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                )
                // Zona destra
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(zoneWidth)
                        .background(Color.Blue.copy(alpha = 0.1f))
                )
            }
        }
        
        // Contenuto delle traduzioni
        content()
    }
}

class NavigationZoneHandler(
    private val scope: CoroutineScope,
    private val visibilityState: TranslationVisibilityState,
    private val config: NavigationZoneConfig
) {
    
    fun handleNavigationLongPress(isLeftZone: Boolean, screenWidth: Float, touchX: Float) {
        if (!config.enabled) return
        
        val zoneWidth = screenWidth * config.zoneWidthPercent
        val isInLeftZone = touchX <= zoneWidth
        val isInRightZone = touchX >= (screenWidth - zoneWidth)
        
        if (isInLeftZone || isInRightZone) {
            visibilityState.temporarilyHide(scope, config.hideDuration)
        }
    }
    
    fun isInNavigationZone(touchX: Float, screenWidth: Float): Boolean {
        val zoneWidth = screenWidth * config.zoneWidthPercent
        return touchX <= zoneWidth || touchX >= (screenWidth - zoneWidth)
    }
}
