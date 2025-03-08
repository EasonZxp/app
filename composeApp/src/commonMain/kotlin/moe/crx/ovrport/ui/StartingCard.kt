package moe.crx.ovrport.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import moe.crx.ovrport.AndroidPlatform
import moe.crx.ovrport.getPlatform
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import overportapp.composeapp.generated.resources.Res
import overportapp.composeapp.generated.resources.decoding_apk
import overportapp.composeapp.generated.resources.or_drag_and_drop_it
import overportapp.composeapp.generated.resources.select_apk_to_begin

@Composable
@Preview
fun StartingCard(
    innerPadding: PaddingValues = PaddingValues(16.dp),
    showLoading: Boolean = false,
    onCardClick: () -> Unit = {}
) {
    val strokeColor = LocalContentColor.current

    Card(
        modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)
            .fillMaxSize()
            .drawBehind {
                drawRoundRect(
                    color = strokeColor,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(
                                16.dp.toPx(),
                                16.dp.toPx()
                            ), 16.dp.toPx()
                        )
                    ),
                    cornerRadius = CornerRadius(32.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(32.dp))
            .clickable(!showLoading, onClick = onCardClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!showLoading) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(16.dp)
                        .width(64.dp)
                        .height(64.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.select_apk_to_begin)
                    )
                    if (getPlatform() !is AndroidPlatform) {
                        Text(
                            text = stringResource(Res.string.or_drag_and_drop_it)
                        )
                    }
                }
            } else {
                BreathingIcon(
                    modifier = Modifier
                        .padding(16.dp)
                        .rotate(30f)
                        .width(96.dp)
                        .height(96.dp)
                )
                Text(
                    text = stringResource(Res.string.decoding_apk)
                )
            }
        }
    }
}

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