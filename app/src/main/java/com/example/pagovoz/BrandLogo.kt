package com.example.pagovoz

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    logoRes: Int = R.drawable.logop,
    imagePadding: Int = 0,
    imageScale: Float = 1f,
    imageOffsetY: Dp = 0.dp,
    backgroundColor: Color = Color.White,
    shape: Shape = RoundedCornerShape(24.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (backgroundColor != Color.Transparent) {
                    Modifier.background(color = backgroundColor, shape = shape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = "HablaPago Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = imageScale,
                    scaleY = imageScale
                )
                .offset(y = imageOffsetY)
                .then(
                    if (imagePadding > 0) Modifier.padding(imagePadding.dp) else Modifier
                )
        )
    }
}
