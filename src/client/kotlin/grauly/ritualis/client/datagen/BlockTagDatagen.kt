package grauly.ritualis.client.datagen

import grauly.ritualis.ModBlockTags
import grauly.ritualis.ModBlocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.block.Block
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.BlockTags
import java.util.concurrent.CompletableFuture

class BlockTagDatagen(
    output: FabricDataOutput?,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>?
) : FabricTagProvider<Block>(output, RegistryKeys.BLOCK, registriesFuture) {

    override fun configure(lookup: RegistryWrapper.WrapperLookup?) {
        val candles = getOrCreateTagBuilder(BlockTags.CANDLES)
        val ritualCandles = getOrCreateTagBuilder(ModBlockTags.RITUAL_CANDLES)
        ModBlocks.candles.forEach { candle ->
            candles.add(candle)
            ritualCandles.add(candle)
        }
        val ritualConnectable = getOrCreateTagBuilder(ModBlockTags.RITUAL_CONNECTABLE)
        ritualConnectable.add(ModBlocks.RITUAL_LINE)
    }
}