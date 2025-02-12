package grauly.ritualis.easing

import kotlin.math.cos

class SimpleSinEasing: EasingHandler {
    override fun retrieveOffset(delta: Double, realDelta: Double): Double = Easings.easeInOutSine(delta)

    override fun recalculateDelta(oldDelta: Double, immediateRealDelta: Double, oldRealDelta: Double): Double = 0.0
}