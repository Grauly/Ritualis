package grauly.ritualis.client.datagen

import grauly.ritualis.ModBlocks
import grauly.ritualis.Ritualis
import grauly.ritualis.block.RitualLine
import net.minecraft.block.Block
import net.minecraft.client.data.*
import net.minecraft.client.data.VariantSettings.Rotation
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import java.util.function.Consumer

object BlockModelDatagen {

    private val numbers = listOf("one", "two", "three", "four")

    fun generateBlockStateModels(blockStateModelGenerator: BlockStateModelGenerator) {
        generateCandleBlockState(ModBlocks.candles[0], "", blockStateModelGenerator.blockStateCollector)
        for (index: Int in 1..Ritualis.COLOR_ORDER.size) {
            generateCandleBlockState(
                ModBlocks.candles[index],
                "${Ritualis.COLOR_ORDER[index - 1].getName()}_",
                blockStateModelGenerator.blockStateCollector
            )
        }
        blockStateModelGenerator.registerGeneric(ModBlocks.FLOATING_BOOK)
        generateRitualLine(blockStateModelGenerator)
    }

    private fun generateRitualLine(blockStateModelGenerator: BlockStateModelGenerator) {
        val lineSegment = "block/ritual_line"
        val lineNotch = "block/ritual_notch"
        val lineDot = "block/ritual_dot"
        val ritualLine = MultipartBlockStateSupplier.create(ModBlocks.RITUAL_LINE)
            .with(BlockStateVariant.create().put(VariantSettings.MODEL, Identifier.of(Ritualis.MODID, lineDot)))

        val directions = listOf(
            RitualLine.CONNECTED_NORTH,
            RitualLine.CONNECTED_EAST,
            RitualLine.CONNECTED_SOUTH,
            RitualLine.CONNECTED_WEST
        )
        val rotations = listOf(Rotation.R0, Rotation.R90, Rotation.R180, Rotation.R270)

        val triConnections: MutableList<When> = mutableListOf()
        val negatedTriConnections: MutableList<When> = mutableListOf()
        for (i in 0..<4) {
            createRitualLinePart(
                ritualLine,
                { When.create().set(directions[i], true) },
                rotations[i],
                lineSegment
            )
            createRitualLinePart(
                ritualLine,
                { When.create().set(directions[i], true).set(directions[(i + 1) % 4], true) },
                rotations[i],
                lineNotch
            )
            triConnections.add(
                When.create()
                    .set(directions[i], true)
                    .set(directions[(i + 1) % 4], true)
                    .set(directions[(i + 2) % 4], true)
            )
            negatedTriConnections.add(
                When.anyOf(
                    When.create().setNegated(directions[i], true),
                    When.create().setNegated(directions[(i + 1) % 4], true),
                    When.create().setNegated(directions[(i + 2) % 4], true),
                )
            )
        }
        val triWhen = When.anyOf(*triConnections.toTypedArray())
        val notTriWhen = When.allOf(*negatedTriConnections.toTypedArray())
        ritualLine.with(When.allOf(triWhen, When.create().set(RitualLine.POWER, 4)), createRotationSegmentBSV(Rotation.R0, "${lineDot}_flare_tall"))
        ritualLine.with(When.allOf(notTriWhen, When.create().set(RitualLine.POWER, 4)), createRotationSegmentBSV(Rotation.R0, "${lineDot}_flare"))
        blockStateModelGenerator.blockStateCollector.accept(ritualLine)
    }

    private fun createRitualLinePart(
        supplier: MultipartBlockStateSupplier,
        condition: () -> When.PropertyCondition,
        rotation: Rotation,
        model: String
    ) {
        supplier.with(condition.invoke(), createRotationSegmentBSV(rotation, model))
        supplier.with(condition.invoke().set(RitualLine.POWER, 4), createRotationSegmentBSV(rotation, "${model}_flare"))
    }

    private fun createRotationSegmentBSV(rotation: Rotation, model: String): BlockStateVariant =
        BlockStateVariant.create()
            .put(VariantSettings.Y, rotation)
            .put(VariantSettings.UVLOCK, true)
            .put(VariantSettings.MODEL, Identifier.of(Ritualis.MODID, model))

    private fun createCandleBSV(candleNumber: Int, lit: Boolean, colorPrefix: String): BlockStateVariant? {
        val candleSuffix = StringBuilder("candle")
        if (candleNumber > 1) candleSuffix.append("s")
        val modelPath = StringBuilder("block/${colorPrefix}candle_${numbers[candleNumber - 1]}_${candleSuffix}")
        if (lit) modelPath.append("_lit")
        return BlockStateVariant.create().put(VariantSettings.MODEL, Identifier.ofVanilla(modelPath.toString()))
    }

    private fun generateCandleBlockState(
        candle: Block,
        colorPrefix: String,
        blockStateCollector: Consumer<BlockStateSupplier>
    ) {
        val variantMap = BlockStateVariantMap.create(Properties.CANDLES, Properties.LIT)
        for (candleNum: Int in 1..4) {
            variantMap.register(candleNum, false, createCandleBSV(candleNum, false, colorPrefix))
            variantMap.register(candleNum, true, createCandleBSV(candleNum, true, colorPrefix))
        }
        blockStateCollector.accept(VariantsBlockStateSupplier.create(candle).coordinate(variantMap))
    }
}
