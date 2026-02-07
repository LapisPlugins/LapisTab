package net.lapismc.lapisTab.events;

import net.lapismc.lapiscore.events.LapisCoreEvent;

import java.util.HashMap;
import java.util.UUID;

/**
 * This event notifies plugins about a refresh of the tab list ordering.
 * You can also get and set the priorities to edit the list before it is applied by LapisTab
 */
public class TabOrderingUpdateEvent extends LapisCoreEvent {

    private HashMap<UUID, Integer> tabListPriority;

    /**
     * Provide the list of priorities to be assessed by listeners to the event
     *
     * @param tabListPriority The list of UUIDs and their tab list order priority
     */
    public TabOrderingUpdateEvent(HashMap<UUID, Integer> tabListPriority) {
        this.tabListPriority = tabListPriority;
    }

    /**
     * Get the list of UUIDs and their assigned priority by the automatic sorting in LapisTab
     *
     * @return a map of UUID to Integer that shows each online players priority if they have one
     */
    public HashMap<UUID, Integer> getTabListPriority() {
        return tabListPriority;
    }

    /**
     * Set the priority that will be applied to the tab list
     *
     * @param tabListPriority a map of UUID to tab list priority. higher the integer = higher in tab list
     */
    public void setTabListPriority(HashMap<UUID, Integer> tabListPriority) {
        this.tabListPriority = tabListPriority;
    }

}
