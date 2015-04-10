package de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DataObject implements INode {
    private DataObjectState state;
    private ObjectLifeCycle olc;
    private String name;
    private List<DataFlow> incomingDataFlow;
    private List<DataFlow> outgoingDataFlow;

    public DataObject(String name, DataObjectState state) {
        this.state = state;
        this.name = name;
        incomingDataFlow = new LinkedList<>();
        outgoingDataFlow = new LinkedList<>();
    }

    public DataObjectState getState() {
        return state;
    }

    public void setState(DataObjectState state) {
        assert null != state :
                "The state of an Data Object must not be null";
        this.state = state;
    }

    public ObjectLifeCycle getOlc() {
        return olc;
    }

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

    @Override
    public void addIncomingEdge(IEdge edge) {
        assert null != edge :
                "The incoming edge of a DataObject must not be null";
        assert edge instanceof DataFlow :
                "The incoming edge of a DataObject must be DataFlow";
        incomingDataFlow.add((DataFlow) edge);
    }

    @Override
    public void addOutgoingEdge(IEdge edge) {
        assert null != edge :
                "The outgoing edge of a DataObject must not be null";
        assert edge instanceof DataFlow :
                "The outgoing edge of a DataObject must be DataFlow";
        outgoingDataFlow.add((DataFlow) edge);

    }

    @Override
    public List<IEdge> getIncomingEdges() {
        return new ArrayList<IEdge>(incomingDataFlow);
    }

    @Override
    public List<IEdge> getOutgoingEdges() {
        return new ArrayList<IEdge>(outgoingDataFlow);
    }

    @Override
    public <T extends IEdge> List<T> getOutgoingEdgesOfType(Class t) {
        if (t.isAssignableFrom(DataObject.class)) {
            return new ArrayList<>((List<T>) outgoingDataFlow);
        }
        return new ArrayList<>();
    }

    @Override
    public <T extends IEdge> List<T> getIncomingEdgesOfType(Class t) {
        if (t.isAssignableFrom(DataObject.class)) {
            return new ArrayList<>((List<T>) incomingDataFlow);
        }
        return new ArrayList<>();
    }
}
