package grauly.ritualis.block

import grauly.ritualis.ModBlockEntities
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FloatingBookBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(
    ModBlockEntities.FLOATING_BOOK_ENTITY,
    pos,
    state
) {
    var ticks = 0
    fun tick(world: World, pos: BlockPos, state: BlockState) {
        ticks++
    }
}