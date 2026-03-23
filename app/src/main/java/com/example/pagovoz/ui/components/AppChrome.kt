package com.example.pagovoz.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import com.example.pagovoz.ui.theme.AppIconSizes
import com.example.pagovoz.ui.theme.AppRadii

@Composable
fun HablaPagoIconTile(
    tint: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    @DrawableRes iconRes: Int? = null,
    icon: ImageVector? = null,
    size: androidx.compose.ui.unit.Dp = AppIconSizes.tileMd,
    iconSize: androidx.compose.ui.unit.Dp = AppIconSizes.lg,
    shape: Shape = RoundedCornerShape(AppRadii.md)
) {
    Surface(
        modifier = modifier.size(size),
        shape = shape,
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                iconRes != null -> {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = contentDescription,
                        tint = tint,
                        modifier = Modifier.size(iconSize)
                    )
                }

                icon != null -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        tint = tint,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }
}

@Composable
fun HablaPagoChevron(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
    size: androidx.compose.ui.unit.Dp = AppIconSizes.md,
    containerColor: Color = Color.Transparent
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(size)
            )
        }
    }
}

fun Modifier.hablaPagoPressable(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.985f,
    pressedAlpha: Float = 0.96f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "hablapago-press-scale"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isPressed) pressedAlpha else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "hablapago-press-alpha"
    )

    graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
        alpha = animatedAlpha
    }
}
