package dev.foltz.predations.client.render.layer;

import dev.foltz.predations.config.DebugConfig;
import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.SquidEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.SquidEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SquidOrbitLayer<E extends SquidEntity> extends FeatureRenderer<E, SquidEntityModel<E>> {
    public SquidOrbitLayer(FeatureRendererContext<E, SquidEntityModel<E>> ctx) { super(ctx); }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light,
                       E squid, float limbAngle, float limbDistance,
                       float tickDelta, float customAngle, float headYaw, float headPitch) {
        if (!DebugConfig.get().orbitEnabled) return;

        double expand = Math.max(0.0, ExtraConfig.get().predatorySquid.latchRangeBlocks);
        var box = squid.getBoundingBox().expand(expand);

        float minX = (float)(box.minX - squid.getX());
        float minY = (float)(box.minY - squid.getY());
        float minZ = (float)(box.minZ - squid.getZ());
        float maxX = (float)(box.maxX - squid.getX());
        float maxY = (float)(box.maxY - squid.getY());
        float maxZ = (float)(box.maxZ - squid.getZ());

        matrices.push();
        VertexConsumer vc = consumers.getBuffer(RenderLayer.getLines());
        var entry = matrices.peek();
        Matrix4f pos = entry.getPositionMatrix();
        Matrix3f norm = entry.getNormalMatrix();

        int r = 255, g = 255, b = 0, a = 180; // yellow

        // bottom
        line(vc,pos,norm,minX,minY,minZ, maxX,minY,minZ,r,g,b,a,light);
        line(vc,pos,norm,maxX,minY,minZ, maxX,minY,maxZ,r,g,b,a,light);
        line(vc,pos,norm,maxX,minY,maxZ, minX,minY,maxZ,r,g,b,a,light);
        line(vc,pos,norm,minX,minY,maxZ, minX,minY,minZ,r,g,b,a,light);
        // top
        line(vc,pos,norm,minX,maxY,minZ, maxX,maxY,minZ,r,g,b,a,light);
        line(vc,pos,norm,maxX,maxY,minZ, maxX,maxY,maxZ,r,g,b,a,light);
        line(vc,pos,norm,maxX,maxY,maxZ, minX,maxY,maxZ,r,g,b,a,light);
        line(vc,pos,norm,minX,maxY,maxZ, minX,maxY,minZ,r,g,b,a,light);
        // pillars
        line(vc,pos,norm,minX,minY,minZ, minX,maxY,minZ,r,g,b,a,light);
        line(vc,pos,norm,maxX,minY,minZ, maxX,maxY,minZ,r,g,b,a,light);
        line(vc,pos,norm,maxX,minY,maxZ, maxX,maxY,maxZ,r,g,b,a,light);
        line(vc,pos,norm,minX,minY,maxZ, minX,maxY,maxZ,r,g,b,a,light);

        matrices.pop();
    }

    private static void line(VertexConsumer vc, Matrix4f m, Matrix3f n,
                             float x1,float y1,float z1, float x2,float y2,float z2,
                             int r,int g,int b,int a,int light) {
        vc.vertex(m,x1,y1,z1).color(r,g,b,a).normal(n,0,1,0).next();
        vc.vertex(m,x2,y2,z2).color(r,g,b,a).normal(n,0,1,0).next();
    }
}
