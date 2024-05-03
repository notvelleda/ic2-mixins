package xyz.velleda.ic2mixins.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ic2.core.block.machine.tileentity.TileEntityMatter.class, remap = false)
public abstract class TileEntityMatter {
    private boolean hasSetUpgradestat = false;

    @Inject(method = "updateEntityServer", at = @At("HEAD"))
    protected void updateEntityServer(CallbackInfo ci) {
        this.hasSetUpgradestat = false;
    }

    @Inject(method = "setUpgradestat", at = @At("HEAD"), cancellable = true)
    public void setUpgradestat(CallbackInfo ci) {
        if (!this.hasSetUpgradestat) {
            ci.cancel();
        } else {
            this.hasSetUpgradestat = true;
        }
    }
}
