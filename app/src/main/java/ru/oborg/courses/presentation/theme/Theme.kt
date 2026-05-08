package ru.oborg.courses.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.oborg.courses.R

private val LightColors = lightColorScheme(
    primary = Color(0xFF18231D),
    onPrimary = Color(0xFFF9F7EC),
    primaryContainer = Color(0xFFDDF6E9),
    onPrimaryContainer = Color(0xFF10241B),
    secondary = Color(0xFFC4F000),
    onSecondary = Color(0xFF172000),
    secondaryContainer = Color(0xFFEFFFAC),
    onSecondaryContainer = Color(0xFF232B00),
    tertiary = Color(0xFF7C7466),
    onTertiary = Color.White,
    background = Color(0xFFF6F4EA),
    onBackground = Color(0xFF171811),
    surface = Color(0xFFFFFEF7),
    onSurface = Color(0xFF171811),
    surfaceVariant = Color(0xFFECEBE0),
    onSurfaceVariant = Color(0xFF3F443C),
    outline = Color(0xFFC9C8BB),
    error = Color(0xFFB3261E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF4F4EF),
    onPrimary = Color(0xFF121310),
    primaryContainer = Color(0xFF2B2E29),
    onPrimaryContainer = Color(0xFFF4F4EF),
    secondary = Color(0xFFC4F000),
    onSecondary = Color(0xFF172000),
    secondaryContainer = Color(0xFF344100),
    onSecondaryContainer = Color(0xFFEFFFAC),
    tertiary = Color(0xFFB8B1C2),
    onTertiary = Color(0xFF23202B),
    background = Color(0xFF070807),
    onBackground = Color(0xFFF0F0EA),
    surface = Color(0xFF111211),
    onSurface = Color(0xFFF0F0EA),
    surfaceVariant = Color(0xFF20211F),
    onSurfaceVariant = Color(0xFFC8C8BE),
    outline = Color(0xFF343632),
    error = Color(0xFFFFB4AB)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

private val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

private val ComfortaaFontFamily = FontFamily(
    Font(R.font.comfortaa_regular, FontWeight.Normal),
    Font(R.font.comfortaa_medium, FontWeight.Medium),
    Font(R.font.comfortaa_semibold, FontWeight.SemiBold),
    Font(R.font.comfortaa_bold, FontWeight.Bold)
)

private val BaseTypography = Typography()

private val AppTypography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = ComfortaaFontFamily, letterSpacing = 0.sp),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = ComfortaaFontFamily, letterSpacing = 0.sp),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = ComfortaaFontFamily, letterSpacing = 0.sp),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = ComfortaaFontFamily, letterSpacing = 0.sp),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = ComfortaaFontFamily, fontSize = 27.sp, lineHeight = 34.sp, letterSpacing = 0.sp),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = ComfortaaFontFamily, fontSize = 22.sp, lineHeight = 29.sp, letterSpacing = 0.sp),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = ComfortaaFontFamily, fontSize = 20.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = ComfortaaFontFamily, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = ComfortaaFontFamily, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = InterFontFamily, fontSize = 16.sp, lineHeight = 23.sp, letterSpacing = 0.sp),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = InterFontFamily, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = InterFontFamily, fontSize = 12.sp, lineHeight = 17.sp, letterSpacing = 0.sp),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = InterFontFamily, letterSpacing = 0.sp),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = InterFontFamily, letterSpacing = 0.sp),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = InterFontFamily, letterSpacing = 0.sp)
)

@Composable
fun ObOrgCoursesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors: ColorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        shapes = AppShapes,
        typography = AppTypography,
        content = content
    )
}
