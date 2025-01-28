package grauly.ritualis.easing

interface EasingHandler {
    fun retrieveOffset(delta: Double, realDelta: Double): Double
    fun recalculateDelta(oldDelta: Double, immediateRealDelta: Double, oldRealDelta: Double): Double

    class IdentityEasingHandler: EasingHandler {
        override fun retrieveOffset(delta: Double, realDelta: Double): Double = delta
        override fun recalculateDelta(oldDelta: Double, immediateRealDelta: Double, oldRealDelta: Double): Double = 0.0
    }
}