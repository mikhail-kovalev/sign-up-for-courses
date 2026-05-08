package ru.oborg.courses.presentation.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.oborg.courses.R

private data class BackgroundStar(
    val x: Float,
    val y: Float,
    val radius: Float,
    val brightness: Float,
    val sparkle: Boolean = false
)

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    glowIntensity: Float = 0.68f,
    patternIntensity: Float = 0.62f,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val clampedGlowIntensity = glowIntensity.coerceIn(0f, 1f)
    val clampedPatternIntensity = patternIntensity.coerceIn(0f, 1f)
    val gradientColors = if (isDark) {
        listOf(
            Color(0xFF02070D),
            Color(0xFF06111A),
            Color(0xFF081923),
            Color(0xFF02070D)
        )
    } else {
        listOf(
            Color(0xFFF8F7F1),
            Color(0xFFF4F6F4),
            Color(0xFFEFF3F5),
            Color(0xFFFAF8F1)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = gradientColors))
    ) {
        BackgroundGlowLayer(
            modifier = Modifier.matchParentSize(),
            intensity = clampedGlowIntensity
        )
        BackgroundPatternIcons(intensity = clampedPatternIntensity)
        content()
    }
}

@Composable
fun PatternedScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    AppBackground(modifier = modifier, content = content)
}

@Composable
private fun BackgroundGlowLayer(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryGlow = if (isDark) Color(0xFF00D5C8) else Color(0xFFDFF3FF)
    val secondaryGlow = if (isDark) Color(0xFF3B82F6) else Color(0xFFEAE6FF)
    val tertiaryGlow = if (isDark) Color(0xFF0EA5E9) else Color(0xFFE7F7EF)
    val dotColor = if (isDark) Color(0xFFBDEFFF) else Color(0xFF8EA4AE)
    val dotAlpha = (if (isDark) 0.24f else 0.20f) * intensity
    val haloAlpha = (if (isDark) 0.08f else 0.11f) * intensity
    Canvas(modifier = modifier) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryGlow.copy(alpha = (if (isDark) 0.22f else 0.74f) * intensity), Color.Transparent),
                center = Offset(size.width * 0.18f, size.height * 0.30f),
                radius = size.minDimension * 0.62f
            ),
            radius = size.minDimension * 0.62f,
            center = Offset(size.width * 0.18f, size.height * 0.30f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondaryGlow.copy(alpha = (if (isDark) 0.18f else 0.62f) * intensity), Color.Transparent),
                center = Offset(size.width * 0.88f, size.height * 0.13f),
                radius = size.minDimension * 0.48f
            ),
            radius = size.minDimension * 0.48f,
            center = Offset(size.width * 0.88f, size.height * 0.13f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(tertiaryGlow.copy(alpha = (if (isDark) 0.14f else 0.58f) * intensity), Color.Transparent),
                center = Offset(size.width * 0.64f, size.height * 0.74f),
                radius = size.minDimension * 0.56f
            ),
            radius = size.minDimension * 0.56f,
            center = Offset(size.width * 0.64f, size.height * 0.74f)
        )

        listOf(
            Triple(0.14f, 0.19f, 1.6f),
            Triple(0.32f, 0.25f, 1.2f),
            Triple(0.58f, 0.17f, 1.4f),
            Triple(0.74f, 0.28f, 2.0f),
            Triple(0.22f, 0.43f, 1.8f),
            Triple(0.47f, 0.52f, 1.1f),
            Triple(0.82f, 0.48f, 1.5f),
            Triple(0.18f, 0.66f, 1.2f),
            Triple(0.36f, 0.75f, 1.7f),
            Triple(0.70f, 0.82f, 1.3f),
            Triple(0.90f, 0.72f, 1.6f)
        ).forEach { (x, y, radius) ->
            drawCircle(
                color = dotColor.copy(alpha = dotAlpha),
                radius = radius.dp.toPx(),
                center = Offset(size.width * x, size.height * y)
            )
            drawCircle(
                color = if (isDark) primaryGlow.copy(alpha = haloAlpha) else tertiaryGlow.copy(alpha = haloAlpha),
                radius = (radius * 7f).dp.toPx(),
                center = Offset(size.width * x, size.height * y)
            )
        }

        val starColor = if (isDark) Color(0xFFE7FBFF) else Color(0xFF7C8B93)
        listOf(
            BackgroundStar(0.09f, 0.12f, 0.7f, 0.28f),
            BackgroundStar(0.18f, 0.18f, 0.5f, 0.18f),
            BackgroundStar(0.29f, 0.14f, 0.9f, 0.42f, sparkle = true),
            BackgroundStar(0.41f, 0.11f, 0.5f, 0.20f),
            BackgroundStar(0.58f, 0.10f, 0.7f, 0.30f),
            BackgroundStar(0.73f, 0.16f, 0.6f, 0.22f),
            BackgroundStar(0.87f, 0.12f, 0.9f, 0.44f, sparkle = true),
            BackgroundStar(0.12f, 0.26f, 0.6f, 0.34f),
            BackgroundStar(0.24f, 0.30f, 0.45f, 0.18f),
            BackgroundStar(0.38f, 0.24f, 1.0f, 0.55f, sparkle = true),
            BackgroundStar(0.52f, 0.30f, 0.55f, 0.24f),
            BackgroundStar(0.67f, 0.25f, 0.75f, 0.32f),
            BackgroundStar(0.92f, 0.28f, 0.45f, 0.20f),
            BackgroundStar(0.08f, 0.39f, 0.85f, 0.40f),
            BackgroundStar(0.20f, 0.45f, 0.55f, 0.23f),
            BackgroundStar(0.34f, 0.40f, 0.7f, 0.30f),
            BackgroundStar(0.49f, 0.46f, 0.45f, 0.18f),
            BackgroundStar(0.62f, 0.41f, 0.9f, 0.48f, sparkle = true),
            BackgroundStar(0.80f, 0.39f, 0.55f, 0.26f),
            BackgroundStar(0.94f, 0.46f, 0.75f, 0.34f),
            BackgroundStar(0.13f, 0.57f, 0.5f, 0.21f),
            BackgroundStar(0.28f, 0.55f, 0.9f, 0.43f, sparkle = true),
            BackgroundStar(0.43f, 0.61f, 0.55f, 0.22f),
            BackgroundStar(0.56f, 0.57f, 0.75f, 0.32f),
            BackgroundStar(0.70f, 0.62f, 0.45f, 0.18f),
            BackgroundStar(0.86f, 0.57f, 0.8f, 0.36f),
            BackgroundStar(0.07f, 0.70f, 0.65f, 0.28f),
            BackgroundStar(0.22f, 0.74f, 0.45f, 0.20f),
            BackgroundStar(0.36f, 0.70f, 0.85f, 0.41f),
            BackgroundStar(0.51f, 0.76f, 0.6f, 0.26f),
            BackgroundStar(0.66f, 0.71f, 0.95f, 0.50f, sparkle = true),
            BackgroundStar(0.77f, 0.78f, 0.45f, 0.17f),
            BackgroundStar(0.91f, 0.73f, 0.65f, 0.29f),
            BackgroundStar(0.14f, 0.87f, 0.9f, 0.45f, sparkle = true),
            BackgroundStar(0.30f, 0.91f, 0.5f, 0.22f),
            BackgroundStar(0.46f, 0.86f, 0.7f, 0.31f),
            BackgroundStar(0.59f, 0.93f, 0.45f, 0.18f),
            BackgroundStar(0.74f, 0.89f, 0.8f, 0.38f),
            BackgroundStar(0.88f, 0.92f, 0.55f, 0.24f)
        ).forEach { star ->
            val center = Offset(size.width * star.x, size.height * star.y)
            val alpha = star.brightness * intensity * if (isDark) 0.36f else 0.24f
            val radiusPx = star.radius.dp.toPx()
            drawCircle(
                color = starColor.copy(alpha = alpha),
                radius = radiusPx,
                center = center
            )
            if (star.sparkle) {
                val arm = (star.radius * 3.4f).dp.toPx()
                drawLine(
                    color = starColor.copy(alpha = alpha * 0.45f),
                    start = Offset(center.x - arm, center.y),
                    end = Offset(center.x + arm, center.y),
                    strokeWidth = 0.65.dp.toPx()
                )
                drawLine(
                    color = starColor.copy(alpha = alpha * 0.45f),
                    start = Offset(center.x, center.y - arm),
                    end = Offset(center.x, center.y + arm),
                    strokeWidth = 0.65.dp.toPx()
                )
            }
        }
    }
}

@Composable
private fun BoxScope.BackgroundPatternIcons(intensity: Float) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val tint = MaterialTheme.colorScheme.onBackground.copy(alpha = (if (isDark) 0.82f else 0.60f) * intensity)
    val accent = MaterialTheme.colorScheme.tertiary.copy(alpha = (if (isDark) 0.58f else 0.46f) * intensity)
    PatternIcon(
        iconRes = R.drawable.ic_lucide_graduation_cap,
        tint = accent,
        modifier = Modifier.align(Alignment.TopStart).offset(34.dp, 54.dp).rotate(-4f),
        sizeDp = 64,
        alphaMultiplier = 0.92f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_book_open,
        tint = tint,
        modifier = Modifier.align(Alignment.TopEnd).offset((-46).dp, 92.dp).rotate(1f),
        sizeDp = 62,
        alphaMultiplier = 0.82f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_pencil,
        tint = tint,
        modifier = Modifier.align(Alignment.TopStart).offset(18.dp, 160.dp).rotate(-24f),
        sizeDp = 38,
        alphaMultiplier = 0.54f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_book_open,
        tint = tint,
        modifier = Modifier.align(Alignment.TopStart).offset(178.dp, 108.dp).rotate(-5f),
        sizeDp = 36,
        alphaMultiplier = 0.46f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_code_2,
        tint = tint,
        modifier = Modifier.align(Alignment.TopEnd).offset((-32).dp, 210.dp).rotate(8f),
        sizeDp = 48,
        alphaMultiplier = 0.70f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_smartphone,
        tint = tint,
        modifier = Modifier.align(Alignment.CenterStart).offset(28.dp, (-118).dp).rotate(1f),
        sizeDp = 54,
        alphaMultiplier = 0.78f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_pencil,
        tint = accent,
        modifier = Modifier.align(Alignment.CenterEnd).offset((-88).dp, (-188).dp).rotate(31f),
        sizeDp = 36,
        alphaMultiplier = 0.54f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_calendar,
        tint = accent,
        modifier = Modifier.align(Alignment.CenterEnd).offset((-20).dp, (-76).dp).rotate(2f),
        sizeDp = 52,
        alphaMultiplier = 0.74f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_mail,
        tint = tint,
        modifier = Modifier.align(Alignment.CenterStart).offset(20.dp, 74.dp).rotate(-1f),
        sizeDp = 54,
        alphaMultiplier = 0.76f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_message_circle,
        tint = tint,
        modifier = Modifier.align(Alignment.CenterEnd).offset((-54).dp, 84.dp).rotate(1f),
        sizeDp = 52,
        alphaMultiplier = 0.72f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_book_open,
        tint = accent,
        modifier = Modifier.align(Alignment.CenterStart).offset(126.dp, 140.dp).rotate(7f),
        sizeDp = 42,
        alphaMultiplier = 0.48f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_pencil,
        tint = tint,
        modifier = Modifier.align(Alignment.BottomStart).offset(58.dp, (-170).dp).rotate(-14f),
        sizeDp = 48,
        alphaMultiplier = 0.74f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_graduation_cap,
        tint = tint,
        modifier = Modifier.align(Alignment.BottomEnd).offset((-70).dp, (-246).dp).rotate(5f),
        sizeDp = 46,
        alphaMultiplier = 0.44f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_database,
        tint = tint,
        modifier = Modifier.align(Alignment.BottomEnd).offset((-54).dp, (-150).dp),
        sizeDp = 48,
        alphaMultiplier = 0.68f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_server,
        tint = accent,
        modifier = Modifier.align(Alignment.BottomEnd).offset((-122).dp, (-34).dp).rotate(1f),
        sizeDp = 46,
        alphaMultiplier = 0.64f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_check,
        tint = tint,
        modifier = Modifier.align(Alignment.BottomStart).offset(136.dp, (-46).dp),
        sizeDp = 42,
        alphaMultiplier = 0.50f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_star,
        tint = accent,
        modifier = Modifier.align(Alignment.TopStart).offset(158.dp, 196.dp).rotate(12f),
        sizeDp = 34,
        alphaMultiplier = 0.54f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_star,
        tint = tint,
        modifier = Modifier.align(Alignment.BottomStart).offset(210.dp, (-102).dp).rotate(-10f),
        sizeDp = 24,
        alphaMultiplier = 0.38f
    )
    PatternIcon(
        iconRes = R.drawable.ic_lucide_pencil,
        tint = tint,
        modifier = Modifier.align(Alignment.BottomEnd).offset((-168).dp, (-98).dp).rotate(-24f),
        sizeDp = 32,
        alphaMultiplier = 0.42f
    )
}

@Composable
private fun PatternIcon(
    iconRes: Int,
    tint: Color,
    modifier: Modifier,
    sizeDp: Int,
    alphaMultiplier: Float = 1f
) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = modifier
            .size(sizeDp.dp)
            .alpha(0.085f * alphaMultiplier),
        tint = tint
    )
}
