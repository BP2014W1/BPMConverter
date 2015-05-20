package de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.IModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

import java.util.*;

/**
 * This class represents an Activity Centric Process Models such as
 * a BPMN model.
 * It consists of different nodes which can be connected using various
 * Edges.
 * Supported nodes are {@link Activity}, {@link Event}, {@link Gateway} and
 * {@link DataObject}.
 */
public class ActivityCentricProcessModel implements IModel {
    /**
     * A Variable to hold the start event of the model.
     * The start event will also be the start node of the model.
     * Hence the final model must contain a start event.
     */
    private Event startEvent;

    /**
     * A Variable to hold the end event of the model.
     * The start event will also be the final node of the model.
     * Hence the final model must contain a end event.
     * Also multiple end events are not supported.
     */
    private Event endEvent;

    /**
     * A Variable to hold the nodes of the Process model.
     * Be aware that the start and end event must be added
     * to this collection as well.
     */
    private Set<INode> nodes;

    public ActivityCentricProcessModel() {
        nodes = new HashSet<>();
    }

    @Override
    public List<INode> getNodes() {
        return new ArrayList<>(nodes);
    }

    /**
     * Adds a new Node to the processModel.
     * Duplicated nodes will be detected an removed.
     *
     * @param newNode The node to be added.
     *                Pre: new node must not be null
     *                     new node must be either of type Activity, Gateway,
     *                     Event or DataObject (Hierarchies are supported)
     */
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

    /**
     * Returns a new list of nodes which fit to the specified
     * @param clazz
     * @param <T> The type of the returned list (should be compatible to clazz)
     * @return The newly created list of nodes.
     * Changing the list will not affect the state of the model.
     * Nevertheless altering the elements may lead to a corrupt state.
     */
    @Override
    public <T extends INode> List<T> getNodesOfClass(Class clazz) {
        List<T> resultNodes = new ArrayList<>();
        for (INode node : nodes) {
            if (clazz.isInstance(node)) {
                resultNodes.add((T)node);
            }
        }
        return resultNodes;
    }

    @Override
    public INode getStartNode() {
        return startEvent;
    }

    /**
     * Set the start Event of the Activity Model.
     * @param startNode The node which represents the start node.
     *                  Pre: the node must not be null;
     *                       the node must be an Event;
     *                       the event must have the type Start;
     */
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


    /**
     * Set the end Event of the Activity Model.
     * @param finalNode The node which represents the end event.
     *                  Pre: the node must not be null;
     *                       the node must be an Event;
     *                       the event must have the type End;
     */
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

    /**
     * Returns a new list containing the end Event.
     * @return A List with the end event or null.
     */
    @Override
    public List<INode> getFinalNodes() {
        List<INode> finalNodes = new ArrayList<>(1);
        finalNodes.add(endEvent);
        return finalNodes;
    }


    /**
     * Returns a new list containing the end Event.
     * @return A List with the end event or an empty list.
     */
    @Override
    public <T extends INode> List<T> getFinalNodesOfClass(Class t) {
        if (t.isAssignableFrom(Event.class)) {
            return (List<T>) getFinalNodes();
        } else {
            return new ArrayList<>(0);
        }
    }

    /**
     * Returns a list with all edges of the model.
     * Therefor, we iterate over the outgoing edges of all nodes.
     * @return A new List with all edges.
     */
    public List<IEdge> getEdges() {
        Collection<IEdge> edges = new HashSet<>();
        for (INode node : nodes) {
            edges.addAll(node.getOutgoingEdges());
        }
        return new ArrayList<IEdge>(edges);
    }
}
