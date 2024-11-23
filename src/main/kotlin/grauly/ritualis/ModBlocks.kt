package grauly.ritualis

import grauly.ritualis.block.RitualCandle
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.CandleBlock
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier

object ModBlocks {

    val candles = mutableListOf<Block>()

    private fun registerCandles() {
        registerCandle("candle")
        for (color in Ritualis.COLOR_ORDER) {
            registerCandle(color.getName() + "_candle")
        }
    }

    private fun registerCandle(itemId: String) {
        val id = Identifier.of(Ritualis.MODID, itemId)
        val key = RegistryKey.of(RegistryKeys.BLOCK, id)
        val settings = AbstractBlock.Settings.create()
        settings.registryKey(key)
        settings.luminance(CandleBlock.STATE_TO_LUMINANCE)
        val block = register(key, RitualCandle(settings))
        ModItems.registerCandleBlockItem(block, id)
        candles.add(block)
    }

    fun init() {
        registerCandles()
    }
    private fun register(key: RegistryKey<Block>, block: Block): Block =
        Registry.register(Registries.BLOCK, key, block)

    private fun register(id: Identifier, block: Block): Block =
        Registry.register(Registries.BLOCK, id, block)

    private fun register(id: String, block: Block): Block =
        register(Identifier.of(Ritualis.MODID, id), block)

}