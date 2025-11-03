package dev.foltz.predations.mixin.squid;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.squid.HeadSuckable;
import dev.foltz.predations.squid.ai.HeadSuckGoal;
import dev.foltz.predations.mixin.entity.MobEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.text.Text;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("unchecked")
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
        // add predatory AI if enabled
        if (ExtraConfig.get().predatorySquid.enabled) {
            ((MobEntityAccessor)(Object)this).getGoalSelector()
                    .add(1, new HeadSuckGoal((SquidEntity)(Object)this));
        }

        // 🟢 set health once from config
        SquidEntity self = (SquidEntity)(Object)this;
        ExtraConfig.PredatorySquidConfig cfg = ExtraConfig.get().predatorySquid;
        if (self.getType() == EntityType.GLOW_SQUID) {
            self.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                    .setBaseValue(cfg.glowSquidMaxHealth);
        } else {
            self.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                    .setBaseValue(cfg.squidMaxHealth);
        }
        self.setHealth(self.getMaxHealth());
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void predation$togglePredatorIfNearPlayer(CallbackInfo ci) {
        if (!ExtraConfig.get().predatorySquid.enabled) return;

        SquidEntity self = (SquidEntity)(Object)this;
        var selector = ((MobEntityAccessor)(Object)this).getGoalSelector();

        List<ServerPlayerEntity> players = self.getWorld().getEntitiesByClass(
                ServerPlayerEntity.class,
                self.getBoundingBox().expand(48),
                p -> !p.isSpectator() && p.isAlive()
        );

        boolean hasGoal = selector.getGoals().stream()
                .anyMatch(g -> g.getGoal() instanceof HeadSuckGoal);

        if (players.isEmpty()) {
            if (hasGoal) {
                selector.getGoals().removeIf(g -> g.getGoal() instanceof HeadSuckGoal);
                this.predation$latched = false;
                this.predation$tongueActive = false;
                this.predation$targetUuid = null;
                if (self.hasVehicle()) self.stopRiding();
            }
        } else {
            if (!hasGoal) {
                selector.add(1, new HeadSuckGoal(self));
            }
        }
    }

    // suffocation immunity only while latched
    @Override
    public boolean isInsideWall() {
        return this.isLatched() ? false : super.isInsideWall();
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
            this.getWorld().getServer().getPlayerManager().broadcast(
                    Text.literal(name + ": nah I hate water, lava is better and warmer"),
                    false
            );
        }
    }

    @Override
    protected int getNextAirOnLand(int air) {
        if (this.isLatched()) {
            // Always reset to max while latched
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
