package de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a DataObject node inside a {@link ActivityCentricProcessModel}.
 * A DataObject node is specified by a name and a state.
 * The state is an instance of {@link DataObjectState}.
 * In addition a {@link ObjectLifeCycle} can be referenced to define a Data Class.
 */
public class DataObject implements INode {
    /**
     * This object defines the state of the DataObject.
     */
    private DataObjectState state;
    /**
     * This variable holds the Object life cycle to re-
     * present the data class. It is not mandatory.
     */
    private ObjectLifeCycle olc;
    private String name;
    private List<DataFlow> incomingDataFlow;
    private List<DataFlow> outgoingDataFlow;

    /**
     * Creates a new DataObject for a given state and name.
     * The name is used to identify DataObjects of one Data Class.
     * @param name The name of the DataObject. For Example "invoice".
     * @param state The state of the data object.
     */
    public DataObject(String name, DataObjectState state) {
        this.state = state;
        this.name = name;
        incomingDataFlow = new LinkedList<>();
        outgoingDataFlow = new LinkedList<>();
    }

    public DataObjectState getState() {
        return state;
    }

    /**
     * Sets the state of the Data Object.
     * The state must not be null;
     * @param state The new state of the Data Object.
     */
    public void setState(DataObjectState state) {
        assert null != state :
                "The state of an Data Object must not be null";
        this.state = state;
    }

    public ObjectLifeCycle getOlc() {
        return olc;
    }

    /**
     * Sets the object life cycle of the Data Object.
     * The Object life cycle can be used to define a DataClass.
     * @param olc The new Object Life Cycle.
     */
    public void setOlc(ObjectLifeCycle olc) {
        assert null != olc :
                "The OLC of a Data Object must not be null";
        this.olc = olc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = null == name ? "" : name;
    }

    /**
     *  Adds a new edge to the incoming edges.
     * @param edge The Edge to be added.
     *             Pre: The edge must not be null.
     *             The edge must be of type DataFlow.
     */
    @Override
    public void addIncomingEdge(IEdge edge) {
        assert null != edge :
                "The incoming edge of a DataObject must not be null";
        assert edge instanceof DataFlow :
                "The incoming edge of a DataObject must be DataFlow";
        incomingDataFlow.add((DataFlow) edge);
    }

    /**
     * Adds a new edge to the outgoing edges.
     * @param edge The Edge to be added.
     *             Pre: The edge must not be null.
     *             The edge must be of type DataFlow.
     */
    @Override
    public void addOutgoingEdge(IEdge edge) {
        assert null != edge :
                "The outgoing edge of a DataObject must not be null";
        assert edge instanceof DataFlow :
                "The outgoing edge of a DataObject must be DataFlow";
        outgoingDataFlow.add((DataFlow) edge);

    }

    /**
     * Returns all incoming edges of the data object.
     * @return Returns a list of edges.
     * The list is a new list, hence changes will not affect the data object.
     * Altering the list elements will do.
     */
    @Override
    public List<IEdge> getIncomingEdges() {
        return new ArrayList<IEdge>(incomingDataFlow);
    }


    /**
     * Returns all outgoing edges of the data object.
     * @return Returns a list of edges.
     * The list is a new list, hence changes will not affect the data object.
     * Altering the list elements will do.
     */
    @Override
    public List<IEdge> getOutgoingEdges() {
        return new ArrayList<IEdge>(outgoingDataFlow);
    }

    /**
     * Returns all outgoing edges of the data object, which are instances of the data object.
     * @param t The class which will be a constraint for the edges to be returned.
     * @return Returns a list of edges.
     * The list is a new list, hence changes will not affect the data object.
     * Altering the list elements will do.
     */
    @Override
    public <T extends IEdge> List<T> getOutgoingEdgesOfType(Class t) {
        if (t.isAssignableFrom(DataObject.class)) {
            return new ArrayList<>((List<T>) outgoingDataFlow);
        }
        return new ArrayList<>();
    }

    /**
     * Returns all incoming edges of the data object, which are instances of the data object.
     * @param t The class which will be a constraint for the edges to be returned.
     * @return Returns a list of edges.
     * The list is a new list, hence changes will not affect the data object.
     * Altering the list elements will do.
     */
    @Override
    public <T extends IEdge> List<T> getIncomingEdgesOfType(Class t) {
        if (t.isAssignableFrom(DataObject.class)) {
            return new ArrayList<>((List<T>) incomingDataFlow);
        }
        return new ArrayList<>();
    }
}
