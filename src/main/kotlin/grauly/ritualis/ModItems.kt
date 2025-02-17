package grauly.ritualis

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.item.ItemGroups
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import kotlin.reflect.KFunction1

object ModItems {

    val FLOATING_BOOK_ITEM = registerBlockItem(ModBlocks.FLOATING_BOOK, "floating_book")
    val RITUAL_LINE_ITEM = registerBlockItem(ModBlocks.RITUAL_LINE, "ritual_line")

    private fun registerBlockItem(block: Block, itemID: String, settings: Settings = Settings()): Item {
        val id = Identifier.of(Ritualis.MODID, itemID)
        val key = RegistryKey.of(RegistryKeys.ITEM, id)
        settings.registryKey(key).useBlockPrefixedTranslationKey()
        return Registry.register(Registries.ITEM, key, BlockItem(block, settings))
    }

    private fun registerPlainItem(
        path: String,
        constructor: KFunction1<Settings, Item>,
        settings: Settings = Settings()
    ): Item {
        val id = Identifier.of(Ritualis.MODID, path)
        val key = RegistryKey.of(RegistryKeys.ITEM, id)
        settings.registryKey(key).useItemPrefixedTranslationKey()
        val item = constructor.call(settings)
        return Registry.register(Registries.ITEM, key, item)
    }

    fun registerCandleBlockItem(candleBlock: Block, id: Identifier) {
        val key = RegistryKey.of(RegistryKeys.ITEM, id)
        val settings = Settings()
        settings.registryKey(key).useBlockPrefixedTranslationKey()
        register(id, BlockItem(candleBlock, settings))
    }

    fun init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COLORED_BLOCKS).register { group ->
            ModBlocks.candles.forEach { candle -> group.add(candle.asItem()) }
        }
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register { group ->
            group.addAfter(Items.ENCHANTING_TABLE, FLOATING_BOOK_ITEM)
            group.addAfter(Items.ENCHANTING_TABLE, RITUAL_LINE_ITEM)
        }
    }

    private fun register(id: String, item: Item): Item =
        register(Identifier.of(Ritualis.MODID, id), item)

    private fun register(id: Identifier, item: Item): Item =
        register(RegistryKey.of(RegistryKeys.ITEM, id), item)

    private fun register(key: RegistryKey<Item>, item: Item): Item {
        return Registry.register(Registries.ITEM, key, item)
    }
}