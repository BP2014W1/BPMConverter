package de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.IModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;

import java.util.*;

/**
 * This class represents a Synchronized Object Life Cycle.
 * A Synchronized Object Life Cycle Aggregates a multiple Object Life Cycles
 * and holds additional synchronization edges between state transitions.
 */
public class SynchronizedObjectLifeCycle implements IModel {
    /**
     * A List of {@link ObjectLifeCycle} representing
     * the synchronized Object Life Cycles.
     * Only Edges of such an OLC can be synchronized.
     */
    private List<ObjectLifeCycle> objectLifeCycles;
    /**
     * The Synchronization edges represent transitions which
     * must be executed together.
     * For each transition there is a list of transitions that
     * will be executed as well.
     */
    private Map<StateTransition, List<StateTransition>> synchronisationEdges;

    /**
     * Creates a new and empty Synchronized Object Life Cycle.
     * With no olcs
     */
    public SynchronizedObjectLifeCycle() {
        objectLifeCycles = new ArrayList<>();
        synchronisationEdges = new HashMap<>();
    }


    public List<ObjectLifeCycle> getOLCs() {
        return objectLifeCycles;
    }

    /**
     * Returns a list of Nodes, this list contains
     * all the nodes from every OLC of this synchronized Object
     * Life Cycle.
     * The list will be created every time so this operation is
     * quite expensive.
     *
     * @return The aggregated List of nodes.
     */
    @Override
    public List<INode> getNodes() {
        List<INode> nodes = new LinkedList<>();
        for (ObjectLifeCycle olc : objectLifeCycles) {
            nodes.addAll(olc.getNodes());
        }
        return nodes;
    }

    /**
     * it is not possible to add new Nodes.
     * @param newNode This parameter will be ignored.
     */
    @Override
    public void addNode(INode newNode) {
        // Do nothing
    }

    /**
     * Returns a list of Nodes, this list contains
     * all the nodes from every OLC of this synchronized Object
     * Life Cycle.
     * Every node must fulfill the condition, means must be an instance
     * of the class specified.
     * The list will be created every time so this operation is
     * quite expensive.
     *
     * @param t The class for the selection condition.
     * @param <T> the return type.
     * @return The aggregated List of nodes.
     */
    @Override
    public <T extends INode> List<T> getNodesOfClass(Class t) {
        List<T> nodes = new LinkedList<>();
        for (ObjectLifeCycle olc : objectLifeCycles) {
            nodes.addAll((List<T>) olc.getNodesOfClass(t));
        }
        return nodes;
    }

    /**
     * There is no specific start state.
     * @return Will always be null.
     */
    @Override
    public INode getStartNode() {
        return null;
    }

    /**
     * it is not possible to set a start state.
     * @param startNode This parameter will be ignored.
     */
    @Override
    public void setStartNode(INode startNode) {
        // Not supported
    }

    /**
     * it is not possible to add a final state.
     * @param finalNode This parameter will be ignored.
     */
    @Override
    public void addFinalNode(INode finalNode) {
        // Not supported
    }

    /**
     * Accumulates the final nodes of all Object Life cycles being
     * part of this synchronized object life cycle.
     * @return Returns the final nodes of all Object Life Cycles
     * of this Synchronized Object Life Cycle.
     */
    @Override
    public List<INode> getFinalNodes() {
        List<INode> nodes = new LinkedList<>();
        for (ObjectLifeCycle olc : objectLifeCycles) {
            nodes.addAll(olc.getFinalNodes());
        }
        return nodes;
    }

    /**
     * Accumulates the final nodes of the specified Type of all Object Life cycles
     * being part of this synchronized object life cycle.
     * @return Returns the final nodes of all Object Life Cycles
     * of this Synchronized Object Life Cycle if they hold the selection ondition.
     */
    @Override
    public <T extends INode> List<T> getFinalNodesOfClass(Class t) {
        List<T> nodes = new LinkedList<>();
        for (ObjectLifeCycle olc : objectLifeCycles) {
            nodes.addAll((List<T>)olc.getFinalNodesOfClass(t));
        }
        return nodes;
    }

    /**
     * Returns all edges of the all Synchronized Object life Cycle, if they
     * are instances of the specified type. Therefore they call the same
     * method on each object life cycle.
     * @param t Specifies the edge type.
     * @param <T> Specifies the return type.
     * @return Returns all edges of the all Synchronized Object life Cycle
     * which fullfill the condition.
     */
    public <T extends IEdge> List<T> getEdgesOfType(Class t) {
        Set<T> edges = new HashSet<>();
        for (INode node : getNodes()) {
            edges.addAll((Collection<T>) node.getOutgoingEdgesOfType(t));
        }
        return new ArrayList<>(edges);
    }

    public void setObjectLifeCycles(List<ObjectLifeCycle> objectLifeCycles) {
        this.objectLifeCycles = objectLifeCycles;
    }

    public Map<StateTransition, List<StateTransition>> getSynchronisationEdges() {
        return synchronisationEdges;
    }

    public void setSynchronisationEdges(Map<StateTransition, List<StateTransition>> synchronisationEdges) {
        this.synchronisationEdges = synchronisationEdges;
    }
}
