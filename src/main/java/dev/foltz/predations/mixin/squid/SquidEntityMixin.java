package dev.foltz.predations.mixin.squid;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.squid.HeadSuckable;
import dev.foltz.predations.squid.ai.HeadSuckGoal;
import dev.foltz.predations.mixin.entity.MobEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.text.Text;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;

import java.util.UUID;

@Mixin(SquidEntity.class)
public abstract class SquidEntityMixin extends LivingEntity implements HeadSuckable {
    @Unique private boolean predation$latched;
    @Unique private boolean predation$tongueActive;
    @Unique private UUID predation$targetUuid;

    protected SquidEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void predation$initCustom(CallbackInfo ci) {
        EntityType<?> type = this.getType();
        if (type == EntityType.SQUID || type == EntityType.GLOW_SQUID) {

            if (ExtraConfig.get().predSquid.enabled) {
                ((MobEntityAccessor)this).getGoalSelector()
                        .add(1, predation$createGoal((SquidEntity)(Object)this));
            }

            SquidEntity self = (SquidEntity)(Object)this;
            ExtraConfig.PredatorySquidConfig cfg = ExtraConfig.get().predSquid;

            EntityAttributeInstance healthAttr = self.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (healthAttr != null) {
                if (self.getType() == EntityType.GLOW_SQUID) {
                    healthAttr.setBaseValue(cfg.glowSquidMaxHealth);
                } else {
                    healthAttr.setBaseValue(cfg.squidMaxHealth);
                }
            }
            self.setHealth(self.getMaxHealth());
        }
    }

    @SuppressWarnings("unchecked")
    @Unique
    private static <T extends SquidEntity & HeadSuckable> HeadSuckGoal<T> predation$createGoal(SquidEntity squid) {
        return new HeadSuckGoal<>((T) squid);
    }

    @Override
    public boolean isInsideWall() {
        return !this.isLatched() && super.isInsideWall();
    }

    @Override
    public boolean canBreatheInWater() {
        return true;
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (source.isOf(DamageTypes.LAVA)) {
            String name = this.getType() == EntityType.GLOW_SQUID ? "Glow Squid" : "Squid";
            var server = this.getWorld().getServer();
            if (server != null) {
                var playerManager = server.getPlayerManager();
                if (playerManager != null) {
                    playerManager.broadcast(
                            Text.literal(name + ": nah I hate water, lava is better and warmer"),
                            false
                    );
                }
            }
        }
    }

    @Override
    protected int getNextAirOnLand(int air) {
        if (this.isLatched()) {
            return this.getMaxAir();
        }
        return super.getNextAirOnLand(air);
    }

    @Override public void setLatched(boolean latched) { this.predation$latched = latched; }
    @Override public boolean isLatched() { return predation$latched; }
    @Override public void setTongueActive(boolean active) { this.predation$tongueActive = active; }
    @Override public boolean isTongueActive() { return predation$tongueActive; }
    @Override public void setTargetUuid(UUID id) { this.predation$targetUuid = id; }
    @Override public UUID getTargetUuid() { return predation$targetUuid; }
}