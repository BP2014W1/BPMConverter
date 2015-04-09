package de.uni_potsdam.hpi.bpt.bp2014.conversion;

public interface IEdge {
    public INode getSource();

    public void setSource(INode source);

    public INode getTarget();

    public void setTarget(INode target);
}
