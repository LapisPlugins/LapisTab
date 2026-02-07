package net.lapismc.lapisTab;

import net.lapismc.lapisTab.events.TabOrderingUpdateEvent;
import net.lapismc.lapiscore.LapisCoreConfiguration;
import net.lapismc.lapiscore.LapisCorePlugin;
import net.lapismc.lapiscore.utils.LapisCoreFileWatcher;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Main class
 */
public final class LapisTab extends LapisCorePlugin implements Listener {

    private final HashMap<UUID, LapisTabPlayer> players = new HashMap<>();
    private Chat chat = null;
    private HookManager hookManager;

    @Override
    public void onEnable() {
        //Get VaultAPI Chat component
        setupVault();
        registerConfiguration(new LapisCoreConfiguration(this, 3, 2, new ArrayList<>()));
        fileWatcher = new LapisCoreFileWatcher(this);
        hookManager = new HookManager(this);

        int prefixSuffixUpdateSpeed = getConfig().getInt("UpdateSpeed.PrefixSuffix");
        tasks.addTask(tasks.runTaskTimer(() -> {
            for (LapisTabPlayer player : players.values()) {
                player.getRepeatingTask().run();
            }
        }, 20, prefixSuffixUpdateSpeed, false));

        int headerFooterUpdateSpeed = getConfig().getInt("UpdateSpeed.HeaderFooter");
        tasks.addTask(tasks.runTaskTimer(() -> {
            for (LapisTabPlayer player : players.values()) {
                player.updateHeaderFooter();
            }
        }, 20, headerFooterUpdateSpeed, false));

        int tabSortingUpdateSpeed = getConfig().getInt("UpdateSpeed.TabSorting");
        tasks.addTask(tasks.runTaskTimer(this::sortTabList, 20, tabSortingUpdateSpeed, false));

        Bukkit.getPluginManager().registerEvents(this, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    /**
     * Get the loaded instance of the HookManager
     *
     * @return HookManager instance
     */
    public HookManager getHookManager() {
        return hookManager;
    }

    /**
     * Get the LapisTabPlayer object for a given player.
     * Ideally this player should currently be online as most of the class functions rely on it
     *
     * @param uuid The UUID of the player
     * @return a LapisTabPlayer object for the UUID given
     */
    public LapisTabPlayer getPlayer(UUID uuid) {
        if (!players.containsKey(uuid))
            players.put(uuid, new LapisTabPlayer(this, uuid));
        return players.get(uuid);
    }

    /**
     * Convert a boolean in the config to a Team Option for a players team
     *
     * @param path The path of the boolean in the config
     * @return Always if true, never if false
     */
    public Team.OptionStatus getTeamOption(String path) {
        return getConfig().getBoolean(path) ? Team.OptionStatus.ALWAYS : Team.OptionStatus.NEVER;
    }

    /**
     * Get the chat provider that was loaded with the plugin, this is generally a permissions plugin
     *
     * @return the VaultAPI chat provider
     */
    public Chat getChatProvider() {
        return chat;
    }

    /**
     * Load and store a LapisTabPlayer object when a player joins
     * This ensures that they have an object regardless of when they last joined, if ever
     *
     * @param e PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        //Make sure we call getTeam here to make sure that the team is generated and set correctly
        //This could be an issue if their object was generated while the player was offline
        //This would give a null result for their team
        getPlayer(e.getPlayer().getUniqueId()).getTeam();
        //Update the header and the footer in one tick so that it is updated asap for the player
        //Prefix, Suffix and Tab order are retained during reconnects so don't need to be triggered here
        getPlayer(e.getPlayer().getUniqueId()).updateHeaderFooter();
    }

    /**
     * Loops over the permissions used to sort the tab list and finds players who have those permissions.
     * This could be slightly intensive on servers with more players, and so should be used sparingly.
     */
    public void sortTabList() {
        //The UUID of the player and the assigned priority derived from their permissions
        HashMap<UUID, Integer> tabListPriority = new HashMap<>();
        List<String> permissions = getConfig().getStringList("TabSorting");
        int currentPriority = permissions.size();
        //Loop over the permissions, if a player has the current permission, assign them the current priority
        //Ignore the player if they are already assigned, we want the highest value for each player
        for (String perm : permissions) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (tabListPriority.containsKey(p.getUniqueId()))
                    continue;
                if (p.hasPermission(perm))
                    tabListPriority.put(p.getUniqueId(), currentPriority);
            }
            currentPriority--;
        }
        //Call the event, and use the result from the event to allow plugins to edit the priority before it is applied
        TabOrderingUpdateEvent event = new TabOrderingUpdateEvent(tabListPriority);
        Bukkit.getPluginManager().callEvent(event);
        tabListPriority = event.getTabListPriority();
        //Loop over the results and set their priorities
        for (UUID uuid : tabListPriority.keySet()) {
            int priority = tabListPriority.get(uuid);
            Bukkit.getPlayer(uuid).setPlayerListOrder(priority);
        }
    }

    private void setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            //Throw error and shutdown
            getLogger().severe("Vault not present, disabling plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            //This happens if vault is installed but a plugin that implements the chat component isn't
            //Most permission plugins should cover this so I doubt we will often run into issues
            getLogger().severe("No chat provider present, make sure you have a plugin that hooks into Vault for us to use, disabling plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        chat = rsp.getProvider();
    }
}
