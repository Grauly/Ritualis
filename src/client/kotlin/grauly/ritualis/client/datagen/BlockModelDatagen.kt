package grauly.ritualis.client.datagen

import grauly.ritualis.ModBlocks
import grauly.ritualis.Ritualis
import grauly.ritualis.block.RitualLine
import net.minecraft.block.Block
import net.minecraft.client.data.*
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
        val lineSegment = "block/ritual_line"
        val lineNotch = "block/ritual_notch"
        blockStateModelGenerator.blockStateCollector.accept(
            MultipartBlockStateSupplier.create(ModBlocks.RITUAL_LINE)
                .with(BlockStateVariant.create().put(VariantSettings.MODEL, Identifier.of(Ritualis.MODID, "block/ritual_dot")))
                .with(When.create().set(RitualLine.CONNECTED_NORTH, true), createRotationSegmentBSV(VariantSettings.Rotation.R0, lineSegment))
                .with(When.create().set(RitualLine.CONNECTED_EAST, true), createRotationSegmentBSV(VariantSettings.Rotation.R90, lineSegment))
                .with(When.create().set(RitualLine.CONNECTED_SOUTH, true), createRotationSegmentBSV(VariantSettings.Rotation.R180, lineSegment))
                .with(When.create().set(RitualLine.CONNECTED_WEST, true), createRotationSegmentBSV(VariantSettings.Rotation.R270, lineSegment))
                .with(When.create().set(RitualLine.CONNECTED_NORTH, true).set(RitualLine.CONNECTED_EAST, true), createRotationSegmentBSV(VariantSettings.Rotation.R0, lineNotch))
                .with(When.create().set(RitualLine.CONNECTED_EAST, true).set(RitualLine.CONNECTED_SOUTH, true), createRotationSegmentBSV(VariantSettings.Rotation.R90, lineNotch))
                .with(When.create().set(RitualLine.CONNECTED_SOUTH, true).set(RitualLine.CONNECTED_WEST, true), createRotationSegmentBSV(VariantSettings.Rotation.R180, lineNotch))
                .with(When.create().set(RitualLine.CONNECTED_WEST, true).set(RitualLine.CONNECTED_NORTH, true), createRotationSegmentBSV(VariantSettings.Rotation.R270, lineNotch))
        )
    }

    private fun createRotationSegmentBSV(rotation: VariantSettings.Rotation, model: String): BlockStateVariant =
        BlockStateVariant.create().put(VariantSettings.Y, rotation).put(VariantSettings.MODEL, Identifier.of(Ritualis.MODID, model))

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