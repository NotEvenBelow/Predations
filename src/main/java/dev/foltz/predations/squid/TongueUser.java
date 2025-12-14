package dev.foltz.predations.squid;

import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface TongueUser {
    void setTongueTarget(@Nullable LivingEntity target);
    void clearTongueTarget();
    @Nullable LivingEntity getTongueTarget();
    boolean isTongueActive();
}
// UNUSED