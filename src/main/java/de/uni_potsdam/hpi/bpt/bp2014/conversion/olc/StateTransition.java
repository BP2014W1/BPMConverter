package de.uni_potsdam.hpi.bpt.bp2014.conversion.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

/**
 * This class represents a StateTransition.
 * This transition connects two States.
 * Each transition should have a label in order to determine
 * the action which must be executed while triggering the
 * transition.
 */
public class StateTransition implements IEdge {
    private INode source;
    private INode target;
    /**
     * The label which identifies the action represented
     * by this transition.
     */
    private String label;

    /**
     * Creates a new Instance of the state transition
     * for a given source, target and label.
     *
     * @param source The source of the new state transition.
     * @param target The target of the new state transition.
     * @param label  the label of the new state transition - must not be null.
     */
    public StateTransition(DataObjectState source, DataObjectState target, String label) {
        assert null != label :
                "The label of a StateTransition will be used to identify" +
                        " combined transitions, hence it must not be null";
        this.source = source;
        this.target = target;
        this.label = label;
    }

    /**
     * Creates a new state transition.
     * The label will be initialized for an empty state.
     */
    public StateTransition() {
        label = "";
    }

    @Override
    public INode getSource() {
        return source;
    }

    /**
     * Sets/Overwrites the source of the edge.
     * The source must not be null and must be of type DataObjectState.
     *
     * @param source The new source of the edge.
     */
    @Override
    public void setSource(INode source) {
        assert null != source :
                "The source node of an edge should not be set to null";
        assert source instanceof DataObjectState :
                "The source node of a state transition should be of Type DataObjectState";
        this.source = source;
    }

    @Override
    public INode getTarget() {
        return target;
    }

    /**
     * Sets/Overwrites the target of the edge.
     * The target must not be null and must be of type DataObjectState.
     *
     * @param target The target of the edge.
     */
    @Override
    public void setTarget(INode target) {
        assert null != target :
                "The target node of an edge should not be set to null";
        assert target instanceof DataObjectState :
                "The target node of a state transition should be of Type DataObjectState";
        this.target = target;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Sets the label of the transition
     * Be aware that null is not an valid value.
     * @param label The new label name.
     */
    public void setLabel(String label) {
        assert null != label :
                "The label of a StateTransition will be used to identify" +
                        " combined transitions, hence it must not be null";
        this.label = label;
    }

    /**
     * If source, target and label are equal the state transitions
     * will be equal.
     * @param other The other Object to compare with
     * @return Returns true, if the state transitions are equal, else false.
     */
    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof StateTransition)) {
            return false;
        }
        StateTransition otherTransition = (StateTransition) other;
        return otherTransition.source.equals(source) &&
                otherTransition.target.equals(target);
    }

    /**
     * A Hash method adapted to the equal method.
     * @return Returns the hashcode.
     */
    @Override
    public int hashCode() {
        return target.hashCode() + source.hashCode();
    }
}
