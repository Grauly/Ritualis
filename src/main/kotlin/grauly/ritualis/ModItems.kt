package grauly.ritualis

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroups
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier

object ModItems {

    fun registerCandleBlockItem(candleBlock: Block, id: Identifier) {
        val key = RegistryKey.of(RegistryKeys.ITEM, id)
        val settings = Item.Settings()
        settings.registryKey(key).useBlockPrefixedTranslationKey()
        register(id, BlockItem(candleBlock, settings))
    }

    fun init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COLORED_BLOCKS).register {group ->
            ModBlocks.candles.forEach {candle -> group.add(candle.asItem())}
        }
    }

    private fun register(id: String, item: Item): Item =
        register(Identifier.of(Ritualis.MODID, id), item)

    private fun register(id: Identifier, item: Item): Item =
        Registry.register(Registries.ITEM, id, item)

    private fun register(key: RegistryKey<Item>, item: Item): Item =
        Registry.register(Registries.ITEM, key, item)
}