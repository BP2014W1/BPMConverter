package de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Activity implements INode {
    private Set<DataFlow> incomingDataFlow;
    private Set<ControlFlow> incomingControlFlow;
    private Set<DataFlow> outgoingDataFlow;
    private Set<ControlFlow> outgoingControlFlow;
    private String name;

    /**
     * This constructor creates a new Activity with an empty name.
     * All variables/ Sets will be initialized.
     */
    public Activity() {
        name = "";
        init();
    }

    /**
     * This constructor creates a new Activity with an given name.
     * All variables/ Sets will be initialized.
     */
    public Activity(String name) {
        this.name = name;
        init();
    }

    /**
     * Initializes all the Sets.
     * Be aware that calling this method multiple times resets the Activity.
     */
    public void init() {
        incomingDataFlow = new HashSet<>();
        incomingControlFlow = new HashSet<>();
        outgoingControlFlow = new HashSet<>();
        outgoingDataFlow = new HashSet<>();
    }

    /**
     * Adds an given edge to the incoming edges.
     * @param edge The edge to be added.
     *
     * Pre: Ehe edge must not be null.
     *      The Edge must be either {@link ControlFlow}
     *      The Edge must be either {@link DataFlow}
     */
    @Override
    public void addIncomingEdge(IEdge edge) {
        assert null != edge :
                "An incoming edge of an node must not be null";
        assert edge instanceof ControlFlow ||
                edge instanceof DataFlow :
                "An Activity can only be target of ControlFlow or DataFlow";
        if (edge instanceof ControlFlow) {
            assert incomingControlFlow.isEmpty() :
                    "Each activity must have only one incoming ControlFlow";
            incomingControlFlow.add((ControlFlow) edge);
        } else {
            incomingDataFlow.add((DataFlow) edge);
        }
    }


    /**
     * Adds an given edge to the outgoing edges.
     * @param edge The edge to be added.
     *
     * Pre: Ehe edge must not be null.
     *      The Edge must be either {@link ControlFlow} or {@link DataFlow}
     */
    @Override
    public void addOutgoingEdge(IEdge edge) {
        assert null != edge :
                "An outgoing edge of an node must not be null";
        assert edge instanceof ControlFlow ||
                edge instanceof DataFlow :
                "An Activity can only be source of ControlFlow or DataFlow";
        if (edge instanceof ControlFlow) {
            assert outgoingControlFlow.isEmpty() :
                    "Each activity must have only one outgoing ControlFlow";
            outgoingControlFlow.add((ControlFlow) edge);
        } else {
            outgoingDataFlow.add((DataFlow) edge);
        }
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
        List<IEdge> incomingEdges = new ArrayList<IEdge>(incomingControlFlow);
        incomingEdges.addAll(incomingDataFlow);
        return incomingEdges;
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
        List<IEdge> outgoingEdges = new ArrayList<IEdge>(outgoingControlFlow);
        outgoingEdges.addAll(outgoingDataFlow);
        return outgoingEdges;
    }

    /**
     * Returns a new List with all outgoing edges of a specific type.
     * Altering the list will not affect the state of the Activity.
     * Nevertheless altering the list elements will.
     *
     * @param t the class which describes the type.
     * @return A new list with all outgoing edges which are from the specified type.
     *         Hierarchies are supported.
     */
    @Override
    public <T extends IEdge> List<T> getOutgoingEdgesOfType(Class t) {
        if (t.isAssignableFrom(DataFlow.class)) {
            return new ArrayList<T>((Set<T>) outgoingDataFlow);
        } else if (t.isAssignableFrom(ControlFlow.class)) {
            return new ArrayList<T>((Set<T>) outgoingControlFlow);
        } else if (t.isAssignableFrom(IEdge.class)) {
            return (List<T>) getOutgoingEdges();
        } else {
            return new ArrayList<T>();
        }
    }

    /**
     * Returns a new List with all outgoing edges of a specific type.
     * Altering the list will not affect the state of the Activity.
     * Nevertheless altering the list elements will.
     *
     * @param t the class which describes the type.
     * @return A new list with all outgoing edges which are from the specified type.
     *         Hierarchies are supported.
     */
    @Override
    public <T extends IEdge> List<T> getIncomingEdgesOfType(Class t) {
        if (t.isAssignableFrom(DataFlow.class)) {
            return new ArrayList<T>((Set<T>) incomingDataFlow);
        } else if (t.isAssignableFrom(ControlFlow.class)) {
            return new ArrayList<T>((Set<T>) incomingControlFlow);
        } else if (t.isAssignableFrom(IEdge.class)) {
            return (List<T>) getIncomingEdges();
        } else {
            return new ArrayList<T>();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        assert null != name :
                "The name of an activity must never be null";
        this.name = name;
    }
}
