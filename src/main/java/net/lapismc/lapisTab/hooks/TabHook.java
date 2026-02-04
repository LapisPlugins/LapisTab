package net.lapismc.lapisTab.hooks;

/**
 * An interface for Hooks to be registered with LapisTab
 */
public interface TabHook {

    /**
     * Should return a human-readable name for your hook. Ideally the name of the plugin it interfaces with
     *
     * @return String name of Hook
     */
    String hookName();

    /**
     * This is a test of if your hook can be enabled or not. e.g. if the plugin you interact with is loaded
     *
     * @return True if your hook can be enabled, otherwise false
     */
    boolean checkForHook();

    /**
     * This should be where you connect to APIs etc, this is only run if checkForHook returns true
     */
    void enableHook();

    /**
     * Check if your hook enabled successfully.
     *
     * @return True if this hook is currently enabled, otherwise false
     */
    boolean isEnabled();

}
