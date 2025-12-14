/** UNUSED
package dev.foltz.predations.config;


import java.util.LinkedHashSet;
import java.util.Set;

public final class ConfigHealth {
    private static final Set<String> BROKEN = new LinkedHashSet<>();
    private static volatile boolean popupShown = false;

    private ConfigHealth() {}

    public static void markBroken(String name) {
        if (name != null && !name.isBlank()) BROKEN.add(name);
    }

    public static void clearBroken(String name) {
        if (name != null) BROKEN.remove(name);
    }

    public static boolean hasIssues() {
        return !BROKEN.isEmpty();
    }

    public static Set<String> brokenList() {
        return Set.copyOf(BROKEN);
    }

    public static boolean popupAlreadyShown() { return popupShown; }
    public static void markPopupShown() { popupShown = true; }
}

 **/
