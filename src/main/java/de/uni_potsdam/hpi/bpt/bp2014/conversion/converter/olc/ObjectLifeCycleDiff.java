package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This class represents an Object Life Cycle Diff.
 * This means it is itself an {@link ObjectLifeCycle} and is created based
 * on the differences of two Object Life cycles.
 * Only additional transitions will be element of object of this class.
 */
public class ObjectLifeCycleDiff extends ObjectLifeCycle {
    /**
     * The Object Life Cycle representing the new version.
     */
    private ObjectLifeCycle newOLC;
    /**
     * The Object Life Cycle representing the old version.
     */
    private ObjectLifeCycle oldOLC;
    /**
     * For every state which is element of an additional transition
     * a new {@link DataObjectState} will be created.
     * This Map maps the name of these states to the states.
     */
    private Map<String, DataObjectState> newStateObjects;

    /**
     * Creates a new ObjectLifeCycleDiff based on the given version.
     *
     * @param newOLC A Representation of the new OLC Version.
     * @param oldOLC A Representation of the old OLC version.
     */
    public ObjectLifeCycleDiff(ObjectLifeCycle newOLC, ObjectLifeCycle oldOLC) {
        assert newOLC != null : "The olcs to be compared should not be null";
        assert oldOLC != null : "The olcs to be compared should not be null";
        this.newOLC = newOLC;
        this.oldOLC = oldOLC;
        initialize();
    }

    /**
     * This method initializes the newStateObject Map.
     * Extracting all states and creating new Objects per state.
     */
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

    /**
     * This method creates a new node for a given one.
     * You can think of this node as a deep copy. The
     * state will be added and the label will be set.
     * Be aware that the {@link #newStateObjects} has to be initialized.
     * DataStates will be shared, this means if you call this method twice
     * for the same node two nodes with the same data object state will be
     * the result.
     *
     * @param oldNode The node to be copied.
     * @return The deep copy of that node.
     */
    private DataObjectState getNewNodeFor(DataObjectState oldNode) {
        if (!newStateObjects.containsKey(oldNode.getName())) {
            newStateObjects.put(oldNode.getName(),
                    new DataObjectState(oldNode.getName()));
            if (newOLC.getFinalNodes().contains(oldNode)) {
                addFinalNode(newStateObjects.get(oldNode.getName()));
            }
            addNode(newStateObjects.get(oldNode.getName()));
        }
        return newStateObjects.get(oldNode.getName());
    }

    /**
     * Determines all nodes which have be added to the {@link #oldOLC} in order
     * to create the {@link #newOLC}.
     * If the oldOLC is not set all transitions of the newOLC will be returned.
     * Be aware that the state transitions will be extracted directly from the
     * OLCs. This means any changes will affect the {@link #newOLC}.
     *
     * @return A Collection of additional StateTransitions.
     */
    public Collection<StateTransition> getAdditionalTransitions() {
        assert newOLC != null : "The new olcs should not be null";
        if (oldOLC == null) {
            return newOLC.getEdgeOfType(StateTransition.class);
        }
        Collection<StateTransition> transitionsOfNew =
                newOLC.getEdgeOfType(StateTransition.class);
        Collection<StateTransition> transitionsOfOld =
                oldOLC.getEdgeOfType(StateTransition.class);
        Collection<StateTransition> additionalTransitions = new HashSet<>();
        for (StateTransition newTransition : transitionsOfNew) {
            if (transitionIsNew(newTransition, transitionsOfOld)) {
                additionalTransitions.add(newTransition);
            }
        }
        return additionalTransitions;
    }

    /**
     * Determines weather or not a transition is new.
     * Therefor we need the transitions of the {@link #oldOLC} and the transition to
     * be checked.
     * A Transition is considered as new if at least one of the following conditions
     * is true:
     * - There is no transition with the label in the set of old transitions
     * - There is no transition with the source state in the old transitions
     * - There is no transition with the target state in the old transitions
     *
     * @param newTransition    The Transition which should be checked.
     * @param transitionsOfOld A Collection of old transitions.
     * @return True if the transition is new false if not.
     */
    private boolean transitionIsNew(StateTransition newTransition,
                                    Collection<StateTransition> transitionsOfOld) {
        for (StateTransition oldTransition : transitionsOfOld) {
            if (oldTransition.getLabel().equals(newTransition.getLabel()) &&
                    ((DataObjectState) oldTransition.getSource()).getName()
                            .equals(((DataObjectState) newTransition.getSource()).getName()) &&
                    ((DataObjectState) oldTransition.getTarget()).getName()
                            .equals(((DataObjectState) newTransition.getTarget()).getName())) {
                return false;
            }

        }
        return true;
    }
}
