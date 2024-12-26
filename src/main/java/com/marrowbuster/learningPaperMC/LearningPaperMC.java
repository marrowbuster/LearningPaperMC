package com.marrowbuster.learningPaperMC;

import com.marrowbuster.learningPaperMC.command.SpinnerCommand;
import com.marrowbuster.learningPaperMC.listener.MainListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LearningPaperMC extends JavaPlugin {

    private static MainListener mainListener;

    /**
     * Upon initiation of the server, a new MainListener class attached to this plugin is invoked.
     */
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(mainListener = new MainListener(this), this);
        this.getCommand("spinner").setExecutor(new SpinnerCommand());
    }

    @Override
    public void onDisable() {
        mainListener.onDisable();
    }

    public static MainListener getMainListener() {
        return mainListener;
    }
}