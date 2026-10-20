package io.github.mortuusars.envelope.client.renderer.entity;

import io.github.mortuusars.envelope.Envelope;
import io.github.mortuusars.envelope.client.model.PigeonModel;
import io.github.mortuusars.envelope.client.renderer.entity.layer.PigeonBackpackLayer;
import io.github.mortuusars.envelope.client.renderer.entity.layer.PigeonHatLayer;
import io.github.mortuusars.envelope.world.entity.Pigeon;
import io.github.mortuusars.envelope.world.entity.PigeonVariant;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PigeonRenderer extends MobRenderer<Pigeon, PigeonModel> {
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(Envelope.resource("pigeon"), "main");

    public PigeonRenderer(EntityRendererProvider.Context context) {
        super(context, new PigeonModel(context.bakeLayer(MODEL_LAYER)), 0.35f);
        addLayer(new PigeonBackpackLayer(this, context.getModelSet()));
        addLayer(new PigeonHatLayer(this, context.getModelSet()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(Pigeon entity) {
        return entity.getVariant().value().texture();
    }

    @Override
    protected @Nullable RenderType getRenderType(Pigeon livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (livingEntity.getVariant().is(PigeonVariant.GHOST)) {
            return RenderType.entityTranslucentCull(getTextureLocation(livingEntity));
        }

        return super.getRenderType(livingEntity, bodyVisible, translucent, glowing);
    }

    @Override
    protected float getBob(Pigeon livingBase, float partialTick) {
        float f = Mth.lerp(partialTick, livingBase.oFlap, livingBase.flap);
        float g = Mth.lerp(partialTick, livingBase.oFlapSpeed, livingBase.flapSpeed);
        return (Mth.sin(f) + 1.0F) * g;
    }
}