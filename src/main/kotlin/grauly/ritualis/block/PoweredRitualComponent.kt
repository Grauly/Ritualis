package grauly.ritualis.block

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

interface PoweredRitualComponent {
    fun getPower(state: BlockState, pos: BlockPos, world: World): Int
    fun setPower(power: Int, state: BlockState, pos: BlockPos, world: World): Int
    fun canConnectInDirection(state: BlockState, direction: Direction): Boolean
}