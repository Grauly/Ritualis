package grauly.ritualis.client.datagen

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.client.data.BlockStateModelGenerator
import net.minecraft.client.data.ItemModelGenerator

class ModelDatagen(output: FabricDataOutput?) : FabricModelProvider(output) {

    override fun generateBlockStateModels(blockStateModelGenerator: BlockStateModelGenerator) {
        BlockModelDatagen.generateBlockStateModels(blockStateModelGenerator)
    }

    override fun generateItemModels(itemModelGenerator: ItemModelGenerator) {
        ItemModelDatagen.generateItemModels(itemModelGenerator)
    }
}