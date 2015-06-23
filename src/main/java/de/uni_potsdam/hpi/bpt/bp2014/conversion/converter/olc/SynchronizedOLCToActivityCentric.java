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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * This model is a converter for synchronized Object Life Cycles.
 * It generates one {@link ActivityCentricProcessModel} based on one
 * {@link SynchronizedObjectLifeCycle}.
 * State transitions will be mapped on Activities and Data Objects nodes
 * on Data Object States.
 */
public class SynchronizedOLCToActivityCentric implements IConverter<SynchronizedObjectLifeCycle, ActivityCentricProcessModel> {
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
        }
        ;
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
    @Override
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
        OLCConversionFlyweight<ActivityCentricProcessModel> flyweight = initializeFlyweight(sOLC);
        new OLCConversionFlyweight<>(sOLC,
                ActivityCentricProcessModel.class);
        Event startEvent = new Event();
        startEvent.type = Event.Type.START;
        initNodesToBeChecked(flyweight);
        Collection<ActivityBuilder> nodeBuilderNodesChecked = new HashSet<>();
        if (nodesToBeChecked.isEmpty()) {
            return flyweight.getModelUnderConstruction();
        }
        boolean exclusive = false;
        if (!flyweight.getModelUnderConstruction().getNodesOfClass(Gateway.class).isEmpty() &&
                ((Gateway) flyweight.getModelUnderConstruction().getNodesOfClass(Gateway.class)
                        .iterator().next()).getType().equals(Gateway.Type.XOR)) {
            exclusive = true;
        }
        do {
            Collection<CombinedTransition> concurrentCombinedTransitions =
                    determineConcurrentCTs(exclusive);
            nodeBuilderNodesChecked.addAll(determineEnabledCTsAndCheckedNodes(concurrentCombinedTransitions));
            nodesToBeChecked.removeAll(nodeBuilderNodesChecked);
            Collection<ActivityBuilder> newNodes = getNewNodes();
            nodesToBeChecked.addAll(newNodes);
            establishControlFlow(nodeBuilderNodesChecked);
        } while (!nodesToBeChecked.isEmpty());
        flyweight.finalizeModel();
        return flyweight.getModelUnderConstruction();
    }

    /**
     * This method establishes the control Flow between the checked nodes
     * and their predecessors.
     *
     * @param nodeBuilderNodesChecked Represents the nodes which have been checked before this iteration.
     */
    private void establishControlFlow(Collection<ActivityBuilder> nodeBuilderNodesChecked) {
        for (ActivityBuilder nodeBuilder : nodesToBeChecked) {
            nodeBuilder.establishIncomingControlFlow();
        }
        for (ActivityBuilder nodeBuilder : nodeBuilderNodesChecked) {
            nodeBuilder.establishIncomingControlFlow();
        }
    }

    /**
     * This method does two things on the one hand it checks weather or not an
     * activity has been checked for combined transition.
     * If it has not been checked, it will be checked.
     *
     * @param concurrentCombinedTransitions The concurrentCombinedTransitions,
     *                                      mandatory to reduce the possible enabled transitions.
     * @return Returns a collection of ActivityBuilders which have already been
     * checked for combined transitions before.
     */
    private Collection<? extends ActivityBuilder> determineEnabledCTsAndCheckedNodes(
            Collection<CombinedTransition> concurrentCombinedTransitions) {
        Collection<ActivityBuilder> nodesChecked = new HashSet<>();
        for (ActivityBuilder activityBuilder : nodesToBeChecked) {
            if (activityBuilder.isChecked()) {
                nodesChecked.add(activityBuilder);
            } else {
                activityBuilder
                        .findEnabledCombinedTransitions()
                        .findPossibleEnabledCombinedTransitions(
                                concurrentCombinedTransitions);
            }
        }
        return nodesChecked;
    }

    /**
     * This method determines all concurrent combined transitions.
     *
     * @param exclusive Indicates weather or not the latest Activities are concurrent.
     * @return A list of concurrent combined transition. If there are non an empty list
     * will be returned.
     */
    private Collection<CombinedTransition> determineConcurrentCTs(boolean exclusive) {
        Collection<CombinedTransition> concurrentCombinedTransitions = new HashSet<>();
        if (!exclusive) {
            for (ActivityBuilder activityBuilder : nodesToBeChecked) {
                if (!activityBuilder.isChecked()) {
                    concurrentCombinedTransitions.add(activityBuilder.getCtExecuted());
                }
            }
        }
        return concurrentCombinedTransitions;
    }

    /**
     * This method initializes the Flyweight.
     * First of all it creates a new Flyweight object for the given Synchronized Object Life Cycle.
     * Then the nodesToBeChecked will be initialized.
     * If there are no nodes a minimalistic flyweight will be created.
     * Else the first Activity/Activities will be created and added.
     * If there are more than one an Gateway (split/fork) will be established before
     * the Gateway.
     *
     * @param sOLC The Synchronized Object Life Cycle to be created.
     * @return The Flyweight which has been created and initialized.
     * @throws InstantiationException The Flyweight could not be created, due to an instantiation Exception.
     * @throws IllegalAccessException ThE Flyweight could not be created, because accessing the constructor of
     *                                the model type was forbidden.
     */
    private OLCConversionFlyweight<ActivityCentricProcessModel> initializeFlyweight(SynchronizedObjectLifeCycle sOLC)
            throws InstantiationException, IllegalAccessException {
        OLCConversionFlyweight<ActivityCentricProcessModel> flyweight = new OLCConversionFlyweight<>(sOLC,
                ActivityCentricProcessModel.class);
        Event startEvent = new Event();
        startEvent.type = Event.Type.START;
        initNodesToBeChecked(flyweight);
        flyweight.getModelUnderConstruction().addNode(startEvent);
        flyweight.getModelUnderConstruction().setStartNode(startEvent);
        if (nodesToBeChecked.size() == 1) {
            nodesToBeChecked.iterator().next().addPredecessor(startEvent);
        } else if (nodesToBeChecked.size() < 1) {
            createNoActivityModel(startEvent, flyweight);
        } else {
            Gateway gateway = new Gateway();
            gateway.setType(nodesToBeCheckedAreConcurrent() ?
                    Gateway.Type.AND :
                    Gateway.Type.XOR);
            ControlFlow incoming = new ControlFlow(startEvent, gateway);
            gateway.addIncomingEdge(incoming);
            startEvent.addOutgoingEdge(incoming);
            for (ActivityBuilder activityBuilder : nodesToBeChecked) {
                activityBuilder.addPredecessor(gateway);
            }
            flyweight.getModelUnderConstruction().addNode(gateway);
        }
        return flyweight;
    }

    /**
     * Adds start and end event to the flyweight connected by one Control Flow edge.
     * Results in a minimalistic Process model. No other nodes or eges will be added.
     * The changes will be saved to the flyweight.
     *
     * @param startEvent The start event created for the flyweight.
     * @param flyweight  The flyweight to be altered.
     */
    private void createNoActivityModel(Event startEvent, OLCConversionFlyweight<ActivityCentricProcessModel> flyweight) {
        Event endEvent = new Event();
        endEvent.setType(Event.Type.END);
        ControlFlow cf = new ControlFlow(startEvent, endEvent);
        startEvent.addOutgoingEdge(cf);
        endEvent.addIncomingEdge(cf);
        flyweight.getModelUnderConstruction().addFinalNode(endEvent);
        flyweight.getModelUnderConstruction().addNode(endEvent);
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

    /**
     * Based on the nodes to be checked the new nodes will be determined.
     * A node is new if and only if it is a successor of at least one node of {@link #nodesToBeChecked}
     * and if the node has not been checked before.
     *
     * @return A Collection of all nodes which have been created during this iteration.
     */
    public Collection<ActivityBuilder> getNewNodes() {
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
        return newNodes;
    }
}
