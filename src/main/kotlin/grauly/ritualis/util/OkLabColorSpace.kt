package grauly.ritualis.util

import net.minecraft.util.math.MathHelper
import java.awt.Color
import kotlin.math.cbrt
import kotlin.math.round

object OkLabColorSpace : ColorSpace {
    data class Lab(
        val L: Double,
        val a: Double,
        val b: Double,
        val alpha: Double = 0.0
    )

    override fun interpolate(a: Color, b: Color, delta: Double): Color {
        val aLab = RGBToLab(a)
        val bLab = RGBToLab(b)
        val resultLab = Lab(
            MathHelper.lerp(delta, aLab.L, bLab.L),
            MathHelper.lerp(delta, aLab.a, bLab.a),
            MathHelper.lerp(delta, aLab.b, bLab.b),
            MathHelper.lerp(delta, aLab.alpha, bLab.alpha)
        )
        return LabToRGB(resultLab)
    }

    //https://bottosson.github.io/posts/oklab/
    private fun RGBToLab(c: Color): Lab {
        val l: Double = 0.4122214708 * c.red / 255.0 + 0.5363325363 * c.green / 255.0 + 0.0514459929 * c.blue / 255.0
        val m: Double = 0.2119034982 * c.red / 255.0 + 0.6806995451 * c.green / 255.0 + 0.1073969566 * c.blue / 255.0
        val s: Double = 0.0883024619 * c.red / 255.0 + 0.2817188376 * c.green / 255.0 + 0.6299787005 * c.blue / 255.0

        val l_: Double = cbrt(l)
        val m_: Double = cbrt(m)
        val s_: Double = cbrt(s)

        return Lab(
            0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_,
            1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_,
            0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_,
            c.alpha / 255.0
        )
    }

    private fun LabToRGB(c: Lab): Color {
        val l_: Double = c.L + 0.3963377774 * c.a + 0.2158037573 * c.b
        val m_: Double = c.L - 0.1055613458 * c.a - 0.0638541728 * c.b
        val s_: Double = c.L - 0.0894841775 * c.a - 1.2914855480 * c.b

        val l = l_ * l_ * l_
        val m = m_ * m_ * m_
        val s = s_ * s_ * s_

        return Color(
            round((+4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s) * 255).toInt(),
            round((-1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s) * 255).toInt(),
            round((-0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s) * 255).toInt(),
            round(c.alpha * 255).toInt()
        )
    }


}