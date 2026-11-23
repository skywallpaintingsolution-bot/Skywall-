package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val MuktaMalarFamily = FontFamily(
  Font(R.font.mukta_malar, FontWeight.Normal),
  Font(R.font.mukta_malar, FontWeight.Medium),
  Font(R.font.mukta_malar, FontWeight.SemiBold),
  Font(R.font.mukta_malar, FontWeight.Bold),
  Font(R.font.mukta_malar, FontWeight.ExtraBold),
)

val NotoSansTamilFamily = FontFamily(
  Font(R.font.noto_sans_tamil, FontWeight.Normal),
  Font(R.font.noto_sans_tamil, FontWeight.Medium),
  Font(R.font.noto_sans_tamil, FontWeight.SemiBold),
  Font(R.font.noto_sans_tamil, FontWeight.Bold),
)

val MontserratFamily = FontFamily(
  Font(R.font.montserrat, FontWeight.SemiBold),
  Font(R.font.montserrat, FontWeight.Bold),
  Font(R.font.montserrat, FontWeight.ExtraBold),
)

val SkywallTypography =
  Typography(
    displaySmall =
      TextStyle(
        fontFamily = MuktaMalarFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
      ),
    headlineMedium =
      TextStyle(
        fontFamily = MuktaMalarFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
      ),
    headlineSmall =
      TextStyle(
        fontFamily = MuktaMalarFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
      ),
    titleLarge =
      TextStyle(
        fontFamily = MuktaMalarFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
      ),
    titleMedium =
      TextStyle(
        fontFamily = MuktaMalarFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
      ),
    titleSmall =
      TextStyle(
        fontFamily = NotoSansTamilFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
      ),
    bodyLarge =
      TextStyle(
        fontFamily = NotoSansTamilFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp,
      ),
    bodyMedium =
      TextStyle(
        fontFamily = NotoSansTamilFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
      ),
    bodySmall =
      TextStyle(
        fontFamily = NotoSansTamilFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 18.sp,
      ),
    labelLarge =
      TextStyle(
        fontFamily = NotoSansTamilFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
      ),
    labelMedium =
      TextStyle(
        fontFamily = NotoSansTamilFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.5.sp,
        lineHeight = 16.sp,
      ),
    labelSmall =
      TextStyle(
        fontFamily = NotoSansTamilFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = 15.sp,
      ),
  )
