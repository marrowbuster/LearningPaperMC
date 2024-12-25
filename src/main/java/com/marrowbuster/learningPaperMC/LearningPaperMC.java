package com.marrowbuster.learningPaperMC;

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
import org.joml.AxisAngle4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LearningPaperMC extends JavaPlugin implements Listener {

    private static final int ITEM_DISPLAY_COUNT = 3;
    private static JavaPlugin instance;

    private final Map<UUID, ItemDisplay[]> orbitingItems = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        instance = this;
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

        if (event.getHand() == EquipmentSlot.HAND
            && player.getInventory().getItemInMainHand().getType() == Material.IRON_SWORD
            && event.getAction().isRightClick()) {
            if (this.orbitingItems.remove(player.getUniqueId()) != null) {
                player.sendActionBar(Component.text("Sword spinners deactivated.", TextColor.color(255, 208, 89)));
                return;
            }

            player.sendMessage(Component.text(player.getName() + " activated the sword spinner!"));

            final Location playerLocation = player.getLocation();
            final ItemDisplay[] items = new ItemDisplay[ITEM_DISPLAY_COUNT];

            this.orbitingItems.put(player.getUniqueId(), items);

            final double distanceFromPlayer = 2.0;
            final double initialSwordAngle = 0;

            for (int i = 0; i < ITEM_DISPLAY_COUNT; i++) {
                final int finalI = i;
                items[i] =
                        playerLocation.getWorld().spawn(playerLocation.clone(), ItemDisplay.class, disp -> {
                            disp.setItemStack(new ItemStack(Material.IRON_SWORD));

                            final double angle = initialSwordAngle + Math.toRadians(finalI * 120);
                            final double x = playerLocation.getX() + distanceFromPlayer * Math.cos(angle);
                            final double z = playerLocation.getZ() + distanceFromPlayer * Math.sin(angle);
                            final double y = playerLocation.getY() + 1;

                            disp.teleport(new Location(playerLocation.getWorld(), x, y, z));

                            final Transformation xform = disp.getTransformation();
                            xform.getLeftRotation().set(new AxisAngle4f((float) Math.toRadians(90d), 1f, 0f, 0f));
                            xform.getRightRotation()
                                    .set(new AxisAngle4f((float) (angle + Math.toRadians(225d)), 0f, 0f, 1f));
                            disp.setTransformation(xform);
                        });
            }

            Bukkit.getScheduler().runTaskTimer(instance, task -> {
                if (!this.orbitingItems.containsKey(player.getUniqueId())) {
                    task.cancel();

                    for (ItemDisplay item : items) {
                        item.remove();
                    }

                    return;
                }

                final Location currentLocation = player.getLocation();

                for (int i = 0; i < ITEM_DISPLAY_COUNT; i++) {
                    double angle = System.currentTimeMillis() * 0.25d % 3600 / 360.0 * Math.toRadians(360);
                    angle += Math.toRadians(i * 120);

                    final double x = currentLocation.getX() + distanceFromPlayer * Math.cos(angle);
                    final double z = currentLocation.getZ() + distanceFromPlayer * Math.sin(angle);
                    final double y = currentLocation.getY() + 1;

                    items[i].teleport(new Location(currentLocation.getWorld(), x, y, z));

                    final Transformation xform = items[i].getTransformation();
                    xform.getLeftRotation().set(new AxisAngle4f((float) Math.toRadians(90d), 1f, 0f, 0f));
                    xform.getRightRotation()
                            .set(new AxisAngle4f((float) (angle + Math.toRadians(225d)), 0f, 0f, 1f));
                    items[i].setTransformation(xform);
                }
            }, 0, 1);
        }
    }
}