package com.marrowbuster.learningPaperMC.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainListener implements Listener {

    /**
     * Known set of weapon materials. Nominally contains each type of sword.
     */
    private static final Set<Material> KNOWN_WEAPONS =
            EnumSet.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD,
                       Material.DIAMOND_SWORD, Material.NETHERITE_SWORD);
    /**
     * Distance from which the swords orbit around the player.
     */
    private static final double ITEM_DISTANCE_FROM_PLAYER = 2.0;

    /**
     * Number of swords that orbit around the player.
     */
    private static final int ITEM_COUNT = 5;

    /**
     * Degrees in a circle.
     */
    private static final double DEGREES_IN_CIRCLE = 360d;

    /**
     * Amount by which each sword is rotated from the next, depending on the number of swords.
     */
    private static final double ITEM_ANGLE_GAP = DEGREES_IN_CIRCLE / ITEM_COUNT;

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
     * HashMap of the summoned sword ItemDisplays which orbit around the player that summoned them.
     */
    private final Map<UUID, ItemDisplay[]> orbitingItems = new HashMap<>();

    /**
     * Main plugin that this class is registered to.
     */
    private final JavaPlugin plugin;

    /**
     * Constructor; creates a new MainListener for the given plugin.
     *
     * @param plugin     {@link JavaPlugin} The main plugin class that calls upon this constructor
     */

    public MainListener(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Sends a greeting message to the player upon entering.
     *
     * @param event     {@link PlayerJoinEvent} Player join event.
     */

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer()
                .sendMessage(Component.text("welcome, " + event.getPlayer().getName() + "!",
                                            TextColor.color(187, 233, 255)));
    }

    /**
     * Sends a farewell message to the player upon leaving. (probs redundant, might wanna change this to a server broadcast)
     *
     * @param event     {@link PlayerQuitEvent} Player quit event.
     */

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.getPlayer()
                .sendMessage(
                        Component.text("byebye, " + event.getPlayer().getName() + "!", TextColor.color(255, 254, 211)));
        this.orbitingItems.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Spawns or despawns a "sword spinner" around the player upon a right-click event with a sword of any type in hand.
     *
     * @param event     {@link PlayerInteractEvent} Player interaction event. Method looks for occurrence of a right click.
     */

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (event.getHand() == EquipmentSlot.HAND && event.getItem() != null &&
            KNOWN_WEAPONS.contains(event.getItem().getType()) && event.getAction().isRightClick()) {
            if (this.orbitingItems.remove(player.getUniqueId()) != null) {
                player.sendActionBar(Component.text("Sword spinner deactivated.", TextColor.color(187, 233, 255)));
                return;
            }

            final Location playerLocation = player.getLocation();
            final ItemStack itemStack = new ItemStack(event.getItem().getType());
            final ItemDisplay[] items = new ItemDisplay[ITEM_COUNT];

            this.orbitingItems.put(player.getUniqueId(), items);

            for (int i = 0; i < ITEM_COUNT; i++) {
                final int finalI = i;
                items[i] = playerLocation.getWorld().spawn(playerLocation.clone(), ItemDisplay.class, itemDisplay -> {
                    itemDisplay.setItemStack(itemStack);

                    final double angle = Math.toRadians(finalI * ITEM_ANGLE_GAP);
                    final double x = playerLocation.getX() + ITEM_DISTANCE_FROM_PLAYER * Math.cos(angle);
                    final double y = playerLocation.getY() + 1;
                    final double z = playerLocation.getZ() + ITEM_DISTANCE_FROM_PLAYER * Math.sin(angle);

                    itemDisplay.teleport(new Location(playerLocation.getWorld(), x, y, z));
                    rotateItemDisplay(itemDisplay, angle);
                });
            }

            Bukkit.getScheduler().runTaskTimer(this.plugin, task -> {
                if (!this.orbitingItems.containsKey(player.getUniqueId())) {
                    task.cancel();

                    for (ItemDisplay item : items) {
                        item.remove();
                    }

                    return;
                }

                final Location currentLocation = player.getLocation();

                for (int i = 0; i < ITEM_COUNT; i++) {
                    final double angle = System.currentTimeMillis() * TIMESCALE % MILLIS_MOD_PERIOD / DEGREES_IN_CIRCLE * Math.toRadians(DEGREES_IN_CIRCLE) +
                                         Math.toRadians(i * ITEM_ANGLE_GAP);
                    final double x = currentLocation.getX() + ITEM_DISTANCE_FROM_PLAYER * Math.cos(angle);
                    final double y = currentLocation.getY() + 1;
                    final double z = currentLocation.getZ() + ITEM_DISTANCE_FROM_PLAYER * Math.sin(angle);

                    items[i].teleport(new Location(currentLocation.getWorld(), x, y, z));
                    rotateItemDisplay(items[i], angle);
                }
            }, 0, 1);

            player.sendActionBar(Component.text("Sword spinner activated. Stay safe!", TextColor.color(187, 233, 255)));
        }
    }

    /**
     * Rotates a given ItemDisplay by a certain angle.
     *
     * @param itemDisplay   {@link ItemDisplay} The ItemDisplay to transform.
     * @param angle         Angle by which to rotate the aforementioned itemDisplay.
     */

    private static void rotateItemDisplay(ItemDisplay itemDisplay, double angle) {
        final Transformation transformation = itemDisplay.getTransformation();

        transformation.getLeftRotation().set(new AxisAngle4f((float) Math.toRadians(90d), 1f, 0f, 0f));
        transformation.getRightRotation().set(new AxisAngle4f((float) (angle + Math.toRadians(225d)), 0f, 0f, 1f));

        itemDisplay.setTransformation(transformation);
        itemDisplay.setInterpolationDelay(0);
    }
}