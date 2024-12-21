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
import org.bukkit.plugin.java.JavaPlugin;

public class LearningPaperMC extends JavaPlugin implements Listener {

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
            playerLocation.getWorld().spawn(playerLocation.clone(), ArmorStand.class, (spawnedStand) -> {
                spawnedStand.setItem(EquipmentSlot.HAND, new ItemStack(Material.IRON_SWORD));
                event.getPlayer().sendMessage(Component.text("original velocity: " + spawnedStand.getVelocity().length()));
                spawnedStand.setVelocity(p.getLocation().getDirection().multiply(3));
                event.getPlayer().sendMessage(Component.text("new velocity: " + spawnedStand.getVelocity().length()));
            });
        }
    }

}