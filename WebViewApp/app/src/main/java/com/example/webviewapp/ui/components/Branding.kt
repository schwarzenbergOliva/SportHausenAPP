package com.example.webviewapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.webviewapp.ui.theme.BrandNavy
import com.example.webviewapp.ui.theme.BrandNavyDeep
import com.example.webviewapp.ui.theme.BrandTeal
import com.example.webviewapp.ui.theme.NeutralDark

/**
 * Fondo de marca con gradiente vertical navy → pizarra.
 * Tono profundo y sobrio que hace destacar la tarjeta y el botón coral.
 */
@Composable
fun BrandBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BrandNavyDeep, BrandNavy, NeutralDark)
                )
            )
    ) {
        content()
    }
}

/**
 * Logo de SportHausen.
 *
 * Provisional: insignia circular con las iniciales "SH" sobre gradiente azul.
 * Para usar el archivo real, reemplaza el contenido por:
 *
 *   Image(
 *       painter = painterResource(R.drawable.logo),
 *       contentDescription = "SportHausen",
 *       modifier = Modifier.size(size)
 *   )
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    Surface(
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = modifier
            .size(size)
            .shadow(elevation = 10.dp, shape = CircleShape)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.background(
                Brush.linearGradient(listOf(BrandTeal, BrandNavy))
            )
        ) {
            Text(
                text = "SH",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = (size.value * 0.40f).sp
            )
        }
    }
}
