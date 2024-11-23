package grauly.ritualis.block

import grauly.ritualis.ModBlockEntities
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.event.listener.GameEventListener

class RitualCandleBlockEntity(
    private val pos: BlockPos,
    private val state: BlockState
) : BlockEntity(ModBlockEntities.RITUAL_CANDLE_ENTITY, pos, state),
    GameEventListener.Holder<CandleEventListener> {
    private val listener = CandleEventListener(pos, this)

    override fun getEventListener(): CandleEventListener = listener

    fun getCurrentState(): BlockState? = world?.getBlockState(pos)


}