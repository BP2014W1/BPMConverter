package de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.IModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;

import java.util.*;

/**
 * Created by Stpehan on 01.04.2015.
 */
public class SynchronizedObjectLifeCycle implements IModel {
    public void setObjectLifeCycles(List<ObjectLifeCycle> objectLifeCycles) {
        this.objectLifeCycles = objectLifeCycles;
    }

    private List<ObjectLifeCycle> objectLifeCycles;

    public Map<StateTransition, List<StateTransition>> getSynchronisationEdges() {
        return synchronisationEdges;
    }

    public void setSynchronisationEdges(Map<StateTransition, List<StateTransition>> synchronisationEdges) {
        this.synchronisationEdges = synchronisationEdges;
    }

    private Map<StateTransition, List<StateTransition>> synchronisationEdges;

    public SynchronizedObjectLifeCycle() {
        objectLifeCycles = new ArrayList<>();
        synchronisationEdges = new HashMap<>();
    }


    public List<ObjectLifeCycle> getOLCs() {
        return objectLifeCycles;
    }

    @Override
    public List<INode> getNodes() {
        List<INode> nodes = new LinkedList<>();
        for (ObjectLifeCycle olc : objectLifeCycles) {
            nodes.addAll(olc.getNodes());
        }
        return nodes;
    }

    @Override
    public void addNode(INode newNode) {
        // Do nothing
    }

    @Override
    public <T extends INode> List<T> getNodesOfClass(Class t) {
        List<T> nodes = new LinkedList<>();
        for (ObjectLifeCycle olc : objectLifeCycles) {
            nodes.addAll((List<T>) olc.getNodesOfClass(t));
        }
        return nodes;
    }

    @Override
    public INode getStartNode() {
        return null;
    }

    @Override
    public void setStartNode(INode startNode) {
        // Not supported
    }

    @Override
    public void addFinalNode(INode finalNode) {
        // Not supported
    }

    @Override
    public List<INode> getFinalNodes() {
        List<INode> nodes = new LinkedList<>();
        for (ObjectLifeCycle olc : objectLifeCycles) {
            nodes.addAll(olc.getFinalNodes());
        }
        return nodes;
    }

    @Override
    public <T extends INode> List<T> getFinalNodesOfClass(Class t) {
        List<T> nodes = new LinkedList<>();
        for (ObjectLifeCycle olc : objectLifeCycles) {
            nodes.addAll((List<T>)olc.getFinalNodesOfClass(t));
        }
        return nodes;
    }

    public <T extends IEdge> List<T> getEdgesOfType(Class t) {
        Set<T> edges = new HashSet<>();
        for (INode node : getNodes()) {
            edges.addAll((Collection<T>) node.getOutgoingEdgesOfType(t));
        }
        return new ArrayList<>(edges);
    }
}
