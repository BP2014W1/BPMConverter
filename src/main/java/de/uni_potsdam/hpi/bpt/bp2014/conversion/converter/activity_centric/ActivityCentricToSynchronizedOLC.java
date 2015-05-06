package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.activity_centric;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IConverter;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.IModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.*;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize.SynchronizedObjectLifeCycle;

import java.util.*;

/**
 * This class is a converter.
 * It offers methods to transform an {@link ActivityCentricProcessModel} into
 * a {@link SynchronizedObjectLifeCycle}.
 */
public class ActivityCentricToSynchronizedOLC implements IConverter {
    /**
     * This variable holds an instance of {@link ActivityCentricProcessModel}.
     * It is model which will be converted into an
     * {@link SynchronizedObjectLifeCycle}.
     *
     * If changes happen to this model during the conversion, they might end
     * up in a corrupt state of the converter or synchronizedObjectLifeCycle.
     */
    private ActivityCentricProcessModel acpm;

    /**
     * Holds a Collection of all Object Life Cycles being a part of the
     * synchronized OLC.
     * There will be exactly one OLC for each DataClass.
     */
    private Collection<ObjectLifeCycle> olcs;

    /**
     * This map holds a collection of distinct data states
     * for each OLC.
     */
    private Map<ObjectLifeCycle, Collection<DataObjectState>> dataStatesPerOLC;

    /**
     * Checked nodes, contains all nodes which have already
     * been checked for their successors.
     */
    private Collection<INode> checkedNodes;
    /**
     * Contains all Nodes which are part of a loop condition
     * They will be only checked twice, which means, that their
     * will be only one trace with this loop
     */
    private Collection<INode> loopNodes;

    /**
     * This Collection hold Lists of iNodes.
     * Every list represents a possible trace inside the activity centric
     * process model.
     */
    private Collection<List<INode>> traces;

    /**
     * This map represent the synchronization edges between the different olcs.
     */
    private Map<StateTransition, List<StateTransition>> synchronisationEdges;

    public ActivityCentricToSynchronizedOLC() {
        checkedNodes = new HashSet<>();
        loopNodes = new HashSet<>();
        synchronisationEdges = new HashMap<>();
    }

    public IModel convert(ActivityCentricProcessModel acpm) {
        assert null !=  acpm : "Null can not be converted into a process model";
        this.acpm = acpm;
        initOLCs();
        identifyDistinctDataStates();
        extractTraces();
        for (List<INode> trace : traces) {
            Map<String, Collection<DataObjectState>> stateCollections = new HashMap<>();
            for (ObjectLifeCycle olc : olcs) {
                Collection<DataObjectState> stateCollection = new HashSet<>();
                stateCollection.add((DataObjectState) olc.getStartNode());
                stateCollections.put(olc.getLabel(), stateCollection);
            }
            for (INode node : trace) {
                Map<String, Collection<DataObjectState>> currentStates = new HashMap<>();
                if (node instanceof Activity) {
                    for (IEdge incomingDF : node.getIncomingEdgesOfType(DataFlow.class)) {
                        DataObject dataObject = (DataObject) incomingDF.getSource();
                        if (currentStates.get(dataObject.getName()) == null) {
                            currentStates.put(dataObject.getName(),
                                    new HashSet<DataObjectState>());
                        }
                        currentStates.get(dataObject.getName()).add(dataObject.getState());
                    }
                    for (Map.Entry<String, Collection<DataObjectState>> entry
                            : stateCollections.entrySet()) {
                        for (DataObjectState predecessor : entry.getValue()) {
                            if (currentStates.containsKey(entry.getKey())) {
                                for (DataObjectState successor : currentStates.get(entry.getKey())) {
                                    StateTransition transition =
                                            new StateTransition(predecessor,
                                                    successor,
                                                    ((Activity) node).getName());
                                    predecessor.addOutgoingEdge(transition);
                                    successor.addIncomingEdge(transition);
                                }
                            }
                        }
                    }
                    for (Map.Entry<String, Collection<DataObjectState>> entry :
                            currentStates.entrySet()) {
                        stateCollections.put(entry.getKey(), entry.getValue());
                    }
                    // TODO: establish synchronisation edges
                    // TODO: reduce redundancy
                    currentStates = new HashMap<>();
                    for (IEdge outgoingDF : node.getOutgoingEdgesOfType(DataFlow.class)) {
                        DataObject dataObject = (DataObject) outgoingDF.getTarget();
                        if (currentStates.get(dataObject.getName()) == null) {
                            currentStates.put(dataObject.getName(),
                                    new HashSet<DataObjectState>());
                        }
                        currentStates.get(dataObject.getName()).add(dataObject.getState());
                    }
                    for (Map.Entry<String, Collection<DataObjectState>> entry
                            : stateCollections.entrySet()) {
                        for (DataObjectState predecessor : entry.getValue()) {
                            if (currentStates.containsKey(entry.getKey())) {
                                for (DataObjectState successor : currentStates.get(entry.getKey())) {
                                    StateTransition transition =
                                            new StateTransition(predecessor,
                                                    successor,
                                                    ((Activity) node).getName());
                                    predecessor.addOutgoingEdge(transition);
                                    successor.addIncomingEdge(transition);
                                }
                            }
                        }
                    }
                    for (Map.Entry<String, Collection<DataObjectState>> entry :
                            currentStates.entrySet()) {
                        stateCollections.put(entry.getKey(), entry.getValue());
                    }
                } else if (node instanceof Gateway &&
                        ((Gateway)node).getType().equals(Gateway.Type.XOR)) {
                    // Currently we do not support edge conditions
                }
            }
        }
        for (Map.Entry<ObjectLifeCycle,Collection<DataObjectState>> entry
                : dataStatesPerOLC.entrySet()) {
            Collection<DataObjectState> finalStates = new HashSet<>();
            for (DataObjectState dataObjectState : entry.getValue()) {
                entry.getKey().addNode(dataObjectState);
                if (dataObjectState.getOutgoingEdges().isEmpty()) {
                    finalStates.add(dataObjectState);
                }
            }
            for (DataObjectState finalState : finalStates) {
                entry.getKey().addFinalNode(finalState);
            }
        }
        SynchronizedObjectLifeCycle synchOLC = new SynchronizedObjectLifeCycle();
        synchOLC.setObjectLifeCycles(new LinkedList<ObjectLifeCycle>(olcs));
        return synchOLC;
    }


    /**
     * This method extracts all traces of the {@link #acpm}.
     * Therefore it uses a method which is analogue to creating a
     * reachability graph for a petri net.
     *
     * TODO: Currently this method does allows duplicated traces
     */
    private void extractTraces() {
        Map<List<INode>, Collection<List<INode>>>
                tracesAndTheirSuccessors = new HashMap<>();
        List<INode> startConfig = new LinkedList<>();
        startConfig.add(acpm.getStartNode());
        Collection<List<INode>> successors = new HashSet<>();
        List<INode> sequentialGroup = new LinkedList<>();
        sequentialGroup.add(acpm.getStartNode()
                .getOutgoingEdgesOfType(ControlFlow.class)
                .iterator().next()
                .getTarget());
        successors.add(sequentialGroup);
        tracesAndTheirSuccessors.put(startConfig, successors);
        boolean groupsContainFinal;
        do {
            groupsContainFinal = true;
            Collection<List<INode>> tracesToBeRemoved = new HashSet<>();
            for (Map.Entry<List<INode>, Collection<List<INode>>> traceAndSuccessors
                    : tracesAndTheirSuccessors.entrySet()) {
                List<INode> trace = traceAndSuccessors.getKey();
                Collection<List<INode>> successorsOfTrace = traceAndSuccessors.getValue();
                if (successorsOfTrace.isEmpty() && !trace.contains(acpm.getFinalNodesOfClass(Event.class)
                        .iterator().next())) {
                    tracesToBeRemoved.add(trace);
                }
                if (groupsContainFinal && !trace.contains(acpm.getFinalNodesOfClass(Event.class)
                        .iterator().next())) {
                    groupsContainFinal = false;
                }
            }
            for (List<INode> trace : tracesToBeRemoved) {
                tracesAndTheirSuccessors.remove(trace);
            }
            Map<List<INode>, Collection<List<INode>>>
                    newTracesAndTheirSuccessors = new HashMap<>();
            for (Map.Entry<List<INode>, Collection<List<INode>>>
                    traceAndSuccessors : tracesAndTheirSuccessors.entrySet()) {
                for (List<INode> successorGroup : traceAndSuccessors.getValue()) {
                    for (INode successor : successorGroup) {
                        List<INode> trace = new ArrayList<>(traceAndSuccessors.getKey());
                        trace.add(successor);
                        newTracesAndTheirSuccessors.put(trace,
                                getSuccessorsFor(trace, successorGroup, successor));

                    }
                }
            }
            if (!newTracesAndTheirSuccessors.isEmpty()) {
                tracesAndTheirSuccessors = newTracesAndTheirSuccessors;
            }
        } while (!groupsContainFinal);
        traces = new HashSet<>(tracesAndTheirSuccessors.keySet());
    }

    private Collection<List<INode>> getSuccessorsFor(
            List<INode> previousTrace, List<INode> previousSuccessorGroup, INode predecessor) {
        Collection<List<INode>> successors = new HashSet<>();
        if (!loopNodes.contains(predecessor)) {
            if (checkedNodes.contains(predecessor)) {
                loopNodes.add(predecessor);
            } else {
                checkedNodes.add(predecessor);
            }
            List<INode> successorGroup = new LinkedList<>(previousSuccessorGroup);
            successorGroup.remove(predecessor);
            if (predecessor instanceof Activity) {
                successorGroup.add(predecessor.getOutgoingEdgesOfType(ControlFlow.class)
                        .iterator().next().getTarget());
                successors.add(successorGroup);
            } else if (predecessor instanceof Gateway) {
                if (((Gateway) predecessor).getType().equals(Gateway.Type.AND)) {
                    boolean enabled = true;
                    for (IEdge incomingCF :
                            predecessor.getIncomingEdgesOfType(ControlFlow.class)) {
                        if (!previousTrace.contains(incomingCF.getSource()) &&
                                !predecessor.equals(incomingCF.getSource())) {
                            enabled = false;
                        }
                    }
                    if (enabled) {
                        for (IEdge outgoingCF :
                                predecessor.getOutgoingEdgesOfType(ControlFlow.class)) {
                            successorGroup.add(outgoingCF.getTarget());
                        }
                        successors.add(successorGroup);
                    }
                } else /* gateway is exclusive */ {
                    for (IEdge outgoingCF :
                            predecessor.getOutgoingEdgesOfType(ControlFlow.class)) {
                        List<INode> newSuccessorGroup = new LinkedList<>(successorGroup);
                        newSuccessorGroup.add(outgoingCF.getTarget());
                        successors.add(newSuccessorGroup);
                    }
                }
            } else /*predecessor is instance of Event*/ {
                return successors;
            }
        }

        Collection<List<INode>> result = new HashSet<>();
        for (List<INode> successorGroup : successors) {
            List<INode> reducedSuccessorGroup = new LinkedList<>(successorGroup);
            for (INode successor : successorGroup) {
                if (successor instanceof Gateway &&
                        ((Gateway)successor).getType().equals(Gateway.Type.AND)) {
                    boolean enabled = true;
                    for (IEdge incomingCF
                            : successor.getIncomingEdgesOfType(ControlFlow.class)) {
                        if (!predecessor.equals(incomingCF.getSource()) &&
                                !previousTrace.contains(incomingCF.getSource())) {
                            enabled = false;
                            break;
                        }
                    }
                    if (!enabled) {
                        reducedSuccessorGroup.remove(successor);
                    }
                }
            }
            result.add(reducedSuccessorGroup);
        }
        return result;
    }

    private void identifyDistinctDataStates() {
        dataStatesPerOLC = new HashMap<>();
        for (INode iNode : acpm.getNodesOfClass(DataObject.class)) {
            ObjectLifeCycle olc = getOLCWithName(((DataObject)iNode).getName());
            if (dataStatesPerOLC.get(olc) == null) {
                dataStatesPerOLC.put(olc, new HashSet<DataObjectState>());
            }
            dataStatesPerOLC.get(olc).add(((DataObject)iNode).getState());
        }
    }

    private ObjectLifeCycle getOLCWithName(String name) {
        for (ObjectLifeCycle olc : olcs) {
            if (olc.getLabel().equals(name)) {
                return olc;
            }
        }
        return null;
    }

    private void initOLCs() {
        olcs = new ArrayList<>();
        Collection<String> dataClassNames = new HashSet<>();
        for (INode iNode : acpm.getNodesOfClass(DataObject.class)) {
            dataClassNames.add(((DataObject)iNode).getName());
        }
        for (String dataClassName : dataClassNames) {
            DataObjectState initState = new DataObjectState("i");
            ObjectLifeCycle olc = new ObjectLifeCycle(dataClassName);
            olc.addNode(initState);
            olc.setStartNode(initState);
            olcs.add(olc);
        }
    }

    @Override
    public <T extends IModel> T convert(IModel model, Class<T> t) {
        return null;
    }
}
