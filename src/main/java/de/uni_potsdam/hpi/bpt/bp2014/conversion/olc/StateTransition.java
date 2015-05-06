package de.uni_potsdam.hpi.bpt.bp2014.conversion.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

public class StateTransition implements IEdge {
    private INode source;
    private INode target;
    private String label;

    public StateTransition(DataObjectState source, DataObjectState target, String label) {
        assert null != label :
                "The label of a StateTransition will be used to identify" +
                        " combined transitions, hence it must not be null";
        this.source = source;
        this.target = target;
        this.label = label;
    }

    public StateTransition() {
        label = "";
    }

    @Override
    public INode getSource() {
        return source;
    }

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

    public void setLabel(String label) {
        assert null != label :
                "The label of a StateTransition will be used to identify" +
                        " combined transitions, hence it must not be null";
        this.label = label;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof StateTransition)) {
            return false;
        }
        StateTransition otherTransition = (StateTransition)other;
        return otherTransition.source.equals(source) &&
                otherTransition.target.equals(target);
    }

    @Override
    public int hashCode() {
        return target.hashCode() + source.hashCode();
    }
}
