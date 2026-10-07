package de.morhenn.seven_wonders_calculator.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.morhenn.seven_wonders_calculator.domain.Category
import de.morhenn.seven_wonders_calculator.ui.theme.Brand
import de.morhenn.seven_wonders_calculator.ui.theme.OverlineStyle
import de.morhenn.seven_wonders_calculator.ui.theme.ScoreStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Repeating Greek key (meander) band, the classic ornament of antiquity. */
@Composable
fun MeanderBand(color: Color, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    Canvas(modifier.height(height).clipToBounds()) {
        val h = size.height
        val unit = h
        val stroke = h / 5f
        val path = Path()
        var x = 0f
        path.moveTo(0f, h - stroke / 2)
        // Each motif: up, right, down, left (inward), down, right to the next motif.
        while (x < size.width) {
            val s = stroke / 2
            path.lineTo(x + s, h - s)
            path.lineTo(x + s, s)
            path.lineTo(x + unit * 0.8f, s)
            path.lineTo(x + unit * 0.8f, h * 0.62f)
            path.lineTo(x + unit * 0.45f, h * 0.62f)
            path.lineTo(x + unit * 0.45f, h - s)
            path.lineTo(x + unit * 1.25f, h - s)
            x += unit * 1.25f
        }
        drawPath(path, color, style = Stroke(width = stroke, join = StrokeJoin.Miter))
    }
}

/** Spaced small-caps header followed by a meander ornament. */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text.uppercase(), style = OverlineStyle, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(12.dp))
        MeanderBand(MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f), Modifier.weight(1f), height = 7.dp)
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

/** Dusk over the ancient world: pyramids, the lighthouse and a temple against a setting sun. */
@Composable
fun SkylineBackdrop(modifier: Modifier = Modifier) {
    val stars = remember {
        val random = Random(7)
        List(40) { Triple(random.nextFloat(), random.nextFloat() * 0.5f, random.nextFloat()) }
    }
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRect(
            Brush.verticalGradient(
                0f to Color(0xFF081127),
                0.55f to Brand.Lapis,
                0.85f to Color(0xFF8C5A4A),
                1f to Color(0xFFD9925A),
            )
        )
        stars.forEach { (sx, sy, twinkle) ->
            drawCircle(Color.White.copy(alpha = 0.25f + 0.5f * twinkle), radius = 1.dp.toPx() * (0.6f + twinkle), center = Offset(sx * w, sy * h))
        }
        val groundY = h * 0.9f
        val sun = Offset(w * 0.8f, h * 0.66f)
        val sunR = h * 0.12f
        drawCircle(Brush.radialGradient(listOf(Brand.Gold.copy(alpha = 0.55f), Color.Transparent), sun, sunR * 3.2f), sunR * 3.2f, sun)
        drawCircle(Brush.verticalGradient(listOf(Brand.GoldLight, Brand.Gold), sun.y - sunR, sun.y + sunR), sunR, sun)

        val far = Color(0xFF16233F)
        val near = Color(0xFF0A1222)
        // Pyramids
        drawPath(triangle(w * -0.02f, w * 0.42f, w * 0.2f, h * 0.38f, groundY), far)
        drawPath(triangle(w * 0.24f, w * 0.5f, w * 0.37f, h * 0.6f, groundY), near)
        // Lighthouse of Alexandria with its fire
        val lx = w * 0.6f
        val top = h * 0.5f
        drawPath(
            Path().apply {
                moveTo(lx - w * 0.04f, groundY); lineTo(lx - w * 0.022f, top + h * 0.06f)
                lineTo(lx + w * 0.022f, top + h * 0.06f); lineTo(lx + w * 0.04f, groundY); close()
                addRect(androidx.compose.ui.geometry.Rect(lx - w * 0.03f, top + h * 0.03f, lx + w * 0.03f, top + h * 0.07f))
                addRect(androidx.compose.ui.geometry.Rect(lx - w * 0.012f, top, lx + w * 0.012f, top + h * 0.04f))
            },
            near,
        )
        val fire = Offset(lx, top - h * 0.015f)
        drawCircle(Brush.radialGradient(listOf(Brand.GoldLight.copy(alpha = 0.8f), Color.Transparent), fire, h * 0.07f), h * 0.07f, fire)
        drawCircle(Brand.GoldLight, h * 0.016f, fire)
        // Temple in front of the sun
        drawTemple(left = w * 0.66f, right = w * 0.97f, groundY = groundY, height = h * 0.3f, color = near)
        drawRect(near, Offset(0f, groundY), Size(w, h - groundY))
    }
}

private fun triangle(left: Float, right: Float, apexX: Float, apexY: Float, baseY: Float) = Path().apply {
    moveTo(left, baseY); lineTo(apexX, apexY); lineTo(right, baseY); close()
}

private fun DrawScope.drawTemple(left: Float, right: Float, groundY: Float, height: Float, color: Color) {
    val width = right - left
    val stepH = height * 0.08f
    drawRect(color, Offset(left - width * 0.04f, groundY - stepH), Size(width * 1.08f, stepH))
    drawRect(color, Offset(left, groundY - stepH * 2), Size(width, stepH))
    val columnsTop = groundY - height * 0.72f
    val columns = 6
    val columnW = width / (columns * 2f - 1f)
    for (i in 0 until columns) {
        drawRect(color, Offset(left + i * columnW * 2f, columnsTop), Size(columnW, groundY - stepH * 2 - columnsTop))
    }
    val beamH = height * 0.1f
    drawRect(color, Offset(left - width * 0.02f, columnsTop - beamH), Size(width * 1.04f, beamH))
    drawPath(triangle(left - width * 0.04f, right + width * 0.04f, left + width / 2, groundY - height, columnsTop - beamH), color)
}

/** Two laurel branches curving around a center, open at the top: the victor's wreath. */
@Composable
fun LaurelWreath(modifier: Modifier = Modifier, color: Color = Brand.Gold) {
    Canvas(modifier) {
        val c = center
        val r = size.minDimension / 2f * 0.76f
        val leafL = r * 0.36f
        val leafW = leafL * 0.42f
        val brush = Brush.linearGradient(listOf(Brand.GoldLight, color, Brand.GoldDeep))
        for (side in listOf(-1f, 1f)) {
            // Left branch runs from the bottom up the left side; right branch mirrors it.
            val start = 100f
            val end = 232f
            drawArc(
                brush = brush,
                startAngle = if (side < 0) start else 180f - end,
                sweepAngle = end - start,
                useCenter = false,
                topLeft = Offset(c.x - r, c.y - r),
                size = Size(2 * r, 2 * r),
                style = Stroke(width = r * 0.035f, cap = StrokeCap.Round),
            )
            val leaves = 8
            for (i in 0 until leaves) {
                val t = i / (leaves - 1f)
                val deg = start + (end - start) * t
                val angle = if (side < 0) deg else 180f - deg
                val rad = angle * PI.toFloat() / 180f
                for (outer in listOf(true, false)) {
                    val radius = if (outer) r * 1.08f else r * 0.92f
                    val p = Offset(c.x + radius * cos(rad), c.y + radius * sin(rad))
                    // Leaves point along the branch toward its tip, splayed outwards and inwards.
                    val tangent = angle + 90f * -side
                    val splay = if (outer) -28f else 28f
                    val scale = 1f - t * 0.35f
                    rotate(tangent + splay * -side, pivot = p) {
                        drawOval(
                            brush = brush,
                            topLeft = Offset(p.x - leafL * scale / 2, p.y - leafW * scale / 2),
                            size = Size(leafL * scale, leafW * scale),
                        )
                    }
                }
            }
        }
    }
}

private data class Particle(
    val x: Float, val delay: Float, val speed: Float, val sway: Float, val phase: Float,
    val spin: Float, val color: Color, val w: Float, val h: Float,
)

/** A short burst of confetti in the category colors. Plays once. */
@Composable
fun ConfettiBurst(modifier: Modifier = Modifier, particleCount: Int = 90) {
    val progress = remember { Animatable(0f) }
    val particles = remember {
        val random = Random(System.nanoTime())
        val colors = Category.entries.map { it.color }.filter { it != Category.CITIES.color } + Brand.Gold
        List(particleCount) {
            Particle(
                x = random.nextFloat(), delay = random.nextFloat() * 0.25f, speed = 0.7f + random.nextFloat() * 0.6f,
                sway = 10f + random.nextFloat() * 30f, phase = random.nextFloat() * 6f, spin = random.nextFloat() * 720f - 360f,
                color = colors[random.nextInt(colors.size)], w = 6f + random.nextFloat() * 6f, h = 10f + random.nextFloat() * 8f,
            )
        }
    }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(2600, easing = LinearEasing)) }
    if (progress.value >= 1f) return
    Canvas(modifier) {
        val t = progress.value
        particles.forEach { p ->
            val local = ((t - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (local <= 0f) return@forEach
            val y = -40f + local * size.height * 1.15f * p.speed
            val x = p.x * size.width + sin(local * 8f + p.phase) * p.sway.dp.toPx() / 3f
            val alpha = if (local > 0.8f) (1f - local) / 0.2f else 1f
            rotate(p.spin * local, pivot = Offset(x, y)) {
                drawRect(p.color.copy(alpha = alpha), Offset(x - p.w / 2, y - p.h / 2), Size(p.w.dp.toPx() / 2.5f, p.h.dp.toPx() / 2.5f))
            }
        }
    }
}

fun medalColors(rank: Int): List<Color>? = when (rank) {
    1 -> listOf(Brand.GoldLight, Brand.Gold, Brand.GoldDeep)
    2 -> listOf(Color.White, Brand.Silver, Brand.SilverDeep)
    3 -> listOf(Color(0xFFF2C29A), Brand.Bronze, Brand.BronzeDeep)
    else -> null
}

/** Rank as a metal medal for the top three, a plain disc otherwise. */
@Composable
fun RankMedal(rank: Int, modifier: Modifier = Modifier, size: Dp = 30.dp) {
    val colors = medalColors(rank)
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (colors != null) Brush.linearGradient(colors)
                else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.surfaceContainerHighest))
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            rank.toString(),
            style = ScoreStyle,
            fontSize = (size.value * 0.48f).sp,
            color = if (colors != null) Color(0xFF2A1C00) else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** How a total is made up: one segment per category, proportional to its points. */
@Composable
fun CompositionBar(parts: List<Pair<Category, Int>>, modifier: Modifier = Modifier, height: Dp = 10.dp) {
    val positive = parts.filter { it.second > 0 }
    Row(
        modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(height / 2)).background(MaterialTheme.colorScheme.surfaceContainerHighest),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        positive.forEach { (category, value) ->
            Box(Modifier.weight(value.toFloat()).fillMaxHeight().background(category.barColor))
        }
    }
}

/** Ivory leader cards would vanish on the bar track, so they get a deeper tone there. */
private val Category.barColor: Color
    get() = if (this == Category.LEADERS) Color(0xFFBFB396) else color

/** Circular progress for win rates. */
@Composable
fun RingProgress(progress: Float, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary, stroke: Dp = 6.dp) {
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Canvas(modifier.padding(stroke / 2)) {
        val s = Stroke(stroke.toPx(), cap = StrokeCap.Round)
        drawArc(track, 0f, 360f, false, style = s)
        if (progress > 0f) drawArc(color, -90f, 360f * progress.coerceIn(0f, 1f), false, style = s)
    }
}
