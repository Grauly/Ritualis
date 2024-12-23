package grauly.ritualis.block

import com.mojang.serialization.MapCodec
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos

class FloatingBook(settings: Settings) : BlockWithEntity(settings.noCollision().breakInstantly().noBlockBreakParticles().nonOpaque()) {
    override fun getCodec(): MapCodec<out BlockWithEntity> = createCodec(::FloatingBook)

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = FloatingBookBlockEntity(pos, state)

    override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.INVISIBLE

}