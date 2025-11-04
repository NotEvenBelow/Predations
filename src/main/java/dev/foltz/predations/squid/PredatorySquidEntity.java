/** This is a joke, it might  be glitchy idrc, i keep it bc i love the animation, dont ask me why lmao **/

package dev.foltz.predations.squid;

import dev.foltz.predations.squid.ai.HeadSuckGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import java.util.UUID;

public class PredatorySquidEntity extends SquidEntity implements HeadSuckable {
    private boolean latched;
    private boolean tongueActive;
    private UUID targetUuid;

    public PredatorySquidEntity(EntityType<? extends SquidEntity> type, World world) {
        super(type, world); 
    }

    @Override
    protected void initGoals() {
        super.initGoals(); 
        this.goalSelector.add(1, new HeadSuckGoal<>(this));
        this.goalSelector.add(5, new SwimAroundGoal(this, 1.0, 10));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return SquidEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
    }

    
    @Override public void setLatched(boolean latched) { this.latched = latched; }
    @Override public boolean isLatched() { return latched; }

    @Override public void setTongueActive(boolean active) { this.tongueActive = active; }
    @Override public boolean isTongueActive() { return tongueActive; }

    @Override public void setTargetUuid(UUID id) { this.targetUuid = id; }
    @Override public UUID getTargetUuid() { return targetUuid; }
}
