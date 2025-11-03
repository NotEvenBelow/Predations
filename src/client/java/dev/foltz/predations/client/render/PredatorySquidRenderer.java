package dev.foltz.predations.client.render;

import dev.foltz.predations.client.render.layer.SquidOrbitLayer;
import dev.foltz.predations.squid.PredatorySquidEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SquidEntityModel;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.util.Identifier;

public class PredatorySquidRenderer extends MobEntityRenderer<PredatorySquidEntity, SquidEntityModel<PredatorySquidEntity>> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/entity/squid/squid.png");

    public PredatorySquidRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new SquidEntityModel<>(ctx.getPart(EntityModelLayers.SQUID)), 0.7f);


        this.addFeature(new dev.foltz.predations.client.render.layer.SquidOrbitLayer<dev.foltz.predations.squid.PredatorySquidEntity>(this));
    }

    @Override
    public Identifier getTexture(PredatorySquidEntity entity) {
        return TEXTURE;
    }
}
