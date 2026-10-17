package dev.towkie.morph.mixin;

import dev.towkie.morph.MorphStore;
import dev.towkie.morph.Scale;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(method = "scale(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;)V", at = @At("HEAD"))
    private void towkie$renderScale(PlayerEntityRenderState state, MatrixStack matrices, CallbackInfo ci) {
        Scale size = MorphStore.getForName(state.name);
        if (size != null) {
            matrices.scale(size.overall * size.width, size.overall * size.height, size.overall * size.width);
        }
    }
}
