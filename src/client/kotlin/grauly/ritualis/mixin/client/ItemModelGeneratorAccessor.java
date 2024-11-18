package grauly.ritualis.mixin.client;

import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.ItemModelOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemModelGenerator.class)
public interface ItemModelGeneratorAccessor {
    @Accessor
    ItemModelOutput getOutput();
}
