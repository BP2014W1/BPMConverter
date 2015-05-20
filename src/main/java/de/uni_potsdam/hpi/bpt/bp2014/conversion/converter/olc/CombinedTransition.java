package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a combined transition.
 * A combined transition aggregates all the transitions
 * which will be executed/ taken together.
 */
public class CombinedTransition {
    /**
     * Holds all transitions combined by this CT.
     * In addition their Object Life Cycle will be created.
     */
    Map<StateTransition, ObjectLifeCycle> transitionsAndOLCs;

    /**
     * Creates a new combined transition.
     * The combined transition will be initialized for one transition
     * and one olc.
     *
     * @param transition The first transition which will be part of the CT.
     * @param olc        The object life cycle containing the first transition.
     */
    public CombinedTransition(StateTransition transition, ObjectLifeCycle olc) {
        transitionsAndOLCs = new HashMap<>();
        transitionsAndOLCs.put(transition, olc);
    }

    /**
     * Checks weather or not a transition should be element of this combined transition transition.
     * Currently we do this by matching the labels.
     *
     * @param transition The transition to be checked.
     * @return True if on transition matches the label false if not.
     */
    public boolean isCombinedTransition(StateTransition transition) {
        for (StateTransition existingTransition : transitionsAndOLCs.keySet()) {
            if (transition.getLabel().equals(existingTransition.getLabel())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new transition and its corresponding object life cycle to the combined transition.
     * An assertion exception can be thrown if they do not match.
     *
     * @param transition The transition to be added.
     * @param olc        The corresponding object life cycle.
     */
    public void addTransitionAndOLC(StateTransition transition, ObjectLifeCycle olc) {
        assert isCombinedTransition(transition) :
                "The transition does not fit into this chunk";
        transitionsAndOLCs.put(transition, olc);
    }

    /**
     * Checks weather or not this combined transition is enabled for a set of states.
     * Therefore we will check if the source of all transitions is part of the enabledStates.
     * @param enabledStates The list of enabled states.
     * @return Returns true if the source of each state is element of the enabledStates, else false.
     */
    public boolean isEnabledForStates(Collection<DataObjectState> enabledStates) {
        for (StateTransition transition : transitionsAndOLCs.keySet()) {
            if (!enabledStates.contains(transition.getSource())) {
                return false;
            }
        }
        return true;
    }

    public Collection<StateTransition> getTransitions() {
        return transitionsAndOLCs.keySet();
    }


    public Map<StateTransition, ObjectLifeCycle> getTransitionsAndOLCs() {
        return transitionsAndOLCs;
    }
}
