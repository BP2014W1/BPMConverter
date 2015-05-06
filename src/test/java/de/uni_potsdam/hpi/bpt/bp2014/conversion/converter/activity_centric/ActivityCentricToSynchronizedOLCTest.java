package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.activity_centric;


import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.*;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import org.junit.Test;

public class ActivityCentricToSynchronizedOLCTest {
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
        acpm2solc.convert(acpm);
    }
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
        acpm2solc.convert(acpm);
    }


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
        acpm2solc.convert(acpm);
    }


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
        DataObjectState initProduct = new DataObjectState("product");
        DataObjectState sendInvoice = new DataObjectState("send");
        DataObjectState deliveredProduct = new DataObjectState("delivered");
        DataObject invoiceInit = new DataObject("Invoice", initInvoice);
        DataObject invoiceSend = new DataObject("Invoice", sendInvoice);
        DataObject productInit = new DataObject("Product", initProduct);
        DataObject productDelivered = new DataObject("Product", deliveredProduct);

        ActivityCentricToSynchronizedOLC acpm2solc = new ActivityCentricToSynchronizedOLC();
        acpm2solc.convert(acpm);
    }
}
