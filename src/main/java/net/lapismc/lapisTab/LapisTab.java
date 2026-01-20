package net.lapismc.lapisTab;

import net.lapismc.lapiscore.LapisCorePlugin;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;
import java.util.UUID;

public final class LapisTab extends LapisCorePlugin implements Listener {

    private final HashMap<UUID, LapisTabPlayer> players = new HashMap<>();
    private Chat chat = null;
    private HookManager hookManager;

    @Override
    public void onEnable() {
        //Get VaultAPI Chat component
        setupVault();
        hookManager = new HookManager(this);
        tasks.runTaskTimer(() -> {
            for (LapisTabPlayer player : players.values()) {
                player.getRepeatingTask().run();
            }
        }, 20, 20, false);
        Bukkit.getPluginManager().registerEvents(this, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public LapisTabPlayer getPlayer(UUID uuid) {
        if (!players.containsKey(uuid))
            players.put(uuid, new LapisTabPlayer(this, uuid));
        return players.get(uuid);
    }

    public Chat getChatProvider() {
        return chat;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        getPlayer(e.getPlayer().getUniqueId());
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
