package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.*;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize.SynchronizedObjectLifeCycle;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SynchronizedOLCToActivityCentricTest {

    private SynchronizedObjectLifeCycle sequence;
    private SynchronizedObjectLifeCycle sequenceOfTwo;
    private SynchronizedObjectLifeCycle forkAndMerge;
    private SynchronizedObjectLifeCycle splitAndJoin;

    @Before
    public void setUpSplitAndJoin() {
        ObjectLifeCycle olc = new ObjectLifeCycle();
        olc.setLabel("Bill");
        DataObjectState node1 = new DataObjectState("init");
        DataObjectState node2 = new DataObjectState("pending");
        StateTransition transition = new StateTransition(node1,node2,"Create bill");
        node1.addOutgoingEdge(transition);
        node2.addIncomingEdge(transition);
        olc.setStartNode(node1);
        olc.addNode(node1);
        olc.addNode(node2);

        DataObjectState node3 = new DataObjectState("initialized");
        transition = new StateTransition(node1,node3,"Initialize bill");
        node1.addOutgoingEdge(transition);
        node3.addIncomingEdge(transition);
        olc.addNode(node3);
        node1 = new DataObjectState("paid");
        transition = new StateTransition(node2, node1, "Pay bill");
        node2.addOutgoingEdge(transition);
        transition = new StateTransition(node3, node1, "Pay bill");
        node1.addIncomingEdge(transition);
        node3.addOutgoingEdge(transition);
        olc.addNode(node1);
        olc.addFinalNode(node1);
        splitAndJoin = new SynchronizedObjectLifeCycle();
        splitAndJoin.getOLCs().add(olc);
        olc = new ObjectLifeCycle();
        olc.setLabel("Customer");
        node1 = new DataObjectState("init");
        node2 = new DataObjectState("created");
        transition = new StateTransition(node1,node2,"Initialize bill");
        node1.addOutgoingEdge(transition);
        node2.addIncomingEdge(transition);
        olc.setStartNode(node1);
        olc.addNode(node1);
        olc.addNode(node2);
        olc.addFinalNode(node2);
        splitAndJoin.getOLCs().add(olc);
    }

    @Before
    public void setUpForkAndMerge() {
        ObjectLifeCycle olc = new ObjectLifeCycle();
        olc.setLabel("Bill");
        DataObjectState node1 = new DataObjectState("init");
        DataObjectState node2 = new DataObjectState("pending");
        StateTransition transition = new StateTransition(node1,node2,"Create bill");
        node1.addOutgoingEdge(transition);
        node2.addIncomingEdge(transition);
        olc.setStartNode(node1);
        olc.addNode(node1);
        olc.addNode(node2);
        node1 = new DataObjectState("paid");
        transition = new StateTransition(node2, node1, "Pay bill");
        node1.addIncomingEdge(transition);
        node2.addOutgoingEdge(transition);
        olc.addNode(node1);
        olc.addFinalNode(node1);
        forkAndMerge = new SynchronizedObjectLifeCycle();
        forkAndMerge.getOLCs().add(olc);
        olc = new ObjectLifeCycle();
        olc.setLabel("Customer");
        node1 = new DataObjectState("init");
        node2 = new DataObjectState("created");
        transition = new StateTransition(node1,node2,"Create Customer");
        node1.addOutgoingEdge(transition);
        node2.addIncomingEdge(transition);
        olc.setStartNode(node1);
        olc.addNode(node1);
        olc.addNode(node2);
        node1 = new DataObjectState("final");
        transition = new StateTransition(node2, node1, "Pay bill");
        node1.addIncomingEdge(transition);
        node2.addOutgoingEdge(transition);
        olc.addNode(node1);
        olc.addFinalNode(node1);
        forkAndMerge.getOLCs().add(olc);
    }

    @Before
    public void setUpSequenceOfTwo() {
        ObjectLifeCycle olc = new ObjectLifeCycle();
        olc.setLabel("Bill");
        DataObjectState node1 = new DataObjectState("init");
        DataObjectState node2 = new DataObjectState("pending");
        StateTransition transition = new StateTransition(node1,node2,"Create bill");
        node1.addOutgoingEdge(transition);
        node2.addIncomingEdge(transition);
        olc.setStartNode(node1);
        olc.addNode(node1);
        olc.addNode(node2);
        node1 = new DataObjectState("paid");
        transition = new StateTransition(node2, node1, "Pay bill");
        node1.addIncomingEdge(transition);
        node2.addOutgoingEdge(transition);
        olc.addNode(node1);

        olc.addFinalNode(node1);
        sequenceOfTwo = new SynchronizedObjectLifeCycle();
        sequenceOfTwo.getOLCs().add(olc);
        olc = new ObjectLifeCycle();
        olc.setLabel("Customer");
        node1 = new DataObjectState("init");
        node2 = new DataObjectState("created");
        transition = new StateTransition(node1,node2,"Create bill");
        node1.addOutgoingEdge(transition);
        node2.addIncomingEdge(transition);
        olc.setStartNode(node1);
        olc.addNode(node1);
        olc.addNode(node2);
        node1 = new DataObjectState("final");
        transition = new StateTransition(node2, node1, "Pay bill");
        node1.addIncomingEdge(transition);
        node2.addOutgoingEdge(transition);
        olc.addNode(node1);
        olc.addFinalNode(node1);
        sequenceOfTwo.getOLCs().add(olc);
    }


    @Before
    public void setUpSequenceBill() {
        ObjectLifeCycle olc = new ObjectLifeCycle();
        olc.setLabel("Bill");
        DataObjectState node1 = new DataObjectState("init");
        DataObjectState node2 = new DataObjectState("pending");
        StateTransition transition = new StateTransition(node1,node2,"Create bill");
        node1.addOutgoingEdge(transition);
        node2.addIncomingEdge(transition);
        olc.setStartNode(node1);
        olc.addNode(node1);
        olc.addNode(node2);
        node1 = new DataObjectState("paid");
        transition = new StateTransition(node2, node1, "Pay bill");
        node1.addIncomingEdge(transition);
        node2.addOutgoingEdge(transition);
        olc.addNode(node1);
        olc.addFinalNode(node1);
        sequence = new SynchronizedObjectLifeCycle();
        sequence.getOLCs().add(olc);
    }

    /**
     * Given: An synchronized object life cycle containing 0 object life cycle.
     * When:  Convert the synchronized OLC to an Activity Centric Model
     * Then:  a new Model consisting only of a Start and an End Event, connected by
     *        one Control Flow edge.
     *        FIXME
     */
    @Test
    public void testConvert() {
        SynchronizedObjectLifeCycle synchronizedObjectLifeCycle = new SynchronizedObjectLifeCycle();
        SynchronizedOLCToActivityCentric converter = new SynchronizedOLCToActivityCentric();
        ActivityCentricProcessModel activityCentric = converter.convert(synchronizedObjectLifeCycle);
        INode start = activityCentric.getStartNode();
        assertFalse("The model has no final node", activityCentric.getFinalStates().isEmpty());
        assertEquals("There is more than one final node", 1, activityCentric.getFinalStates().size());
        INode end = activityCentric.getFinalStates().get(0);
        assertTrue("The final node is not an Event", end instanceof Event);
        assertEquals("The final node is not of type Event", Event.Type.END, ((Event)end).getType());
        assertNotEquals("The start node of the model is null", null, start);
        assertTrue("The tart node is not of type Event", start instanceof Event);
        assertEquals("The Start Event is not of type 'START'", Event.Type.START, ((Event) start).getType());
        assertEquals("The Start Event has more than one successor", 1, start.getOutgoingEdges().size());
        assertEquals("The successor of the Start Event is not the End Event", end, start.getOutgoingEdges().get(0).getTarget());
        assertEquals("The End Event has more than one predecessor", 1, end.getIncomingEdges().size());
        assertEquals("The predecessor of the final Event is not the Start Event", start, end.getIncomingEdges().get(0).getSource());
        assertEquals("There are more nodes than the Start and End Event", 2, activityCentric.getNodes().size());
    }

    /**
     * Given: A Synchronized Object Life Cycle, which consists of one Object Life Cycle.
     *        This Object Life Cycles specifies a sequence of State transitions.
     * When:  You convert the given synchronized OLC into an Activity Centric Process Model
     * Then:  The Process Model should describe a sequence of Activities, with one data input
     *        and one data output object each.
     */
    @Test
    public void testSequence() {
        SynchronizedOLCToActivityCentric converter = new SynchronizedOLCToActivityCentric();
        ActivityCentricProcessModel activityCentric = converter.convert(sequence);
        INode start = activityCentric.getStartNode();
        Activity activity;
        assertTrue("The start node is not of type Event",
                start instanceof Event);
        assertEquals("The start Event is not of type 'START'",
                Event.Type.START, ((Event) start).getType());
        assertTrue("The Start Event had incoming Data Flow",
                start.getIncomingEdgesOfType(DataFlow.class).isEmpty());
        assertTrue("The Start Event had outgoing Data Flow",
                start.getOutgoingEdgesOfType(DataFlow.class).isEmpty());
        assertEquals("The Start Event had more or less than one outgoing edge",
                1, start.getOutgoingEdgesOfType(ControlFlow.class).size());
        assertTrue("The successor of the start event is not an Activity",
                start.getOutgoingEdgesOfType(ControlFlow.class).get(0).getTarget()
                        instanceof Activity);
        activity = (Activity)start.getOutgoingEdgesOfType(ControlFlow.class).get(0).getTarget();
        assertEquals("The first activity is not \"Create bill\"",
                "Create bill", activity.getName());
        DataObject dataObject;
        assertEquals("\"Create bill\" has more or less than one data input",
                1, activity.getIncomingEdgesOfType(DataFlow.class).size());
        assertEquals("\"Create bill\" has more or less than one data output",
                1, activity.getOutgoingEdgesOfType(DataFlow.class).size());
        dataObject = (DataObject)activity.getIncomingEdgesOfType(DataFlow.class).get(0).getSource();
        assertEquals("\"Create bill\" has a wrong data input object",
                "Bill", dataObject.getName());
        assertEquals("\"Create bill\" has a data input with the wrong state",
                "init", dataObject.getState().getName());
        dataObject = (DataObject)activity.getOutgoingEdgesOfType(DataFlow.class).get(0).getTarget();
        assertEquals("\"Create bill\" has a wrong data output",
                "Bill", dataObject.getName());
        assertEquals("\"Create bill\" has a wrong data output object state",
                "pending", dataObject.getState().getName());
        assertTrue("The successor of \"Create bill\" is not an Activity",
                activity.getOutgoingEdgesOfType(ControlFlow.class).get(0).getTarget()
                        instanceof Activity);
        activity = (Activity)activity.getOutgoingEdgesOfType(ControlFlow.class).get(0).getTarget();
        assertEquals("The 2nd Activity is not \"Pay bill\"",
                "Pay bill", activity.getName());
        assertEquals("The Activity \"Pay bill\" has more than one incoming data flow",
                1, activity.getIncomingEdgesOfType(DataFlow.class).size());
        assertEquals("the data input of Activity \"Pay bill\" is not bill",
                "Bill", ((DataObject) activity.getIncomingEdgesOfType(DataFlow.class).get(0).getSource()).getName());
        assertEquals("The data input of Activity \"Pay bill\" is not in the state \"pending\"",
                "pending", ((DataObject) activity.getIncomingEdgesOfType(DataFlow.class).get(0).getSource()).getState().getName());
        assertEquals("The Activity \"Pay bill\" has more than one outgoing data flow",
                1, activity.getOutgoingEdgesOfType(DataFlow.class).size());
        assertEquals("the data output of Activity \"Pay bill\" is not bill",
                "Bill", ((DataObject) activity.getOutgoingEdgesOfType(DataFlow.class).get(0).getTarget()).getName());
        assertEquals("The data output of Activity \"Pay bill\" is not in the state \"paid\"",
                "paid", ((DataObject) activity.getOutgoingEdgesOfType(DataFlow.class).get(0).getTarget()).getState().getName());
    }

    /**
     * Given:  Is an Synchronized Object Life Cycle containing two OLCs. Each OLC consists of
     *         three different states. The states are ordered in a sequence and the two transitions
     *         are the same in the two OLCs.
     * When:   You convert this synchronize Object Life Cycle to an Activity Centric Process Model.
     * Then:   There should be no exceptions. The created Activity Centric Process Model should consist
     *         of two sequential Activities with each 2 I/O-Objects.
     *         The naming of the Activities should be the same as the naming of the Edges.
     */
    @Test
    public void testSequenceOfTwo() {
        SynchronizedOLCToActivityCentric converter = new SynchronizedOLCToActivityCentric();
        ActivityCentricProcessModel activityCentric = converter.convert(sequenceOfTwo);
        System.out.println("");
    }

    @Test
    public void testForkAndMerge() {
        SynchronizedOLCToActivityCentric converter = new SynchronizedOLCToActivityCentric();
        ActivityCentricProcessModel activityCentric = converter.convert(forkAndMerge);
        System.out.println("");
    }

    @Test
    public void testSplitAndJoin() {
        SynchronizedOLCToActivityCentric converter = new SynchronizedOLCToActivityCentric();
        ActivityCentricProcessModel activityCentric = converter.convert(splitAndJoin);
        System.out.println("");
    }
}
