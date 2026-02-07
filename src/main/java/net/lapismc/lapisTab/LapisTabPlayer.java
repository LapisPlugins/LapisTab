package net.lapismc.lapisTab;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.lapismc.lapisTab.events.TabPlayerCreatedEvent;
import net.lapismc.lapisTab.hooks.TabHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class represents a single Player on the server and manages their scoreboard to provide prefix and suffix
 */
public class LapisTabPlayer {

    private final LapisTab plugin;
    private final UUID uuid;
    private final HashMap<TabHook, String> overridePrefixes = new HashMap<>();
    private final HashMap<TabHook, String> overrideSuffixes = new HashMap<>();
    private String prefix = "";
    private String suffix = "";
    private Team team;

    /**
     * This should only be accessed internally by LapisTab
     * For API access use {@link LapisTab#getPlayer(UUID)}
     *
     * @param plugin The LapisTab instance
     * @param uuid   The UUID of the player being controlled
     */
    public LapisTabPlayer(LapisTab plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
        getTeam();
        //Cannot set team options if team is null, this means player isn't online
        if (team != null) {
            //Ensure team options are set correctly
            if (!getCollisionRule().equals(plugin.getTeamOption("TeamOptions.PlayerBump"))) {
                setCollisionRule(plugin.getTeamOption("TeamOptions.PlayerBump"));
            }
            if (!getNameTagVisibility().equals(plugin.getTeamOption("TeamOptions.NameTagVisibility"))) {
                setNameTagVisibility(plugin.getTeamOption("TeamOptions.NameTagVisibility"));
            }
            if (!getDeathMessageVisibility().equals(plugin.getTeamOption("TeamOptions.DeathMessageVisibility"))) {
                setDeathMessageVisibility(plugin.getTeamOption("TeamOptions.DeathMessageVisibility"));
            }
        }
        TabPlayerCreatedEvent event = new TabPlayerCreatedEvent(this);
        Bukkit.getPluginManager().callEvent(event);
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

    /*
    Tab List Header and Footer Management
     */

    /**
     * Update the players tab list header and footer
     */
    public void updateHeaderFooter() {
        Player p = Bukkit.getPlayer(getUUID());
        //Don't run if the player isn't online
        if (p == null)
            return;
        //Giving the OfflinePlayer to the method so it can replace PAPI placeholders
        String header = plugin.config.getMessage("TabList.Header", Bukkit.getOfflinePlayer(getUUID()));
        String footer = plugin.config.getMessage("TabList.Footer", Bukkit.getOfflinePlayer(getUUID()));
        //Only send the header or footer if they are set
        //Handle multi-line header or footer
        TextComponent headerComponent = processMultilineString(header);
        TextComponent footerComponent = processMultilineString(footer);

        p.sendPlayerListHeaderAndFooter(headerComponent, footerComponent);
    }

    /**
     * Process a string with ";" line separators into multiline components
     *
     * @param s The delimited string
     * @return a component with new lines as needed
     */
    public TextComponent processMultilineString(String s) {
        //If there are no line separators, just return the text as a component
        if (!s.contains(";")) {
            return Component.text(s);
        }
        TextComponent comp = Component.empty();
        //Loop over each line and add it to the component
        String[] arr = s.split(";");
        for (int i = 0; i < arr.length; i++) {
            comp = comp.append(Component.text(arr[i]));
            //If we aren't on the last line, add a new line
            if (i < arr.length - 1) {
                comp = comp.appendNewline();
            }
        }
        return comp;
    }

    /*
    Team Options
     */

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
     * Check how this players' nametag is shown to other players
     *
     * @return the OptionStatus associated with this players NAME_TAG_VISIBILITY
     */
    public Team.OptionStatus getNameTagVisibility() {
        return team.getOption(Team.Option.NAME_TAG_VISIBILITY);
    }

    /**
     * Set the NAME_TAG_VISIBILITY option for this player
     *
     * @param status The status you wish to set the rule to
     */
    public void setNameTagVisibility(Team.OptionStatus status) {
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, status);
    }

    /**
     * Check how this players' death message is shown to other players
     *
     * @return the OptionStatus associated with this players DEATH_MESSAGE_VISIBILITY
     */
    public Team.OptionStatus getDeathMessageVisibility() {
        return team.getOption(Team.Option.DEATH_MESSAGE_VISIBILITY);
    }

    /**
     * Set the DEATH_MESSAGE_VISIBILITY option for this player
     *
     * @param status The status you wish to set the rule to
     */
    public void setDeathMessageVisibility(Team.OptionStatus status) {
        team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, status);
    }


    /*
    Direct scoreboard access
     */

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
     * @return the players team, or null if the player is offline
     */
    protected Team getTeam() {
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

    /*
    Util methods
     */

    /**
     * Get the players UUID
     *
     * @return the UUID of the player that this class represents
     */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * This is a repeating task used internally to update the players prefix and suffix
     *
     * @return a runnable task
     */
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
