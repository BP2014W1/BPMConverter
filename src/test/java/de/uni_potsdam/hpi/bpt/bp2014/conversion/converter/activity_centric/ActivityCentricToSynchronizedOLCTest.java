package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.activity_centric;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.*;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize.SynchronizedObjectLifeCycle;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ActivityCentricToSynchronizedOLCTest {
    /**
     * Given: is a small activity centric Process model.
     *        The models consists of a start event, an
     *        activity and an end event. There is no Data
     *        Flow.
     * When:  You generate a synchronized Object Life Cycle
     *        based on this activity centric process model
     * Then:  an {@link de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize.SynchronizedObjectLifeCycle}
     *        with no {@link de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle}
     *        will be created.
     */
    @Test
    public void testSequence() {
        ActivityCentricProcessModel acpm = new ActivityCentricProcessModel();
        Event startEvent = new Event();
        startEvent.setType(Event.Type.START);
        Activity activity = new Activity();
        activity.setName("Do something");
        Event endEvent = new Event();
        endEvent.setType(Event.Type.END);
        ControlFlow cf1 = new ControlFlow(startEvent, activity);
        startEvent.addOutgoingEdge(cf1);
        activity.addIncomingEdge(cf1);
        ControlFlow cf2 = new ControlFlow(activity, endEvent);
        activity.addOutgoingEdge(cf2);
        endEvent.addIncomingEdge(cf2);
        acpm.setStartNode(startEvent);
        acpm.addNode(startEvent);
        acpm.addNode(endEvent);
        acpm.addNode(activity);
        acpm.addFinalNode(endEvent);
        ActivityCentricToSynchronizedOLC acpm2solc = new ActivityCentricToSynchronizedOLC();
        SynchronizedObjectLifeCycle solc = (SynchronizedObjectLifeCycle) acpm2solc.convert(acpm);
        assertTrue("There should be now Object Life Cycles.", solc.getOLCs().isEmpty());
        assertNull("There should be no startNode.", solc.getStartNode());
        assertTrue("There should be no final node.", solc.getFinalNodes().isEmpty());
        assertTrue("There should be no final node.", solc.getSynchronisationEdges().isEmpty());
    }



    /**
     * Given: A simple {@link ActivityCentricProcessModel}
     *        it consists of two activities which are exclusive.
     *        (Between a split and a merge)
     * When:  You generate a {@link SynchronizedObjectLifeCycle}
     * Then:  The {@link SynchronizedObjectLifeCycle} Object
     *        should contain no {@link ObjectLifeCycle}
     */
    @Test
    public void testXOR() {
        ActivityCentricProcessModel acpm = new ActivityCentricProcessModel();
        Event startEvent = new Event();
        startEvent.setType(Event.Type.START);
        Activity activity = new Activity();
        activity.setName("Do something");
        Activity activity2 = new Activity();
        activity2.setName("Do something else");
        Gateway split = new Gateway();
        split.setType(Gateway.Type.XOR);
        Gateway merge = new Gateway();
        merge.setType(Gateway.Type.XOR);
        Event endEvent = new Event();
        endEvent.setType(Event.Type.END);
        ControlFlow cf1 = new ControlFlow(startEvent, split);
        startEvent.addOutgoingEdge(cf1);
        split.addIncomingEdge(cf1);
        ControlFlow cf2 = new ControlFlow(split, activity);
        split.addOutgoingEdge(cf2);
        activity.addIncomingEdge(cf2);
        ControlFlow cf3 = new ControlFlow(split, activity2);
        split.addOutgoingEdge(cf3);
        activity2.addIncomingEdge(cf3);
        ControlFlow cf4 = new ControlFlow(activity2, merge);
        activity2.addOutgoingEdge(cf4);
        merge.addIncomingEdge(cf4);
        ControlFlow cf5 = new ControlFlow(activity, merge);
        activity.addOutgoingEdge(cf5);
        merge.addIncomingEdge(cf5);
        ControlFlow cf6 = new ControlFlow(merge, endEvent);
        merge.addOutgoingEdge(cf6);
        endEvent.addIncomingEdge(cf6);
        acpm.setStartNode(startEvent);
        acpm.addNode(startEvent);
        acpm.addNode(split);
        acpm.addNode(merge);
        acpm.addNode(endEvent);
        acpm.addNode(activity);
        acpm.addFinalNode(endEvent);
        ActivityCentricToSynchronizedOLC acpm2solc = new ActivityCentricToSynchronizedOLC();
        SynchronizedObjectLifeCycle solc = (SynchronizedObjectLifeCycle) acpm2solc.convert(acpm);
        assertTrue("There should be now Object Life Cycles.", solc.getOLCs().isEmpty());
        assertNull("There should be no startNode.", solc.getStartNode());
        assertTrue("There should be no final node.", solc.getFinalNodes().isEmpty());
        assertTrue("There should be no final node.", solc.getSynchronisationEdges().isEmpty());
    }


    /**
     * Given: Is a simple Activity Centric Process model
     *        with to concurrent activities (between fork
     *        and join) without dataflow.
     * When:  You convert this model into an {@link SynchronizedObjectLifeCycle}
     * Then:  1. No exception should be thrown
     *        2. The SynchronizedObjectLifeCycle object should contain no {@link ObjectLifeCycle}
     */
    @Test
    public void testAND() {
        ActivityCentricProcessModel acpm = new ActivityCentricProcessModel();
        Event startEvent = new Event();
        startEvent.setType(Event.Type.START);
        Activity activity = new Activity();
        activity.setName("Do something");
        Activity activity2 = new Activity();
        activity2.setName("Do another thing");
        Gateway fork = new Gateway();
        fork.setType(Gateway.Type.AND);
        Gateway join = new Gateway();
        join.setType(Gateway.Type.AND);
        Event endEvent = new Event();
        endEvent.setType(Event.Type.END);
        ControlFlow cf1 = new ControlFlow(startEvent, fork);
        startEvent.addOutgoingEdge(cf1);
        fork.addIncomingEdge(cf1);
        ControlFlow cf2 = new ControlFlow(fork, activity);
        fork.addOutgoingEdge(cf2);
        activity.addIncomingEdge(cf2);
        ControlFlow cf3 = new ControlFlow(fork, activity2);
        fork.addOutgoingEdge(cf3);
        activity2.addIncomingEdge(cf3);
        ControlFlow cf4 = new ControlFlow(activity2, join);
        activity2.addOutgoingEdge(cf4);
        join.addIncomingEdge(cf4);
        ControlFlow cf5 = new ControlFlow(activity, join);
        activity.addOutgoingEdge(cf5);
        join.addIncomingEdge(cf5);
        ControlFlow cf6 = new ControlFlow(join, endEvent);
        join.addOutgoingEdge(cf6);
        endEvent.addIncomingEdge(cf6);
        acpm.setStartNode(startEvent);
        acpm.addNode(startEvent);
        acpm.addNode(fork);
        acpm.addNode(join);
        acpm.addNode(endEvent);
        acpm.addNode(activity);
        acpm.addFinalNode(endEvent);
        ActivityCentricToSynchronizedOLC acpm2solc = new ActivityCentricToSynchronizedOLC();
        SynchronizedObjectLifeCycle solc = (SynchronizedObjectLifeCycle) acpm2solc.convert(acpm);
        assertTrue("There should be now Object Life Cycles.", solc.getOLCs().isEmpty());
        assertNull("There should be no startNode.", solc.getStartNode());
        assertTrue("There should be no final node.", solc.getFinalNodes().isEmpty());
        assertTrue("There should be no final node.", solc.getSynchronisationEdges().isEmpty());
    }


    /**
     * Given: Is an Activity centric process model.
     *        It consists of two concurrent Activities.
     *        With each one data input and one data output.
     *        There are two different DataObjects.
     * When:  This model is transformed into an {@link SynchronizedObjectLifeCycle}
     * Then:  The generated Model will consist of
     *        two Object life cycles with two States
     *        each.
     */
    @Test
    public void testDataObjects() {
        ActivityCentricProcessModel acpm = new ActivityCentricProcessModel();
        Event startEvent = new Event();
        startEvent.setType(Event.Type.START);
        Activity activity = new Activity();
        activity.setName("Deliver Product");
        Activity activity2 = new Activity();
        activity2.setName("Send Invoice");
        Gateway fork = new Gateway();
        fork.setType(Gateway.Type.AND);
        Gateway join = new Gateway();
        join.setType(Gateway.Type.AND);
        Event endEvent = new Event();
        endEvent.setType(Event.Type.END);
        ControlFlow cf1 = new ControlFlow(startEvent, fork);
        startEvent.addOutgoingEdge(cf1);
        fork.addIncomingEdge(cf1);
        ControlFlow cf2 = new ControlFlow(fork, activity);
        fork.addOutgoingEdge(cf2);
        activity.addIncomingEdge(cf2);
        ControlFlow cf3 = new ControlFlow(fork, activity2);
        fork.addOutgoingEdge(cf3);
        activity2.addIncomingEdge(cf3);
        ControlFlow cf4 = new ControlFlow(activity2, join);
        activity2.addOutgoingEdge(cf4);
        join.addIncomingEdge(cf4);
        ControlFlow cf5 = new ControlFlow(activity, join);
        activity.addOutgoingEdge(cf5);
        join.addIncomingEdge(cf5);
        ControlFlow cf6 = new ControlFlow(join, endEvent);
        join.addOutgoingEdge(cf6);
        endEvent.addIncomingEdge(cf6);
        acpm.setStartNode(startEvent);
        acpm.addNode(startEvent);
        acpm.addNode(fork);
        acpm.addNode(join);
        acpm.addNode(endEvent);
        acpm.addNode(activity);
        acpm.addFinalNode(endEvent);

        DataObjectState initInvoice = new DataObjectState("init");
        DataObjectState initProduct = new DataObjectState("init");
        DataObjectState sendInvoice = new DataObjectState("send");
        DataObjectState deliveredProduct = new DataObjectState("delivered");
        DataObject invoiceInit = new DataObject("Invoice", initInvoice);
        DataObject invoiceSend = new DataObject("Invoice", sendInvoice);
        DataObject productInit = new DataObject("Product", initProduct);
        DataObject productDelivered = new DataObject("Product", deliveredProduct);
        DataFlow df = new DataFlow(invoiceInit, activity);
        activity.addIncomingEdge(df);
        invoiceInit.addOutgoingEdge(df);
        df = new DataFlow(activity, invoiceSend);
        invoiceSend.addIncomingEdge(df);
        activity.addOutgoingEdge(df);
        df = new DataFlow(productInit, activity2);
        productInit.addOutgoingEdge(df);
        activity2.addIncomingEdge(df);
        df = new DataFlow(activity2, productDelivered);
        productDelivered.addIncomingEdge(df);
        activity2.addOutgoingEdge(df);

        acpm.addNode(invoiceInit);
        acpm.addNode(invoiceSend);
        acpm.addNode(productInit);
        acpm.addNode(productDelivered);

        ActivityCentricToSynchronizedOLC acpm2solc = new ActivityCentricToSynchronizedOLC();
        SynchronizedObjectLifeCycle solc = (SynchronizedObjectLifeCycle) acpm2solc.convert(acpm);
        assertEquals("There should be 2 Object Life Cycles.", 2, solc.getOLCs().size());
        assertNull("There should be no startNode.", solc.getStartNode());
        assertFalse("There should be more than one final node.", solc.getFinalNodes().isEmpty());
        assertEquals("There should be four synchronization Edge.", 4, solc.getSynchronisationEdges().size());
        assertEquals("The sum of the Nodes in two Models should be 6", 6, solc.getNodes().size());
        assertEquals("The 1st synchronized OLC should have 3 nodes", 3, solc.getOLCs().get(0).getNodes().size());
        assertEquals("The 2nd synchronized OLC should have 3 nodes", 3, solc.getOLCs().get(1).getNodes().size());
        assertTrue("There should be one \"Invoice\" and one \"Product\" olc",
                (solc.getOLCs().get(0).getLabel().equals("Invoice") && solc.getOLCs().get(1).getLabel().equals("Product")) ||
                        (solc.getOLCs().get(1).getLabel().equals("Invoice") && solc.getOLCs().get(0).getLabel().equals("Product")));
        ObjectLifeCycle invoiceOLC;
        ObjectLifeCycle productOLC;
        if (solc.getOLCs().get(0).getLabel().equals("Invoice")) {
            invoiceOLC = solc.getOLCs().get(0);
            productOLC = solc.getOLCs().get(1);
        } else {
            invoiceOLC = solc.getOLCs().get(1);
            productOLC = solc.getOLCs().get(0);
        }

        // Check Object Life Cycle of Invoice
        DataObjectState currentNode = (DataObjectState)invoiceOLC.getStartNode();
        assertEquals("The 1st node should be the pseudo node \"i\"", "i", currentNode.getName());
        assertEquals("the 1st node should have exactly one outgoing edge", 1, currentNode.getOutgoingEdges().size());
        assertEquals("The 1st transition should be labeled with \"t\" this indicates a silent transition", "t", ((StateTransition)currentNode.getOutgoingEdges().get(0)).getLabel());
        currentNode = (DataObjectState) currentNode.getOutgoingEdges().iterator().next().getTarget();
        assertEquals("The 2nd node should be the state \"init\"", "init", currentNode.getName());
        assertEquals("the 2nd node should have exactly one outgoing edge", 1, currentNode.getOutgoingEdges().size());
        assertEquals("The 2nd transition should be labeled with \"send\"", "Deliver Product", ((StateTransition)currentNode.getOutgoingEdges().get(0)).getLabel());
        currentNode = (DataObjectState) currentNode.getOutgoingEdges().iterator().next().getTarget();
        assertEquals("The 3rd node should be the state \"send\"", "send", currentNode.getName());
        assertTrue("the 3rd node should have no outgoing edge", currentNode.getOutgoingEdges().isEmpty());

        // Check Object Life Cycle of Invoice
        currentNode = (DataObjectState)productOLC.getStartNode();
        assertEquals("The 1st node should be the pseudo node \"i\"", "i", currentNode.getName());
        assertEquals("the 1st node should have exactly one outgoing edge", 1, currentNode.getOutgoingEdges().size());
        assertEquals("The 1st transition should be labeled with \"t\" this indicates a silent transition", "t", ((StateTransition)currentNode.getOutgoingEdges().get(0)).getLabel());
        currentNode = (DataObjectState) currentNode.getOutgoingEdges().iterator().next().getTarget();
        assertEquals("The 2nd node should be the state \"init\"", "init", currentNode.getName());
        assertEquals("the 2nd node should have exactly one outgoing edge", 1, currentNode.getOutgoingEdges().size());
        assertEquals("The 2nd transition should be labeled with \"deliver\"", "Send Invoice", ((StateTransition) currentNode.getOutgoingEdges().get(0)).getLabel());
        currentNode = (DataObjectState) currentNode.getOutgoingEdges().iterator().next().getTarget();
        assertEquals("The 3rd node should be the state \"delivered\"", "delivered", currentNode.getName());
        assertTrue("the 3rd node should have no outgoing edge", currentNode.getOutgoingEdges().isEmpty());
    }


    /**
     * Given: A {@link ActivityCentricProcessModel} with
     *        * One Activity (Do something)
     *        * with two in- and output DataObjects
     *        * and a start and end event.
     * When:  Transform this model into an {@link SynchronizedObjectLifeCycle}
     * Then:  There should be a synchronized OLC with
     *        * two OLCs
     *        * Synchronized via two edges (t and do something)
     */
    @Test
    public void testSynchronizedEdges() {
        ActivityCentricProcessModel acpm = new ActivityCentricProcessModel();
        Event startEvent = new Event();
        startEvent.setType(Event.Type.START);
        Activity activity = new Activity();
        activity.setName("Do something");
        Event endEvent = new Event();
        endEvent.setType(Event.Type.END);
        ControlFlow cf1 = new ControlFlow(startEvent, activity);
        startEvent.addOutgoingEdge(cf1);
        activity.addIncomingEdge(cf1);
        ControlFlow cf2 = new ControlFlow(activity, endEvent);
        activity.addOutgoingEdge(cf2);
        endEvent.addIncomingEdge(cf2);
        acpm.setStartNode(startEvent);
        acpm.addNode(startEvent);
        acpm.addNode(endEvent);
        acpm.addNode(activity);
        acpm.addFinalNode(endEvent);

        DataObjectState initInvoice = new DataObjectState("init");
        DataObjectState initProduct = new DataObjectState("init");
        DataObjectState sendInvoice = new DataObjectState("send");
        DataObjectState deliveredProduct = new DataObjectState("delivered");
        DataObject invoiceInit = new DataObject("Invoice", initInvoice);
        DataObject invoiceSend = new DataObject("Invoice", sendInvoice);
        DataObject productInit = new DataObject("Product", initProduct);
        DataObject productDelivered = new DataObject("Product", deliveredProduct);
        DataFlow df = new DataFlow(invoiceInit, activity);
        activity.addIncomingEdge(df);
        invoiceInit.addOutgoingEdge(df);
        df = new DataFlow(activity, invoiceSend);
        invoiceSend.addIncomingEdge(df);
        activity.addOutgoingEdge(df);
        df = new DataFlow(productInit, activity);
        productInit.addOutgoingEdge(df);
        activity.addIncomingEdge(df);
        df = new DataFlow(activity, productDelivered);
        productDelivered.addIncomingEdge(df);
        activity.addOutgoingEdge(df);

        acpm.addNode(invoiceInit);
        acpm.addNode(invoiceSend);
        acpm.addNode(productInit);
        acpm.addNode(productDelivered);

        ActivityCentricToSynchronizedOLC acpm2solc = new ActivityCentricToSynchronizedOLC();
        SynchronizedObjectLifeCycle solc = (SynchronizedObjectLifeCycle) acpm2solc.convert(acpm);
        assertEquals("There should be 2 Object Life Cycles.", 2, solc.getOLCs().size());
        assertNull("There should be no startNode.", solc.getStartNode());
        assertFalse("There should be more than one final node.", solc.getFinalNodes().isEmpty());
        assertEquals("There should be four synchronization Edge.", 4, solc.getSynchronisationEdges().size());
        assertEquals("The sum of the Nodes in two Models should be 6", 6, solc.getNodes().size());
        assertEquals("The 1st synchronized OLC should have 3 nodes", 3, solc.getOLCs().get(0).getNodes().size());
        assertEquals("The 2nd synchronized OLC should have 3 nodes", 3, solc.getOLCs().get(1).getNodes().size());
        assertTrue("There should be one \"Invoice\" and one \"Product\" olc",
                (solc.getOLCs().get(0).getLabel().equals("Invoice") && solc.getOLCs().get(1).getLabel().equals("Product")) ||
                        (solc.getOLCs().get(1).getLabel().equals("Invoice") && solc.getOLCs().get(0).getLabel().equals("Product")));
        ObjectLifeCycle invoiceOLC;
        ObjectLifeCycle productOLC;
        if (solc.getOLCs().get(0).getLabel().equals("Invoice")) {
            invoiceOLC = solc.getOLCs().get(0);
            productOLC = solc.getOLCs().get(1);
        } else {
            invoiceOLC = solc.getOLCs().get(1);
            productOLC = solc.getOLCs().get(0);
        }

        // Check Object Life Cycle of Invoice
        DataObjectState currentNode = (DataObjectState)invoiceOLC.getStartNode();
        assertEquals("The 1st node should be the pseudo node \"i\"", "i", currentNode.getName());
        assertEquals("the 1st node should have exactly one outgoing edge", 1, currentNode.getOutgoingEdges().size());
        assertEquals("The 1st transition should be labeled with \"t\" this indicates a silent transition", "t", ((StateTransition)currentNode.getOutgoingEdges().get(0)).getLabel());
        currentNode = (DataObjectState) currentNode.getOutgoingEdges().iterator().next().getTarget();
        assertEquals("The 2nd node should be the state \"init\"", "init", currentNode.getName());
        assertEquals("the 2nd node should have exactly one outgoing edge", 1, currentNode.getOutgoingEdges().size());
        assertEquals("The 2nd transition should be labeled with \"Do something\"", "Do something", ((StateTransition)currentNode.getOutgoingEdges().get(0)).getLabel());
        currentNode = (DataObjectState) currentNode.getOutgoingEdges().iterator().next().getTarget();
        assertEquals("The 3rd node should be the state \"send\"", "send", currentNode.getName());
        assertTrue("the 3rd node should have no outgoing edge", currentNode.getOutgoingEdges().isEmpty());

        // Check Object Life Cycle of Invoice
        currentNode = (DataObjectState)productOLC.getStartNode();
        assertEquals("The 1st node should be the pseudo node \"i\"", "i", currentNode.getName());
        assertEquals("the 1st node should have exactly one outgoing edge", 1, currentNode.getOutgoingEdges().size());
        assertEquals("The 1st transition should be labeled with \"t\" this indicates a silent transition", "t", ((StateTransition)currentNode.getOutgoingEdges().get(0)).getLabel());
        currentNode = (DataObjectState) currentNode.getOutgoingEdges().iterator().next().getTarget();
        assertEquals("The 2nd node should be the state \"init\"", "init", currentNode.getName());
        assertEquals("the 2nd node should have exactly one outgoing edge", 1, currentNode.getOutgoingEdges().size());
        assertEquals("The 2nd transition should be labeled with \"Do something\"", "Do something", ((StateTransition) currentNode.getOutgoingEdges().get(0)).getLabel());
        currentNode = (DataObjectState) currentNode.getOutgoingEdges().iterator().next().getTarget();
        assertEquals("The 3rd node should be the state \"delivered\"", "delivered", currentNode.getName());
        assertTrue("the 3rd node should have no outgoing edge", currentNode.getOutgoingEdges().isEmpty());

        for (Map.Entry<StateTransition, List<StateTransition>> stateTransitionListEntry : solc.getSynchronisationEdges().entrySet()) {
            assertEquals("Their should be only one synchronization Edge per Edge", 1, stateTransitionListEntry.getValue().size());
            for (StateTransition transition : stateTransitionListEntry.getValue()) {
                assertEquals("The Transitions are not synchronized correctly", stateTransitionListEntry.getKey().getLabel(), transition.getLabel());
            }
        }
    }
}
