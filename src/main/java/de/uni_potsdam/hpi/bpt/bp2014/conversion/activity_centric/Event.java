package de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

import java.util.ArrayList;
import java.util.List;

/**
 * Instances of this class represent Events of inside an {@link ActivityCentricProcessModel}.
 * Such a Event can be either a Start or an End Event.
 * The type of the event is specified by the type parameter, which is an instance of
 * {@link Type}.
 */
public class Event implements INode {
    /**
     * An Event is only allowed to have either an incoming or an
     * outgoing edge.
     * This edge must be of type {@link ControlFlow} and will be added.
     */
    public ControlFlow edge;
    /**
     * The type of the event, this variable is either END or START.
     */
    public Type type;

    public Type getType() {
        return type;
    }

    /**
     * Set the type of the event.
     * @param type The new type of the event.
     *             Pre: The type must not be null.
     */
    public void setType(Type type) {
        assert null != type :
                "The type of a gateway must not be null";
        this.type = type;
    }

    /**
     * Sets the edge of the event.
     * Be aware that only an End Event can have an incoming edge
     * and that once the edge has been set you may not change it.
     *
     * @param edge The edge which should be the incoming edge.
     *             Pre: The existing {@link #edge} must be null.
     *             The {@link #type} must be{@link Type#END}
     *             The edge must be an instance of {@link ControlFlow}
     */
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

    /**
     * Sets the edge of the event.
     * Be aware that only an End Event can have an outgoing edge
     * and that once the edge has been set you may not change it.
     *
     * @param edge The edge which should be the outgoing edge.
     *             Pre: The existing {@link #edge} must be null.
     *             The {@link #type} must be{@link Type#START}
     *             The edge must be an instance of {@link ControlFlow}
     */
    @Override
    public void addOutgoingEdge(IEdge edge) {
        assert type.equals(Type.START) :
                "Only Start Events are allowed to have an outgoing edge";
        assert null != edge :
                "The outgoing edge of an Start Event must nut be null";
        assert edge instanceof ControlFlow :
                "The outgoing edge of an Start Event must be Control Flow";
        this.edge = (ControlFlow)edge;
    }

    /**
     * Returns a list with the edge, if the Event is of type {@link Type#END}.
     * Else an empty list will be returned.
     * @return Returns the list of incoming edges.
     */
    @Override
    public List<IEdge> getIncomingEdges() {
        List<IEdge> incomingEdges = new ArrayList<>(1);
        if (type.equals(Type.END)) {
            incomingEdges.add(edge);
        }
        return incomingEdges;
    }

    /**
     * Returns a list with the edge, if the Event is of type {@link Type#START}.
     * Else an empty list will be returned.
     * @return Returns the list of outgoing edges.
     */
    @Override
    public List<IEdge> getOutgoingEdges() {
        List<IEdge> outgoingEdges = new ArrayList<>(1);
        if (type.equals(Type.START)) {
            outgoingEdges.add(edge);
        }
        return outgoingEdges;
    }


    /**
     * Returns a list with the edge, if the Event is of type {@link Type#START}
     * and if the class specified by the parameter is of the type ControlFlow.class.
     * if one of these conditions is false an empty list will be returned.
     * @param t The Class which will specify the returned elements.
     * @return Returns the list of outgoing edges.
     */
    @Override
    public <T extends IEdge> List<T> getOutgoingEdgesOfType(Class t) {
        List<T> outgoingEdges = new ArrayList<>(1);
        if (t.isAssignableFrom(ControlFlow.class) && type.equals(Type.START)) {
            outgoingEdges.add((T)edge);
        }
        return outgoingEdges;
    }

    /**
     * Returns a list with the edge, if the Event is of type {@link Type#END}
     * and if the class specified by the parameter is of the type ControlFlow.class.
     * if one of these conditions is false an empty list will be returned.
     * @param t The Class which will specify the returned elements.
     * @return Returns the list of incoming edges.
     */
    @Override
    public <T extends IEdge> List<T> getIncomingEdgesOfType(Class t) {
        List<T> incomingEdges = new ArrayList<>(1);
        if (t.isAssignableFrom(ControlFlow.class) && type.equals(Type.END) && null != edge) {
            incomingEdges.add((T)edge);
        }
        return incomingEdges;
    }

    /**
     * This enumeration defines the different possible Types of an Event.
     * An Event can either be an End or and a Start Event.
     */
    public enum Type {
        START, END
    }
}
