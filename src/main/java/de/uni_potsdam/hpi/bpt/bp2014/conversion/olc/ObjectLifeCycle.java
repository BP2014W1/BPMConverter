package de.uni_potsdam.hpi.bpt.bp2014.conversion.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.IModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class ObjectLifeCycle implements IModel {
    private List<DataObjectState> states;
    private DataObjectState startNode;
    private List<DataObjectState> finalStates;
    private String label;

    public ObjectLifeCycle(String label) {
        assert null != label :
                "The label of an ObjectLifeCycle represents the data Object," +
                        " hence it should never be null";
        this.label = label;
        init();
    }

    public ObjectLifeCycle() {
        this.label = "";
        init();
    }

    private void init() {
        states = new ArrayList<>();
        finalStates = new ArrayList<>();
    }


    @Override
    public List<INode> getNodes() {
        return new ArrayList<INode>(states);
    }

    @Override
    public void addNode(INode newNode) {
        assert newNode != null :
                "You should never add a node which is null to a model";
        assert newNode instanceof DataObjectState :
                "Nodes added to a object life cycle should be of type DataObjectState";
        states.add((DataObjectState)newNode);
    }

    @Override
    public <T extends INode> List<T> getNodesOfClass(Class T) {
        if (T == DataObjectState.class) {
            return new LinkedList<T>((List<T>)states);
        } else {
            return new LinkedList<T>();
        }
    }

    @Override
    public INode getStartNode() {
        return startNode;
    }

    @Override
    public void setStartNode(INode startNode) {
        assert startNode != null :
                "You should never add a node which is null to a model";
        assert startNode instanceof DataObjectState :
                "Nodes added to a object life cycle should be of type DataObjectState";
        this.startNode = (DataObjectState)startNode;
    }

    @Override
    public void addFinalNode(INode finalNode) {
        assert finalNode != null :
                "You should never add a node which is null to a model";
        assert finalNode instanceof DataObjectState :
                "Nodes added to a object life cycle should be of type DataObjectState";
        assert finalNode.getOutgoingEdges().isEmpty() :
                "A final node should have no outgoing edges";
        finalStates.add((DataObjectState) finalNode);
    }

    @Override
    public List<INode> getFinalStates() {
        return new ArrayList<INode>(finalStates);
    }

    @Override
    public <T extends INode> List<T> getFinalNodesOfClass(Class T) {
        if (T == DataObjectState.class || T == INode.class) {
            return new ArrayList<T>((List<T>) finalStates);
        } else {
            return new ArrayList<T>();
        }
    }

    public void initializeFinalStates() {
        for (DataObjectState state : states) {
            if (state.getOutgoingEdges().isEmpty() &&
                    !finalStates.contains(state)) {
                finalStates.add(state);
            }
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        assert null != label :
                "The label of an ObjectLifeCycle represents the data Object," +
                        " hence it should never be null";
        this.label = label;
    }

    public <T extends IEdge> List<T> getEdgeOfType(Class T) {
        List<T> edges = new ArrayList<>();
        for (INode node : states) {
            edges.addAll((List<T>)node.getOutgoingEdgesOfType(T));
        }
        return edges;
    }
}
