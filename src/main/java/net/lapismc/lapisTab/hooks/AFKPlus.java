package net.lapismc.lapisTab.hooks;

import net.lapismc.afkplus.api.AFKStartEvent;
import net.lapismc.afkplus.api.AFKStopEvent;
import net.lapismc.lapisTab.LapisTab;
import net.lapismc.lapisTab.LapisTabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;

public class AFKPlus implements TabHook, Listener {

    LapisTab plugin;
    boolean enabled = false;

    public AFKPlus(LapisTab plugin) {
        this.plugin = plugin;
    }

    @Override
    public String hookName() {
        return "AFKPlus";
    }

    /**
     * Check if AFKPlus is installed and running
     *
     * @return true if AFKPlus is installed and running, false otherwise
     */
    @Override
    public boolean checkForHook() {
        Plugin AFKPlusJava = Bukkit.getPluginManager().getPlugin("AFKPlus");
        return AFKPlusJava != null;
    }

    /**
     * Register events to allow this plugin to respond to AFKPlus events
     */
    @Override
    public void enableHook() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        enabled = true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler
    public void onAFKStart(AFKStartEvent e) {
        //TODO: make the prefix configurable
        LapisTabPlayer player = plugin.getPlayer(e.getPlayer().getUUID());
        player.setOverridePrefix(this, "AFK");
        player.setCollisionRule(Team.OptionStatus.NEVER);
    }

    @EventHandler
    public void onAFKStop(AFKStopEvent e) {
        LapisTabPlayer player = plugin.getPlayer(e.getPlayer().getUUID());
        player.clearOverridePrefix(this);
        player.setCollisionRule(Team.OptionStatus.ALWAYS);
    }

}
