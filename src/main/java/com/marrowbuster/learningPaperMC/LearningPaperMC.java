package com.marrowbuster.learningPaperMC;

import com.marrowbuster.learningPaperMC.listener.MainListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LearningPaperMC extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new MainListener(this), this);
    }
}