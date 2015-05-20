package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.*;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize.CombinedTransition;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize.SynchronizedObjectLifeCycle;

import java.util.*;

/**
 * This class represents a convert.
 * It generates fragments from different OLC versions.
 * Fragments are {@link ActivityCentricProcessModel} which consist of one
 * Start Event and an Activity and one End Event.
 * The Fragments will represent additional combined transitions.
 */
public class FragmentsFromOLCVersions {

    /**
     * A Collection of Object Life Cycles which represent
     * the grouped olcs.
     */
    private Collection<? extends ObjectLifeCycle> groupedOLCs;

    /**
     * This method generates a collection of fragments from multiple Object Life Cycles.
     * Therefore it needs both the new and the old version of each OLC which should be
     * taken into account.
     *
     * @param oldOLCs A Collection of Object Life Cycles representing the old version.
     * @param newOLCs A Collection of Object Life Cycles representing the new version.
     * @return A Collection of Activity Centric Process Model - the fragments generated.
     */
    public Collection<ActivityCentricProcessModel> convert(Collection<? extends ObjectLifeCycle> oldOLCs,
                                                           Collection<? extends ObjectLifeCycle> newOLCs) {
        Collection<ActivityCentricProcessModel> acpms = new HashSet<>();
        this.groupedOLCs = groupOLCVersions(oldOLCs, newOLCs);
        SynchronizedObjectLifeCycle diffSynchronizedOLC = new SynchronizedObjectLifeCycle();
        diffSynchronizedOLC.setObjectLifeCycles(new LinkedList<>(groupedOLCs));
        OLCConversionFlyweight<ActivityCentricProcessModel> flyweight = null;
        try {
            flyweight = new OLCConversionFlyweight<>(diffSynchronizedOLC,
                    ActivityCentricProcessModel.class);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        for (CombinedTransition combinedTransition : flyweight.getCombinedTransitions()) {
            acpms.add(createFragment(combinedTransition));
        }
        return acpms;
    }

    /**
     * This method creates an Fragment for a given combined transition.
     * Such a Fragment will consist of a Start Event an End Event and an Activity with multiple Out and Inputs.
     *
     * @param combinedTransition The Combined transition which will be taken into accoutn.
     * @return The generated Fragment.
     */
    private ActivityCentricProcessModel createFragment(CombinedTransition combinedTransition) {
        ActivityCentricProcessModel acpm = new ActivityCentricProcessModel();
        Event startEvent = new Event();
        startEvent.setType(Event.Type.START);
        Event endEvent = new Event();
        endEvent.setType(Event.Type.END);
        Activity activity = new Activity();
        String name = "";
        acpm.addNode(startEvent);
        acpm.setStartNode(startEvent);
        acpm.addNode(activity);
        acpm.addNode(endEvent);
        acpm.addFinalNode(endEvent);
        ControlFlow cf = new ControlFlow(startEvent, activity);
        startEvent.addOutgoingEdge(cf);
        activity.addIncomingEdge(cf);
        cf = new ControlFlow(activity, endEvent);
        activity.addOutgoingEdge(cf);
        endEvent.addIncomingEdge(cf);
        for (Map.Entry<StateTransition, ObjectLifeCycle> transitionAndOLC :
                combinedTransition.getTransitionsAndOLCs().entrySet()) {
            if (!name.contains(transitionAndOLC.getKey().getLabel())) {
                name = name + transitionAndOLC.getKey().getLabel() + ", ";
            }
            DataObject input = new DataObject(transitionAndOLC.getValue().getLabel(),
                    (DataObjectState) transitionAndOLC.getKey().getSource());
            DataObject output = new DataObject(transitionAndOLC.getValue().getLabel(),
                    (DataObjectState) transitionAndOLC.getKey().getTarget());
            DataFlow inputFLow = new DataFlow(input, activity);
            DataFlow outputFlow = new DataFlow(activity, output);
            input.addOutgoingEdge(inputFLow);
            output.addIncomingEdge(outputFlow);
            activity.addIncomingEdge(inputFLow);
            activity.addOutgoingEdge(outputFlow);
            acpm.addNode(input);
            acpm.addNode(output);
        }
        if (name.length() >= 2) {
            name = name.substring(0, name.length() - 2);
        }
        activity.setName(name);
        return acpm;
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
