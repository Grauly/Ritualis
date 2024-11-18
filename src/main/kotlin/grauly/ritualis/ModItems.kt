package grauly.ritualis

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.item.ItemGroups
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import kotlin.reflect.KFunction1

object ModItems {

    val DUMMY_ITEM = registerPlainItem("dummy", ::Item)

    private fun registerPlainItem(path: String, constructor: KFunction1<Settings, Item>, settings: Settings = Settings()): Item {
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
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COLORED_BLOCKS).register {group ->
            ModBlocks.candles.forEach {candle -> group.add(candle.asItem())}
            group.add(DUMMY_ITEM)
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