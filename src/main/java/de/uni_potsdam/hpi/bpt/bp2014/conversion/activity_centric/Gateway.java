package de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This Class represents a Gateway for the {@link ActivityCentricProcessModel}.
 * Each Gateway can either be of the type {@link Type#XOR} or {@link Type#AND}.
 * Gateways are not allowed to have outgoing or incoming edges which are not
 * instances of {@link ControlFlow}.
 * Gateways are the only nodes, which can have more than one incoming/outgoing
 * ControlFlow edge.
 */
public class Gateway implements INode {
    private List<ControlFlow> incomingControlFlow;
    private List<ControlFlow> outgoingControlFlow;
    /**
     * Defines the type of the Gateway.
     * Be aware that a type is mandatory if you
     * want to use a converter.
     */
    private Type type;

    public Gateway() {
        incomingControlFlow = new LinkedList<>();
        outgoingControlFlow = new LinkedList<>();
    }

    /**
     * Adds an incoming edge to the set of incoming edges.
     * @param edge The edge which will be added.
     *             Pre: the edge must not be null
     *             The edge must be of type ControlFlow.
     */
    @Override
    public void addIncomingEdge(IEdge edge) {
        assert null != edge :
                "An incoming edge must not be null";
        assert edge instanceof ControlFlow :
                "A gateway supports only ControlFlow";
        incomingControlFlow.add((ControlFlow)edge);
    }

    /**
     * Adds an outgoing edge to the set of outgoing edges.
     * @param edge The edge which will be added.
     *             Pre: the edge must not be null
     *             The edge must be of type ControlFlow.
     */
    @Override
    public void addOutgoingEdge(IEdge edge) {
        assert null != edge :
                "An outgoing edge must not be null";
        assert edge instanceof ControlFlow :
                "A gateway supports only ControlFlow";
        outgoingControlFlow.add((ControlFlow)edge);
        
    }

    /**
     * Returns a new List with all incoming edges.
     * Altering the list will not affect the state of the Activity.
     * Nevertheless altering the list elements will.
     *
     * @return A new list with all incoming edges.
     */
    @Override
    public List<IEdge> getIncomingEdges() {
        return new ArrayList<IEdge>(incomingControlFlow);
    }

    /**
     * Returns a new List with all outgoing edges.
     * Altering the list will not affect the state of the Activity.
     * Nevertheless altering the list elements will.
     *
     * @return A new list with all outgoing edges.
     */
    @Override
    public List<IEdge> getOutgoingEdges() {
        return new ArrayList<IEdge>(outgoingControlFlow);
    }

    /**
     * Returns a new List with all outgoing edges of a specific type.
     * Altering the list will not affect the state of the Activity.
     * Nevertheless altering the list elements will.
     * (Supported types are {@link ControlFlow}.)
     *
     * @param t the class which describes the type.
     * @return A new list with all outgoing edges which are from the specified type.
     *         Hierarchies are supported.
     */
    @Override
    public <T extends IEdge> List<T> getOutgoingEdgesOfType(Class t) {
        if (t.isAssignableFrom(ControlFlow.class)) {
            return new ArrayList<>((List<T>)outgoingControlFlow);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Returns a new List with all incoming edges of a specific type.
     * Altering the list will not affect the state of the Activity.
     * Nevertheless altering the list elements will.
     * (Supported types are {@link ControlFlow}.)
     *
     * @param t the class which describes the type.
     * @return A new list with all incoming edges which are from the specified type.
     *         Hierarchies are supported.
     */
    @Override
    public <T extends IEdge> List<T> getIncomingEdgesOfType(Class t) {
        if (t.isAssignableFrom(ControlFlow.class)) {
            return new ArrayList<>((List<T>)incomingControlFlow);
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

    /**
     * Defines the possible types for an Gateway.
     */
    public enum Type {
        XOR, AND
    }
}
