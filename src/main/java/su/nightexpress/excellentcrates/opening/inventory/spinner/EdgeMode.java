package su.nightexpress.excellentcrates.opening.inventory.spinner;

public enum EdgeMode {
    /**
     * Bait reward slides one position past the win slot.
     */
    OVERSHOOT,
    /**
     * Bait reward stops one position short of the win slot.
     */
    UNDERSHOOT,
    /**
     * Picks {@link #OVERSHOOT} or {@link #UNDERSHOOT} at random for every opening.
     */
    RANDOM
}
