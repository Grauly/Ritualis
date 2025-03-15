package grauly.ritualis.block

import com.google.common.collect.ImmutableMap
import grauly.ritualis.ModBlockTags
import grauly.ritualis.Ritualis
import grauly.ritualis.util.OkLabColorSpace
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.IntProperty
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.WorldView
import net.minecraft.world.tick.ScheduledTickView
import java.awt.Color
import java.util.*

class RitualLine(settings: Settings) : Block(settings) {

    init {
        defaultState = defaultState
            .with(CONNECTED_NORTH, false)
            .with(CONNECTED_EAST, false)
            .with(CONNECTED_SOUTH, false)
            .with(CONNECTED_WEST, false)
            .with(POWER, 0)
        for (state in stateManager.states) {
            if (state.get(POWER) != 0) continue
            SHAPES[state] = getShapeForState(state)
        }
    }

    private fun getShapeForState(state: BlockState): VoxelShape {
        val neededShapes: MutableList<VoxelShape> = mutableListOf()
        if (state.get(CONNECTED_NORTH)) neededShapes.add(CONNECTOR_SHAPES[Direction.NORTH]!!)
        if (state.get(CONNECTED_EAST)) neededShapes.add(CONNECTOR_SHAPES[Direction.EAST]!!)
        if (state.get(CONNECTED_SOUTH)) neededShapes.add(CONNECTOR_SHAPES[Direction.SOUTH]!!)
        if (state.get(CONNECTED_WEST)) neededShapes.add(CONNECTOR_SHAPES[Direction.WEST]!!)
        if (state.get(CONNECTED_NORTH) && state.get(CONNECTED_EAST))
            neededShapes.add(createCuboidShape(11.0, 0.0, 3.0, 13.0, 1.0, 5.0))
        if (state.get(CONNECTED_EAST) && state.get(CONNECTED_SOUTH))
            neededShapes.add(createCuboidShape(11.0, 0.0, 11.0, 13.0, 1.0, 13.0))
        if (state.get(CONNECTED_SOUTH) && state.get(CONNECTED_WEST))
            neededShapes.add(createCuboidShape(3.0, 0.0, 11.0, 5.0, 1.0, 13.0))
        if (state.get(CONNECTED_WEST) && state.get(CONNECTED_NORTH))
            neededShapes.add(createCuboidShape(3.0, 0.0, 3.0, 5.0, 1.0, 5.0))
        var finalVoxelShape = DOT_SHAPE
        for (connectionShape in neededShapes) {
            finalVoxelShape = VoxelShapes.union(finalVoxelShape, connectionShape)
        }
        return finalVoxelShape
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape {
        val lookupState = state.with(POWER, 0)
        return SHAPES[lookupState]!!
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        val world = ctx.world
        val pos = ctx.blockPos
        return calculateConnectionState(world, pos)
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        world: WorldView,
        tickView: ScheduledTickView,
        pos: BlockPos,
        direction: Direction,
        neighborPos: BlockPos,
        neighborState: BlockState,
        random: Random
    ): BlockState {
        var finalState = calculateConnectionState(world, pos)
        finalState = finalState.with(POWER, state.get(POWER))
        return finalState
    }

    private fun calculateConnectionState(world: WorldView, pos: BlockPos): BlockState {
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

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(CONNECTED_NORTH, CONNECTED_EAST, CONNECTED_SOUTH, CONNECTED_WEST, POWER)
    }

    companion object {
        val CONNECTED_NORTH: BooleanProperty = BooleanProperty.of("north")
        val CONNECTED_EAST: BooleanProperty = BooleanProperty.of("east")
        val CONNECTED_SOUTH: BooleanProperty = BooleanProperty.of("south")
        val CONNECTED_WEST: BooleanProperty = BooleanProperty.of("west")
        val POWER: IntProperty = IntProperty.of("power", 0, 4)
        private val START_COLOR: Color = Color(59, 0, 91)
        private val END_COLOR: Color = Color(241, 42, 252)
        val COLORS = generatePallete()

        val DOT_SHAPE: VoxelShape = createCuboidShape(5.0, 0.0, 5.0, 11.0, 1.0, 11.0)
        val CONNECTOR_SHAPES: EnumMap<Direction, VoxelShape> = EnumMap(
            ImmutableMap.of(
                Direction.NORTH, createCuboidShape(5.0, 0.0, 0.0, 11.0, 1.0, 5.0),
                Direction.EAST, createCuboidShape(11.0, 0.0, 5.0, 16.0, 1.0, 11.0),
                Direction.SOUTH, createCuboidShape(5.0, 0.0, 11.0, 11.0, 1.0, 16.0),
                Direction.WEST, createCuboidShape(0.0, 0.0, 5.0, 5.0, 1.0, 11.0)
            )
        )

        var SHAPES: HashMap<BlockState, VoxelShape> = HashMap()

        private fun generatePallete(): IntArray {
            val valueAmount = POWER.values.last()
            val array = IntArray(valueAmount + 1)
            for (i: Int in 0..valueAmount) {
                array[i] = OkLabColorSpace.interpolate(START_COLOR, END_COLOR, i / valueAmount.toDouble()).rgb
            }
            return array
        }
    }
}