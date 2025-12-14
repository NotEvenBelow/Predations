package dev.foltz.predations.client;

import dev.foltz.predations.client.gui.LifeRitualScreen; // <-- IMPORT SCREEN
import dev.foltz.predations.client.render.PredatoryGlowSquidRenderer;
import dev.foltz.predations.client.render.PredatorySquidRenderer;
import dev.foltz.predations.client.render.layer.GlowSquidOrbitLayer;
import dev.foltz.predations.client.render.layer.SquidOrbitLayer;
import dev.foltz.predations.entity.FamishedCowEntity;
import dev.foltz.predations.entity.ModEntities;
import dev.foltz.predations.item.ModItems; // <-- IMPORT ITEMS
import dev.foltz.predations.squid.ModSquidEntities;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback; // <-- IMPORT CALLBACK

import net.minecraft.client.MinecraftClient; // <-- IMPORT CLIENT
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SquidEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult; // <-- IMPORT

public class PredationsModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModSquidEntities.PREDATORY_SQUID, PredatorySquidRenderer::new);
        EntityRendererRegistry.register(ModSquidEntities.PREDATORY_GLOW_SQUID, PredatoryGlowSquidRenderer::new);

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
            if (type == EntityType.SQUID) {
                @SuppressWarnings("unchecked")
                FeatureRendererContext<SquidEntity, SquidEntityModel<SquidEntity>> c =
                        (FeatureRendererContext<SquidEntity, SquidEntityModel<SquidEntity>>) renderer;
                helper.register(new SquidOrbitLayer<>(c));
            } else if (type == EntityType.GLOW_SQUID) {
                @SuppressWarnings("unchecked")
                FeatureRendererContext<GlowSquidEntity, SquidEntityModel<GlowSquidEntity>> c =
                        (FeatureRendererContext<GlowSquidEntity, SquidEntityModel<GlowSquidEntity>>) renderer;
                helper.register(new GlowSquidOrbitLayer<>(c));
            }
        });

        EntityRendererRegistry.register(ModEntities.FAMISHED_COW, (EntityRendererFactory.Context ctx) ->
                new MobEntityRenderer<FamishedCowEntity, CowEntityModel<FamishedCowEntity>>(
                        ctx, new CowEntityModel<>(ctx.getPart(EntityModelLayers.COW)), 0.7f) {
                    private final Identifier TEX = new Identifier("predations", "textures/entity/famished_cow.png");

                    @Override
                    public Identifier getTexture(FamishedCowEntity entity) {
                        return TEX;
                    }
                }
        );

        EntityRendererRegistry.register(ModEntities.THROWN_POTION_OF_CURING, FlyingItemEntityRenderer::new);
        dev.foltz.predations.client.guano.ClientFertilizerNetworking.register();

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient && player.getStackInHand(hand).isOf(ModItems.LIFE_RITUAL)) {
                MinecraftClient.getInstance().setScreen(new LifeRitualScreen());
                return TypedActionResult.pass(player.getStackInHand(hand));
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });
    }
}