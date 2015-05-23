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
     * <p>
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

    /**
     * Creates a new Object of the class.
     * Sets and collections will be initialized (empty).
     */
    public ActivityCentricToSynchronizedOLC() {
        checkedNodes = new HashSet<>();
        loopNodes = new HashSet<>();
        synchronisationEdges = new HashMap<>();
    }

    /**
     * Generates an {@link SynchronizedObjectLifeCycle} from an given {@link ActivityCentricProcessModel}.
     * Activities will be transformed to transitions and there will be an OLC for each unique DataObject name.
     * For more details about the conversion algorithm {@see bpt.hpi.uni-potsdam.de/pub/Public/AndreasMeyer/Technical_Report_Activity-centric_and_Artifact-centric_Process_Model_Roundtrip.pdf}
     *
     * @param acpm Is the Activity Centric Process Model to be converted.
     * @return The generated model, an instance of {@link SynchronizedObjectLifeCycle}
     */
    public IModel convert(ActivityCentricProcessModel acpm) {
        assert null != acpm : "Null can not be converted into a process model";
        this.acpm = acpm;
        initOLCs();
        identifyDistinctDataStates();
        extractTraces();
        for (List<INode> trace : traces) {
            new HashMap<>();
            Map<String, Collection<DataObjectState>> stateCollections = getInitialStateCollections();
            for (INode node : trace) {
                if (node instanceof Activity) {
                    Map<String, Collection<DataObjectState>> currentStates = extractCurrentStates(node);
                    connectStatesForActivity(stateCollections, currentStates, null);
                    establishSynchronizationEdges(stateCollections, currentStates);
                    stateCollections.putAll(currentStates);
                    currentStates = getStatesAfterNode(node);
                    connectStatesForActivity(stateCollections, currentStates, (Activity) node);
                    establishSynchronizationEdges(stateCollections, currentStates);
                    stateCollections.putAll(currentStates);
                }
            }
        }
        detectFinalStates();

        return buildSynchronizedObjectLifeCycle();
    }

    /**
     * Creates an synchronized Object Life Cycle for all OLCs
     * created during the conversion.
     * @return The synchronized object life cycle which has been created.
     */
    private IModel buildSynchronizedObjectLifeCycle() {
        SynchronizedObjectLifeCycle synchOLC = new SynchronizedObjectLifeCycle();
        synchOLC.setObjectLifeCycles(new LinkedList<>(olcs));
        synchOLC.setSynchronisationEdges(synchronisationEdges);
        return synchOLC;
    }

    /**
     * This method detects all final states.
     * If a state has no outgoing edges it is considered final.
     * Such a state will be added to collection and than marked as
     * final for the specific object life cycle.
     */
    private void detectFinalStates() {
        for (Map.Entry<ObjectLifeCycle, Collection<DataObjectState>> entry
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
    }


    /**
     * Establishes state transitions between the predecessor and successor states.
     * The transition represents an action taken by an activity.
     * If the activity is null the action will be a silent transition marked with "t".
     *
     * @param stateCollections The collection of all states mapped to the name of the OLC.
     * @param currentStates    The currently extracted States mapped.
     * @param node             The node representing the action.
     */
    private void connectStatesForActivity(Map<String, Collection<DataObjectState>> stateCollections,
                                          Map<String, Collection<DataObjectState>> currentStates,
                                          Activity node) {
        for (Map.Entry<String, Collection<DataObjectState>> entry
                : stateCollections.entrySet()) {
            for (DataObjectState predecessor : entry.getValue()) {
                if (currentStates.containsKey(entry.getKey())) {
                    for (DataObjectState successor : currentStates.get(entry.getKey())) {
                        StateTransition transition =
                                new StateTransition(predecessor,
                                        successor,
                                        (node == null) ? "t" : ((Activity) node).getName());
                        predecessor.addOutgoingEdge(transition);
                        successor.addIncomingEdge(transition);
                    }
                }
            }
        }

    }

    /**
     * Returns a map containing all data nodes created by a given node.
     * In general the node should be of type {@link Activity}.
     * In that case the map would contain the states of the data outputs.
     * In addition it maps the data object name to the data object states.
     * This indicates all possible states after the termination.
     *
     * @param node The node which outgoing dataflow edges will be checked.
     * @return Returns a map, containing the data object names (key) and a Collection of
     * states of the data object (value).
     */
    private Map<String, Collection<DataObjectState>> getStatesAfterNode(INode node) {
        Map<String, Collection<DataObjectState>> currentStates = new HashMap<>();
        for (IEdge outgoingDF : node.getOutgoingEdgesOfType(DataFlow.class)) {
            DataObject dataObject = (DataObject) outgoingDF.getTarget();
            if (currentStates.get(dataObject.getName()) == null) {
                currentStates.put(dataObject.getName(),
                        new HashSet<DataObjectState>());
            }
            currentStates.get(dataObject.getName()).add(dataObject.getState());
        }
        return currentStates;
    }

    /**
     * This method establishes the synchronization edges.
     * Therefore it checks for all entries of the current States and for each state which is
     * different a link will added.
     * Then the incoming edges will synchronized.
     * TODO: Check if this works - there might be some wrong connections.
     *
     * @param stateCollections The collection of all states in a map.
     *                         it maps from the data object name to a collection of the states.
     * @param currentStates The map of all possible enabled states per data object.
     *                      The relation between data object and states is expressed by the map.
     */
    private void establishSynchronizationEdges(Map<String, Collection<DataObjectState>> stateCollections,
                                               Map<String, Collection<DataObjectState>> currentStates) {
        for (Map.Entry<String, Collection<DataObjectState>> entry :
                currentStates.entrySet()) {
            List<StateTransition> links = new ArrayList<>();
            for (Map.Entry<String, Collection<DataObjectState>> entry2
                    : currentStates.entrySet()) {
                if (!entry.getKey().equals(entry2.getKey())) {
                    for (DataObjectState state : entry2.getValue()) {
                        for (IEdge transition :
                                state.getIncomingEdgesOfType(StateTransition.class)) {
                            if (stateCollections.get(entry2.getKey())
                                    .contains(transition.getSource())) {
                                links.add((StateTransition) transition);
                            }
                        }
                    }
                }
            }
            for (DataObjectState state : entry.getValue()) {
                for (IEdge transition :
                        state.getIncomingEdgesOfType(StateTransition.class)) {
                    if (stateCollections.get(entry.getKey())
                            .contains(transition.getSource())) {
                        synchronisationEdges.put((StateTransition) transition, links);
                    }
                }
            }
        }
    }

    /**
     * This method extracts the current states from a node.
     * Current states are the states available before the execution.
     * Therefore it checks all the incoming DataFlowEdges and adds the state
     * of the DataInput.
     * These states will be clustered by the name of the data object/ data class / olc.
     *
     * @param node The node to be checked. Should be an Activity but is not mandatory.
     * @return A Map, the key is the name of the data object, the value is a collection
     * with all the states available before the activity.
     */
    private Map<String, Collection<DataObjectState>> extractCurrentStates(INode node) {
        Map<String, Collection<DataObjectState>> currentStates = new HashMap<>();
        for (IEdge incomingDF : node.getIncomingEdgesOfType(DataFlow.class)) {
            DataObject dataObject = (DataObject) incomingDF.getSource();
            if (currentStates.get(dataObject.getName()) == null) {
                currentStates.put(dataObject.getName(),
                        new HashSet<DataObjectState>());
            }
            currentStates.get(dataObject.getName()).add(dataObject.getState());
        }
        return currentStates;
    }


    /**
     * This method extracts all traces of the {@link #acpm}.
     * Therefore it uses a method which is analogue to creating a
     * reachability graph for a petri net.
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
            Collection<List<INode>> tracesToBeRemoved = new HashSet<>();
            groupsContainFinal = getTracesToBeRemoved(tracesAndTheirSuccessors, tracesToBeRemoved);
            tracesAndTheirSuccessors.keySet().removeAll(tracesToBeRemoved);
            Map<List<INode>, Collection<List<INode>>>
                    newTracesAndTheirSuccessors = determineNewTraces(tracesAndTheirSuccessors);
            if (!newTracesAndTheirSuccessors.isEmpty()) {
                tracesAndTheirSuccessors = newTracesAndTheirSuccessors;
            }
        } while (!groupsContainFinal);
        traces = new HashSet<>(tracesAndTheirSuccessors.keySet());
    }

    /**
     * This method determines the new traces.
     * Based on a given trace and a given group of successors it will create all possible traces
     * with their successor Groups.
     * Those will be added to list and then returned.
     * @param tracesAndTheirSuccessors The traces and successors. The key represents the trace (list of nodes)
     *                                 and the value the possible successor groups. Each list of the value collection
     *                                 is exclusive, the elements inside such a list parallel.
     * @return The Map representing the new traces and their succesors.
     */
    private Map<List<INode>, Collection<List<INode>>> determineNewTraces(
            Map<List<INode>, Collection<List<INode>>> tracesAndTheirSuccessors) {
        Map<List<INode>, Collection<List<INode>>>
                newTracesAndTheirSuccessors = new HashMap<>();
        for (Map.Entry<List<INode>, Collection<List<INode>>>
                traceAndSuccessors : tracesAndTheirSuccessors.entrySet()) {
            if (traceAndSuccessors.getValue().isEmpty()) {
                newTracesAndTheirSuccessors.put(traceAndSuccessors.getKey(),
                        traceAndSuccessors.getValue());
            }
            for (List<INode> successorGroup : traceAndSuccessors.getValue()) {
                for (INode successor : successorGroup) {
                    List<INode> trace = new ArrayList<>(traceAndSuccessors.getKey());
                    trace.add(successor);
                    newTracesAndTheirSuccessors.put(trace,
                            getSuccessorsFor(trace, successorGroup, successor));

                }
            }
        }
        return newTracesAndTheirSuccessors;
    }

    /**
     * Determins all traces which have to be removed.
     * Traces who have not successor and no final node inside the trace will be removed.
     * The traces will be saved inside the map given as a second paramter.
     * @param tracesAndTheirSuccessors The Collection of all Traces (List of Nodes) and their possible Successors.
     *                                 The successors are saved inside a Collection of Lists of Nodes. The nodes inside
     *                                 a List a parallel the lists are exclusive.
     * @param tracesToBeRemoved
     * @return Returns true if every group contains a final node. Else it returns false.
     */
    private boolean getTracesToBeRemoved(
            Map<List<INode>, Collection<List<INode>>> tracesAndTheirSuccessors,
            Collection<List<INode>> tracesToBeRemoved) {
        boolean groupsContainFinal = true;
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
        return groupsContainFinal;
    }

    /**
     * This method returns a a Collection of a List of successors.
     * This collection represents all possible successors of a specified node (predecessor).
     * In order to determine those nodes we need some information:
     *
     * @param previousTrace          The previous trace, with all nodes triggered before the predecessor.
     * @param previousSuccessorGroup The previous successor Group, all nodes which have been enabled
     *                               at the end of the previousTrace.
     * @param predecessor            The Predecessor of the successors. Means the node which will be checked for
     *                               successors.
     * @return A Collection of List of Nodes. These collection represents all successor. Every list
     * inside the collection hold a number of concurrent successors. The List represent exclusive groups.
     */
    private Collection<List<INode>> getSuccessorsFor(
            List<INode> previousTrace, List<INode> previousSuccessorGroup, INode predecessor) {
        Collection<List<INode>> successors = new HashSet<>();
        if (!loopNodes.contains(predecessor)) {
            if (!(predecessor instanceof Gateway) && checkedNodes.contains(predecessor)) {
                loopNodes.add(predecessor);
            } else if (!(predecessor instanceof Gateway)) {
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
                        ((Gateway) successor).getType().equals(Gateway.Type.AND)) {
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

    /**
     * Extracts all states from the Activity Centric Process model.
     * For each Data Object (identified by the name of the {@link DataObject} node)
     * the state will be extracted and added to the {@link #dataStatesPerOLC} Map.
     * <p>
     * Calling this method twice would discard the results of the first run.
     */
    private void identifyDistinctDataStates() {
        dataStatesPerOLC = new HashMap<>();
        for (INode iNode : acpm.getNodesOfClass(DataObject.class)) {
            ObjectLifeCycle olc = getOLCWithName(((DataObject) iNode).getName());
            if (dataStatesPerOLC.get(olc) == null) {
                dataStatesPerOLC.put(olc, new HashSet<DataObjectState>());
            }
            dataStatesPerOLC.get(olc).add(((DataObject) iNode).getState());
        }
    }

    /**
     * This method goes through the {@link #olcs} Collection.
     * The first Object Life Cycle with the name specified by the parameter
     * will be returned.
     * If no OLC was found null will be returned.
     *
     * @param name The name of the Object Life Cycle.
     * @return The first Object Life Cycle matching the criteria or null.
     */
    private ObjectLifeCycle getOLCWithName(String name) {
        for (ObjectLifeCycle olc : olcs) {
            if (olc.getLabel().equals(name)) {
                return olc;
            }
        }
        return null;
    }

    /**
     * This method initializes the {@link #olcs} list.
     * The unique Data object names will be extracted from the Activity Centric
     * Process Model and for each name a {@link ObjectLifeCycle} will be created.
     */
    private void initOLCs() {
        olcs = new ArrayList<>();
        Collection<String> dataClassNames = new HashSet<>();
        for (INode iNode : acpm.getNodesOfClass(DataObject.class)) {
            dataClassNames.add(((DataObject) iNode).getName());
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
        if (model instanceof ActivityCentricProcessModel) {
            return (T) convert((ActivityCentricProcessModel) model);
        }
        return null;
    }

    /**
     * Creates an Map which contains all the initial States
     * for every objectLife Cycle.
     * The Object Life Cycle is identified by the name.
     * key: Object Life Cycle label
     * value: Collection of Initial States.
     *
     * @return Returns the created map, for every olc.
     */
    public Map<String, Collection<DataObjectState>> getInitialStateCollections() {
        Map<String, Collection<DataObjectState>> stateCollections = new HashMap<>();
        for (ObjectLifeCycle olc : olcs) {
            Collection<DataObjectState> stateCollection = new HashSet<>();
            stateCollection.add((DataObjectState) olc.getStartNode());
            stateCollections.put(olc.getLabel(), stateCollection);
        }
        return stateCollections;
    }
}