package de.uni_potsdam.hpi.bpt.bp2014.conversion;

import java.util.List;

/**
 * A node can have successors and predecessors.
 * Because nodes are part of a graph these other notes can be found following
 * the edges.
 * Such as every node should have methods to access the incoming and outgoing edges.
 * In addition there should be methods to make a selection and only return edges
 * of a specific type (e.g. {@link de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition}.
 *
 * This interface provides methods add and get to provide these possibilities.
 *
 * To prevent unexpected state changes it is recommended to return shallow copies of the actual
 * lists. (not the edges)
 */
public interface INode {

    public void addIncomingEdge(IEdge edge);

    public void addOutgoingEdge(IEdge edge);

    public List<IEdge> getIncomingEdges();

    public List<IEdge> getOutgoingEdges();

    public <T extends IEdge> List<T> getOutgoingEdgesOfType(Class T);

    public <T extends IEdge> List<T> getIncomingEdgesOfType(Class T);
}
