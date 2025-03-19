package grauly.ritualis.block

import grauly.ritualis.Ritualis
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class RitualPowerSource(settings: Settings): Block(settings), PoweredRitualComponent {

    override fun getPower(state: BlockState, pos: BlockPos, world: World): Int {
        return if(world.getReceivedStrongRedstonePower(pos) == 15) 15 else 0
    }

    override fun setPower(power: Int, state: BlockState, pos: BlockPos, world: World): Int {
        return power
    }

    override fun canConnectInDirection(state: BlockState, direction: Direction): Boolean = true

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hit: BlockHitResult
    ): ActionResult {
        if(!world.isClient()) Ritualis.LOGGER.info(" -+ update triggered with power {}", getPower(state, pos, world))
        world.updateNeighbors(pos, this)
        if(!world.isClient()) Ritualis.LOGGER.info(" -+ end of update")
        return super.onUse(state, world, pos, player, hit)
    }
}