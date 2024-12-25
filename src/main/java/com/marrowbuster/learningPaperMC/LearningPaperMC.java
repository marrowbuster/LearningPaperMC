package com.marrowbuster.learningPaperMC;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LearningPaperMC extends JavaPlugin implements Listener {

    private static JavaPlugin instance;

    private Map<UUID, ItemDisplay[]> orbitingItems = new HashMap<>();

    public static JavaPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        instance = this;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(Component.text("welcome, " + event.getPlayer().getName() + "!"));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.getPlayer().sendMessage(Component.text("byebye, " + event.getPlayer().getName() + "!"));
        this.orbitingItems.remove(event.getPlayer().getUniqueId());
    }

    // OLD USER CODE

    /* @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        ItemStack ironSword = new ItemStack(Material.IRON_SWORD, 1);
        Player p = event.getPlayer();
        if (p.getInventory().getItemInMainHand().isSimilar(ironSword)) {
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            {
                event.getPlayer().sendMessage(Component.text(event.getPlayer().getName() + " threw a sword into terrain."));
            }
            else if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                event.getPlayer().sendMessage(Component.text(event.getPlayer().getName() + " threw a sword into the sky."));
            }
            Location playerLocation = p.getLocation();

            ItemDisplay[] items = new ItemDisplay[3];
            for (int i = 0; i < 3; i++) {
                int finalI = i;
                items[finalI] = playerLocation.getWorld().spawn(playerLocation.clone(), ItemDisplay.class, (disp) -> {
                    disp.setItemStack(new ItemStack(Material.IRON_SWORD));

                    Transformation xform = disp.getTransformation();
                    xform.getTranslation().set(new Vector3f((float) Math.sin(finalI * Math.toRadians(120d)), 1f, (float) Math.cos(finalI * Math.toRadians(120d))));
                    xform.getLeftRotation().set(new AxisAngle4f((float) Math.toRadians(90d), 1f, 0f, 0f));
                    // ugly and inefficient but this shit works at least -.-;;;
                    xform.getRightRotation().set(new AxisAngle4f((float) Math.toRadians(-45d + finalI * -120d), 0f, 0f, 1f));
                    disp.setTransformation(xform);
                });
                orbitingItems.put(p.getUniqueId(), items);
                Bukkit.getScheduler().runTaskTimer(instance, task -> {
                    if (!items[finalI].isValid()) { // display was removed from the world, abort task
                        task.cancel();
                        return;
                    }
                    Transformation xform = items[finalI].getTransformation();
                    xform.getTranslation().set(new Vector3f((float) Math.sin((finalI + 1) * Math.toRadians(120d)), 1f, (float) Math.cos((finalI + 1) * Math.toRadians(100d))));
                    xform.getRightRotation().set(new AxisAngle4f((float) Math.toRadians(-45d + (finalI + 1) * -120d), 0f, 0f, 1f));
                    items[finalI].setInterpolationDelay(0); // no delay to the interpolation
                    items[finalI].setInterpolationDuration(100); // set the duration of the interpolated rotation
                }, 1, 100);
            }
        }
    } */

    // NEW CODE GIVEN BY NONE OTHER THAN CUNTGPT

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        ItemStack ironSword = new ItemStack(Material.IRON_SWORD, 1);
        Player player = event.getPlayer();

        if (event.getHand() == EquipmentSlot.HAND && player.getInventory().getItemInMainHand().isSimilar(ironSword)) {
            if (event.getAction().isRightClick()) {
                final var existingItems = this.orbitingItems.remove(player.getUniqueId());

                if (existingItems != null) {
                    for (final var itemDisplay : existingItems) {
                        itemDisplay.remove();
                    }

                    player.sendActionBar(Component.text("Sword spinners deactivated.", TextColor.color(255, 208, 89)));
                    return;
                }

                player.sendMessage(Component.text(player.getName() + " activated the sword spinner!"));

                Location playerLocation = player.getLocation();
                ItemDisplay[] items = new ItemDisplay[3];

                this.orbitingItems.put(player.getUniqueId(), items);

                double radius = 2.0; // Distance from the player
                double initialAngle = 0; // Starting angle for the swords

                // Spawn the swords in their initial positions
                for (int i = 0; i < 3; i++) {
                    int finalI = i;
                    items[finalI] = playerLocation.getWorld().spawn(playerLocation.clone(), ItemDisplay.class, (disp) -> {
                        disp.setItemStack(new ItemStack(Material.IRON_SWORD));

                        double angle = initialAngle + Math.toRadians(finalI * 120); // 120 degrees apart
                        double x = playerLocation.getX() + radius * Math.cos(angle);
                        double z = playerLocation.getZ() + radius * Math.sin(angle);
                        double y = playerLocation.getY() + 1; // Offset above the player

                        disp.teleport(new Location(playerLocation.getWorld(), x, y, z));

                        Transformation xform = disp.getTransformation();
                        xform.getLeftRotation().set(new AxisAngle4f((float) Math.toRadians(90d), 1f, 0f, 0f));
                        xform.getRightRotation().set(new AxisAngle4f((float) (angle + Math.toRadians(225d)), 0f, 0f, 1f)); // Orient outward
                        disp.setTransformation(xform);
                    });
                }

                // Schedule the rotation task
                Bukkit.getScheduler().runTaskTimer(instance, task -> {
                    if (!this.orbitingItems.containsKey(player.getUniqueId())) {
                        task.cancel();
                        for (ItemDisplay item : items) {
                            if (item.isValid()) {
                                item.remove();
                            }
                        }
                        return;
                    }

                    Location currentLocation = player.getLocation();
                    for (int i = 0; i < 3; i++) {
                        double angle = ((System.currentTimeMillis() * 0.25d) % 3600) / 360.0 * Math.toRadians(360); // Smooth rotation
                        angle += Math.toRadians(i * 120); // Offset for each sword

                        double x = currentLocation.getX() + radius * Math.cos(angle);
                        double z = currentLocation.getZ() + radius * Math.sin(angle);
                        double y = currentLocation.getY() + 1;

                        items[i].teleport(new Location(currentLocation.getWorld(), x, y, z));

                        Transformation xform = items[i].getTransformation();
                        xform.getLeftRotation().set(new AxisAngle4f((float) Math.toRadians(90d), 1f, 0f, 0f));
                        xform.getRightRotation().set(new AxisAngle4f((float) (angle + Math.toRadians(225d)), 0f, 0f, 1f));
                        items[i].setTransformation(xform);
                    }
                }, 0, 1); // Run every 2 ticks for smooth rotation
            }
        }
    }
}