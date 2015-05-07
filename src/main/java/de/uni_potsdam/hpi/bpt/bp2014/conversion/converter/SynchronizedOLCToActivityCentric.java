package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IConverter;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.IModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.ActivityCentricProcessModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.ControlFlow;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.Event;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.Gateway;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.olc.ActivityBuilder;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.olc.OLCConversionFlyweight;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize.SynchronizedObjectLifeCycle;

import java.util.*;

public class SynchronizedOLCToActivityCentric implements IConverter {
    private SynchronizedObjectLifeCycle synchronizedObjectLifeCycle;
    private Collection<ActivityBuilder> nodesToBeChecked;

    /**
     * This methods creates a new activity centric process model.
     * It uses a synchronized object life cycle as an input, which
     * must be defined in {@link #synchronizedObjectLifeCycle}.
     * Other attributes of this class will be initialized and changed
     * during that process.
     * You may not manipulate this process.
     * The converion is an implementation of the algorithm described
     * in {@see http://bpt.hpi.uni-potsdam.de/pub/Public/AndreasMeyer/Technical_Report_Activity-centric_and_Artifact-centric_Process_Model_Roundtrip.pdf}.
     *
     * @return The generated ActivityCentricProcessModel.
     */
    public ActivityCentricProcessModel convert() {
        try {
            return olcToACP(synchronizedObjectLifeCycle);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        };
        return null;
    }


    public ActivityCentricProcessModel convert(SynchronizedObjectLifeCycle sOLC) {
        synchronizedObjectLifeCycle = sOLC;
        return convert();
    }

    // TODO: Handle start state is final state
    public ActivityCentricProcessModel olcToACP(SynchronizedObjectLifeCycle sOLC)
            throws InstantiationException, IllegalAccessException {
        OLCConversionFlyweight<ActivityCentricProcessModel> flyweight =
                new OLCConversionFlyweight<>(sOLC,
                        ActivityCentricProcessModel.class);
        Event startEvent = new Event();
        startEvent.type = Event.Type.START;
        initNodesToBeChecked(flyweight);
        Collection<ActivityBuilder> nodeBuilderNodesChecked = new HashSet<>();
        flyweight.getModelUnderConstruction().addNode(startEvent);
        flyweight.getModelUnderConstruction().setStartNode(startEvent);
        boolean exclusive = false;
        if (nodesToBeChecked.size() == 1) {
            nodesToBeChecked.iterator().next().addPredecessor(startEvent);
        } else if (nodesToBeChecked.size() < 1) {
            Event endEvent = new Event();
            endEvent.setType(Event.Type.END);
            ControlFlow cf = new ControlFlow(startEvent, endEvent);
            startEvent.addOutgoingEdge(cf);
            endEvent.addIncomingEdge(cf);
            flyweight.getModelUnderConstruction().addFinalNode(endEvent);
            flyweight.getModelUnderConstruction().addNode(endEvent);
            return flyweight.getModelUnderConstruction();
        } else {
            Gateway gateway = new Gateway();
            if (nodesToBeCheckedAreDisjoint()) {
                gateway.setType(Gateway.Type.AND);
            } else {
                gateway.setType(Gateway.Type.XOR);
                exclusive = true;
            }
            ControlFlow incoming = new ControlFlow(startEvent, gateway);
            gateway.addIncomingEdge(incoming);
            startEvent.addOutgoingEdge(incoming);
            for (ActivityBuilder activityBuilder : nodesToBeChecked) {
                activityBuilder.addPredecessor(gateway);
            }
            flyweight.getModelUnderConstruction().addNode(gateway);
        }
        do {
            Collection<CombinedTransition> concurrentCombinedTransitions =
                    new HashSet<>();
            if (!exclusive) {
                for (ActivityBuilder activityBuilder : nodesToBeChecked) {
                    if (!activityBuilder.isChecked()) {
                        concurrentCombinedTransitions.add(activityBuilder.getCtExecuted());
                    }
                }
            }
            for (ActivityBuilder activityBuilder : nodesToBeChecked) {
                if (activityBuilder.isChecked()) {
                    nodeBuilderNodesChecked.add(activityBuilder);
                } else {
                    activityBuilder
                            .findEnabledCombinedTransitions()
                            .findPossibleEnabledCombinedTransitions(
                                    concurrentCombinedTransitions);
                }
            }
            nodesToBeChecked.removeAll(nodeBuilderNodesChecked);
            Collection<ActivityBuilder> newNodes = new HashSet<>();
            for (ActivityBuilder activityBuilder : nodesToBeChecked) {
                Collection<ActivityBuilder> successors = activityBuilder.getSuccessorActivities();
                activityBuilder.establishOutgoingControlFlow();
                for (ActivityBuilder successor : successors) {
                    if (!successor.isChecked()) {
                        newNodes.add(successor);
                    }
                }
            }
            nodesToBeChecked.addAll(newNodes);
            for (ActivityBuilder nodeBuilder : nodesToBeChecked) {
                nodeBuilder.establishIncomingControlFlow();
            }
            for (ActivityBuilder nodeBuilder : nodeBuilderNodesChecked) {
                nodeBuilder.establishIncomingControlFlow();
            }
        } while (!nodesToBeChecked.isEmpty());
        flyweight.finalizeModel();
        return flyweight.getModelUnderConstruction();
    }

    private boolean nodesToBeCheckedAreDisjoint() {
        for (ActivityBuilder node1 : nodesToBeChecked) {
            for (ActivityBuilder node2 : nodesToBeChecked) {
                if (!node1.equals(node2) && !node1.inputSetsAreDisjoint(node2)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void initNodesToBeChecked(
            OLCConversionFlyweight<ActivityCentricProcessModel> flyweight) {
        Collection<DataObjectState> startStates = new HashSet<>();
        nodesToBeChecked = new LinkedList<>();
        for (ObjectLifeCycle objectLifeCycle : flyweight.getsOLC().getOLCs()) {
            startStates.add((DataObjectState) objectLifeCycle.getStartNode());
        }
        for (CombinedTransition combinedTransition : flyweight.getCombinedTransitions()) {
            if (combinedTransition.isEnabledForStates(startStates)) {
                nodesToBeChecked.add(flyweight.getActivityBuilderFor(combinedTransition,
                        startStates));
            }
        }
    }

    @Override
    public <T extends IModel> T convert(IModel model, Class<T> t) {
        assert model instanceof SynchronizedObjectLifeCycle :
                "The given model is no synchrnoized OLC";
        synchronizedObjectLifeCycle = (SynchronizedObjectLifeCycle) model;
        return (T) convert();
    }
}
