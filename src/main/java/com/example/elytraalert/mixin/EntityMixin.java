package com.example.elytraalert.mixin;

import com.example.elytraalert.ElytraAlertClient;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    private void elytraalert$glow(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && ElytraAlertClient.glowColorFor(this) != -1) cir.setReturnValue(true);
    }

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void elytraalert$color(CallbackInfoReturnable<Integer> cir) {
        int c = ElytraAlertClient.glowColorFor(this);
        if (c != -1) cir.setReturnValue(c);
    }
}
