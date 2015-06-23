package de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.scenario;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.ActivityCentricProcessModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * This class's instances represent a Production Case Management Scenario.
 * Such a scenario consist of multiple Fragments, which can be modeled
 * using {@link de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.ActivityCentricProcessModel}s.
 */
public class Scenario implements IModel {
    private Collection<ActivityCentricProcessModel> fragments;

    public Scenario(Collection<ActivityCentricProcessModel> fragments) {
        this.fragments = new HashSet<>(fragments);
    }

    /**
     * This method adds a new Fragment to the scenario.
     * Such a Fragment is represented using ActivityCentricProcessModels.
     *
     * @param fragment
     * Pre: the fragment must not be null
     */
    public void addFragment(ActivityCentricProcessModel fragment) {
        assert null != fragment : "The Fragment must not be null";
        fragments.add(fragment);
    }

    /**
     * This method returns all Fragments.
     * A new Collection is returned to save the state of the scenario.
     * @return The collection of fragments.
     */
    public Collection<ActivityCentricProcessModel> getFragments() {
        return new HashSet<ActivityCentricProcessModel>(fragments);
    }

    @Override
    public List<INode> getNodes() {
        List<INode> nodes = new LinkedList<>();
        for (ActivityCentricProcessModel fragment : fragments) {
            nodes.addAll(fragment.getNodes());
        }
        return nodes;
    }

    @Override
    public void addNode(INode newNode) {
        throw new UnsupportedOperationException("You cannot add nodes to a scenario");
        // Action not supported
    }

    @Override
    public <T extends INode> List<T> getNodesOfClass(Class clazz) {
        List<T> nodes = new LinkedList<>();
        for (ActivityCentricProcessModel fragment : fragments) {
            nodes.addAll((List<T>)fragment.getNodesOfClass(clazz));
        }
        return nodes;
    }

    @Override
    public INode getStartNode() {
        throw new UnsupportedOperationException("There is more than one start node, check each fragment");
    }

    @Override
    public void setStartNode(INode startNode) {
        throw new UnsupportedOperationException("You cannot set the start node of a scenario.");
    }

    @Override
    public void addFinalNode(INode finalNode) {
        throw new UnsupportedOperationException("The model is final, change the fragments instead");
    }

    @Override
    public List<INode> getFinalNodes() {
        List<INode> nodes = new LinkedList<>();
        for (ActivityCentricProcessModel fragment : fragments) {
            nodes.addAll(fragment.getFinalNodes());
        }
        return nodes;
    }

    @Override
    public <T extends INode> List<T> getFinalNodesOfClass(Class clazz) {
        List<T> nodes = new LinkedList<>();
        for (ActivityCentricProcessModel fragment : fragments) {
            nodes.addAll((List<T>)fragment.getFinalNodesOfClass(clazz));
        }
        return nodes;
    }
}
