package grauly.ritualis.client.datagen

import grauly.ritualis.ModBlocks
import grauly.ritualis.Ritualis
import grauly.ritualis.mixin.client.ItemModelGeneratorAccessor
import net.minecraft.client.data.ItemModelGenerator
import net.minecraft.client.data.ItemModels
import net.minecraft.item.Item
import net.minecraft.util.Identifier

object ItemModelDatagen {

    fun generateItemModels(itemModelGenerator: ItemModelGenerator) {
        itemModelGenerator as ItemModelGeneratorAccessor
        generateCandleItemModel(ModBlocks.candles[0].asItem(), "", itemModelGenerator)
        for (index: Int in 1..Ritualis.COLOR_ORDER.size) {
            val color = Ritualis.COLOR_ORDER[index - 1]
            val item = ModBlocks.candles[index].asItem()
            generateCandleItemModel(item, "${color.getName()}_", itemModelGenerator)

        }
    }

    private fun generateCandleItemModel(
        item: Item,
        colorString: String,
        itemModelGeneratorAccessor: ItemModelGeneratorAccessor
    ) {
        val baseCandle = ItemModels.basic(Identifier.of("minecraft", "item/${colorString}candle"))
        val candleOverlay = ItemModels.basic(Identifier.of(Ritualis.MODID, "item/ritual_candle_overlay"))
        val composite = ItemModels.composite(baseCandle, candleOverlay)
        itemModelGeneratorAccessor.output.accept(item, composite)
    }
}