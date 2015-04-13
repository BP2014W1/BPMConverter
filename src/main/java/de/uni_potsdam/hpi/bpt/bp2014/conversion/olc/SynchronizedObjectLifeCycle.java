package de.uni_potsdam.hpi.bpt.bp2014.conversion.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

import java.util.*;

/**
 * Created by Stpehan on 01.04.2015.
 */
public class SynchronizedObjectLifeCycle implements IModel {
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
    public List<INode> getFinalStates() {
        List<INode> nodes = new LinkedList<>();
        for (ObjectLifeCycle olc : objectLifeCycles) {
            nodes.addAll(olc.getFinalStates());
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
}
