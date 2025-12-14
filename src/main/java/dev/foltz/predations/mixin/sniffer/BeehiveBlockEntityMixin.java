package dev.foltz.predations.mixin.sniffer;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Field;
import java.util.List;

@Mixin(BeehiveBlockEntity.class)
public abstract class BeehiveBlockEntityMixin {

    @Unique private static Field cachedNbtField;
    @Unique private static Field cachedTicksField;
    @Unique private static boolean reflectionInitialized = false;

    @Redirect(method = "addBee", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean modifyBeeOnAdd(List<Object> list, Object beeData) {
        try {
            ExtraConfig.BetterSnifferRelatedFeaturesConfig config = ExtraConfig.getSnifferConfig();
            if (config.enabled && config.beeMoreEffectiveWhenPollenateWithTorchflower) {

                if (!reflectionInitialized) {
                    Class<?> clazz = beeData.getClass();
                    for (Field f : clazz.getDeclaredFields()) {
                        f.setAccessible(true);

                        if (f.getType() == NbtCompound.class) {
                            cachedNbtField = f;
                        }

                        if (f.getType() == int.class) {
                            if (f.getInt(beeData) >= 2400) {
                                cachedTicksField = f;
                            }
                        }
                    }
                    reflectionInitialized = true;

                    if (cachedNbtField == null) {
                        System.err.println("[Predations] CRITICAL: Failed to locate BeeData NBT field via reflection.");
                    }
                    if (cachedTicksField == null) {
                        System.err.println("[Predations] CRITICAL: Failed to locate BeeData timer field (Value >= 2400).");
                    }
                }

                if (cachedNbtField != null && cachedTicksField != null) {
                    NbtCompound nbt = (NbtCompound) cachedNbtField.get(beeData);

                    if (nbt != null && nbt.contains("PredationsTorchflowerPollen") && nbt.getBoolean("PredationsTorchflowerPollen")) {
                        double multiplier = config.beeMoreEffectiveWhenPollenateWithTorchflowerMultiplier;

                        if (multiplier > 0.1) {
                            int currentTicks = cachedTicksField.getInt(beeData);
                            int newTicks = (int) (currentTicks / multiplier);

                            cachedTicksField.setInt(beeData, newTicks);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Predations] Error in BeehiveBlockEntityMixin reflection:");
            e.printStackTrace();
        }

        return list.add(beeData);
    }
}