package de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ActivityCentricProcessModel implements IModel {
    private Event startEvent;
    private Event endEvent;
    private Set<INode> nodes;

    public ActivityCentricProcessModel() {
        nodes = new HashSet<>();
    }

    @Override
    public List<INode> getNodes() {
        return new ArrayList<>(nodes);
    }

    @Override
    public void addNode(INode newNode) {
        assert null != newNode :
                "A Node added to a model should never be null";
        assert newNode instanceof Activity ||
                newNode instanceof Gateway ||
                newNode instanceof Event ||
                newNode instanceof DataObject :
                "A Node, which is part of an activity centric process model " +
                        "must be either a Event, Activity, Gateway or DataObject";
        this.nodes.add(newNode);
    }

    @Override
    public <T extends INode> List<T> getNodesOfClass(Class T) {
        List<T> resultNodes = new ArrayList<>();
        for (INode node : nodes) {
            if (node.getClass() == T) {
                resultNodes.add((T)node);
            }
        }
        return resultNodes;
    }

    @Override
    public INode getStartNode() {
        return startEvent;
    }

    @Override
    public void setStartNode(INode startNode) {
        assert null != startNode :
                "The start node of an model must not be null";
        assert startNode instanceof Event :
                "The start node of an activity centric process model " +
                        "must be an event";
        assert ((Event)startNode).type.equals(Event.Type.START) :
                "The start node must be a start event";
        this.startEvent = (Event)startNode;
    }

    @Override
    public void addFinalNode(INode finalNode) {
        assert null != finalNode :
                "The final node of an model must not be null";
        assert finalNode instanceof Event :
                "The final node of an activity centric process model " +
                        "must be an event";
        assert ((Event)finalNode).type.equals(Event.Type.END) :
                "The final node must be a final event";
        this.endEvent = (Event)finalNode;
    }

    @Override
    public List<INode> getFinalStates() {
        List<INode> finalNodes = new ArrayList<>(1);
        finalNodes.add(endEvent);
        return finalNodes;
    }

    @Override
    public <T extends INode> List<T> getFinalNodesOfClass(Class t) {
        if (t.isAssignableFrom(Event.class)) {
            return (List<T>)getFinalStates();
        } else {
            return new ArrayList<>(0);
        }
    }
}
