package su.nightexpress.excellentcrates.opening.inventory.spinner;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Purely cosmetic 'near miss' settings of a reward spinner.
 * <p>
 * None of these settings affect the final reward: it is rolled when a crate is opened and is always placed
 * into the win slot. Edging only replaces filler (visual) items of the animation.
 */
public class EdgingSettings implements Writeable {

    private static final String ENABLED       = ".Enabled";
    private static final String CHANCE        = ".Near_Miss.Chance";
    private static final String RARITIES      = ".Near_Miss.Rarities";
    private static final String MODE          = ".Near_Miss.Mode";
    private static final String SKIP_ON_WIN   = ".Near_Miss.Skip_On_Win";
    private static final String RAMP_ENABLED  = ".Rarity_Ramp.Enabled";
    private static final String RAMP_SPINS    = ".Rarity_Ramp.Spins";
    private static final String RAMP_STRENGTH = ".Rarity_Ramp.Strength";

    private static final String[] ENABLED_INFO = {
        "[ THIS SETTING DOES NOT AFFECT THE FINAL REWARD, IT IS PREDICTED WHEN PLAYER OPENED A CRATE ]",
        "Controls whether this spinner teases players with rare rewards near the end of the animation.",
        "Only filler (visual) items of the animation are affected."
    };

    private static final String[] CHANCE_INFO = {
        "Chance (in percent) for an opening to display a 'near miss': a bait reward that lands right next to the win slot.",
        "Set to 0 to disable near misses and keep the rarity ramp only."
    };

    private static final String[] RARITIES_INFO = {
        "Rarity ids used as a bait for near misses.",
        "Leave empty to automatically use the rarest rarity available for this spinner."
    };

    private static final String[] MODE_INFO = {
        "Where the bait reward stops relative to the win slot.",
        "OVERSHOOT - bait slides one position past the win slot.",
        "UNDERSHOOT - bait stops one position short of the win slot.",
        "RANDOM - picks one of the above for every opening."
    };

    private static final String[] SKIP_ON_WIN_INFO = {
        "Controls whether near misses are skipped when a player actually won a bait rarity reward."
    };

    private static final String[] RAMP_ENABLED_INFO = {
        "Controls whether rarer rewards are displayed more often as the animation approaches its end."
    };

    private static final String[] RAMP_SPINS_INFO = {
        "Amount of the final spins the ramp is applied to."
    };

    private static final String[] RAMP_STRENGTH_INFO = {
        "How hard rarity weights are bent on the final spin.",
        "0.0 - no bending, 1.0 - all rarities are equally common, 2.0 - rarity weights are fully inverted."
    };

    private final boolean     enabled;
    private final double      baitChance;
    private final Set<String> baitRarities;
    private final EdgeMode    edgeMode;
    private final boolean     skipOnWin;
    private final boolean     rampEnabled;
    private final int         rampSpins;
    private final double      rampStrength;

    public EdgingSettings(boolean enabled,
                          double baitChance,
                          @NotNull Set<String> baitRarities,
                          @NotNull EdgeMode edgeMode,
                          boolean skipOnWin,
                          boolean rampEnabled,
                          int rampSpins,
                          double rampStrength) {
        this.enabled = enabled;
        this.baitChance = baitChance;
        this.baitRarities = baitRarities.stream().map(String::toLowerCase).collect(Collectors.toCollection(HashSet::new));
        this.edgeMode = edgeMode;
        this.skipOnWin = skipOnWin;
        this.rampEnabled = rampEnabled;
        this.rampSpins = rampSpins;
        this.rampStrength = rampStrength;
    }

    @NotNull
    public static EdgingSettings disabled() {
        return new EdgingSettings(false, 35D, new HashSet<>(), EdgeMode.RANDOM, true, true, 12, 2D);
    }

    @NotNull
    public static EdgingSettings defaults() {
        return new EdgingSettings(true, 35D, new HashSet<>(), EdgeMode.RANDOM, true, true, 12, 2D);
    }

    @NotNull
    public static EdgingSettings read(@NotNull FileConfig config, @NotNull String path) {
        EdgingSettings defaults = disabled();

        boolean enabled = ConfigValue.create(path + ENABLED, defaults.isEnabled(), ENABLED_INFO).read(config);

        double baitChance = ConfigValue.create(path + CHANCE, defaults.getBaitChance(), CHANCE_INFO).read(config);

        Set<String> baitRarities = ConfigValue.create(path + RARITIES, defaults.getBaitRarities(), RARITIES_INFO).read(config);

        EdgeMode edgeMode = ConfigValue.create(path + MODE, EdgeMode.class, defaults.getEdgeMode(), MODE_INFO).read(config);

        boolean skipOnWin = ConfigValue.create(path + SKIP_ON_WIN, defaults.isSkipOnWin(), SKIP_ON_WIN_INFO).read(config);

        boolean rampEnabled = ConfigValue.create(path + RAMP_ENABLED, defaults.isRampEnabled(), RAMP_ENABLED_INFO).read(config);

        int rampSpins = ConfigValue.create(path + RAMP_SPINS, defaults.getRampSpins(), RAMP_SPINS_INFO).read(config);

        double rampStrength = ConfigValue.create(path + RAMP_STRENGTH, defaults.getRampStrength(), RAMP_STRENGTH_INFO).read(config);

        return new EdgingSettings(enabled, baitChance, baitRarities, edgeMode, skipOnWin, rampEnabled, rampSpins, rampStrength);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        this.write(config, path + ENABLED, this.enabled, ENABLED_INFO);
        this.write(config, path + CHANCE, this.baitChance, CHANCE_INFO);
        this.write(config, path + RARITIES, this.baitRarities, RARITIES_INFO);
        this.write(config, path + MODE, this.edgeMode.name(), MODE_INFO);
        this.write(config, path + SKIP_ON_WIN, this.skipOnWin, SKIP_ON_WIN_INFO);
        this.write(config, path + RAMP_ENABLED, this.rampEnabled, RAMP_ENABLED_INFO);
        this.write(config, path + RAMP_SPINS, this.rampSpins, RAMP_SPINS_INFO);
        this.write(config, path + RAMP_STRENGTH, this.rampStrength, RAMP_STRENGTH_INFO);
    }

    private void write(@NotNull FileConfig config, @NotNull String path, @NotNull Object value, String... info) {
        config.set(path, value);
        config.setComments(path, info);
    }

    public boolean hasNearMiss() {
        return this.enabled && this.baitChance > 0D;
    }

    public boolean isRampActive(int spinsLeft) {
        return this.enabled && this.rampEnabled && this.rampSpins > 0 && spinsLeft <= this.rampSpins;
    }

    /**
     * @return How far the animation is into the ramp window, where {@code 1.0} is its very last spin.
     */
    public double getRampProgress(int spinsLeft) {
        if (this.rampSpins <= 0) return 0D;

        double progress = (double) (this.rampSpins - spinsLeft + 1) / (double) this.rampSpins;
        return Math.clamp(progress, 0D, 1D);
    }

    /**
     * Bends a rarity weight towards the rarest ones. Since a smaller weight means a rarer rarity, a negative
     * exponent flips the weights around and makes rare rarities the most common ones on the reel.
     */
    public double getRampedWeight(double weight, double progress) {
        if (weight <= 0D) return 0D;

        return Math.pow(weight, 1D - (progress * this.rampStrength));
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public double getBaitChance() {
        return this.baitChance;
    }

    @NotNull
    public Set<String> getBaitRarities() {
        return this.baitRarities;
    }

    @NotNull
    public EdgeMode getEdgeMode() {
        return this.edgeMode;
    }

    public boolean isSkipOnWin() {
        return this.skipOnWin;
    }

    public boolean isRampEnabled() {
        return this.rampEnabled;
    }

    public int getRampSpins() {
        return this.rampSpins;
    }

    public double getRampStrength() {
        return this.rampStrength;
    }
}
