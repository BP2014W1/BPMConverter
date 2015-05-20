package de.uni_potsdam.hpi.bpt.bp2014.conversion;

/**
 * This is an interface for edges inside a process model.
 * An edge should have a source and target.
 * To access these elements there should be some kind of
 * getters and setters.
 */
public interface IEdge {
    public INode getSource();

    public void setSource(INode source);

    public INode getTarget();

    public void setTarget(INode target);
}
