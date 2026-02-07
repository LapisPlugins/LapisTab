package net.lapismc.lapisTab.events;

import net.lapismc.lapisTab.LapisTabPlayer;
import net.lapismc.lapiscore.events.LapisCoreEvent;

/**
 * Notification event for when a new player is loaded into LapisTab
 * Hooks can use this to immediately set options or overrides if necessary
 * This event should only be fired by LapisTab to ensure that it is only triggered when a TabPlayer object is new
 */
public class TabPlayerCreatedEvent extends LapisCoreEvent {

    private final LapisTabPlayer player;

    /**
     * This should only be used by LapisTab
     *
     * @param player The player who has just been loaded into LapisTab
     */
    public TabPlayerCreatedEvent(LapisTabPlayer player) {
        this.player = player;
    }

    /**
     * The player who has just been created in LapisTab
     *
     * @return LapisTabPlayer object for the player who has just been loaded into the plugin
     */
    public LapisTabPlayer getPlayer() {
        return player;
    }

}
