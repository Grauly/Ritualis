package grauly.ritualis.event.impl

import grauly.ritualis.ModBlockTags
import grauly.ritualis.block.RitualCandleBlockEntity
import grauly.ritualis.event.SimpleDelayedEventListener
import net.minecraft.block.CandleBlock
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.GameEvent
import kotlin.math.roundToInt

abstract class CandleEventListener(
    protected val pos: BlockPos,
    protected val candleBlockEntity: RitualCandleBlockEntity
) : SimpleDelayedEventListener(false) {
    override fun isEventListenable(world: ServerWorld, emitterPosition: Vec3d, emitter: GameEvent.Emitter): Boolean {
        //if its not a block, I don't care right now
        if (emitter.affectedState == null) return true
        //is it a ritual candle?
        if (!emitter.affectedState!!.isIn(ModBlockTags.RITUAL_CANDLES)) return true
        //ok, it might say its one, but it aint got candles?
        if (emitter.affectedState!!.getOrEmpty(CandleBlock.CANDLES).isEmpty) return false
        val senderPower: Int = emitter.affectedState!!.get(CandleBlock.CANDLES) * 4
        return pos.isWithinDistance(emitterPosition, senderPower.toDouble())
    }

    override fun calculatePropagationDelay(emitterPosition: Vec3d): Int =
        pos.toCenterPos().distanceTo(emitterPosition).times(getPropagationTicksPerBlock()).roundToInt()

    override fun getCooldownTime(): Int = 20

    override fun getRange(): Int = 16
}