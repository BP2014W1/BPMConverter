package de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;

import java.util.*;

public class CombinedTransition {
    Map<StateTransition, ObjectLifeCycle> transitionsAndOLCs;

    public CombinedTransition(StateTransition transition, ObjectLifeCycle olc) {
        transitionsAndOLCs = new HashMap<>();
        transitionsAndOLCs.put(transition, olc);
    }

    public boolean isCombinedTransition(StateTransition transition) {
        for (StateTransition existingTransition : transitionsAndOLCs.keySet()) {
            if (transition.getLabel().equals(existingTransition.getLabel())) {
                return true;
            }
        }
        return false;
    }

    public void addTransitionAndOLC(StateTransition transition, ObjectLifeCycle olc) {
        assert isCombinedTransition(transition) :
                "The transition does not fit into this chunk";
        transitionsAndOLCs.put(transition, olc);
    }

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
