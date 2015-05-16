package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;

import java.util.*;

/**
 * Created by Stpehan on 16.05.2015.
 */
public class ObjectLifeCycleDiff extends ObjectLifeCycle {
    private ObjectLifeCycle newOLC;
    private ObjectLifeCycle oldOLC;
    private Map<String, DataObjectState> newStateObjects;

    public ObjectLifeCycleDiff(ObjectLifeCycle newOLC, ObjectLifeCycle oldOLC) {
        assert newOLC != null : "The olcs to be compared should not be null";
        assert oldOLC != null : "The olcs to be compared should not be null";
        this.newOLC = newOLC;
        this.oldOLC = oldOLC;
    }

    public ObjectLifeCycleDiff() {
    }

    private void initialize() {
        this.setLabel(newOLC.getLabel());
        newStateObjects = new HashMap<>();
        for (StateTransition transition : getAdditionalTransitions()) {
            DataObjectState newSource = getNewNodeFor((DataObjectState) transition.getSource());
            DataObjectState newTarget = getNewNodeFor((DataObjectState) transition.getTarget());
            StateTransition newTransition = new StateTransition(newSource,
                    newTarget, transition.getLabel());
            newSource.addOutgoingEdge(newTransition);
            newTarget.addIncomingEdge(newTransition);
        }
    }

    private DataObjectState getNewNodeFor(DataObjectState oldNode) {
        if (!newStateObjects.containsKey(oldNode.getName())) {
            newStateObjects.put(oldNode.getName(),
                    new DataObjectState(oldNode.getName()));
            if (newOLC.getFinalStates().contains(oldNode)) {
                addFinalNode(newStateObjects.get(oldNode.getName()));
            }
            addNode(newStateObjects.get(oldNode.getName()));
        }
        return newStateObjects.get(oldNode.getName());
    }

    public Collection<StateTransition> getAdditionalTransitions() {
        assert newOLC != null : "The new olcs should not be null";
        if (oldOLC == null) {
            return oldOLC.getEdgeOfType(StateTransition.class);
        }
        Collection<StateTransition> transitionsOfNew =
                newOLC.getEdgeOfType(StateTransition.class);
        Collection<StateTransition> transitionsOfOld =
                oldOLC.getEdgeOfType(StateTransition.class);
        Collection<StateTransition> additionalTransitions = new  HashSet<>();
        for (StateTransition newTransition : transitionsOfNew) {
            if (transitionIsNew(newTransition, transitionsOfOld)) {
                additionalTransitions.add(newTransition);
            }
        }
        return additionalTransitions;
    }

    private boolean transitionIsNew(StateTransition newTransition,
                                    Collection<StateTransition> transitionsOfOld) {
        for (StateTransition oldTransition : transitionsOfOld) {
            if (oldTransition.getLabel().equals(newTransition.getLabel()) &&
                    ((DataObjectState)oldTransition.getSource()).getName()
                            .equals(((DataObjectState) newTransition.getSource()).getName()) &&
                    ((DataObjectState)oldTransition.getTarget()).getName()
                            .equals(((DataObjectState) newTransition.getTarget()).getName())) {
                return false;
            }

        }
        return true;
    }

    public ObjectLifeCycle getOldOLC() {
        return oldOLC;
    }

    public void setOldOLC(ObjectLifeCycle oldOLC) {
        this.oldOLC = oldOLC;
    }

    public ObjectLifeCycle getNewOLC() {
        return newOLC;
    }

    public void setNewOLC(ObjectLifeCycle newOLC) {
        this.newOLC = newOLC;
    }
}
