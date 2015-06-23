package de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.olc.ObjectLifeCycleDiff;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;

import java.util.*;

/**
 * A Synchronized OLC which will be initialized from multiple OLC creating a diff between different versions.
 */
public class SynchronizedDiff extends SynchronizedObjectLifeCycle {
    public  SynchronizedDiff(Collection<? extends ObjectLifeCycle> oldOLCs,
                             Collection<? extends ObjectLifeCycle> newOLCs) {
        Collection<? extends ObjectLifeCycle> groupedOLC = groupOLCVersions(oldOLCs, newOLCs);
        super.setObjectLifeCycles(new LinkedList<>(groupedOLC));
    }


    /**
     * This method groups the object life cycle versions.
     * For each new Object Life Cycle an old one will be determined and aggregated into an
     * {@link ObjectLifeCycleDiff}.
     *
     * @param oldOLCs The collection of old Object Life cycles.
     * @param newOLCs The collection of new Object Life Cycles.
     * @return A Collection of Object Life Cycle Diffs representing the grouped OLCs.
     */
    private Collection<ObjectLifeCycleDiff> groupOLCVersions(
            Collection<? extends ObjectLifeCycle> oldOLCs,
            Collection<? extends ObjectLifeCycle> newOLCs) {
        Map<String, ObjectLifeCycle> newGrouped = new HashMap<>();
        Map<String, ObjectLifeCycle> oldGrouped = new HashMap<>();
        Collection<ObjectLifeCycleDiff> grouped = new HashSet<>();
        for (ObjectLifeCycle oldOLC : oldOLCs) {
            oldGrouped.put(oldOLC.getLabel(), oldOLC);
        }
        for (ObjectLifeCycle newOLC : newOLCs) {
            newGrouped.put(newOLC.getLabel(), newOLC);
        }
        for (String olcName : newGrouped.keySet()) {
            if (oldGrouped.containsKey(olcName)) {
                grouped.add(new ObjectLifeCycleDiff(
                        oldGrouped.get(olcName),
                        newGrouped.get(olcName)));
            }
        }
        return grouped;
    }
}
