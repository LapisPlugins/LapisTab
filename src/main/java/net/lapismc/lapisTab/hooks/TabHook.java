package net.lapismc.lapisTab.hooks;

public interface TabHook {

    String hookName();

    boolean checkForHook();

    void enableHook();

    boolean isEnabled();

}
