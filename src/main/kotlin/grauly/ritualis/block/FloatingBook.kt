package grauly.ritualis.block

import com.mojang.serialization.MapCodec
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FloatingBook(settings: Settings) : BlockWithEntity(settings.noCollision().breakInstantly().noBlockBreakParticles().nonOpaque()) {

    init {
        defaultState = defaultState.with(ACTIVE, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(ACTIVE)
    }

    override fun getCodec(): MapCodec<out BlockWithEntity> = createCodec(::FloatingBook)

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = FloatingBookBlockEntity(pos, state)

    override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.INVISIBLE

    override fun onStateReplaced(
        state: BlockState,
        world: World,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean
    ) {
        if (!newState.isOf(this)) return
        if (!state.isOf(this)) return
        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity !is FloatingBookBlockEntity) return
        blockEntity.notifyStateChange(world, pos, newState)
    }

    override fun <T : BlockEntity?> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T> {
        return BlockEntityTicker { world, pos, state, blockEntity ->
            if(blockEntity !is FloatingBookBlockEntity) return@BlockEntityTicker
            (blockEntity as FloatingBookBlockEntity).tick(world, pos, state)
        }
    }

    companion object {
        val ACTIVE: BooleanProperty = BooleanProperty.of("active")
    }
}