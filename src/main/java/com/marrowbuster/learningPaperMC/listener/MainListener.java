package com.marrowbuster.learningPaperMC.listener;

import com.google.common.base.Preconditions;
import com.marrowbuster.learningPaperMC.LearningPaperMC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

public class MainListener implements Listener {

    /**
     * Known set of weapon materials. Nominally contains each type of sword.
     */
    private static final Set<Material> KNOWN_WEAPONS =
            EnumSet.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD,
                       Material.DIAMOND_SWORD, Material.NETHERITE_SWORD);

    public static final int MIN_ITEM_COUNT = 3;
    public static final int MAX_ITEM_COUNT = 7;

    /**
     * Distance from which the swords orbit around the player.
     */
    private static final double ITEM_DISTANCE_FROM_PLAYER = 2.0;

    private static final double THROWN_ITEM_TRAVEL_OFFSET = 0.2;

    /**
     * Degrees in a circle.
     */
    private static final double DEGREES_IN_CIRCLE = 360d;

    /**
     * Value by which the System.currentTimeMillis() parameter in the bukkit scheduler lambda function in
     * onPlayerRightClick is modded by. Nominally 10 * DEGREES_IN_CIRCLE = 3600.
     */
    private static final double MILLIS_MOD_PERIOD = 10 * DEGREES_IN_CIRCLE;

    /**
     * Timescale by which the System.currentTimeMillis() parameter in the bukkit scheduler lambda function in
     * onPlayerRightClick is multiplied by. Controls how quickly (or slowly) the swords rotate around the player.
     */
    private static final double TIMESCALE = 0.2d;

    /**
     * HashMap of player settings and the summoned sword ItemDisplays which orbit around the player that summoned them.
     */
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    /**
     * Main plugin that this class is registered to.
     */
    private final JavaPlugin plugin;

    /**
     * Constructor; creates a new MainListener for the given plugin.
     *
     * @param plugin {@link JavaPlugin} The main plugin class that calls upon this constructor
     */
    public MainListener(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void cleanup() {
        this.playerDataMap.values().forEach(data -> {
            data.orbitingItems.forEach(Entity::remove);
            data.orbitingItems.clear();
        });
    }

    public PlayerData getPlayerData(UUID uniqueId) {
        return this.playerDataMap.get(uniqueId);
    }

    /**
     * Sends a greeting message to the player upon entering.
     *
     * @param event {@link PlayerJoinEvent} Player join event.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer()
                .sendMessage(Component.text("welcome, " + event.getPlayer().getName() + "!", NamedTextColor.AQUA));

        this.playerDataMap.computeIfAbsent(event.getPlayer().getUniqueId(), uuid -> new PlayerData(
                (int) LearningPaperMC.getInstance()
                        .getConfig()
                        .get(event.getPlayer().getUniqueId() + ".item-count", MIN_ITEM_COUNT)));
    }

    /**
     * Sends a farewell message to the player upon leaving. (probs redundant, might wanna change this to a server
     * broadcast)
     *
     * @param event {@link PlayerQuitEvent} Player quit event.
     */
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.getPlayer()
                .sendMessage(Component.text("byebye, " + event.getPlayer().getName() + "!", NamedTextColor.YELLOW));

        despawnItems(this.playerDataMap.get(event.getPlayer().getUniqueId()));
    }

    /**
     * Spawns or despawns a "sword spinner" around the player upon a right-click event with a sword of any type in
     * hand.
     *
     * @param event {@link PlayerInteractEvent} Player interaction event. Method looks for occurrence of a right click.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        if (event.getItem() != null && KNOWN_WEAPONS.contains(event.getItem().getType()) &&
            event.getAction().isRightClick()) {
            final PlayerData data = this.playerDataMap.get(uuid);

            if (!data.orbitingItems.isEmpty()) {
                despawnItems(data);
                return;
            }

            final Location playerLocation = player.getLocation();
            final ItemStack itemStack = new ItemStack(event.getItem().getType());

            IntStream.range(0, data.itemCount).forEach(index -> addItemDisplay(data, itemStack, playerLocation));

            Bukkit.getScheduler().runTaskTimer(this.plugin, task -> {
                if (data.orbitingItems.isEmpty()) {
                    task.cancel();
                    player.sendActionBar(Component.text("Sword spinner deactivated.", NamedTextColor.YELLOW));
                    return;
                }

                final Location currentLocation = player.getLocation();

                for (int index = 0; index < data.orbitingItems.size(); index++) {
                    final var itemDisplay = data.orbitingItems.get(index);
                    final double angle =
                            System.currentTimeMillis() * TIMESCALE % MILLIS_MOD_PERIOD / DEGREES_IN_CIRCLE *
                            Math.toRadians(DEGREES_IN_CIRCLE) +
                            Math.toRadians(index * DEGREES_IN_CIRCLE / data.itemCount);

                    itemDisplay.teleport(new Location(currentLocation.getWorld(), currentLocation.getX() +
                                                                                  ITEM_DISTANCE_FROM_PLAYER *
                                                                                  Math.cos(angle),
                                                      currentLocation.getY() + 1, currentLocation.getZ() +
                                                                                  ITEM_DISTANCE_FROM_PLAYER *
                                                                                  Math.sin(angle)));
                    rotateItemDisplay(data.orbitingItems.get(index), angle);
                }
            }, 0, 1);

            player.sendActionBar(Component.text("Sword spinner activated. Stay safe!", NamedTextColor.AQUA));
        } else if (event.getItem() == null && event.getAction().isLeftClick()) {
            final PlayerData data = this.playerDataMap.get(uuid);

            if (!data.orbitingItems.isEmpty()) {
                final ItemDisplay itemDisplay = data.orbitingItems.removeLast();
                final Location spawnLocation = player.getLocation();

                // intuition: finding angle from the arctangent of the x and z vectors
                final double angle =
                        Math.atan(spawnLocation.getDirection().getZ() / spawnLocation.getDirection().getX());

                rotateItemDisplay(itemDisplay, angle);

                if (data.thrownItems.isEmpty()) {
                    Bukkit.getScheduler().runTaskTimer(this.plugin, task -> {
                        if (data.thrownItems.isEmpty()) {
                            task.cancel();
                            return;
                        }

                        data.thrownItems.removeIf(thrownItem -> {
                            shiftItemDisplay(thrownItem, angle);
                            final boolean remove = thrownItem.getLocation().distance(player.getLocation()) > 50d;

                            if (remove) {
                                thrownItem.remove();
                            }

                            return remove;
                        });
                    }, 0, 1);
                }

                data.thrownItems.add(itemDisplay);
            }
        }
    }

    private static void addItemDisplay(PlayerData data, ItemStack itemStack, Location playerLocation) {
        data.orbitingItems.add(playerLocation.getWorld()
                                       .spawn(playerLocation.clone(), ItemDisplay.class,
                                              itemDisplay -> itemDisplay.setItemStack(itemStack)));
    }

    private static void despawnItems(PlayerData data) {
        data.orbitingItems.forEach(Entity::remove);
        data.orbitingItems.clear();
    }

    /**
     * Rotates a given ItemDisplay by a certain angle.
     *
     * @param itemDisplay {@link ItemDisplay} The ItemDisplay to transform.
     * @param angle       Angle by which to rotate the aforementioned itemDisplay.
     */
    private static void rotateItemDisplay(ItemDisplay itemDisplay, double angle) {
        final Transformation transformation = itemDisplay.getTransformation();

        transformation.getLeftRotation().set(new AxisAngle4f((float) Math.toRadians(90d), 1f, 0f, 0f));
        transformation.getRightRotation().set(new AxisAngle4f((float) (angle + Math.toRadians(225d)), 0f, 0f, 1f));

        itemDisplay.setTransformation(transformation);
    }

    // inuition; follow the trajectory　along which the player looks
    private static void shiftItemDisplay(ItemDisplay itemDisplay, double angle) {
        final var location = itemDisplay.getLocation();
        itemDisplay.teleport(
                new Location(location.getWorld(), location.getX() + THROWN_ITEM_TRAVEL_OFFSET * Math.cos(angle),
                             location.getY(), location.getZ() + THROWN_ITEM_TRAVEL_OFFSET * Math.sin(angle)));
    }

    public static class PlayerData {
        private final List<ItemDisplay> orbitingItems = new ArrayList<>(MIN_ITEM_COUNT);
        private final List<ItemDisplay> thrownItems = new ArrayList<>(MIN_ITEM_COUNT);

        private int itemCount;

        public PlayerData(int itemCount) {
            Preconditions.checkArgument(itemCount >= MIN_ITEM_COUNT && itemCount <= 7,
                                        "Item count needs to be between %s and %s", MIN_ITEM_COUNT, MAX_ITEM_COUNT);
            this.itemCount = itemCount;
        }

        public void setItemCount(int newItemCount, @NotNull Player player) {
            if (!this.orbitingItems.isEmpty()) {
                int difference = this.itemCount - newItemCount;

                if (difference > 0) {
                    while (difference-- > 0) {
                        this.orbitingItems.removeLast().remove();
                    }
                } else if (difference < 0) {
                    final ItemStack itemStack = this.orbitingItems.getLast().getItemStack();

                    while (difference++ < 0) {
                        addItemDisplay(this, itemStack, player.getLocation());
                    }
                }
            }

            this.itemCount = newItemCount;
            LearningPaperMC.getInstance().getConfig().set(player.getUniqueId() + ".item-count", newItemCount);
        }
    }
}