package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.CustomerStage
import com.example.data.model.ThemePreference

@Immutable
data class SkywallExtendedColors(
  val brandPrimary: Color,
  val brandSecondary: Color,
  val mutedText: Color,
  val cardBorder: Color,
  val surfaceElevated: Color,
  val accentSoft: Color,
  val dangerSoft: Color,
  val stageEnquiry: Color,
  val stageQuote: Color,
  val stageConfirmed: Color,
  val stageProgress: Color,
  val stageDone: Color,
  val isDark: Boolean,
) {
  fun stageColor(stage: CustomerStage): Color =
    when (stage) {
      CustomerStage.ENQUIRY -> stageEnquiry
      CustomerStage.QUOTE -> stageQuote
      CustomerStage.CONFIRMED -> stageConfirmed
      CustomerStage.PROGRESS -> stageProgress
      CustomerStage.DONE -> stageDone
    }
}

val LocalSkywallColors = staticCompositionLocalOf {
  SkywallExtendedColors(
    brandPrimary = BrandNavy,
    brandSecondary = BrandSky,
    mutedText = LightMuted,
    cardBorder = LightOutline,
    surfaceElevated = LightSurfaceVariant,
    accentSoft = LightAccentSoft,
    dangerSoft = LightDangerSoft,
    stageEnquiry = StageEnquiryLight,
    stageQuote = StageQuoteLight,
    stageConfirmed = StageConfirmedLight,
    stageProgress = StageProgressLight,
    stageDone = StageDoneLight,
    isDark = false,
  )
}

private val SkywallLightColorScheme =
  lightColorScheme(
    primary = BrandOcean,
    onPrimary = Color.White,
    primaryContainer = LightAccentSoft,
    onPrimaryContainer = BrandNavy,
    secondary = BrandSky,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightInk,
    tertiary = StageDoneLight,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightInk,
    surface = LightSurface,
    onSurface = LightInk,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightMuted,
    outline = LightOutline,
    outlineVariant = LightOutline,
    error = LightDanger,
    onError = Color.White,
    errorContainer = LightDangerSoft,
    onErrorContainer = LightDanger,
  )

private val SkywallDarkColorScheme =
  darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkAccentInk,
    primaryContainer = DarkAccentSoft,
    onPrimaryContainer = BrandNavyLight,
    secondary = BrandSkyLight,
    onSecondary = DarkAccentInk,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkInk,
    tertiary = StageDoneDark,
    onTertiary = DarkAccentInk,
    background = DarkBackground,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMuted,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = DarkDanger,
    onError = DarkAccentInk,
    errorContainer = DarkDangerSoft,
    onErrorContainer = DarkDanger,
  )

private val SkywallShapes =
  Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
  )

@Composable
fun SkywallTheme(
  themePreference: ThemePreference = ThemePreference.AUTO,
  content: @Composable () -> Unit,
) {
  val useDarkTheme =
    when (themePreference) {
      ThemePreference.AUTO -> isSystemInDarkTheme()
      ThemePreference.LIGHT -> false
      ThemePreference.DARK -> true
    }

  val colorScheme = if (useDarkTheme) SkywallDarkColorScheme else SkywallLightColorScheme
  val extendedColors =
    if (useDarkTheme) {
      SkywallExtendedColors(
        brandPrimary = BrandNavyLight,
        brandSecondary = BrandSkyLight,
        mutedText = DarkMuted,
        cardBorder = DarkOutline,
        surfaceElevated = DarkSurfaceVariant,
        accentSoft = DarkAccentSoft,
        dangerSoft = DarkDangerSoft,
        stageEnquiry = StageEnquiryDark,
        stageQuote = StageQuoteDark,
        stageConfirmed = StageConfirmedDark,
        stageProgress = StageProgressDark,
        stageDone = StageDoneDark,
        isDark = true,
      )
    } else {
      SkywallExtendedColors(
        brandPrimary = BrandNavy,
        brandSecondary = BrandSky,
        mutedText = LightMuted,
        cardBorder = LightOutline,
        surfaceElevated = LightSurfaceVariant,
        accentSoft = LightAccentSoft,
        dangerSoft = LightDangerSoft,
        stageEnquiry = StageEnquiryLight,
        stageQuote = StageQuoteLight,
        stageConfirmed = StageConfirmedLight,
        stageProgress = StageProgressLight,
        stageDone = StageDoneLight,
        isDark = false,
      )
    }

  CompositionLocalProvider(LocalSkywallColors provides extendedColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = SkywallTypography,
      shapes = SkywallShapes,
      content = content,
    )
  }
}
