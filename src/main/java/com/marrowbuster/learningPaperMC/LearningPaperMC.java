package com.marrowbuster.learningPaperMC;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Matrix4f;

public class LearningPaperMC extends JavaPlugin implements Listener {

    private final Plugin plugin;

    public LearningPaperMC(Plugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
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
            ItemDisplay swordDisplay = playerLocation.getWorld().spawn(playerLocation.clone(), ItemDisplay.class, (disp) -> {
                disp.setRotation(-180f + 1 * 120f, 0);
                disp.setItemStack(new ItemStack(Material.GOLDEN_SWORD));
            });
            Matrix4f mat = new Matrix4f().scale(0.5F);
            Bukkit.getScheduler().runTaskTimer(plugin, task -> {
                if (!swordDisplay.isValid()) { // display was removed from the world, abort task
                    task.cancel();
                    return;
                }

                swordDisplay.setTransformationMatrix(mat.rotateY(((float) Math.toRadians(180)) + 0.1F /* prevent the client from interpolating in reverse */));
                swordDisplay.setInterpolationDelay(0); // no delay to the interpolation
                swordDisplay.setInterpolationDuration(1); // set the duration of the interpolated rotation
            }, 1 /* delay the initial transformation by one tick from display creation */, 1);
        }
    }

}