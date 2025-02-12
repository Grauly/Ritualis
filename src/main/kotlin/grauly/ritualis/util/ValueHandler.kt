package grauly.ritualis.util

interface ValueHandler<A> {
    fun partialTick(timePassedTicks: Float)
    fun updateGoal(newGoal: A)
    fun getValue(): A
    fun setValue(newValue: A)
}