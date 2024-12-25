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
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainListener implements Listener {

    private static final double ITEM_DISTANCE_FROM_PLAYER = 2.0;
    private static final int ITEM_COUNT = 3;
    private static final double ITEM_ANGLE_GAP = 360D / ITEM_COUNT;

    private final Map<UUID, ItemDisplay[]> orbitingItems = new HashMap<>();
    private final JavaPlugin plugin;

    public MainListener(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer()
                .sendMessage(Component.text("welcome, " + event.getPlayer().getName() + "!",
                                            TextColor.color(187, 233, 255)));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.getPlayer()
                .sendMessage(
                        Component.text("byebye, " + event.getPlayer().getName() + "!", TextColor.color(255, 254, 211)));
        this.orbitingItems.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (event.getHand() == EquipmentSlot.HAND &&
            player.getInventory().getItemInMainHand().getType() == Material.IRON_SWORD &&
            event.getAction().isRightClick()) {
            if (this.orbitingItems.remove(player.getUniqueId()) != null) {
                player.sendActionBar(Component.text("Sword spinners deactivated.", TextColor.color(187, 233, 255)));
                return;
            }

            final Location playerLocation = player.getLocation();
            final ItemDisplay[] items = new ItemDisplay[ITEM_COUNT];

            this.orbitingItems.put(player.getUniqueId(), items);

            for (int i = 0; i < ITEM_COUNT; i++) {
                final int finalI = i;
                items[i] = playerLocation.getWorld().spawn(playerLocation.clone(), ItemDisplay.class, itemDisplay -> {
                    itemDisplay.setItemStack(new ItemStack(Material.IRON_SWORD));

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
                    final double angle = System.currentTimeMillis() * 0.25d % 3600 / 360.0 * Math.toRadians(360) +
                                         Math.toRadians(i * 120);
                    final double x = currentLocation.getX() + ITEM_DISTANCE_FROM_PLAYER * Math.cos(angle);
                    final double y = currentLocation.getY() + 1;
                    final double z = currentLocation.getZ() + ITEM_DISTANCE_FROM_PLAYER * Math.sin(angle);

                    items[i].teleport(new Location(currentLocation.getWorld(), x, y, z));
                    rotateItemDisplay(items[i], angle);
                }
            }, 0, 1);

            player.sendActionBar(Component.text("Sword spinner activated.", TextColor.color(187, 233, 255)));
        }
    }

    private static void rotateItemDisplay(ItemDisplay itemDisplay, double angle) {
        final var transformation = itemDisplay.getTransformation();

        transformation.getLeftRotation().set(new AxisAngle4f((float) Math.toRadians(90d), 1f, 0f, 0f));
        transformation.getRightRotation().set(new AxisAngle4f((float) (angle + Math.toRadians(225d)), 0f, 0f, 1f));

        itemDisplay.setTransformation(transformation);
    }
}