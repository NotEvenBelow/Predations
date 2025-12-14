package dev.foltz.predations.llama;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;

import java.util.EnumSet;
import java.util.List;

public class LlamaKickGoal extends Goal {

    private final MobEntity mob;
    private int lastHandledAttackTime = 0;
    private LivingEntity targetToKick;

    // Global cooldown (40 ticks)
    private long nextKickTime = 0;

    public LlamaKickGoal(MobEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (!mob.isAlive()) return false;

        // 1. GLOBAL COOLDOWN
        if (mob.getWorld().getTime() < nextKickTime) return false;

        this.targetToKick = null;

        // 2. PROXIMITY CHECK (Undead, Arthropod, Extras)
        LivingEntity proxTarget = findProximityTarget();
        if (proxTarget != null) {
            this.targetToKick = proxTarget;
            return true;
        }

        // 3. RETALIATION CHECK (Attacker)
        int lastAttacked = mob.getLastAttackedTime();
        if (lastAttacked != this.lastHandledAttackTime) {
            LivingEntity attacker = mob.getAttacker();
            if (attacker != null && attacker.isAlive()) {
                double distSq = mob.squaredDistanceTo(attacker);

                // Added: mob.canSee(attacker) prevents kicking through walls
                if (distSq <= 4.0 && mob.canSee(attacker)) {
                    this.targetToKick = attacker;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        return false; // Instant kick.
    }

    @Override
    public void start() {
        if (targetToKick == null) return;

        this.nextKickTime = mob.getWorld().getTime() + 40L;

        ExtraConfig.KickingLlamaConfig config = ExtraConfig.get().kickingLlama;

        if (targetToKick == mob.getAttacker()) {
            this.lastHandledAttackTime = mob.getLastAttackedTime();
        }

        mob.getLookControl().lookAt(targetToKick, 30.0f, 30.0f);
        mob.swingHand(Hand.MAIN_HAND, true);
        mob.playSound(SoundEvents.ENTITY_LLAMA_SPIT, 1.0f, 0.9f + mob.getRandom().nextFloat() * 0.2f);
        spawnSpitParticles(targetToKick);

        float damage = calculateDamage(config);
        targetToKick.damage(mob.getDamageSources().mobAttack(mob), damage);

        double hVel = config.llamaKickHorizontalVelocity;
        double vVel = config.llamaKickVerticalVelocity;

        Vec3d dirVec = targetToKick.getPos().subtract(mob.getPos());
        if (dirVec.lengthSquared() < 1.0E-7) {
            dirVec = mob.getRotationVec(1.0f);
        }

        Vec3d horizontalDir = new Vec3d(dirVec.x, 0, dirVec.z).normalize();
        double kbX = horizontalDir.x * hVel;
        double kbZ = horizontalDir.z * hVel;

        Vec3d currentVel = targetToKick.getVelocity();
        targetToKick.setVelocity(currentVel.x + kbX, Math.max(currentVel.y, vVel), currentVel.z + kbZ);
        targetToKick.velocityModified = true;
    }

    private LivingEntity findProximityTarget() {
        ExtraConfig.KickingLlamaConfig config = ExtraConfig.get().kickingLlama;

        boolean checkUndead = config.llamaKickUndeadWhenNear;
        boolean checkArthropod = config.llamaKickArthropodWhenNear;
        List<String> extras = config.llamaExtraEntities;
        boolean checkExtras = extras != null && !extras.isEmpty();

        if (!checkUndead && !checkArthropod && !checkExtras) return null;

        Box box = mob.getBoundingBox().expand(2.0, 1.0, 2.0);
        List<LivingEntity> list = mob.getWorld().getEntitiesByClass(LivingEntity.class, box, e -> e != mob && e.isAlive());

        for (LivingEntity e : list) {
            if (e instanceof PlayerEntity) continue;

            // Added: mob.canSee(e) check
            if (!mob.canSee(e)) continue;

            boolean valid = false;
            if (checkUndead && e.getGroup() == EntityGroup.UNDEAD) valid = true;
            else if (checkArthropod && e.getGroup() == EntityGroup.ARTHROPOD) valid = true;
            else if (checkExtras) {
                String id = Registries.ENTITY_TYPE.getId(e.getType()).toString();
                if (extras.contains(id)) valid = true;
            }

            if (valid) return e;
        }
        return null;
    }

    private float calculateDamage(ExtraConfig.KickingLlamaConfig config) {
        Difficulty diff = mob.getWorld().getDifficulty();
        if (diff == Difficulty.EASY) return config.llamaKickDamageHeartsEasy * 2.0f;
        else if (diff == Difficulty.NORMAL) return config.llamaKickDamageHeartsNormal * 2.0f;
        else if (diff == Difficulty.HARD) return config.llamaKickDamageHeartsHard * 2.0f;
        return 0.0f;
    }

    private void spawnSpitParticles(LivingEntity target) {
        Vec3d look = target.getEyePos().subtract(mob.getEyePos()).normalize();
        for (int i = 0; i < 7; i++) {
            double ox = (mob.getRandom().nextDouble() - 0.5) * 0.3;
            double oy = (mob.getRandom().nextDouble() - 0.5) * 0.3;
            double oz = (mob.getRandom().nextDouble() - 0.5) * 0.3;

            mob.getWorld().addParticle(ParticleTypes.SPIT,
                    mob.getX() + look.x * 0.6 + ox,
                    mob.getEyeY() - 0.1 + oy,
                    mob.getZ() + look.z * 0.6 + oz,
                    look.x * 0.4, 0.1, look.z * 0.4);
        }
    }
}