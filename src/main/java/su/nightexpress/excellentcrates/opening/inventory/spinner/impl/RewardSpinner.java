package su.nightexpress.excellentcrates.opening.inventory.spinner.impl;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentcrates.api.crate.Reward;
import su.nightexpress.excellentcrates.config.Config;
import su.nightexpress.excellentcrates.crate.impl.Crate;
import su.nightexpress.excellentcrates.crate.impl.Rarity;
import su.nightexpress.excellentcrates.opening.inventory.InventoryOpening;
import su.nightexpress.excellentcrates.opening.inventory.spinner.AbstractSpinner;
import su.nightexpress.excellentcrates.opening.inventory.spinner.EdgeMode;
import su.nightexpress.excellentcrates.opening.inventory.spinner.EdgingSettings;
import su.nightexpress.excellentcrates.opening.inventory.spinner.SpinMode;
import su.nightexpress.excellentcrates.opening.inventory.spinner.SpinnerData;
import su.nightexpress.excellentcrates.Placeholders;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.random.Rnd;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RewardSpinner extends AbstractSpinner {

    private static final int NO_SPIN = -1;

    private final Set<Rarity>    rarities;
    private final EdgingSettings edging;

    private int rewardIndex;

    private Reward baitReward;
    private int    baitSpin = NO_SPIN;
    private int    baitSlot = NO_SPIN;

    public RewardSpinner(@NotNull SpinnerData data, @NotNull InventoryOpening opening, @NotNull Set<Rarity> rarities) {
        this(data, opening, rarities, EdgingSettings.disabled());
    }

    public RewardSpinner(@NotNull SpinnerData data,
                         @NotNull InventoryOpening opening,
                         @NotNull Set<Rarity> rarities,
                         @NotNull EdgingSettings edging) {
        super(data, opening);
        this.rarities = rarities;
        this.edging = edging;
        this.rewardIndex = opening.getRewards().size(); // Start from latest index after previous reward spinners added their rewards.

        this.prepareRewards();
        this.prepareEdging();
    }

    private boolean isWinSlot(int slot) {
        return Lists.contains(this.winSlots, slot);
    }

    private void prepareRewards() {
        for (int winSlot : this.winSlots) {
            if (Lists.contains(this.slots, winSlot)) {
                this.opening.addReward(this.rollReward(false));
            }
        }
    }

    /**
     * Picks a rare reward to be displayed right next to the win slot, so the animation looks like the player
     * barely missed it. The bait is a filler item only, the actual reward is already rolled and untouched.
     */
    private void prepareEdging() {
        if (!this.edging.hasNearMiss()) return;

        // This spinner does not land a real reward, so there is nothing to miss.
        if (this.rewardIndex >= this.opening.getRewards().size()) return;

        int winSpin = this.getWinSpin();
        if (winSpin == NO_SPIN) return;

        Set<Rarity> baitRarities = this.getBaitRarities();
        if (baitRarities.isEmpty()) return;

        if (this.edging.isSkipOnWin() && this.isRolledRarity(baitRarities)) return;

        if (!Rnd.chance(this.edging.getBaitChance())) return;

        int spin = this.getBaitSpin(winSpin);
        if (spin == NO_SPIN) return;

        this.baitReward = this.opening.getCrate().rollReward(this.opening.getPlayer(), Rnd.get(baitRarities));
        this.baitSpin = spin;
        this.baitSlot = this.getWinSlot();
    }

    /**
     * @return Amount of the remaining spins at the moment a real reward is put on the reel, or {@link #NO_SPIN}
     * when this spinner does not display one.
     */
    private int getWinSpin() {
        int winSlot = this.getWinSlot();
        if (winSlot == NO_SPIN) return NO_SPIN;

        // Sequental spinners shift items towards the win slot, so a reward must be placed that many spins earlier.
        if (this.data.getMode() == SpinMode.SEQUENTAL) return Lists.indexOf(this.slots, winSlot) + 1;

        return 1;
    }

    private int getWinSlot() {
        for (int winSlot : this.winSlots) {
            if (Lists.contains(this.slots, winSlot)) return winSlot;
        }
        return NO_SPIN;
    }

    /**
     * @return Amount of the remaining spins at which the bait must be put on the reel. A bait placed one spin
     * earlier than the reward overshoots the win slot, one spin later stops short of it.
     */
    private int getBaitSpin(int winSpin) {
        int offset = switch (this.edging.getEdgeMode()) {
            case OVERSHOOT -> 1;
            case UNDERSHOOT -> -1;
            case RANDOM -> Rnd.nextBoolean() ? 1 : -1;
        };

        if (this.isBaitSpin(winSpin + offset)) return winSpin + offset;
        if (this.isBaitSpin(winSpin - offset)) return winSpin - offset;

        return NO_SPIN;
    }

    private boolean isBaitSpin(int spinsLeft) {
        if (spinsLeft < 1 || spinsLeft > this.requiredSpins) return false;

        // Never take over a spin that puts a real reward into a win slot.
        return !this.isPredictedSpin(spinsLeft);
    }

    private boolean isRolledRarity(@NotNull Set<Rarity> rarities) {
        List<Reward> rewards = this.opening.getRewards();

        return rewards.subList(Math.min(this.rewardIndex, rewards.size()), rewards.size()).stream()
            .anyMatch(reward -> rarities.contains(reward.getRarity()));
    }

    /**
     * @return Rarities this spinner may use as a near miss bait, defaulting to the rarest one available.
     */
    @NotNull
    private Set<Rarity> getBaitRarities() {
        Crate crate = this.opening.getCrate();
        Player player = this.opening.getPlayer();

        Set<Rarity> available = new HashSet<>();
        this.rarities.forEach(rarity -> {
            if (crate.hasRewards(player, rarity)) available.add(rarity);
        });

        Set<String> configured = this.edging.getBaitRarities();
        if (!configured.isEmpty() && !configured.contains(Placeholders.WILDCARD)) {
            available.removeIf(rarity -> !configured.contains(rarity.getId()));
            return available;
        }

        return available.stream()
            .min(Comparator.comparingDouble(Rarity::getWeight))
            .map(Set::of)
            .orElseGet(Set::of);
    }

    @NotNull
    private Reward rollReward(boolean visual) {
        Crate crate = this.opening.getCrate();
        Player player = this.opening.getPlayer();

        boolean ramped = visual && this.edging.isRampActive(this.getSpinsLeft());

        if (!visual || ramped || Config.OPENINGS_GUI_SIMULATE_REAL_CHANCES.get()) {
            double progress = ramped ? this.edging.getRampProgress(this.getSpinsLeft()) : 0D;

            Map<Rarity, Double> rarityMap = new HashMap<>();
            this.rarities.forEach(rarity -> {
                if (crate.hasRewards(player, rarity)) {
                    rarityMap.put(rarity, ramped ? this.edging.getRampedWeight(rarity.getWeight(), progress) : rarity.getWeight());
                }
            });
            if (rarityMap.isEmpty()) throw new IllegalStateException("No rewards available!");

            Rarity rarity = Rnd.getByWeight(rarityMap);
            return crate.rollReward(this.opening.getPlayer(), rarity);
        }
        else {
            List<Reward> rewards = crate.getRewards(player);
            rewards.removeIf(reward -> !this.rarities.contains(reward.getRarity()));
            if (rewards.isEmpty()) throw new IllegalStateException("No rewards available!");

            return Rnd.get(rewards);
        }
    }

    @Override
    @NotNull
    public ItemStack createItem(int slot) {
        Reward reward = this.shouldUsePredictedReward(slot) ? this.opening.getRewards().get(this.rewardIndex++) : this.rollVisualReward(slot);
        if (reward == null) return new ItemStack(Material.AIR);

        return reward.getPreviewItem();
    }

    @Nullable
    private Reward rollVisualReward(int slot) {
        Reward bait = this.pollBaitReward(slot);

        return bait == null ? this.rollReward(true) : bait;
    }

    @Nullable
    private Reward pollBaitReward(int slot) {
        if (this.baitReward == null || this.getSpinsLeft() != this.baitSpin) return null;

        // Slot-wise spinners fill every slot on a spin, so the bait must be limited to the win slot itself.
        SpinMode mode = this.data.getMode();
        if ((mode == SpinMode.INDEPENDENT || mode == SpinMode.RANDOM) && slot != this.baitSlot) return null;

        Reward bait = this.baitReward;
        this.baitReward = null; // The bait is displayed exactly once.
        return bait;
    }

    private int getSpinsLeft() {
        return Math.toIntExact(this.requiredSpins - this.spinCount);
    }

    private boolean shouldUsePredictedReward(int slot) {
        if (this.rewardIndex >= this.opening.getRewards().size()) return false;

        SpinMode mode = this.data.getMode();
        if (mode == SpinMode.INDEPENDENT || mode == SpinMode.RANDOM) {
            return this.getSpinsLeft() == 1 && this.isWinSlot(slot);
        }

        return this.isPredictedSpin(this.getSpinsLeft());
    }

    private boolean isPredictedSpin(int spinsLeft) {
        if (this.data.getMode() != SpinMode.SEQUENTAL) return spinsLeft == 1;

        for (int winSlot : this.winSlots) {
            int index = Lists.indexOf(this.slots, winSlot) + 1;
            if (index > 0 && spinsLeft == index) return true;
        }

        return false;
    }

    @Override
    protected void spinRandom() {
        this.spinIndependent(); // Random mode makes no sense for reward spinners. Also it's not possible to predict reward for it.
    }

    @Override
    protected void onStop() {

    }
}
