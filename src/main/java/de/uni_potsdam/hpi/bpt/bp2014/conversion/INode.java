package de.uni_potsdam.hpi.bpt.bp2014.conversion;

import java.util.List;

public interface INode {

    public void addIncomingEdge(IEdge edge);

    public void addOutgoingEdge(IEdge edge);

    public List<IEdge> getIncomingEdges();

    public List<IEdge> getOutgoingEdges();

    public <T extends IEdge> List<T> getOutgoingEdgesOfType(Class T);

    public <T extends IEdge> List<T> getIncomingEdgesOfType(Class T);
}
