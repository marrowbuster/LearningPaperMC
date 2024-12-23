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
                    disp.setRotation(-180f + finalI * 120f, 0);
                    disp.setItemStack(new ItemStack(Material.IRON_SWORD));

                    Transformation xform = disp.getTransformation();
                    xform.getTranslation().set(new Vector3f(1f, 1f, 0f));
                    xform.getLeftRotation().set(new AxisAngle4f((float) Math.toRadians(90d), 1f, 0f, 0f));
                    xform.getRightRotation().set(new AxisAngle4f((float) Math.toRadians(-135d), 0f, 0f, 1f));
                    disp.setTransformation(xform);
                });
                orbitingItems.put(p.getUniqueId(), items);
                Matrix4f mat = new Matrix4f();
                Bukkit.getScheduler().runTaskTimer(instance, task -> {
                    if (!items[finalI].isValid()) { // display was removed from the world, abort task
                        task.cancel();
                        return;
                    }

                    items[finalI].setTransformationMatrix(mat.rotateLocalX((float) Math.PI + 0.1F /* prevent the client from interpolating in reverse */));
                    items[finalI].setInterpolationDelay(0); // no delay to the interpolation
                    items[finalI].setInterpolationDuration(100); // set the duration of the interpolated rotation
                }, 1 /* delay the initial transformation by one tick from display creation */, 100);
            }

        }
    }

}