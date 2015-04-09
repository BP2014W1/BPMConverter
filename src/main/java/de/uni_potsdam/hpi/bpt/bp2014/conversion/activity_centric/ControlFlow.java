package de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

public class ControlFlow implements IEdge {
    public INode source;
    public INode target;

    public ControlFlow(INode source, INode target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public INode getSource() {
        return source;
    }

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
