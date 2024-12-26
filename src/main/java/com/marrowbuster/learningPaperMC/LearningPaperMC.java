package com.marrowbuster.learningPaperMC;

import com.marrowbuster.learningPaperMC.listener.MainListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LearningPaperMC extends JavaPlugin {

    /**
     * Upon initiation of the server, a new MainListener class attached to this plugin is invoked.
     */
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new MainListener(this), this);
    }
}