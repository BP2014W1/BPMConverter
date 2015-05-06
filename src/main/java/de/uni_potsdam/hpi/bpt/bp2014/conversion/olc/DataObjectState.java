package de.uni_potsdam.hpi.bpt.bp2014.conversion.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class DataObjectState implements INode {
    private Collection<StateTransition> incomingEdges;
    private Collection<StateTransition> outgoingEdges;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataObjectState(String name) {
        this.name = name;
        incomingEdges = new HashSet<>();
        outgoingEdges = new HashSet<>();
    }

    @Override
    public void addIncomingEdge(IEdge edge) {
        assert null != edge :
                "An incoming edge must not be null";
        assert edge instanceof StateTransition :
                "An incoming edge of a OLC must be a StateTransition";
        this.incomingEdges.add((StateTransition)edge);
    }

    @Override
    public void addOutgoingEdge(IEdge edge) {
        assert null != edge :
                "An outgoing edge must not be null";
        assert edge instanceof StateTransition :
                "An outgoing edge of a OLC must be a StateTransition";
        this.outgoingEdges.add((StateTransition) edge);
    }

    @Override
    public List<IEdge> getIncomingEdges() {
        return new ArrayList<IEdge>(incomingEdges);
    }

    @Override
    public List<IEdge> getOutgoingEdges() {
        return new ArrayList<IEdge>(outgoingEdges);
    }

    @Override
    public <T extends IEdge> List<T> getOutgoingEdgesOfType(Class t) {
        if (t.isAssignableFrom(StateTransition.class) || t.equals(IEdge.class)) {
            return new ArrayList<T>((List<T>)outgoingEdges);
        } else {
            return new ArrayList<T>();
        }
    }

    @Override
    public <T extends IEdge> List<T> getIncomingEdgesOfType(Class t) {
        if (t.isAssignableFrom(StateTransition.class) || t.equals(IEdge.class)) {
            return new ArrayList<T>((List<T>)incomingEdges);
        } else {
            return new ArrayList<T>();
        }
    }
}
