package grauly.ritualis.block

import grauly.ritualis.ModBlockTags
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.RedstoneWireBlock
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.IntProperty

class RitualLine(settings: Settings) : Block(settings) {

    init {
        defaultState = defaultState
            .with(CONNECTED_NORTH, false)
            .with(CONNECTED_EAST, false)
            .with(CONNECTED_SOUTH, false)
            .with(CONNECTED_WEST, false)
            .with(POWER, 0)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        val world = ctx.world
        val pos = ctx.blockPos
        var finalState = defaultState
        finalState = finalState.with(CONNECTED_NORTH, world.getBlockState(pos.north()).isIn(ModBlockTags.RITUAL_CONNECTABLE))
        finalState = finalState.with(CONNECTED_EAST, world.getBlockState(pos.east()).isIn(ModBlockTags.RITUAL_CONNECTABLE))
        finalState = finalState.with(CONNECTED_SOUTH, world.getBlockState(pos.south()).isIn(ModBlockTags.RITUAL_CONNECTABLE))
        finalState = finalState.with(CONNECTED_WEST, world.getBlockState(pos.west()).isIn(ModBlockTags.RITUAL_CONNECTABLE))
        return finalState
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(CONNECTED_NORTH, CONNECTED_EAST, CONNECTED_SOUTH, CONNECTED_WEST, POWER)
    }

    companion object {
        val CONNECTED_NORTH = BooleanProperty.of("north")
        val CONNECTED_EAST = BooleanProperty.of("east")
        val CONNECTED_SOUTH = BooleanProperty.of("south")
        val CONNECTED_WEST = BooleanProperty.of("west")
        val POWER = IntProperty.of("power", 0, 4)
    }
}