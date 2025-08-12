package eu.kanade.translation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TranslationVisibilityState {
    var isVisible by mutableStateOf(true)
        private set
    
    var isTemporarilyHidden by mutableStateOf(false)
        private set
    
    fun toggleVisibility() {
        isVisible = !isVisible
    }
    
    fun showTranslations() {
        isVisible = true
        isTemporarilyHidden = false
    }
    
    fun hideTranslations() {
        isVisible = false
        isTemporarilyHidden = false
    }
    
    fun temporarilyHide(scope: CoroutineScope, duration: Long = 2000) {
        if (!isTemporarilyHidden && isVisible) {
            isTemporarilyHidden = true
            scope.launch {
                delay(duration)
                isTemporarilyHidden = false
            }
        }
    }
    
    val shouldShowTranslations: Boolean
        get() = isVisible && !isTemporarilyHidden
}

@Composable
fun rememberTranslationVisibilityState(): TranslationVisibilityState {
    return remember { TranslationVisibilityState() }
}
