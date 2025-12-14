package dev.foltz.predations.squid;

import java.util.UUID;

public interface HeadSuckable {
    void setLatched(boolean latched);
    boolean isLatched();

    void setTongueActive(boolean active);
    boolean isTongueActive();

    void setTargetUuid(UUID id);
    UUID getTargetUuid();
}
