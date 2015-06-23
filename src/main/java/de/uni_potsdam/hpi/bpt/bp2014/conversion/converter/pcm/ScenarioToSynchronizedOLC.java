package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.pcm;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IConverter;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.ActivityCentricProcessModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.scenario.Scenario;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.activity_centric.ActivityCentricToSynchronizedOLC;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize.SynchronizedObjectLifeCycle;

import java.util.*;

/**
 * This class is a converter, which generates a {@link SynchronizedObjectLifeCycle}
 * based on a PCM Scenario. A PCM scenario is an aggregation of {@link ActivityCentricProcessModel}.
 * Hence a List of those models - called fragments - must be provided.
 */
public class ScenarioToSynchronizedOLC implements IConverter<Scenario, SynchronizedObjectLifeCycle> {

    /**
     * The list of fragments representing the Production Case Management Scenario.
     */
    private Collection<ActivityCentricProcessModel> fragments;
    /**
     * A map which maps the Data Object names to a Collection of Object Life cycles.
     * There will be at least one ObjectLifeCycles and not more thant #{@link #fragments}
     */
    private Map<String, Collection<ObjectLifeCycle>> olcsPerDataClass;
    /**
     * A Collection holding the final Object Life Cycles which will be needed in order to
     * build the synchronized object life cycle.
     */
    private Collection<ObjectLifeCycle> olcs;

    /**
     * This method generates an {@link SynchronizedObjectLifeCycle} based on a a Collection
     * of {@link ActivityCentricProcessModel}.
     * First of all the Collections {@link #fragments}, {@link #olcsPerDataClass} and
     * {@link #olcs} will be initialized.
     * Afterward we will generate Object Life Cycles for each Activity Centric Process Model.
     * Before creating an Object Life Cycle we will integrate the created OLCs.
     *
     * @param fragments The list of fragments representing the PCM Scenario.
     * @return The generated Synchronized Object Life Cycle.
     */
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

    /**
     * This method generates the Object Life Cycles.
     * it will create object life cycles for each data object in each fragment.
     * Therefore it used the Converter {@link ActivityCentricToSynchronizedOLC}
     * and extracts the object life cycles from the {@link SynchronizedObjectLifeCycle}.
     * They will be saved inside {@link #olcsPerDataClass}.
     */
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
    /**
     * This method takes the Object life cycles inside {@link #olcsPerDataClass}
     * and creates one olc per data class.
     * We assume that the start state will be called init.
     * As well as that the final states will have no outgoing edges.
     */
    private void integrateStates() {
        for (Map.Entry<String, Collection<ObjectLifeCycle>> olcsAndName : olcsPerDataClass.entrySet()) {
            Map<DataObjectState, Collection<StateTransition>> successors = new HashMap<>();
            Map<String, DataObjectState> states = new HashMap<>();
            initializeSuccessorsAndStates(olcsAndName, successors, states);
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

    /**
     * This method initializes the successors and states.
     * Therefore it iterates of every Object Life Cycle.
     * For each transition the source and target will be determined, and if no such state
     * has been added before it will be added to the states.
     * In addition the target will be added to the list of successors for the source.
     * @param olcsAndName A MapEntry holding all Object Life Cycles for one name name.
     * @param successors A Map holding all DataStates and a Collection of their successors. (Will save the result)
     * @param states A Map for all States (will save the result)
     */
    private void initializeSuccessorsAndStates(Map.Entry<String, Collection<ObjectLifeCycle>> olcsAndName, Map<DataObjectState,
            Collection<StateTransition>> successors,
            Map<String, DataObjectState> states) {
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
    }


    /**
     * Checks weather or not a transition already exists. A transition exists if and only if
     * all the following conditions are true:
     * - The source and target names are the same
     * - The state transition label is the same.
     * @param stateTransitions A Collection of all existing transitions.
     * @param transition The transition to be checked.
     * @return Returns true if the transition exists else it will return false.
     */
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

    @Override
    public SynchronizedObjectLifeCycle convert(Scenario model) {
        return convert(model.getFragments());
    }
}