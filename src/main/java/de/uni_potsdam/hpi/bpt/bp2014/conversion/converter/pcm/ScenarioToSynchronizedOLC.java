package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.pcm;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.ActivityCentricProcessModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.activity_centric.ActivityCentricToSynchronizedOLC;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize.SynchronizedObjectLifeCycle;

import java.util.*;

public class ScenarioToSynchronizedOLC {

    private Collection<ActivityCentricProcessModel> fragments;
    private Map<String, Collection<ObjectLifeCycle>> olcsPerDataClass;
    private HashSet<ObjectLifeCycle> olcs;

    public SynchronizedObjectLifeCycle convert(Collection<ActivityCentricProcessModel> fragments) {
        this.fragments = fragments;
        olcsPerDataClass = new HashMap<>();
        olcs = new HashSet<>();

        generateObjectLifeCycles();
        integrateStates();

        SynchronizedObjectLifeCycle sOLC = new SynchronizedObjectLifeCycle();
        sOLC.setObjectLifeCycles(new ArrayList<>(olcs));
        return sOLC;
    }



    private void generateObjectLifeCycles() {
        for (ActivityCentricProcessModel fragment : fragments) {
            ActivityCentricToSynchronizedOLC acpm2solc = new ActivityCentricToSynchronizedOLC();
            SynchronizedObjectLifeCycle solc = (SynchronizedObjectLifeCycle) acpm2solc.convert(fragment);
            for (ObjectLifeCycle objectLifeCycle : solc.getOLCs()) {
                if (!olcsPerDataClass.containsKey(objectLifeCycle.getLabel())) {
                    olcsPerDataClass.put(objectLifeCycle.getLabel(), new HashSet<ObjectLifeCycle>());
                }
                olcsPerDataClass.get(objectLifeCycle.getLabel()).add(objectLifeCycle);
            }
        }
    }



    private void integrateStates() {
        for (Map.Entry<String, Collection<ObjectLifeCycle>> olcsAndName : olcsPerDataClass.entrySet()) {
            Map<DataObjectState, Collection<StateTransition>> successors = new HashMap<>();
            Map<String, DataObjectState> states = new HashMap<>();
            for (ObjectLifeCycle olc : olcsAndName.getValue()) {
                for (IEdge transition : olc.getEdgeOfType(StateTransition.class)) {
                    if (!((DataObjectState)transition.getSource()).getName().equals("i")) {
                        String sourceName = ((DataObjectState) transition.getSource()).getName();
                        String targetName = ((DataObjectState) transition.getTarget()).getName();
                        if (!states.containsKey(sourceName)) {
                            states.put(sourceName, new DataObjectState(sourceName));
                        }
                        if (!states.containsKey(targetName)) {
                            states.put(targetName, new DataObjectState(targetName));
                        }
                        if (!successors.containsKey(states.get(sourceName))) {
                            successors.put(states.get(sourceName), new HashSet<StateTransition>());
                        }
                        if (!successorExists(successors.get(states.get(sourceName)), (StateTransition) transition)) {
                            StateTransition newTransition = new StateTransition(
                                    states.get(sourceName),
                                    states.get(targetName),
                                    ((StateTransition) transition).getLabel());
                            successors.get(states.get(sourceName)).add(newTransition);
                            states.get(sourceName).addOutgoingEdge(newTransition);
                            states.get(targetName).addIncomingEdge(newTransition);
                        }
                    }
                }
            }
            ObjectLifeCycle olc = new ObjectLifeCycle(olcsAndName.getKey());
            for (DataObjectState dataObjectState : states.values()) {
                olc.addNode(dataObjectState);
                if (dataObjectState.getName().equals("init")) {
                    olc.setStartNode(dataObjectState);
                }
                if (dataObjectState.getOutgoingEdges().isEmpty()) {
                    olc.addFinalNode(dataObjectState);
                }
            }
            olcs.add(olc);
        }
    }



    private boolean successorExists(Collection<StateTransition> stateTransitions, StateTransition transition) {
        for (StateTransition stateTransition : stateTransitions) {
            if (((DataObjectState)stateTransition.getSource()).getName().equals(
                    ((DataObjectState)transition.getSource()).getName()) &&
                    ((DataObjectState)stateTransition.getTarget()).getName().equals(
                            ((DataObjectState)transition.getTarget()).getName()) &&
                    stateTransition.getLabel().equals(transition.getLabel())) {
                return true;
            }
        }
        return false;
    }
}
