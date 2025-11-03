package dev.foltz.predations.client.render;

import dev.foltz.predations.client.render.layer.GlowSquidOrbitLayer;
import dev.foltz.predations.squid.PredatoryGlowSquidEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SquidEntityModel;
import net.minecraft.util.Identifier;

public class PredatoryGlowSquidRenderer extends MobEntityRenderer<PredatoryGlowSquidEntity, SquidEntityModel<PredatoryGlowSquidEntity>> {
    private static final Identifier TEXTURE = new Identifier("predations", "textures/entity/glow_squid.png");

    public PredatoryGlowSquidRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new SquidEntityModel<>(ctx.getPart(EntityModelLayers.GLOW_SQUID)), 0.7f);
        this.addFeature(new dev.foltz.predations.client.render.layer.GlowSquidOrbitLayer<dev.foltz.predations.squid.PredatoryGlowSquidEntity>(this));
    }

    @Override
    public Identifier getTexture(PredatoryGlowSquidEntity entity) {
        return TEXTURE;
    }
}
