package de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

import java.util.ArrayList;
import java.util.List;


public class Event implements INode {
    public ControlFlow edge;
    public Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        assert null != type :
                "The type of a gateway must not be null";
        this.type = type;
    }

    @Override
    public void addIncomingEdge(IEdge edge) {
        assert type.equals(Type.END) :
                "Only End Events are allowed to have an incoming edge";
        assert null != edge :
                "The incoming edge of an End Event must nut be null";
        assert edge instanceof ControlFlow :
                "The incoming edge of an End Event must be Control Flow";
        this.edge = (ControlFlow)edge;
    }

    @Override
    public void addOutgoingEdge(IEdge edge) {
        assert type.equals(Type.START) :
                "Only End Events are allowed to have an outgoing edge";
        assert null != edge :
                "The outgoing edge of an End Event must nut be null";
        assert edge instanceof ControlFlow :
                "The outgoing edge of an End Event must be Control Flow";
        this.edge = (ControlFlow)edge;
    }

    @Override
    public List<IEdge> getIncomingEdges() {
        List<IEdge> incomingEdges = new ArrayList<>(1);
        if (type.equals(Type.END)) {
            incomingEdges.add(edge);
        }
        return incomingEdges;
    }

    @Override
    public List<IEdge> getOutgoingEdges() {
        List<IEdge> outgoingEdges = new ArrayList<>(1);
        if (type.equals(Type.START)) {
            outgoingEdges.add(edge);
        }
        return outgoingEdges;
    }

    @Override
    public <T extends IEdge> List<T> getOutgoingEdgesOfType(Class t) {
        List<T> outgoingEdges = new ArrayList<>(1);
        if (t.isAssignableFrom(ControlFlow.class) && type.equals(Type.START)) {
            outgoingEdges.add((T)edge);
        }
        return outgoingEdges;
    }

    @Override
    public <T extends IEdge> List<T> getIncomingEdgesOfType(Class t) {
        List<T> incomingEdges = new ArrayList<>(1);
        if (t.isAssignableFrom(ControlFlow.class) && type.equals(Type.END) && null != edge) {
            incomingEdges.add((T)edge);
        }
        return incomingEdges;
    }

    public enum Type {
        START, END
    }
}
