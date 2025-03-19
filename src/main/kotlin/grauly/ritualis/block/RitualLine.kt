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
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.IntProperty
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldView
import net.minecraft.world.block.WireOrientation
import net.minecraft.world.tick.ScheduledTickView
import org.joml.Math.clamp
import java.awt.Color
import java.util.*
import kotlin.math.sign

class RitualLine(settings: Settings) : Block(settings), PoweredRitualComponent {

    init {
        defaultState = defaultState
            .with(CONNECTED_NORTH, false)
            .with(CONNECTED_EAST, false)
            .with(CONNECTED_SOUTH, false)
            .with(CONNECTED_WEST, false)
            .with(POWER, 0)
            .with(POWERED_FROM, Direction.NORTH)
        for (state in stateManager.states) {
            if (state.get(POWER) != 0) continue
            if (state.get(POWERED_FROM) != Direction.NORTH) continue
            SHAPES[state] = getShapeForState(state)
        }
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape {
        val lookupState = state
            .with(POWER, 0)
            .with(POWERED_FROM, Direction.NORTH)
        return SHAPES[lookupState]!!
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        val world = ctx.world
        val pos = ctx.blockPos
        return calculateConnectionState(world, pos)
    }

    override fun neighborUpdate(
        state: BlockState,
        world: World,
        pos: BlockPos,
        sourceBlock: Block,
        wireOrientation: WireOrientation?,
        notify: Boolean
    ) {
        if(world.isClient()) return
        //TODO: check placement constraints to see if I need to start breaking stuff
        if (sourceBlock !is PoweredRitualComponent) return
        Ritualis.LOGGER.info("{},{}, received update from valid powered component", pos, world.isClient())
        if (getPower(state) == 0) {
            //locate power source and lock into it
            for (direction: Direction in Direction.Type.HORIZONTAL.intersect(getConnectedDirections(state).toSet())) {
                val offsetPos = pos.offset(direction)
                val targetState = world.getBlockState(offsetPos)
                if (!targetState.isIn(ModBlockTags.RITUAL_CONNECTABLE)) continue
                if (targetState.block !is PoweredRitualComponent) continue
                val component = targetState.block as PoweredRitualComponent
                if (component.getPower(targetState, offsetPos, world) == 0) continue
                Ritualis.LOGGER.info("{},{}, updating powered_from to: {}", pos, world.isClient(), direction)
                world.setBlockState(pos, state.with(POWERED_FROM, direction))
                break
            }
        }
        //retrieve power source
        val checkTargetPos = pos.offset(state.get(POWERED_FROM))
        val checkTargetState = world.getBlockState(checkTargetPos)
        val power = getPower(state)
        var powerChange = -1
        if (checkTargetState.isIn(ModBlockTags.RITUAL_CONNECTABLE)) {
            if (checkTargetState.block !is PoweredRitualComponent) return
            val powerComponent = checkTargetState.block as PoweredRitualComponent
            val otherPower = powerComponent.getPower(checkTargetState, checkTargetPos, world)
            powerChange = (otherPower - power).sign
            Ritualis.LOGGER.info("{},{} power change by {} from {} (own) and {} (other)", pos, world.isClient(), powerChange, power, otherPower)
        }
        setPower(power + powerChange, state, pos, world)
    }

    private fun getConnectedDirections(state: BlockState): List<Direction> {
        val list: MutableList<Direction> = mutableListOf()
        if (state.get(CONNECTED_NORTH)) list.add(Direction.NORTH)
        if (state.get(CONNECTED_EAST)) list.add(Direction.EAST)
        if (state.get(CONNECTED_SOUTH)) list.add(Direction.SOUTH)
        if (state.get(CONNECTED_WEST)) list.add(Direction.WEST)
        return list
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
        val finalState = calculateConnectionState(world, pos)
        val power = getPower(state)
        return finalState.with(POWER, power)
    }

    private fun calculateConnectionState(world: WorldView, pos: BlockPos): BlockState {
        var finalState = defaultState
        finalState = finalState.with(CONNECTED_NORTH, checkConnectable(pos, world, Direction.NORTH))
        finalState = finalState.with(CONNECTED_EAST, checkConnectable(pos, world, Direction.EAST))
        finalState = finalState.with(CONNECTED_SOUTH, checkConnectable(pos, world, Direction.SOUTH))
        finalState = finalState.with(CONNECTED_WEST, checkConnectable(pos, world, Direction.WEST))
        return finalState
    }

    private fun checkConnectable(originPos: BlockPos, world: WorldView, checkDirection: Direction): Boolean {
        val targetPos = originPos.offset(checkDirection)
        val targetState = world.getBlockState(targetPos)
        if (!targetState.isIn(ModBlockTags.RITUAL_CONNECTABLE)) return false
        if (targetState.block !is PoweredRitualComponent) return true
        return (targetState.block as PoweredRitualComponent).canConnectInDirection(targetState, checkDirection.opposite)
    }

    private fun getPower(state: BlockState) = state.get(POWER)

    override fun getPower(state: BlockState, pos: BlockPos, world: World): Int = getPower(state)

    override fun setPower(power: Int, state: BlockState, pos: BlockPos, world: World): Int {
        if (!state.isOf(this)) return Int.MIN_VALUE
        val clamped = clamp(power, POWER.values.first(), POWER.values.last())
        world.setBlockState(pos, state.with(POWER, clamped))
        return clamped
    }

    override fun canConnectInDirection(state: BlockState, direction: Direction): Boolean =
        POWERED_FROM.values.contains(direction)

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(CONNECTED_NORTH, CONNECTED_EAST, CONNECTED_SOUTH, CONNECTED_WEST, POWER, POWERED_FROM)
    }

    companion object {
        val CONNECTED_NORTH: BooleanProperty = BooleanProperty.of("north")
        val CONNECTED_EAST: BooleanProperty = BooleanProperty.of("east")
        val CONNECTED_SOUTH: BooleanProperty = BooleanProperty.of("south")
        val CONNECTED_WEST: BooleanProperty = BooleanProperty.of("west")
        val POWERED_FROM: EnumProperty<Direction> = EnumProperty.of(
            "powered_from",
            Direction::class.java,
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST
        )
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
    }
}