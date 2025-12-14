package dev.foltz.predations.secret;

public final class KnockbackContext {
    private static final ThreadLocal<Boolean> NO_KB = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private KnockbackContext() {}

    public static void enableNoKB() { NO_KB.set(Boolean.TRUE); }
    public static void disableNoKB() { NO_KB.set(Boolean.FALSE); }
    public static boolean isNoKB() { return Boolean.TRUE.equals(NO_KB.get()); }
}
