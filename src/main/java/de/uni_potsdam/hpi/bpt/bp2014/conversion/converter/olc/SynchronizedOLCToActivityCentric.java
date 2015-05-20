package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IConverter;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.IModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.ActivityCentricProcessModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.ControlFlow;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.Event;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.Gateway;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize.SynchronizedObjectLifeCycle;

import java.util.*;

/**
 * This model is a converter for synchronized Object Life Cycles.
 * It generates one {@link ActivityCentricProcessModel} based on one
 * {@link SynchronizedObjectLifeCycle}.
 * State transitions will be mapped on Activities and Data Objects nodes
 * on Data Object States.
 */
public class SynchronizedOLCToActivityCentric implements IConverter {
    /**
     * The Synchronized Object Life Cycle which will be converted.
     */
    private SynchronizedObjectLifeCycle synchronizedObjectLifeCycle;
    /**
     * A list of ActivityBuilders representing activities in the
     * generated model. Each ActivityBuilder will be checked once
     * for successors. This List holds all the nodes which have
     * to be checked.
     */
    private Collection<ActivityBuilder> nodesToBeChecked;

    /**
     * This methods creates a new activity centric process model.
     * It uses a synchronized object life cycle as an input, which
     * must be defined in {@link #synchronizedObjectLifeCycle}.
     * Other attributes of this class will be initialized and changed
     * during that process.
     * You may not manipulate this process.
     * The conversion is an implementation of the algorithm described
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


    /**
     * This method sets {@link #synchronizedObjectLifeCycle} and then calls the
     * {@link #convert()} method in order to generate an {@link ActivityCentricProcessModel}
     * The result will be returned.
     *
     * @param sOLC The Synchronized Object Life Cycle which is the base for the conversion.
     * @return Returns the generated Activity Centric Process Model.
     */
    public ActivityCentricProcessModel convert(SynchronizedObjectLifeCycle sOLC) {
        synchronizedObjectLifeCycle = sOLC;
        return convert();
    }

    /**
     * This methods creates a new activity centric process model.
     * It uses a synchronized object life cycle as an input, which
     * must be defined in {@link #synchronizedObjectLifeCycle}.
     * Other attributes of this class will be initialized and changed
     * during that process.
     * You may not manipulate this process.
     * The conversion is an implementation of the algorithm described
     * in {@see http://bpt.hpi.uni-potsdam.de/pub/Public/AndreasMeyer/Technical_Report_Activity-centric_and_Artifact-centric_Process_Model_Roundtrip.pdf}.
     *
     * @return The generated ActivityCentricProcessModel.
     */
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
            if (nodesToBeCheckedAreConcurrent()) {
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

    /**
     * Checks weather or not to nodes are concurrent.
     * Two nodes are concurrent if and only if there input Sets
     * are disjoint.
     *
     * @return Returns false if they have shared data inputs, else true.
     */
    private boolean nodesToBeCheckedAreConcurrent() {
        for (ActivityBuilder node1 : nodesToBeChecked) {
            for (ActivityBuilder node2 : nodesToBeChecked) {
                if (!node1.equals(node2) && !node1.inputSetsAreDisjoint(node2)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Initializes the nodes to be checked by adding all initial DataObject
     * states to the enabled states and than determining the enabled Activities.
     * The flyweight is needed in order to receive the ActivityBuilder for a
     * combined Transition.
     *
     * @param flyweight The Flyweight which holds shared resources.
     */
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
                "The given model is not a synchronized OLC";
        synchronizedObjectLifeCycle = (SynchronizedObjectLifeCycle) model;
        return (T) convert();
    }
}
