package de.uni_potsdam.hpi.bpt.bp2014.conversion;

import java.util.List;

/**
 * An Interface for Process Models.
 * This interface defines an api to access all the elements of a process model.
 * A Model as one start node and a set of final nodes and an unspecified number
 * of nodes in between.
 * These models should be accessible from the outside.
 */
public interface IModel {
    /**
     * @return Should return all nodes of the process model including start and final ones.
     */
    public List<INode> getNodes();

    /**
     * Adds a new node to the collection of all nodes.
     * Start and final nodes should be added as well.
     * @param newNode The new node to be added.
     */
    public void addNode(INode newNode);

    /**
     * @param T The Type of the nodes to be returned.
     * @param <T> The Generics of the list to be returned.
     * @return Returns all nodes of a certain type. Final nodes and the start node may be included.
     */
    public <T extends INode> List<T> getNodesOfClass(Class T);

    /**
     * @return Returns the start node of the process model or null if it has not been set.
     */
    public INode getStartNode();

    /**
     * Sets a new start node. It is up to the concrete implementation to specify the behavior in more detail.
     * Some might want to prohibit overwriting an existing start node or nodes of a specific type.
     * Start nodes should be added using {@link #addNode(INode)}
     * @param startNode The node to be added as start node.
     */
    public void setStartNode(INode startNode);

    /**
     * Adds a new final node to the process model.
     * Restrictions should be implemented if necessary.
     * Final nodes must be added using {@link #addNode(INode)} in addition.
     * @param finalNode The final node to be added.
     */
    public void addFinalNode(INode finalNode);

    /**
     * @return Returns a list of all final nodes. If no final node has been set an empty list should be returned.
     */
    public List<INode> getFinalNodes();

    /**
     * @param T The Type of the nodes. Only final nodes of this type should be returned.
     * @param <T> The type of the generic response.
     * @return Returns a List of type <T> for all final nodes of the specified Type T.
     */
    public <T extends INode> List<T> getFinalNodesOfClass(Class T);
}
