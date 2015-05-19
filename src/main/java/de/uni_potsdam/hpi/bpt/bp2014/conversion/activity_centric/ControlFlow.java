package de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

/**
 * This class represents a control flow edge.
 * It connects to nodes which can be connected by control flow.
 * Supported types are {@link Activity}, {@link Gateway}, {@link Event}.
 */
public class ControlFlow implements IEdge {
    public INode source;
    public INode target;

    /**
     * Creates a new instance of the control flow
     * with specified source and target.
     * Be aware that both nodes must not be null and
     * should meet the constraints, such that they are
     * not null and instances of one of the supported types.
     *
     * @param source The source of the new Edge.
     * @param target The target of the new Edge.
     */
    public ControlFlow(INode source, INode target) {
        setSource(source);
        setTarget(target);
    }

    @Override
    public INode getSource() {
        return source;
    }

    /**
     * Set the source of the edge.
     *
     * @param source The new source.
     *               Pre: The node must not be null.
     *               the node must be of a supported type.
     */
    @Override
    public void setSource(INode source) {
        assert null != source :
                "The source of a control flow must not be null";
        assert source instanceof Activity ||
                source instanceof Gateway ||
                source instanceof Event :
                "The source must be either Activity, Gateway or Event";
        this.source = source;
    }

    @Override
    public INode getTarget() {
        return target;
    }


    /**
     * Set the target of the edge.
     *
     * @param target The new source.
     *               Pre: The node must not be null.
     *               the node must be of a supported type.
     */
    @Override
    public void setTarget(INode target) {
        assert null != target :
                "The target of a control flow must not be null";

        assert target instanceof Activity ||
                target instanceof Gateway ||
                target instanceof Event :
                "The target must be either Activity, Gateway or Event";
        this.target = target;
    }
}
