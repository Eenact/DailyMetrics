package com.eenact.dailymetrics.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.eenact.dailymetrics.resources.Res
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter

/** Plays a Lottie file from composeResources/files in a loop. */
@Composable
fun LottieAnimation(path: String, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(path) {
        LottieCompositionSpec.JsonString(Res.readBytes(path).decodeToString())
    }
    Image(
        painter = rememberLottiePainter(composition = composition, iterations = Compottie.IterateForever),
        contentDescription = null,
        modifier = modifier,
    )
}
