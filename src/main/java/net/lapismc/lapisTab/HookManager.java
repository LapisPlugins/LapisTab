package net.lapismc.lapisTab;

import net.lapismc.lapisTab.hooks.AFKPlus;
import net.lapismc.lapisTab.hooks.TabHook;

import java.util.ArrayList;
import java.util.List;

public class HookManager {

    private final LapisTab plugin;
    private final List<TabHook> hooks = new ArrayList<>();

    public HookManager(LapisTab plugin) {
        this.plugin = plugin;
        //Register our own hooks on the first tick of the server
        //This means their plugins should be loaded
        plugin.tasks.runTask(this::registerLocalHooks, false);
    }

    /**
     * Provide a hook and enable it
     *
     * @param hook The hook you want to register
     */
    public void registerHook(TabHook hook) {
        hooks.add(hook);
        if (hook.checkForHook()) {
            hook.enableHook();
            plugin.getLogger().info("Hook " + hook.hookName() + " was successfully loaded");
        }
    }

    /**
     * Get a hook by name
     *
     * @param hookName The name of the hook you want to retrieve
     * @return the hook if found, null if there is no hook by that name
     */
    public TabHook getHook(String hookName) {
        for (TabHook hook : hooks) {
            if (hook.hookName().equalsIgnoreCase(hookName))
                return hook;
        }
        return null;
    }

    private void registerLocalHooks() {
        hooks.add(new AFKPlus(plugin));
        for (TabHook hook : hooks) {
            if (hook.checkForHook()) {
                hook.enableHook();
                plugin.getLogger().info("Hook " + hook.hookName() + " was successfully loaded");
            }
        }
    }

}
