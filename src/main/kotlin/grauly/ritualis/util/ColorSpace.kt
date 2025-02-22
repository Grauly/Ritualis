package grauly.ritualis.util

import java.awt.Color

interface ColorSpace {
    fun interpolate(a: Color, b: Color, delta: Double): Color
}