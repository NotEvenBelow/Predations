package dev.foltz.predations.kuru;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.kuru_status.KuruStatusEffects;
import dev.foltz.predations.util.IEntityDataSaver;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;

public class KuruHandler {
    private static final String KURU_TIMER_KEY = "PredationsKuruTimer";
    private static final String KURU_STAGE_KEY = "PredationsKuruStage";
    private static final String KURU_STAGE_3_TIMER_KEY = "PredationsKuruStage3Timer";

    public static void tick(MinecraftServer server) {
        if (!ExtraConfig.getKuruConfig().enabled) {
            return;
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.hasStatusEffect(KuruStatusEffects.KURU)) {
                int currentTimer = getKuruTimer(player);
                int newTimer = currentTimer + 1;
                setKuruTimer(player, newTimer);

                int currentStage = getKuruStage(player);
                int newStage = getStageForTime(newTimer);

                if (newStage > currentStage) {
                    setKuruStage(player, newStage);
                    applyStageEffects(player, newStage);

                    if (newStage == 3) {
                        setKuruStage3Timer(player, 0);
                    }
                }

                if (currentStage == 3 && ExtraConfig.getKuruConfig().stage4Kill) {
                    int stage3Timer = getKuruStage3Timer(player);
                    stage3Timer++;
                    setKuruStage3Timer(player, stage3Timer);

                    int killTimeTicks = ExtraConfig.getKuruConfig().stage3SurvivedKillSecond * 20;

                    if (stage3Timer >= killTimeTicks) {
                        player.kill();
                    }
                }
            }
        }
    }

    private static int getStageForTime(int ticks) {
        int currentStage = 0;
        Map<String, ExtraConfig.KuruStage> stages = ExtraConfig.getKuruConfig().stages;

        for (Map.Entry<String, ExtraConfig.KuruStage> entry : stages.entrySet()) {
            try {
                int stageNum = Integer.parseInt(entry.getKey());
                if (ticks >= entry.getValue().startTick && stageNum > currentStage) {
                    currentStage = stageNum;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid Kuru stage key (must be a number): " + entry.getKey());
            }
        }
        return currentStage;
    }

    private static void applyStageEffects(ServerPlayerEntity player, int stage) {
        String stageKey = String.valueOf(stage);
        ExtraConfig.KuruStage stageConfig = ExtraConfig.getKuruConfig().stages.get(stageKey);

        if (stageConfig == null) {
            return;
        }

        for (ExtraConfig.KuruStageEffect effectConfig : stageConfig.effects) {
            Optional<StatusEffect> effect = Registries.STATUS_EFFECT.getOrEmpty(new Identifier(effectConfig.effectId));
            if (effect.isPresent()) {
                int durationTicks = effectConfig.durationSeconds * 20;
                player.addStatusEffect(new StatusEffectInstance(effect.get(), durationTicks, effectConfig.amplifier, true, false, true));
            }
        }
    }


    private static NbtCompound getPersistentData(LivingEntity entity) {
        IEntityDataSaver dataSaver = (IEntityDataSaver) entity;
        return dataSaver.getPersistentData();
    }

    public static int getKuruTimer(LivingEntity entity) {
        return getPersistentData(entity).getInt(KURU_TIMER_KEY);
    }

    public static void setKuruTimer(LivingEntity entity, int ticks) {
        getPersistentData(entity).putInt(KURU_TIMER_KEY, ticks);
    }

    public static int getKuruStage(LivingEntity entity) {
        return getPersistentData(entity).getInt(KURU_STAGE_KEY);
    }

    public static void setKuruStage(LivingEntity entity, int stage) {
        getPersistentData(entity).putInt(KURU_STAGE_KEY, stage);
    }

    public static int getKuruStage3Timer(LivingEntity entity) {
        return getPersistentData(entity).getInt(KURU_STAGE_3_TIMER_KEY);
    }

    public static void setKuruStage3Timer(LivingEntity entity, int ticks) {
        getPersistentData(entity).putInt(KURU_STAGE_3_TIMER_KEY, ticks);
    }

    public static void clearKuru(LivingEntity entity) {
        NbtCompound nbt = getPersistentData(entity);
        nbt.remove(KURU_TIMER_KEY);
        nbt.remove(KURU_STAGE_KEY);
        nbt.remove(KURU_STAGE_3_TIMER_KEY);

        Map<String, ExtraConfig.KuruStage> stages = ExtraConfig.getKuruConfig().stages;
        for (ExtraConfig.KuruStage stageConfig : stages.values()) {
            for (ExtraConfig.KuruStageEffect effectConfig : stageConfig.effects) {
                Optional<StatusEffect> effect = Registries.STATUS_EFFECT.getOrEmpty(new Identifier(effectConfig.effectId));
                effect.ifPresent(entity::removeStatusEffect);
            }
        }
    }
}