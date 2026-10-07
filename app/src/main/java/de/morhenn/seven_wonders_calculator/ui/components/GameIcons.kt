package de.morhenn.seven_wonders_calculator.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Original icons for game concepts Material has no symbol for. Drawn on a 24×24 grid. */
object GameIcons {
    /** Stepped pyramid: wonder stages. */
    val Pyramid: ImageVector by lazy {
        icon("Pyramid") {
            moveTo(12f, 3f); lineTo(15.2f, 8.5f); horizontalLineTo(8.8f); close()
            moveTo(8.2f, 9.7f); horizontalLineTo(15.8f); lineTo(18.6f, 14.5f); horizontalLineTo(5.4f); close()
            moveTo(4.7f, 15.7f); horizontalLineTo(19.3f); lineTo(22f, 20.3f); horizontalLineTo(2f); close()
        }
    }

    /** A stack of coins next to a single coin. */
    val Coin: ImageVector by lazy {
        icon("Coin", PathFillType.EvenOdd) {
            pill(2f, 17.5f, 13f, 21.5f)
            pill(3f, 12.8f, 14f, 16.8f)
            pill(2f, 8.1f, 13f, 12.1f)
            circle(18.5f, 16.5f, 4.5f)
            circle(18.5f, 16.5f, 2.3f)
        }
    }

    val Crown: ImageVector by lazy {
        icon("Crown") {
            moveTo(3.2f, 17.5f); lineTo(2f, 7f); lineTo(7.5f, 11f); lineTo(12f, 4f)
            lineTo(16.5f, 11f); lineTo(22f, 7f); lineTo(20.8f, 17.5f); close()
            moveTo(3.2f, 19f); horizontalLineTo(20.8f); verticalLineTo(21f); horizontalLineTo(3.2f); close()
        }
    }

    /** Drawing compass, one of the three science symbols. */
    val Compass: ImageVector by lazy {
        icon("Compass") {
            circle(12f, 4.5f, 2.2f)
            moveTo(10.7f, 6.2f); lineTo(12.3f, 6.9f); lineTo(6.3f, 21.2f); lineTo(4.6f, 20.5f); close()
            moveTo(13.3f, 6.2f); lineTo(11.7f, 6.9f); lineTo(17.7f, 21.2f); lineTo(19.4f, 20.5f); close()
            moveTo(7.4f, 14.3f); horizontalLineTo(16.6f); verticalLineTo(15.9f); horizontalLineTo(7.4f); close()
        }
    }

    /** Stone tablet, one of the three science symbols. */
    val Tablet: ImageVector by lazy {
        icon("Tablet", PathFillType.EvenOdd) {
            moveTo(5f, 21f); verticalLineTo(8f)
            curveTo(5f, 4.5f, 8f, 3f, 12f, 3f)
            curveTo(16f, 3f, 19f, 4.5f, 19f, 8f)
            verticalLineTo(21f); close()
            moveTo(8f, 9f); horizontalLineTo(16f); verticalLineTo(10.6f); horizontalLineTo(8f); close()
            moveTo(8f, 12.7f); horizontalLineTo(16f); verticalLineTo(14.3f); horizontalLineTo(8f); close()
            moveTo(8f, 16.4f); horizontalLineTo(13.5f); verticalLineTo(18f); horizontalLineTo(8f); close()
        }
    }

    /** Eight-pointed star for "any symbol" wildcards. */
    val Wildcard: ImageVector by lazy {
        icon("Wildcard") {
            moveTo(12f, 2f); lineTo(14f, 8.5f); lineTo(20.5f, 6.5f); lineTo(16.5f, 12f)
            lineTo(20.5f, 17.5f); lineTo(14f, 15.5f); lineTo(12f, 22f); lineTo(10f, 15.5f)
            lineTo(3.5f, 17.5f); lineTo(7.5f, 12f); lineTo(3.5f, 6.5f); lineTo(10f, 8.5f); close()
        }
    }

    private fun icon(name: String, fillType: PathFillType = PathFillType.NonZero, block: PathBuilder.() -> Unit) =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
            .path(fill = SolidColor(Color.Black), pathFillType = fillType, pathBuilder = block)
            .build()

    private fun PathBuilder.pill(left: Float, top: Float, right: Float, bottom: Float) {
        val r = (bottom - top) / 2
        moveTo(left + r, top)
        horizontalLineTo(right - r)
        arcToRelative(r, r, 0f, false, true, 0f, 2 * r)
        horizontalLineTo(left + r)
        arcToRelative(r, r, 0f, false, true, 0f, -2 * r)
        close()
    }

    private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
        moveTo(cx - r, cy)
        arcToRelative(r, r, 0f, true, true, 2 * r, 0f)
        arcToRelative(r, r, 0f, true, true, -2 * r, 0f)
        close()
    }
}
