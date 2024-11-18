package grauly.ritualis.client.datagen

import grauly.ritualis.ModBlocks
import grauly.ritualis.Ritualis
import grauly.ritualis.mixin.client.ItemModelGeneratorAccessor
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.client.data.*
import net.minecraft.util.Identifier
import java.util.*

class ItemModelDatagen(output: FabricDataOutput?) : FabricModelProvider(output) {

    override fun generateBlockStateModels(p0: BlockStateModelGenerator?) {
    }

    override fun generateItemModels(itemModelGenerator: ItemModelGenerator) {
        val ima = itemModelGenerator as ItemModelGeneratorAccessor
        val baseCandle = ItemModels.basic(Identifier.of("minecraft","item/candle"))
        val candleOverlay = ItemModels.basic(Identifier.of(Ritualis.MODID, "item/ritual_candle_overlay"))
        val composite = ItemModels.composite(baseCandle, candleOverlay)
        val modelA = Model(Optional.of(Identifier.ofVanilla("a")), Optional.empty(), TextureKey.LAYER0)
        val modelB = Model(Optional.of(Identifier.ofVanilla("b")), Optional.empty(), TextureKey.LAYER0)
        ima.output.accept(ModBlocks.candles[1].asItem(), composite)
        //itemModelGenerator.register(ModBlocks.candles[1].asItem(), modelA)
    }
}