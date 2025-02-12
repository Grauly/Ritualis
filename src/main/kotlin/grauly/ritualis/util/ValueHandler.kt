package grauly.ritualis.util

interface ValueHandler<A> {
    fun partialTick(deltaTime: Float)
    fun updateGoal(newGoal: A)
    fun getValue(): A
    fun setValue(newValue: A)
}