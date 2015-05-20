package de.uni_potsdam.hpi.bpt.bp2014.conversion.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Instances of this class can be used to represent states of a DataObject.
 * Such a state can be used inside an {@link ObjectLifeCycle} to create
 * the state transition system.
 * In addition these states can be added to a {@link de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.DataObject}
 * to refine the definition.
 */
public class DataObjectState implements INode {
    /**
     * As a part of a Object Life Cycle a DataObjectState
     * can be target of multiple StateTransitions.
     * Such transitions should be added as incoming Edges.
     */
    private Collection<StateTransition> incomingEdges;
    /**
     * As a part of a Object Life Cycle a DataObjectState
     * can be source of multiple StateTransitions.
     * Such transitions should be added as outgoing Edges.
     */
    private Collection<StateTransition> outgoingEdges;
    private String name;

    /**
     * Creates a new instance of the DataObjectState for a specified name.
     * The name can be null or changed afterwards.
     * @param name The name/identifier of the state.
     */
    public DataObjectState(String name) {
        this.name = name;
        incomingEdges = new HashSet<>();
        outgoingEdges = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Adds an incoming edge.
     * Such an Edge should be a state transition and not null.
     * You may throw assertion exceptions to verify this.
     * @param edge The Edge to be added as an incoming edge.
     */
    @Override
    public void addIncomingEdge(IEdge edge) {
        assert null != edge :
                "An incoming edge must not be null";
        assert edge instanceof StateTransition :
                "An incoming edge of a OLC must be a StateTransition";
        this.incomingEdges.add((StateTransition)edge);
    }


    /**
     * Adds an outgoing edge.
     * Such an Edge should be a state transition and not null.
     * You may throw assertion exceptions to verify this.
     * @param edge The Edge to be added as an outgoing edge.
     */
    @Override
    public void addOutgoingEdge(IEdge edge) {
        assert null != edge :
                "An outgoing edge must not be null";
        assert edge instanceof StateTransition :
                "An outgoing edge of a OLC must be a StateTransition";
        this.outgoingEdges.add((StateTransition) edge);
    }

    /**
     * Returns all incoming edges.
     * The returned list will newly created.
     * So, changes to the list will not affect the state of the DataObjectState.
     * Nevertheless, changing the elements will.
     * @return A new list containing all incoming edges.
     */
    @Override
    public List<IEdge> getIncomingEdges() {
        return new ArrayList<IEdge>(incomingEdges);
    }

    /**
     * Returns all outgoing edges.
     * The returned list will newly created.
     * So, changes to the list will not affect the state of the DataObjectState.
     * Nevertheless, changing the elements will.
     * @return A new list containing all outgoing edges.
     */
    @Override
    public List<IEdge> getOutgoingEdges() {
        return new ArrayList<IEdge>(outgoingEdges);
    }

    /**
     * Returns all outgoing edges of a specified type.
     * This is like a selection over the available edges.
     * @param t The parameter specifies the type of the edges.
     * @param <T> Defines the return type.
     * @return Returns a List of type T with all Outgoing edges
     * of the type t.
     */
    @Override
    public <T extends IEdge> List<T> getOutgoingEdgesOfType(Class t) {
        if (t.isAssignableFrom(StateTransition.class) || t.equals(IEdge.class)) {
            return new ArrayList<T>((Collection<T>)outgoingEdges);
        } else {
            return new ArrayList<T>();
        }
    }

    /**
     * Returns all incoming edges of a specified type.
     * This is like a selection over the available edges.
     * @param t The parameter specifies the type of the edges.
     * @param <T> Defines the return type.
     * @return Returns a List of type T with all Incoming edges
     * of the type t.
     */
    @Override
    public <T extends IEdge> List<T> getIncomingEdgesOfType(Class t) {
        if (t.isAssignableFrom(StateTransition.class) || t.equals(IEdge.class)) {
            return new ArrayList<T>((Collection<T>)incomingEdges);
        } else {
            return new ArrayList<T>();
        }
    }
}
