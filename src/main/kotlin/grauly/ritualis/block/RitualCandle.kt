package grauly.ritualis.block

import grauly.ritualis.ModEvents
import grauly.ritualis.Ritualis
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.CandleBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

class RitualCandle(settings: Settings?) : CandleBlock(settings), BlockEntityProvider {

    override fun onStateReplaced(
        state: BlockState,
        world: World,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean
    ) {
        super.onStateReplaced(state, world, pos, newState, moved)
        if (world !is ServerWorld) return
        if (newState.block !is RitualCandle) return
        if (isLitCandle(state) == isLitCandle(newState)) return
        Ritualis.LOGGER.info("$pos detected state change: $state -> $newState")
        val eventToDispatch = if (isLitCandle(newState)) ModEvents.CANDLE_IGNITE else ModEvents.CANDLE_EXTINGUISH
        world.emitGameEvent(null, eventToDispatch, pos)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return RitualCandleBlockEntity(pos, state)
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        super.scheduledTick(state, world, pos, random)
        (world.getBlockEntity(pos) as RitualCandleBlockEntity).scheduledTick()
    }
}