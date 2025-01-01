package grauly.ritualis.block

import com.mojang.serialization.MapCodec
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FloatingBook(settings: Settings) : BlockWithEntity(settings.noCollision().breakInstantly().noBlockBreakParticles().nonOpaque()) {
    override fun getCodec(): MapCodec<out BlockWithEntity> = createCodec(::FloatingBook)

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = FloatingBookBlockEntity(pos, state)

    override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.INVISIBLE

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
}