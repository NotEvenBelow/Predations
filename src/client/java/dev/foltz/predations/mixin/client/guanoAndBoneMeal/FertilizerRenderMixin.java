package dev.foltz.predations.mixin.client.guanoAndBoneMeal;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.guano.FertilizerAccess;
import dev.foltz.predations.item.ModItems;
import net.minecraft.block.CropBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class FertilizerRenderMixin {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawBlockOutline(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/entity/Entity;DDDLnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"))
    private void renderFertilizerOverlay(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        boolean holdingBoneMeal = player.getMainHandStack().isOf(Items.BONE_MEAL) || player.getOffHandStack().isOf(Items.BONE_MEAL);
        boolean holdingGuano = player.getMainHandStack().isOf(ModItems.GUANO_FERTILIZER) || player.getOffHandStack().isOf(ModItems.GUANO_FERTILIZER);

        if (!holdingBoneMeal && !holdingGuano) return;

        ExtraConfig.BatGuanoAndBoneMealConfig config = ExtraConfig.getBatGuanoConfig();
        if (!config.enabled) return;

        boolean shouldRender = false;

        if (holdingBoneMeal && config.vanillaBoneMealSpeedUpCropGrowthInsteadofInstantGrowth) {
            shouldRender = true;
        }

        if (holdingGuano && !config.guanoFertilizerInstantFinishCropGrowth) {
            shouldRender = true;
        }

        if (!shouldRender) return;

        HitResult target = client.crosshairTarget;
        if (target == null || target.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = ((BlockHitResult) target).getBlockPos();
        if (!(client.world.getBlockState(pos).getBlock() instanceof CropBlock)) return;

        Chunk chunk = client.world.getChunk(pos);
        boolean isFertilized = false;
        if (chunk instanceof FertilizerAccess access) {
            isFertilized = access.predations$isFertilized(pos);
        }

        float r = isFertilized ? 0.0f : 1.0f;
        float g = isFertilized ? 1.0f : 0.0f;
        float b = 0.0f;
        float a = 0.4f;

        Vec3d camPos = camera.getPos();
        double x = pos.getX() - camPos.x;
        double y = pos.getY() - camPos.y;
        double z = pos.getZ() - camPos.z;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        float yLevel = (float) (y);
        float min = 0.0f;
        float max = 1.0f;
        float offset = 0.05f;

        buffer.vertex(matrices.peek().getPositionMatrix(), (float)x + min, yLevel + offset, (float)z + min).color(r, g, b, a).next();
        buffer.vertex(matrices.peek().getPositionMatrix(), (float)x + min, yLevel + offset, (float)z + max).color(r, g, b, a).next();
        buffer.vertex(matrices.peek().getPositionMatrix(), (float)x + max, yLevel + offset, (float)z + max).color(r, g, b, a).next();
        buffer.vertex(matrices.peek().getPositionMatrix(), (float)x + max, yLevel + offset, (float)z + min).color(r, g, b, a).next();

        tessellator.draw();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}