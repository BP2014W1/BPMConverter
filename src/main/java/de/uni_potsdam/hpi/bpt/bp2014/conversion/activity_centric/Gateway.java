package de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Gateway implements INode {
    private List<ControlFlow> incomingControlFlow;
    private List<ControlFlow> outgoingControlFlow;
    private Type type;

    public Gateway() {
        incomingControlFlow = new LinkedList<>();
        outgoingControlFlow = new LinkedList<>();
    }

    @Override
    public void addIncomingEdge(IEdge edge) {
        assert null != edge :
                "An incoming edge must not be null";
        assert edge instanceof ControlFlow :
                "A gateway supports only ControlFlow";
        incomingControlFlow.add((ControlFlow)edge);
    }

    @Override
    public void addOutgoingEdge(IEdge edge) {
        assert null != edge :
                "An outgoing edge must not be null";
        assert edge instanceof ControlFlow :
                "A gateway supports only ControlFlow";
        outgoingControlFlow.add((ControlFlow)edge);
        
    }

    @Override
    public List<IEdge> getIncomingEdges() {
        return new ArrayList<IEdge>(incomingControlFlow);
    }

    @Override
    public List<IEdge> getOutgoingEdges() {
        return new ArrayList<IEdge>(outgoingControlFlow);
    }

    @Override
    public <T extends IEdge> List<T> getOutgoingEdgesOfType(Class t) {
        if (t.isAssignableFrom(ControlFlow.class)) {
            return new ArrayList<>((List<T>)outgoingControlFlow);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public <T extends IEdge> List<T> getIncomingEdgesOfType(Class t) {
        if (t.isAssignableFrom(ControlFlow.class)) {
            return new ArrayList<>((List<T>)outgoingControlFlow);
        } else {
            return new ArrayList<>();
        }
    }

    public void setType(Type type) {
        assert null != type :
                "The type of a gateway must not be null";
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        XOR, AND
    }
}
