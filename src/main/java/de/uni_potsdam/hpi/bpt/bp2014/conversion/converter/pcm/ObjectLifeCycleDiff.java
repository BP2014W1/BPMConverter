package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.pcm;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Stpehan on 16.05.2015.
 */
public class ObjectLifeCycleDiff {
    private ObjectLifeCycle newOLC;

    private ObjectLifeCycle oldOLC;

    public ObjectLifeCycleDiff(ObjectLifeCycle newOLC, ObjectLifeCycle oldOLC) {
        assert newOLC != null : "The olcs to be compared should not be null";
        assert oldOLC != null : "The olcs to be compared should not be null";
        this.newOLC = newOLC;
        this.oldOLC = oldOLC;
    }

    public ObjectLifeCycleDiff() {
    }

    public Collection<StateTransition> getAdditionalTransitions() {
        assert newOLC != null : "The olcs to be compared should not be null";
        assert oldOLC != null : "The olcs to be compared should not be null";
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
