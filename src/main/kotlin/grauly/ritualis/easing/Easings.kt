package grauly.ritualis.easing

import kotlin.math.cos
import kotlin.math.pow

object Easings {

    fun easeInOutSine(x: Double): Double{
        return (-(cos(Math.PI * x) - 1) / 2);
    }

    fun easeInOutCubic(x: Double): Double{
        return if(x < 0.5) {4.0 * x * x * x} else {1 - (-2 * x + 2).pow(3) / 2.0};
    }
}