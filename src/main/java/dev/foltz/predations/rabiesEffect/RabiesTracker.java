package dev.foltz.predations.rabiesEffect;

public interface RabiesTracker {
    int getRabiesTicks();
    void setRabiesTicks(int ticks);
    void incrementRabiesTicks();
}