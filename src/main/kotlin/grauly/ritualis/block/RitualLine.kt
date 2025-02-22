package grauly.ritualis.block

import grauly.ritualis.ModBlockTags
import grauly.ritualis.Ritualis
import grauly.ritualis.util.OkLabColorSpace
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.IntProperty
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.block.WireOrientation
import java.awt.Color

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
        finalState =
            finalState.with(CONNECTED_NORTH, world.getBlockState(pos.north()).isIn(ModBlockTags.RITUAL_CONNECTABLE))
        finalState =
            finalState.with(CONNECTED_EAST, world.getBlockState(pos.east()).isIn(ModBlockTags.RITUAL_CONNECTABLE))
        finalState =
            finalState.with(CONNECTED_SOUTH, world.getBlockState(pos.south()).isIn(ModBlockTags.RITUAL_CONNECTABLE))
        finalState =
            finalState.with(CONNECTED_WEST, world.getBlockState(pos.west()).isIn(ModBlockTags.RITUAL_CONNECTABLE))
        return finalState
    }

    override fun neighborUpdate(
        state: BlockState,
        world: World,
        pos: BlockPos,
        sourceBlock: Block,
        wireOrientation: WireOrientation?,
        notify: Boolean
    ) {
        super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify)
        var finalState = defaultState
        finalState = finalState.with(POWER, state.get(POWER))
        finalState =
            finalState.with(CONNECTED_NORTH, world.getBlockState(pos.north()).isIn(ModBlockTags.RITUAL_CONNECTABLE))
        finalState =
            finalState.with(CONNECTED_EAST, world.getBlockState(pos.east()).isIn(ModBlockTags.RITUAL_CONNECTABLE))
        finalState =
            finalState.with(CONNECTED_SOUTH, world.getBlockState(pos.south()).isIn(ModBlockTags.RITUAL_CONNECTABLE))
        finalState =
            finalState.with(CONNECTED_WEST, world.getBlockState(pos.west()).isIn(ModBlockTags.RITUAL_CONNECTABLE))
        world.setBlockState(pos, finalState)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(CONNECTED_NORTH, CONNECTED_EAST, CONNECTED_SOUTH, CONNECTED_WEST, POWER)
    }

    companion object {
        val CONNECTED_NORTH: BooleanProperty = BooleanProperty.of("north")
        val CONNECTED_EAST: BooleanProperty = BooleanProperty.of("east")
        val CONNECTED_SOUTH: BooleanProperty = BooleanProperty.of("south")
        val CONNECTED_WEST: BooleanProperty = BooleanProperty.of("west")
        val POWER: IntProperty = IntProperty.of("power", 0, 4)
        private val START_COLOR: Color = Color(0x8b, 0x02, 0xd6) //0xff8b02d6.toInt()
        private val END_COLOR: Color = Color(0xcb, 0x02, 0xd6) //0xffcb02d6.toInt()
        val COLORS = generatePallete()

        private fun generatePallete(): IntArray {
            val valueAmount = POWER.values.last()
            val array = IntArray(valueAmount + 1)
            Ritualis.LOGGER.info("{}", valueAmount)
            for (i: Int in 0..valueAmount) {
                array[i] = OkLabColorSpace.interpolate(START_COLOR, END_COLOR, i / valueAmount.toDouble()).rgb
            }
            return array
        }
    }
}