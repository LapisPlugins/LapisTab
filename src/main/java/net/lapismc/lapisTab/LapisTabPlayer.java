package net.lapismc.lapisTab;

import net.lapismc.lapisTab.hooks.TabHook;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.UUID;

public class LapisTabPlayer {

    private final LapisTab plugin;
    private final UUID uuid;
    private final HashMap<TabHook, String> overridePrefixes = new HashMap<>();
    private final HashMap<TabHook, String> overrideSuffixes = new HashMap<>();
    private String prefix = "";
    private String suffix = "";
    private Team team;

    public LapisTabPlayer(LapisTab plugin, OfflinePlayer op) {
        this(plugin, op.getUniqueId());
    }

    public LapisTabPlayer(LapisTab plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
        getTeam();
    }

    /*
    PREFIXES
     */

    /**
     * Calculates and returns the prefix of the player with any overrides from hooks
     *
     * @return a String representing the players current prefix
     */
    public String getDisplayPrefix() {
        if (!overridePrefixes.isEmpty()) {
            //Join the prefixes with a space
            StringBuilder overridePrefix = new StringBuilder();
            for (String prefix : overridePrefixes.values()) {
                overridePrefix.append(prefix).append(" ");
            }
            return overridePrefix.toString();
        } else {
            return prefix;
        }
    }

    /**
     * Update the players prefix from Vault
     * This is used to make sure that any location based prefixes are being updated
     */
    public void updatePrefix() {
        Player p = Bukkit.getPlayer(uuid);
        prefix = plugin.getChatProvider().getPlayerPrefix(p);
    }

    /**
     * Add an override to this players prefix for your hook
     *
     * @param hook           The hook that this override should be associated with
     * @param overridePrefix The string to place in prefix
     */
    public void setOverridePrefix(TabHook hook, String overridePrefix) {
        overridePrefixes.put(hook, overridePrefix);
    }

    /**
     * Remove your hooks override on this players prefix
     *
     * @param hook The hook that is removing
     */
    public void clearOverridePrefix(TabHook hook) {
        overridePrefixes.remove(hook);
    }

    /*
    SUFFIXES
     */

    /**
     * Calculates and returns the suffix of the player with any overrides from hooks
     *
     * @return a String representing the players current suffix
     */
    public String getDisplaySuffix() {
        if (!overrideSuffixes.isEmpty()) {
            //Join the suffixes with a space
            StringBuilder overrideSuffix = new StringBuilder();
            for (String suffix : overrideSuffixes.values()) {
                overrideSuffix.append(suffix).append(" ");
            }
            return overrideSuffix.toString();
        } else {
            return suffix;
        }
    }

    /**
     * Update the players suffix from Vault
     * This is used to make sure that any location based suffixes are being updated
     */
    public void updateSuffix() {
        Player p = Bukkit.getPlayer(uuid);
        suffix = plugin.getChatProvider().getPlayerSuffix(p);
    }

    /**
     * Add an override to this players suffix for your hook
     *
     * @param hook           The hook that this override should be associated with
     * @param overrideSuffix The string to place in suffix
     */
    public void setOverrideSuffix(TabHook hook, String overrideSuffix) {
        overrideSuffixes.put(hook, overrideSuffix);
    }

    /**
     * Remove your hooks override on this players suffix
     *
     * @param hook The hook that is removing
     */
    public void clearOverrideSuffix(TabHook hook) {
        overrideSuffixes.remove(hook);
    }

    /**
     * Check how this player collides with other players and entities
     *
     * @return the OptionStatus associated with this players COLLISION_RULE
     */
    public Team.OptionStatus getCollisionRule() {
        return team.getOption(Team.Option.COLLISION_RULE);
    }

    /**
     * Set the COLLISION_RULE option for this player
     *
     * @param status The status you wish to set the rule to
     */
    public void setCollisionRule(Team.OptionStatus status) {
        team.setOption(Team.Option.COLLISION_RULE, status);
    }

    /**
     * Get or generate a scoreboard for us to use
     *
     * @return the players scoreboard
     */
    private Scoreboard getScoreboard() {
        return Bukkit.getScoreboardManager().getMainScoreboard();
    }

    /**
     * Get or generate the players team
     *
     * @return the players team
     */
    private Team getTeam() {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null)
            return null;
        Scoreboard sb = getScoreboard();
        //Generate the team
        String teamName = "LapisTab-" + p.getName();
        if (sb.getTeam(teamName) == null) {
            team = sb.registerNewTeam(teamName);
        } else {
            team = sb.getTeam(teamName);
        }
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        return team;
    }

    /**
     * Get the players UUID
     *
     * @return the UUID of the player that this class represents
     */
    public UUID getUUID() {
        return this.uuid;
    }

    public Runnable getRepeatingTask() {
        return () -> {
            //Don't run this code if the player is offline
            if (!Bukkit.getOfflinePlayer(uuid).isOnline())
                return;
            Player p = Bukkit.getPlayer(uuid);

            //Trigger an update of the vault prefix
            updatePrefix();
            //Set the players prefix on their team
            team.setPrefix(getDisplayPrefix());

            //Trigger an update of the vault suffix
            updateSuffix();
            //Set the players suffix on their team
            team.setSuffix(getDisplaySuffix());

            //Make sure the player is in the team
            if (!team.hasEntry(p.getName()))
                team.addEntry(p.getName());
        };
    }

}
