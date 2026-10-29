package com.example.pdftools

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale

/** Scalable, original illustrations; no screenshot-based layouts or bundled stock artwork. */
@Composable
fun OnboardingArt(page: Int, modifier: Modifier = Modifier) {
    Canvas(modifier.aspectRatio(1f)) {
        scale(size.width / 320f, size.height / 320f, Offset.Zero) {
            drawCircle(Color(0xFFE3EEFF), 140f, Offset(160f, 160f))
            drawCircle(Color(0xFFA9CEFF), 6f, Offset(36f, 80f))
            drawCircle(Color(0xFFBCD8FF), 4f, Offset(284f, 236f))
            when (page) {
                0 -> {
                    rotate(-12f, Offset(94f, 135f)) { paper(Offset(38f, 65f), 108f, 154f) }
                    rotate(12f, Offset(229f, 135f)) { paper(Offset(176f, 65f), 108f, 154f) }
                    paper(Offset(104f, 142f), 114f, 158f)
                    arrow(Offset(152f, 102f), Offset(164f, 130f))
                }
                1 -> {
                    paper(Offset(55f, 58f), 163f, 216f)
                    val corners = listOf(Offset(43f, 48f), Offset(230f, 48f), Offset(43f, 285f), Offset(230f, 285f))
                    corners.forEachIndexed { index, corner ->
                        val x = if (index % 2 == 0) 17f else -17f
                        val y = if (index < 2) 17f else -17f
                        drawLine(Blue, corner, corner + Offset(x, 0f), 5f, StrokeCap.Round)
                        drawLine(Blue, corner, corner + Offset(0f, y), 5f, StrokeCap.Round)
                    }
                    drawRoundRect(Blue.copy(alpha = .09f), Offset(39f, 163f), Size(195f, 20f), CornerRadius(10f))
                    drawLine(Color(0xFF45A3FF), Offset(38f, 173f), Offset(235f, 173f), 3f, StrokeCap.Round)
                    paper(Offset(185f, 182f), 102f, 105f)
                }
                else -> {
                    paper(Offset(28f, 68f), 120f, 185f)
                    rotate(9f, Offset(242f, 130f)) { photo(Offset(180f, 65f), 111f, 116f) }
                    rotate(-8f, Offset(240f, 225f)) { photo(Offset(178f, 180f), 111f, 116f) }
                    arrow(Offset(153f, 133f), Offset(178f, 133f))
                    arrow(Offset(177f, 228f), Offset(150f, 228f))
                }
            }
        }
    }
}

private fun DrawScope.paper(origin: Offset, width: Float, height: Float) {
    drawRoundRect(Color(0xFFBCD3F4).copy(alpha = .3f), origin + Offset(0f, 7f), Size(width, height), CornerRadius(13f))
    drawRoundRect(Color.White, origin, Size(width, height), CornerRadius(13f))
    val fold = width * .25f
    val shape = Path().apply {
        moveTo(origin.x + width - fold, origin.y)
        lineTo(origin.x + width - fold, origin.y + fold)
        lineTo(origin.x + width, origin.y + fold)
        close()
    }
    drawPath(shape, Color(0xFFD7E7FC))
    val left = origin.x + width * .17f
    repeat(5) { row ->
        val y = origin.y + height * .36f + row * height * .105f
        drawLine(if (row == 0) Blue else Color(0xFFBFD8FA), Offset(left, y), Offset(left + width * if (row == 0) .45f else .66f, y),
            if (row == 0) 6f else 5f, StrokeCap.Round)
    }
}

private fun DrawScope.photo(origin: Offset, width: Float, height: Float) {
    drawRoundRect(Color(0xFFBCD3F4).copy(alpha = .35f), origin + Offset(0f, 6f), Size(width, height), CornerRadius(13f))
    drawRoundRect(Color.White, origin, Size(width, height), CornerRadius(13f))
    drawRoundRect(Color(0xFFE3EFFF), origin + Offset(10f, 10f), Size(width - 20f, height - 25f), CornerRadius(7f))
    drawCircle(Color(0xFFA1C7FF), 10f, origin + Offset(width * .72f, height * .3f))
    val mountains = Path().apply {
        moveTo(origin.x + 10f, origin.y + height - 15f)
        lineTo(origin.x + width * .42f, origin.y + height * .4f)
        lineTo(origin.x + width * .65f, origin.y + height * .68f)
        lineTo(origin.x + width * .78f, origin.y + height * .54f)
        lineTo(origin.x + width - 10f, origin.y + height - 15f)
        close()
    }
    drawPath(mountains, Color(0xFF6EA6FA))
}

private fun DrawScope.arrow(from: Offset, to: Offset) {
    drawLine(Blue, from, to, 5f, StrokeCap.Round)
    val horizontal = kotlin.math.abs(to.x - from.x) > kotlin.math.abs(to.y - from.y)
    val sign = if ((if (horizontal) to.x - from.x else to.y - from.y) >= 0) 1f else -1f
    val a = if (horizontal) Offset(-8f * sign, -7f) else Offset(-7f, -8f * sign)
    val b = if (horizontal) Offset(-8f * sign, 7f) else Offset(7f, -8f * sign)
    drawPath(Path().apply { moveTo(to.x + a.x, to.y + a.y); lineTo(to.x, to.y); lineTo(to.x + b.x, to.y + b.y) }, Blue,
        style = Stroke(4f, cap = StrokeCap.Round))
}

