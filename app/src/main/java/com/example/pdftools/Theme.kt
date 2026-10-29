package com.example.pdftools

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Blue = Color(0xFF2563EB)
val Ink = Color(0xFF13234A)
val Muted = Color(0xFF6E7D98)
val CanvasColor = Color(0xFFF5F8FF)
val Line = Color(0xFFDDE8F7)

@Composable
fun PdfTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Blue, onPrimary = Color.White, primaryContainer = Color(0xFFE8F1FF), onPrimaryContainer = Ink,
            secondary = Blue, onSecondary = Color.White, secondaryContainer = Color(0xFFE8F1FF), onSecondaryContainer = Blue,
            tertiary = Blue, tertiaryContainer = Color(0xFFE8F1FF), background = CanvasColor, onBackground = Ink,
            surface = Color.White, onSurface = Ink, onSurfaceVariant = Muted, surfaceVariant = CanvasColor,
            surfaceContainer = Color.White, surfaceContainerLow = Color.White, surfaceContainerHigh = Color.White,
            surfaceContainerHighest = CanvasColor, outline = Color(0xFFB9CBE4), outlineVariant = Line,
            surfaceTint = Blue
        ),
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(18.dp), large = RoundedCornerShape(22.dp)),
        typography = Typography(
            headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp),
            headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
            titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
            titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
            bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 23.sp),
            bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
            bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
            labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
            labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
        ), content = content
    )
}

