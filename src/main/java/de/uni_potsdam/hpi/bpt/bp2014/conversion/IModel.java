package de.uni_potsdam.hpi.bpt.bp2014.conversion;

import java.util.List;

public interface IModel {
    public List<INode> getNodes();

    public void addNode(INode newNode);

    public <T extends INode> List<T> getNodesOfClass(Class T);

    public INode getStartNode();

    public void setStartNode(INode startNode);

    public void addFinalNode(INode finalNode);

    public List<INode> getFinalNodes();

    public <T extends INode> List<T> getFinalNodesOfClass(Class T);
}
