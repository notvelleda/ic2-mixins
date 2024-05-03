package xyz.velleda.ic2mixins.mixins;

import java.util.Collection;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ic2.core.block.machine.tileentity.TileEntityStandardMachine.class, remap = false)
public abstract class TileEntityStandardMachine<RI, RO, I> {
    @Overwrite
    protected Collection<ItemStack> getOutput(RO output) {
        return (Collection)output;
    }
}
