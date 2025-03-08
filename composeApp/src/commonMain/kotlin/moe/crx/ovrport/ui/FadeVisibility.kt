package moe.crx.ovrport.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable

@Composable
fun FadeVisibility(
    condition: Boolean,
    block: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        condition,
        enter = fadeIn(),
        exit = fadeOut(),
        content = block
    )
}