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

/**
 * A hook for managing AFK players prefix, suffix and bump team options
 */
public class AFKPlus implements TabHook, Listener {

    LapisTab plugin;
    boolean enabled = false;

    /**
     * Initialize the hook with the LapisTab plugin
     *
     * @param plugin the LapisTab plugin instance
     */
    public AFKPlus(LapisTab plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the name of this hook
     *
     * @return A string of the name of this Hook
     */
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

    /**
     * When a player starts AFK, set their prefix, suffix and collision rule from the configs
     *
     * @param e The player AFK start event
     */
    @EventHandler
    public void onAFKStart(AFKStartEvent e) {
        LapisTabPlayer player = plugin.getPlayer(e.getPlayer().getUUID());
        String prefix = plugin.config.getMessage("AFKPlus.Prefix");
        String suffix = plugin.config.getMessage("AFKPlus.Suffix");
        if (!prefix.isEmpty())
            player.setOverridePrefix(this, prefix);
        if (!suffix.isEmpty())
            player.setOverrideSuffix(this, suffix);
        player.setCollisionRule(getAFKBump());
        player.setNameTagVisibility(getAFKTag());
    }

    /**
     * When the player leaves AFK, we reset their prefix, suffix and collision rule to their defaults
     *
     * @param e The player AFK stop event
     */
    @EventHandler
    public void onAFKStop(AFKStopEvent e) {
        LapisTabPlayer player = plugin.getPlayer(e.getPlayer().getUUID());
        player.clearOverridePrefix(this);
        player.clearOverrideSuffix(this);
        player.setCollisionRule(getDefaultBump());
        player.setNameTagVisibility(getDefaultTag());
    }

    /**
     * Fetch if the player should be able to bump when AFK
     *
     * @return ALWAYS if they should bump, otherwise NEVER
     */
    private Team.OptionStatus getAFKBump() {
        return plugin.getTeamOption("Hooks.AFKPlus.Bump");
    }

    /**
     * Fetch if the players nametag should be visible when AFK
     *
     * @return ALWAYS if it is visible, otherwise NEVER
     */
    private Team.OptionStatus getAFKTag() {
        return plugin.getTeamOption("Hooks.AFKPlus.Nametag");
    }

    /**
     * Fetch if the player should be able to bump when no longer AFK
     *
     * @return ALWAYS if they should bump, otherwise NEVER
     */
    private Team.OptionStatus getDefaultBump() {
        return plugin.getTeamOption("TeamOptions.PlayerBump");
    }

    /**
     * Fetch if the players' nametag should be visible when no longer AFK
     *
     * @return ALWAYS if it is visible, otherwise NEVER
     */
    private Team.OptionStatus getDefaultTag() {
        return plugin.getTeamOption("TeamOptions.NameTagVisibility");
    }

}
