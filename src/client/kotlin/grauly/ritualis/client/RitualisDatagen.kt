package grauly.ritualis.client

import grauly.ritualis.client.datagen.ModelDatagen
import grauly.ritualis.client.datagen.BlockTagDatagen
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

class RitualisDatagen : DataGeneratorEntrypoint {

    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        pack.addProvider(::ModelDatagen)
        pack.addProvider(::BlockTagDatagen)
    }
}