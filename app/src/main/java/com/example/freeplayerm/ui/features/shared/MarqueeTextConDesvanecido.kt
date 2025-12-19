package com.example.freeplayerm.ui.features.shared

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun MarqueeTextConDesvanecido(
    text: String,
    modifier: Modifier = Modifier,
    gradientEdgeColor: Color = MaterialTheme.colorScheme.background,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    style: TextStyle = LocalTextStyle.current,
    fadeWidth: Dp = 10.dp // valor por defecto: 10.dp
) {
    MarqueeText(
        text = text,
        modifier = modifier,
        gradientEdgeColor = gradientEdgeColor,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        style = style,
        fadeWidth = fadeWidth
    )
}

@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    gradientEdgeColor: Color = Color.White, // no requerido para la máscara, queda por compatibilidad
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    fadeWidth: Dp = 10.dp
) {
    // createText aplica padding start = fadeWidth para que el texto "en reposo" empiece a 10.dp
    val createText = @Composable { localModifier: Modifier ->
        Text(
            text = text,
            textAlign = textAlign,
            modifier = localModifier,
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = 1,
            onTextLayout = onTextLayout,
            style = style,
        )
    }

    var offset by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val textLayoutInfoState =
        remember { androidx.compose.runtime.mutableStateOf<TextLayoutInfo?>(null) }

    LaunchedEffect(textLayoutInfoState.value) {
        val textLayoutInfo = textLayoutInfoState.value ?: return@LaunchedEffect
        if (textLayoutInfo.textWidth <= textLayoutInfo.containerWidth) return@LaunchedEffect
        val duration = 7500 * textLayoutInfo.textWidth / textLayoutInfo.containerWidth
        val delayMs = 1000L

        do {
            val animation = TargetBasedAnimation(
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = duration,
                        delayMillis = 1000,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                typeConverter = Int.VectorConverter,
                initialValue = 0,
                targetValue = -textLayoutInfo.textWidth
            )
            val startTime = withFrameNanos { it }
            do {
                val playTime = withFrameNanos { it } - startTime
                offset = (animation.getValueFromNanos(playTime))
            } while (!animation.isFinishedFromNanos(playTime))
            delay(delayMs)
        } while (true)
    }

    // IMPORTANTE: aplicamos la máscara con drawWithContent en el modifier del SubcomposeLayout
    SubcomposeLayout(
        modifier = modifier
            .clipToBounds()
            .graphicsLayer {
                alpha = 0.99f
            } // forzar layer para que BlendMode funcione consistentemente
            .drawWithContent {
                drawContent()

                if (size.width > 0f) {
                    val fadeWidthPx = fadeWidth.toPx()
                    val fadeRatio = (fadeWidthPx / size.width).coerceIn(0f, 0.5f)

                    if (fadeRatio == 0f) return@drawWithContent

                    val brush = Brush.horizontalGradient(
                        // Invertimos los colores: transparente en los bordes, opaco en el centro.
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            fadeRatio to Color.Black,
                            1f - fadeRatio to Color.Black,
                            1f to Color.Transparent
                        )
                    )

                    drawRect(brush = brush, blendMode = BlendMode.DstIn)
                }
            }
    ) { constraints ->
        val infiniteWidthConstraints = constraints.copy(maxWidth = Int.MAX_VALUE)

        // Medimos el texto principal **con el padding start = fadeWidth** para que arranque a 10.dp
        var mainText = subcompose(MarqueeLayers.MainText) {
            // aplicamos padding start al modifier usado para el texto
            createText(textModifier
                .width(infiniteWidthConstraints.maxWidth.toDp())
                .then(Modifier))
        }.first().measure(infiniteWidthConstraints)

        var secondPlaceableWithOffset: Pair<Placeable, Int>? = null

        if (mainText.width <= constraints.maxWidth) {
            // cabe: renderizamos tal cual (no marquee)
            mainText = subcompose(MarqueeLayers.SecondaryText) {
                // si cabe, dejamos que ocupe el ancho disponible, pero el contenido ya tiene padding visual
                createText(textModifier.fillMaxWidth())
            }.first().measure(constraints)
            textLayoutInfoState.value = null
        } else {
            // no cabe: preparamos marquee y clon secundario
            val spacing = constraints.maxWidth * 2 / 3
            textLayoutInfoState.value = TextLayoutInfo(
                textWidth = mainText.width + spacing,
                containerWidth = constraints.maxWidth
            )
            val secondTextOffset = mainText.width + offset + spacing
            val secondTextSpace = constraints.maxWidth - secondTextOffset
            if (secondTextSpace > 0) {
                secondPlaceableWithOffset = subcompose(MarqueeLayers.SecondaryText) {
                    createText(textModifier)
                }.first().measure(infiniteWidthConstraints) to secondTextOffset
            }
        }

        layout(
            width = constraints.maxWidth,
            height = mainText.height
        ) {
            mainText.place(offset, 0)
            secondPlaceableWithOffset?.let {
                it.first.place(it.second, 0)
            }
            // NO colocamos ningún box de gradient aquí: la máscara ya lo hace en el drawWithContent
        }
    }
}

private enum class MarqueeLayers { MainText, SecondaryText }
private data class TextLayoutInfo(val textWidth: Int, val containerWidth: Int)
