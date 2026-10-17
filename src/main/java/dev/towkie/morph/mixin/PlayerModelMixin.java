package dev.towkie.morph.mixin;

import dev.towkie.morph.MorphStore;
import dev.towkie.morph.Scale;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerModelMixin {
    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("TAIL"))
    private void towkie$bodyProportions(PlayerEntityRenderState state, CallbackInfo ci) {
        PlayerEntityModel model = (PlayerEntityModel)(Object)this;
        Scale s = MorphStore.getForName(state.name);
        float head = s == null ? 1 : s.head;
        float body = s == null ? 1 : s.body;
        float arms = s == null ? 1 : s.arms;
        float legs = s == null ? 1 : s.legs;
        change(model.head, head);
        change(model.hat, head);
        change(model.body, body);
        change(model.jacket, body);
        change(model.leftArm, arms);
        change(model.rightArm, arms);
        change(model.leftSleeve, arms);
        change(model.rightSleeve, arms);
        change(model.leftLeg, legs);
        change(model.rightLeg, legs);
        change(model.leftPants, legs);
        change(model.rightPants, legs);
    }

    private static void change(ModelPart part, float amount) {
        part.xScale = amount;
        part.yScale = amount;
        part.zScale = amount;
    }
}
