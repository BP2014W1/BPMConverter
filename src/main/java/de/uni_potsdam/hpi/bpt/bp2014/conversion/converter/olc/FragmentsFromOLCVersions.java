package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.ActivityCentricProcessModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Stpehan on 16.05.2015.
 */
public class FragmentsFromOLCVersions {

    private Collection<ObjectLifeCycleDiff> groupedOLCs;

    public Collection<ActivityCentricProcessModel> convert(Collection<ObjectLifeCycle> oldOLCs,
                                                           Collection<ObjectLifeCycle> newOLCs) {
        this.groupedOLCs = groupOLCVersions(oldOLCs, newOLCs);
        return null;
    }

    private Collection<ObjectLifeCycleDiff> groupOLCVersions(
            Collection<ObjectLifeCycle> oldOLCs,
            Collection<ObjectLifeCycle> newOLCs) {
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
