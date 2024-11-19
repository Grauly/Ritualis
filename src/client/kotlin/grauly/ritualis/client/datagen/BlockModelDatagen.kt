package grauly.ritualis.client.datagen

import grauly.ritualis.ModBlocks
import grauly.ritualis.Ritualis
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
    }

    private fun bsv(candleNumber: Int, lit: Boolean, colorPrefix: String): BlockStateVariant? {
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
            variantMap.register(candleNum, false, bsv(candleNum, false, colorPrefix))
            variantMap.register(candleNum, true, bsv(candleNum, true, colorPrefix))
        }
        blockStateCollector.accept(VariantsBlockStateSupplier.create(candle).coordinate(variantMap))
    }
}