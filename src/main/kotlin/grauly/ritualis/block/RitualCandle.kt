package grauly.ritualis.block

import grauly.ritualis.ModEvents
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.CandleBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent

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
        val eventToDispatch = if (isLitCandle(newState)) ModEvents.CANDLE_IGNITE else ModEvents.CANDLE_EXTINGUISH
        world.emitGameEvent(eventToDispatch, pos, GameEvent.Emitter.of(newState))
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return RitualCandleBlockEntity(pos, state)
    }

    override fun <T : BlockEntity?> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        if (world !is ServerWorld) return null
        return BlockEntityTicker { world, pos, state, blockEntity ->
            if (world !is ServerWorld) return@BlockEntityTicker
            if (blockEntity !is RitualCandleBlockEntity) return@BlockEntityTicker
            (blockEntity as RitualCandleBlockEntity).tick()
        }
    }
}