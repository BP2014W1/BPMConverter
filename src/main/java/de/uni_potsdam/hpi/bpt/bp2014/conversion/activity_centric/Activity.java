package de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

import java.util.*;

public class Activity implements INode {
    private Set<DataFlow> incomingDataFlow;
    private Set<ControlFlow> incomingControlFlow;
    private Set<DataFlow> outgoingDataFlow;
    private Set<ControlFlow> outgoingControlFlow;
    private String name;

    public Activity() {
        name = "";
        init();
    }

    public Activity(String name) {
        this.name = name;
        init();
    }

    public void init() {
        incomingDataFlow = new HashSet<>();
        incomingControlFlow = new HashSet<>();
        outgoingControlFlow = new HashSet<>();
        outgoingDataFlow = new HashSet<>();
    }

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
            incomingControlFlow.add((ControlFlow)edge);
        } else {
            incomingDataFlow.add((DataFlow)edge);
        }
    }

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
            outgoingControlFlow.add((ControlFlow)edge);
        } else {
            outgoingDataFlow.add((DataFlow)edge);
        }
    }

    @Override
    public List<IEdge> getIncomingEdges() {
        List<IEdge> incomingEdges = new ArrayList<IEdge>(incomingControlFlow);
        incomingEdges.addAll(incomingDataFlow);
        return incomingEdges;
    }

    @Override
    public List<IEdge> getOutgoingEdges() {
        List<IEdge> outgoingEdges = new ArrayList<IEdge>(outgoingControlFlow);
        outgoingEdges.addAll(incomingDataFlow);
        return outgoingEdges;
    }

    @Override
    public <T extends IEdge> List<T> getOutgoingEdgesOfType(Class T) {
        if (T == IEdge.class) {
            return (List<T>)getOutgoingEdges();
        } else if (T == DataFlow.class) {
            return new ArrayList<T>((Set<T>)outgoingDataFlow);
        } else if (T == ControlFlow.class) {
            return new ArrayList<T>((Set<T>)outgoingControlFlow);
        } else {
            return new ArrayList<T>();
        }
    }

    @Override
    public <T extends IEdge> List<T> getIncomingEdgesOfType(Class T) {
        if (T == IEdge.class) {
            return (List<T>)getIncomingEdges();
        } else if (T == DataFlow.class) {
            return new ArrayList<T>((Set<T>)incomingDataFlow);
        } else if (T == ControlFlow.class) {
            return new ArrayList<T>((Set<T>)incomingControlFlow);
        } else {
            return new ArrayList<T>();
        }
    }

    public void setName(String name) {
        assert null != name :
                "The name of an activity must never be null";
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
