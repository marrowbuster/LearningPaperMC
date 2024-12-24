package com.marrowbuster.learningPaperMC;

import net.kyori.adventure.text.Component;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LearningPaperMC extends JavaPlugin implements Listener {

    private static JavaPlugin instance;

    Map<UUID, ItemDisplay[]> orbitingItems = new HashMap<>();

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
    }

    @EventHandler
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
                    // disp.setRotation(-180f + finalI * 120f, 0);
                    disp.setItemStack(new ItemStack(Material.IRON_SWORD));

                    Transformation xform = disp.getTransformation();
                    xform.getTranslation().set(new Vector3f((float) Math.sin(finalI * Math.toRadians(120d)), 1f, (float) Math.cos(finalI * Math.toRadians(120d))));
                    xform.getLeftRotation().set(new AxisAngle4f((float) Math.toRadians(90d), 1f, 0f, 0f));
                    // ugly and inefficient but this shit works at least -.-;;;
                    xform.getRightRotation().set(new AxisAngle4f((float) Math.toRadians(-45d + finalI * -120d), 0f, 0f, 1f));
                    disp.setTransformation(xform);
                });
                orbitingItems.put(p.getUniqueId(), items);
                /* Bukkit.getScheduler().runTaskTimer(instance, task -> {
                    if (!items[finalI].isValid()) { // display was removed from the world, abort task
                        task.cancel();
                        return;
                    }
                    items[finalI].setRotation(items[finalI]., 0);


                    Transformation xform = items[finalI].getTransformation();


                    items[finalI].setTransformation(mat.rotateX((float) Math.PI + 0.1F));
                    items[finalI].setInterpolationDelay(0); // no delay to the interpolation
                    items[finalI].setInterpolationDuration(100); // set the duration of the interpolated rotation
                }, 1, 100); */
            }

        }
    }

}