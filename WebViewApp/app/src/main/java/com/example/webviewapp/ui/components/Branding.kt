package com.example.webviewapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.webviewapp.R
import com.example.webviewapp.ui.theme.BrandNavy
import com.example.webviewapp.ui.theme.BrandNavyDeep
import com.example.webviewapp.ui.theme.NeutralDark
import androidx.compose.ui.graphics.Color

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
 * Placa de marca: el logotipo completo de SportHausen (llama + wordmark + bajada)
 * sobre una superficie blanca redondeada.
 *
 * El logotipo usa tinta navy oscura, por lo que necesita un fondo claro para leerse
 * bien sobre el gradiente oscuro del login. La placa comparte el blanco y el radio
 * de la tarjeta del formulario para que la pantalla se sienta como un mismo sistema.
 */
@Composable
fun BrandLogoPlate(
    modifier: Modifier = Modifier,
    logoWidth: Dp = 224.dp
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(R.drawable.logo_sporthausen),
            contentDescription = "SportHausen — Deporte y Comunidad",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(horizontal = 28.dp, vertical = 20.dp)
                .width(logoWidth)
                .aspectRatio(559f / 248f)
        )
    }
}
