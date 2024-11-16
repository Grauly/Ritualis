package grauly.ritualis.client

import grauly.ritualis.client.datagen.ItemModelDatagen
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import kotlin.reflect.full.primaryConstructor

class RitualisDatagen : DataGeneratorEntrypoint {

    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        pack.addProvider(::ItemModelDatagen)
    }
}