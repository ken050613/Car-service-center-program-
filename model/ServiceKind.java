package apu.model;

/**
 * Normal service uses a 1-hour slot; major service uses a 3-hour slot (assignment spec).
 * Catalog codes {@link #catalogCode()} hold the manager-set prices for each tier.
 */
public enum ServiceKind {
    NORMAL(60, "NORMAL"),
    MAJOR(180, "MAJOR");

    private final int durationMinutes;
    private final String catalogCode;

    ServiceKind(int durationMinutes, String catalogCode) {
        this.durationMinutes = durationMinutes;
        this.catalogCode = catalogCode;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    /** Code used in {@link ServiceItem} / price catalogue for this tier. */
    public String catalogCode() {
        return catalogCode;
    }
}
