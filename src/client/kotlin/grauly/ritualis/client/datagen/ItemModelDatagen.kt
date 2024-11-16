package grauly.ritualis.client.datagen

import grauly.ritualis.ModBlocks
import grauly.ritualis.Ritualis
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.client.data.BlockStateModelGenerator
import net.minecraft.client.data.ItemModelGenerator
import net.minecraft.client.data.Models

class ItemModelDatagen(output: FabricDataOutput?) : FabricModelProvider(output) {

    override fun generateBlockStateModels(p0: BlockStateModelGenerator?) {
    }

    override fun generateItemModels(itemModelGenerator: ItemModelGenerator) {
        Ritualis.LOGGER.info("ping")
        itemModelGenerator.register(ModBlocks.candles[1].asItem(), Models.GENERATED)
    }

}