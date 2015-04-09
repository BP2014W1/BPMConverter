package de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;

public class DataFlow implements IEdge {
    private INode source;
    private INode target;

    public DataFlow(DataObject source, Activity target) {
        this.source = source;
        this.target = target;
    }

    public DataFlow(Activity source, DataObject target) {
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
                "The source of an edge should not be null";
        assert source instanceof Activity ||
                source instanceof DataObject :
                "The source of an DataFlow must be either " +
                        "an Activity or and DataObject";
        this.source = source;
    }

    @Override
    public INode getTarget() {
        return target;
    }

    @Override
    public void setTarget(INode target) {
        assert null != target :
                "The target of an edge should not be null";
        assert target instanceof Activity ||
                target instanceof DataObject :
                "The target of an DataFlow must be either " +
                        "an Activity or and DataObject";
        this.target = target;
    }
}
