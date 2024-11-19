package grauly.ritualis.block

import com.mojang.serialization.MapCodec
import grauly.ritualis.ModEvents
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.CandleBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
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
        if(world !is ServerWorld) return
        if(newState.block !is RitualCandle) return
        val eventToDispatch = if(newState.get(LIT) == true) ModEvents.CANDLE_IGNITE else ModEvents.CANDLE_EXTINGUISH
        world.emitGameEvent(null, eventToDispatch, pos)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {

    }
}