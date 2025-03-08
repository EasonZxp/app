package moe.crx.ovrport.ui

import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.delay

@Composable
fun BreathingIcon(modifier: Modifier, duration: Int = 1500) {
    var isFaded by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (isFaded) 0.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    LaunchedEffect(Unit) {
        while (true) {
            isFaded = !isFaded
            delay(duration.toLong())
        }
    }

    Icon(
        Icons.Default.Bolt,
        contentDescription = null,
        modifier = modifier.alpha(alpha)
    )
}
