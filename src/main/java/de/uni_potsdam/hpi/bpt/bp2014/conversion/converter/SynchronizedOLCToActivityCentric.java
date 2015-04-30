package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter;

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

public class SynchronizedOLCToActivityCentric implements IConverter {
    /*
     * private SynchronizedObjectLifeCycle synchronizedObjectLifeCycle;
     * private List<CombinedTransition> combinedTransitions;
     * private List<INode> nodesToBeChecked;
     * private List<INode> nodesChecked;
     * private Set<INode> enabledNodes;
     * private Map<INode, CombinedTransition> nodesAndTheirTransition;
     * private Set<DataObjectState> enabledStates;
     * private List<CombinedTransition> enabledCombinedTransitions;
     * private List<CombinedTransition> possibleEnabledCombinedTransitions;
     * private ActivityCentricProcessModel model;
     * private Event startEvent;
     * private Map<INode, List<IEdge>> incomingEdgesPerNode;
     * <p/>
     * public SynchronizedOLCToActivityCentric() {
     * nodesChecked = new ArrayList<>();
     * nodesToBeChecked = new LinkedList<>();
     * enabledNodes = new HashSet<>();
     * nodesAndTheirTransition = new HashMap<>();
     * incomingEdgesPerNode = new HashMap<>();
     * }
     * <p/>
     * public ActivityCentricProcessModel convert(SynchronizedObjectLifeCycle synchronizedOLC) {
     * List<INode> nodesCreated = new LinkedList<>();
     * synchronizedObjectLifeCycle = synchronizedOLC;
     * init();
     * do {
     * INode node = nodesToBeChecked.get(0);
     * updateEnabledStatesFor(node);
     * initializeEnabledCombinedTransitions();
     * initializePossibleEnabledCombinedTransitions();
     * enabledCombinedTransitions.addAll(possibleEnabledCombinedTransitions);
     * //nodesCreated.addAll(getAndRemoveExistingNodesFromEnabledOnes(node));
     * for (CombinedTransition combinedTransition : enabledCombinedTransitions) {
     * if (!nodesAndTheirTransition.containsValue(combinedTransition)) {
     * INode activity = createNodeFor  (combinedTransition);
     * addDataNodesForActivityToModel(activity);
     * nodesCreated.add(activity);
     * checkForFinalState(activity);
     * nodesAndTheirTransition.put(activity, combinedTransition);
     * } else {
     * for (Map.Entry<INode, CombinedTransition> entry : nodesAndTheirTransition.entrySet()) {
     * if (combinedTransition.equals(entry.getValue())
     * && !enabledNodes.contains(entry.getKey())
     * && !nodesCreated.contains(entry.getKey())) {
     * nodesCreated.add(entry.getKey());
     * break;
     * }
     * }
     * }
     * }
     * nodesToBeChecked.remove(node);
     * nodesChecked.add(node);
     * enabledNodes.addAll(nodesCreated);
     * addControlFlow(node, nodesCreated);
     * enabledNodes.removeAll(nodesCreated);
     * if (nodesToBeChecked.isEmpty()) {
     * enabledNodes.clear();
     * nodesToBeChecked.addAll(nodesCreated);
     * nodesToBeChecked.removeAll(nodesChecked);
     * enabledNodes.addAll(nodesCreated);
     * nodesCreated.clear();
     * }
     * } while (!nodesToBeChecked.isEmpty());
     * establishJoinsAndMerges();
     * addEndEvent();
     * establishJoinsAndMerges();
     * return model;
     * }
     * <p/>
     * private Collection getAndRemoveExistingNodesFromEnabledOnes(INode node) {
     * Collection<INode> existingNodes = new HashSet<>();
     * for (Map.Entry<INode, CombinedTransition> entry : nodesAndTheirTransition.entrySet()) {
     * if (enabledCombinedTransitions.contains(entry.getValue())) {
     * existingNodes.add(entry.getKey());
     * enabledCombinedTransitions.remove(entry.getValue());
     * }
     * }
     * return existingNodes;
     * }
     * <p/>
     * private void addEndEvent() {
     * Set<INode> finalActivities = new HashSet<>();
     * for (INode node : model.getNodesOfClass(Activity.class)) {
     * if (node.getOutgoingEdgesOfType(ControlFlow.class).isEmpty()) {
     * finalActivities.add(node);
     * }
     * }
     * Event endEvent = new Event();
     * endEvent.setType(Event.Type.END);
     * model.addNode(endEvent);
     * model.addFinalNode(endEvent);
     * List<IEdge> incomingEdges = new LinkedList<>();
     * if (finalActivities.isEmpty()) {
     * ControlFlow flow = new ControlFlow(startEvent, endEvent);
     * startEvent.addOutgoingEdge(flow);
     * incomingEdges.add(flow);
     * }
     * for (INode node : finalActivities) {
     * ControlFlow flow = new ControlFlow(node, endEvent);
     * node.addOutgoingEdge(flow);
     * incomingEdges.add(flow);
     * }
     * incomingEdgesPerNode.put(endEvent, incomingEdges);
     * }
     * <p/>
     * private void establishJoinsAndMerges() {
     * for (Map.Entry<INode, List<IEdge>> entry : incomingEdgesPerNode.entrySet()) {
     * if (1 == entry.getValue().size()) {
     * if (entry.getKey().getIncomingEdgesOfType(ControlFlow.class).isEmpty()) {
     * entry.getKey().addIncomingEdge(entry.getValue().get(0));
     * }
     * } else {
     * // TODO: make pretty
     * boolean isJoin = false;
     * for (IEdge edge : entry.getValue()) {
     * for (IEdge edge2 : entry.getValue()) {
     * if (!edge.equals(edge2)) {
     * if (!Collections.disjoint(nodesAndTheirTransition.get(
     * edge.getSource())
     * .getTransitionsAndOLCs()
     * .values(),
     * nodesAndTheirTransition.get(edge2.getSource()).getTransitionsAndOLCs().values())) {
     * isJoin = true;
     * break;
     * }
     * }
     * }
     * if (isJoin) {
     * break;
     * }
     * }
     * if (isJoin) {
     * createJoin(entry.getKey());
     * } else {
     * createMerge(entry.getKey());
     * }
     * incomingEdgesPerNode.put(entry.getKey(), new LinkedList<>(entry.getKey().getIncomingEdgesOfType(ControlFlow.class)));
     * }
     * }
     * }
     * <p/>
     * private void createJoin(INode key) {
     * createGatewayJoin(key, Gateway.Type.XOR);
     * }
     * <p/>
     * private void createMerge(INode key) {
     * createGatewayJoin(key, Gateway.Type.AND);
     * }
     * <p/>
     * private void createGatewayJoin(INode target, Gateway.Type type) {
     * Gateway join = new Gateway();
     * join.setType(type);
     * for (IEdge edge : incomingEdgesPerNode.get(target)) {
     * join.addIncomingEdge(edge);
     * edge.setTarget(join);
     * }
     * ControlFlow flow = new ControlFlow(join, target);
     * target.addIncomingEdge(flow);
     * join.addOutgoingEdge(flow);
     * model.addNode(join);
     * }
     * <p/>
     * private void checkForFinalState(INode activity) {
     * <p/>
     * }
     * <p/>
     * private void addControlFlow(INode source) {
     * if (1 >= enabledNodes.size()) {
     * for (INode target : enabledNodes) {
     * ControlFlow flow = new ControlFlow(source, target);
     * source.addOutgoingEdge(flow);
     * if (!incomingEdgesPerNode.containsKey(target)) {
     * incomingEdgesPerNode.put(target, new ArrayList<IEdge>());
     * }
     * incomingEdgesPerNode.get(target).add(flow);
     * }
     * return;
     * }
     * for (INode target : enabledNodes) {
     * for (INode target2 : enabledNodes) {
     * if (!target.equals(target2)) {
     * if (!Collections.disjoint(
     * nodesAndTheirTransition.get(target).getTransitionsAndOLCs().keySet(),
     * nodesAndTheirTransition.get(target2).getTransitionsAndOLCs().keySet())) {
     * addXorConstruct(source);
     * return;
     * }
     * }
     * }
     * }
     * addAndConstruct(source);
     * }
     * <p/>
     * private void addControlFlow(INode source, Collection<INode> nodesCreated) {
     * if (1 >= nodesCreated.size()) {
     * for (INode target : nodesCreated) {
     * ControlFlow flow = new ControlFlow(source, target);
     * source.addOutgoingEdge(flow);
     * if (!incomingEdgesPerNode.containsKey(target)) {
     * incomingEdgesPerNode.put(target, new ArrayList<IEdge>());
     * }
     * incomingEdgesPerNode.get(target).add(flow);
     * }
     * return;
     * }
     * for (INode target : nodesCreated) {
     * for (INode target2 : nodesCreated) {
     * if (!target.equals(target2)) {
     * if (!Collections.disjoint(
     * nodesAndTheirTransition.get(target).getTransitionsAndOLCs().values(),
     * nodesAndTheirTransition.get(target2).getTransitionsAndOLCs().values())) {
     * addXorConstruct(source);
     * return;
     * }
     * }
     * }
     * }
     * addAndConstruct(source);
     * }
     * <p/>
     * private void addXorConstruct(INode source) {
     * addSplitFork(source, Gateway.Type.XOR);
     * }
     * <p/>
     * private void addSplitFork(INode source, Gateway.Type type) {
     * Gateway gateway = new Gateway();
     * gateway.setType(type);
     * ControlFlow flow = new ControlFlow(source, gateway);
     * gateway.addIncomingEdge(flow);
     * source.addOutgoingEdge(flow);
     * for (INode target : enabledNodes) {
     * flow = new ControlFlow(gateway, target);
     * gateway.addOutgoingEdge(flow);
     * if (!incomingEdgesPerNode.containsKey(target)) {
     * incomingEdgesPerNode.put(target, new ArrayList<IEdge>());
     * }
     * incomingEdgesPerNode.get(target).add(flow);
     * }
     * model.addNode(gateway);
     * nodesChecked.add(gateway);
     * }
     * <p/>
     * private void addAndConstruct(INode source) {
     * addSplitFork(source, Gateway.Type.AND);
     * }
     * <p/>
     * // TODO: Reuse existing nodes
     * private void addDataNodesForActivityToModel(INode activity) {
     * for (IEdge edge : activity.getOutgoingEdgesOfType(DataFlow.class)) {
     * model.addNode(edge.getTarget());
     * }
     * for (IEdge edge : activity.getIncomingEdgesOfType(DataFlow.class)) {
     * model.addNode(edge.getTarget());
     * }
     * model.addNode(activity);
     * }
     * <p/>
     * private void updateEnabledStatesFor(INode node) {
     * if (node instanceof Event) {
     * return;
     * }
     * for (IEdge dataFlow : node.getOutgoingEdgesOfType(DataFlow.class)) {
     * DataObjectState currentState = ((DataObject) dataFlow.getTarget()).getState();
     * List<DataObjectState> disabledStates = new LinkedList<>();
     * for (DataObjectState oldState : enabledStates) {
     * for (ObjectLifeCycle olc : synchronizedObjectLifeCycle.getOLCs()) {
     * if (olc.getNodes().contains(oldState) && olc.getNodes().contains(currentState)) {
     * disabledStates.add(oldState);
     * }
     * }
     * }
     * enabledStates.removeAll(disabledStates);
     * enabledStates.add(currentState);
     * }
     * }
     * <p/>
     * private void init() {
     * initializeModelElements();
     * initializeCombinedTransitions();
     * initializeEnabledStates();
     * initializeEnabledCombinedTransitions();
     * }
     * <p/>
     * private void initializeModelElements() {
     * model = new ActivityCentricProcessModel();
     * startEvent = new Event();
     * startEvent.setType(Event.Type.START);
     * model.setStartNode(startEvent);
     * model.addNode(startEvent);
     * nodesToBeChecked.add(startEvent);
     * }
     * <p/>
     * <p/>
     * private void initializeCombinedTransitions() {
     * combinedTransitions = new ArrayList<>();
     * for (ObjectLifeCycle olc : synchronizedObjectLifeCycle.getOLCs()) {
     * for (IEdge transition : olc.getEdgeOfType(StateTransition.class)) {
     * boolean hasBeenAdded = false;
     * for (CombinedTransition combinedTransition : combinedTransitions) {
     * if (combinedTransition.isCombinedTransition((StateTransition) transition)) {
     * combinedTransition.addTransitionAndOLC((StateTransition) transition, olc);
     * hasBeenAdded = true;
     * }
     * }
     * if (!hasBeenAdded) {
     * combinedTransitions.add(new CombinedTransition((StateTransition) transition, olc));
     * }
     * }
     * }
     * }
     * <p/>
     * private void initializeEnabledCombinedTransitions() {
     * enabledCombinedTransitions = new ArrayList<>();
     * for (CombinedTransition combinedTransition : combinedTransitions) {
     * boolean hasToBeEnabled = true;
     * for (StateTransition transition :
     * combinedTransition.getTransitionsAndOLCs().keySet()) {
     * hasToBeEnabled = enabledStates.contains(transition.getSource());
     * }
     * if (hasToBeEnabled) {
     * enabledCombinedTransitions.add(combinedTransition);
     * }
     * }
     * }
     * <p/>
     * private void initializePossibleEnabledCombinedTransitions() {
     * possibleEnabledCombinedTransitions = new LinkedList<>();
     * for (CombinedTransition combinedTransition : combinedTransitions) {
     * for (StateTransition transition :
     * combinedTransition.getTransitionsAndOLCs().keySet()) {
     * if (enabledStates.contains(transition.getSource()) &&
     * !enabledCombinedTransitions.contains(combinedTransition)) {
     * possibleEnabledCombinedTransitions.add(combinedTransition);
     * break;
     * }
     * }
     * }
     * reducePossibleEnabledCombinedTransitions();
     * }
     * <p/>
     * private void reducePossibleEnabledCombinedTransitions() {
     * List<DataObjectState> statesToBeReached = new ArrayList<>();
     * List<CombinedTransition> combinedTransitionsToBeRemoved = new ArrayList<>();
     * for (INode node : enabledNodes) {
     * for (IEdge edge : node.getOutgoingEdgesOfType(DataFlow.class)) {
     * statesToBeReached.add(((DataObject) edge.getTarget()).getState());
     * }
     * }
     * for (CombinedTransition combinedTransition :
     * possibleEnabledCombinedTransitions) {
     * boolean hasToBeRemoved = false;
     * for (StateTransition transition :
     * combinedTransition.getTransitionsAndOLCs().keySet()) {
     * hasToBeRemoved = !statesToBeReached.contains(transition.getSource());
     * if (hasToBeRemoved) {
     * combinedTransitionsToBeRemoved.add(combinedTransition);
     * break;
     * }
     * }
     * }
     * possibleEnabledCombinedTransitions.removeAll(combinedTransitionsToBeRemoved);
     * }
     * <p/>
     * private void initializeEnabledStates() {
     * enabledStates = new HashSet<>();
     * for (ObjectLifeCycle olc : synchronizedObjectLifeCycle.getOLCs()) {
     * enabledStates.add((DataObjectState) olc.getStartNode());
     * }
     * }
     * <p/>
     * private INode createNodeFor(CombinedTransition combinedTransition) {
     * Activity newNode = new Activity();
     * model.addNode(newNode);
     * nodesAndTheirTransition.put(newNode, combinedTransition);
     * if (null == combinedTransition) {
     * newNode.setName("");
     * return newNode;
     * }
     * for (Map.Entry<StateTransition, ObjectLifeCycle> entry :
     * combinedTransition.getTransitionsAndOLCs().entrySet()) {
     * if (null == newNode.getName() || newNode.getName().isEmpty()) {
     * newNode.setName(entry.getKey().getLabel());
     * }
     * DataObject input = new DataObject(entry.getValue().getLabel(),
     * (DataObjectState) entry.getKey().getSource());
     * DataObject output = new DataObject(entry.getValue().getLabel(),
     * (DataObjectState) entry.getKey().getTarget());
     * newNode.addIncomingEdge(new DataFlow(input, newNode));
     * newNode.addOutgoingEdge(new DataFlow(newNode, output));
     * }
     * return newNode;
     * }
     *
     * @Override public <T extends IModel> T convert(IModel model, Class T) {
     * if (T != ActivityCentricProcessModel.class ||
     * !(model instanceof  SynchronizedObjectLifeCycle)) {
     * return null;
     * }
     * return (T)convert((SynchronizedObjectLifeCycle)model);
     * }
     */

    private IModel model;
    private Collection<INode> uncheckedNodes;
    private Collection<INode> checkedNodes;
    private Collection<CombinedTransition> combinedTransitions;
    private SynchronizedObjectLifeCycle synchronizedObjectLifeCycle;
    private Collection<DataObjectState> enabledStates;
    private Collection<CombinedTransition> enabledCombinedTransitions;
    private Collection<CombinedTransition> possibleEnabledCombinedTransitions;
    private INode currentNode;
    private Collection<INode> enabledNodes;
    private List<INode> createdNodes;
    private Map<INode, List<INode>> nopLists;
    private Map<INode, CombinedTransition> combinedTransitionPerActivity;

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
        init();
        do {
            findEnabledCombinedTransitions();
            createNOPActivities();
            createActivities();
            addSplitsAndForksAndControlFlow();
            addJoinsAndMergesAndControlFlow();
            establishEndEvent();
        } while (!uncheckedNodes.isEmpty());
        return null;
    }

    /**
     * This method creates the NOP activities for the current node.
     * Therefor it takes a look at the data output of the current activity.
     * For each state, which is part of the outputand which describes a
     * final state of an data object a NOP activity will be created.
     * The NOP activities will associated to the current activity and
     * then added to the checked nodes.
     */
    private void createNOPActivities() {
        for (ObjectLifeCycle olc : synchronizedObjectLifeCycle.getOLCs()) {
            for (IEdge transition :
                    currentNode.getOutgoingEdgesOfType(StateTransition.class)) {
                if (olc.getFinalStates().contains(transition.getTarget())) {
                    createNOPActivity((DataObjectState) transition.getTarget());
                }
            }
        }
    }

    /**
     * Creates a NOP activity for a give Data Object State.
     * The Date Object State should be a final one.
     * The activity, that has been created will be adde t
     * the model and to the list of checked ndodes.
     * There will nther data input nor data output for this
     * activitity.
     *
     * @param finalState The state which will be used for the creation
     *                   of the activity
     */
    private void createNOPActivity(DataObjectState finalState) {
        Activity nopActivity = new Activity("nop : " + finalState.getName());
        model.addNode(nopActivity);
        if (nopLists.get(currentNode) == null) {
            nopLists.put(currentNode, new ArrayList<INode>());
        }
        nopLists.get(currentNode).add(nopActivity);
    }

    private void establishEndEvent() {
        // TODO implement

    }

    private void addJoinsAndMergesAndControlFlow() {
        // TODO implement

    }

    private void addSplitsAndForksAndControlFlow() {
        // TODO implement
    }

    /**
     * This method creates an activity for each enabled combined transition.
     * The created Activity will be added to the new process model.
     * It will also be added to the activities reachabel from the current node.
     *
     */
    private void createActivities() {
        for (CombinedTransition ect : enabledCombinedTransitions) {
            if (!combinedTransitionPerActivity.values().contains(ect)) {
                createActivityFor(ect);
            }
        }
    }

    /**
     * This methods creates an activity for a given CombinedTransition.
     * The name of the transition will be generated from the name of the
     * state transitions which are part of the combined transition.
     * In addition there will be a input and output data object for each
     * state transition which is part of the combined transition.
     *
     * @param ct The combined transition whih represents the action executed
     *           by the activity.
     */
    private void createActivityFor(CombinedTransition ct) {
        String name = "";
        Activity activity = new Activity();
        for (Map.Entry<StateTransition,ObjectLifeCycle> entry :
                ct.getTransitionsAndOLCs().entrySet()) {
            if (!name.contains(entry.getKey().getLabel())) {
                name = name + entry.getKey().getLabel() + " ";
            }
            DataObject input = new DataObject(entry.getValue().getLabel(),
                    (DataObjectState)entry.getKey().getSource());
            DataObject output = new DataObject(entry.getValue().getLabel(),
                    (DataObjectState)entry.getKey().getSource());
            DataFlow incoming = new DataFlow(input, activity);
            input.addOutgoingEdge(incoming);
            activity.addIncomingEdge(incoming);
            DataFlow outgoing = new DataFlow(activity, output);
            output.addIncomingEdge(outgoing);
            activity.addOutgoingEdge(outgoing);
            model.addNode(input);
            model.addNode(output);
        }
        model.addNode(activity);
        combinedTransitionPerActivity.put(activity, ct);
        name = name.substring(0, name.length() - 2);
        activity.setName(name);
    }

    /**
     * This methods finds all {@link #enabledCombinedTransitions} for
     * the {@link #currentNode}
     * This will be done in two steps first all {@link CombinedTransition}
     * which depend only on the data object states written by the
     * current Activity will be added.
     * Afterwards we determine combined transition which can be enabled
     * by the combined execution of concurrent activiies. Hence we have
     * to take a look at all currently enabled activities.
     */
    private void findEnabledCombinedTransitions() {
        if (!(currentNode instanceof Event)) {
            updateEnabledStates();
        }
        updateEnabledCombinedTransitions();
        updatePossibleEnabledCombinedTransitions();
        reducePossibleEnabledCombinedTransitions();
    }


    /**
     * Find all combined transitions from the possible enabled ones,
     * which are enabled after termination of all enabled nodes.
     * Therefor determine the states of all data objects after
     * termination of the enabled nodes.
     * The algorithm starts with all data objects in the state
     * before the execution of the enabled activities.
     * Next we simulate the execution of each enabled activity
     * and update the state of the data objects according to the
     * output of the activities.
     * No state will be changed twice, because we will never be
     * in the situation that two or more nodes which write the
     * same data object will be enabled at the same time.
     * The result will be directly written to the collection
     * {@link #possibleEnabledCombinedTransitions}.
     */
    private void reducePossibleEnabledCombinedTransitions() {
        Set<INode> statesAfterTermination;
        // Find data objects and their states written by enabled nodes
        statesAfterTermination = findDataStatesWrittenByEnabledNodes();
        // Add states for data objects not written by enabled nodes
        addStatesForDataObjectsNotChanged(statesAfterTermination);
        // Find combined transitions which will not be enabled after termination
        possibleEnabledCombinedTransitions.removeAll(
                findNotEnabledCombinedTransitions(statesAfterTermination));
    }

    /**
     * This methods collects all possible enabled combined trantions,
     * which will not be enabeld after termination of all enabled nodes.
     * Therefor the {@link #possibleEnabledCombinedTransitions} have to be
     * initialized and states which will be availabel after termiantion must
     * be provided.
     *
     * @param statesAfterTermination The data states available ater termiantion
     *                               of all enabled nodes.
     * @return The collection of not possible enabled combined transitions.
     */
    private Collection<CombinedTransition> findNotEnabledCombinedTransitions(
            Set<INode> statesAfterTermination) {
        Collection<CombinedTransition> combinedTransitionsToBeRemoved
                = new HashSet<>();
        for (CombinedTransition pect : possibleEnabledCombinedTransitions) {
            for (StateTransition transition : pect.getTransitions()) {
                if (!statesAfterTermination.contains(transition.getSource())) {
                    combinedTransitionsToBeRemoved.add(pect);
                    break;
                }
            }
        }
        return combinedTransitionsToBeRemoved;
    }

    /**
     * This methods collects all states which will not be altered
     * by the activities/nodes currently enabled. And adds them to
     * the provided Collection of nodes.
     *
     * @param StatesAfterTermination The states which will be altered,
     *                               found states will be added.
     */
    private void addStatesForDataObjectsNotChanged(
            Collection<INode> statesAfterTermination) {
        for (ObjectLifeCycle olc : synchronizedObjectLifeCycle.getOLCs()) {
            Set<INode> possibleStatesOfObject =
                    new HashSet<>(olc.getNodesOfClass(DataObjectState.class));
            possibleStatesOfObject.retainAll(statesAfterTermination);
            if (possibleStatesOfObject.isEmpty()) {
                possibleStatesOfObject =
                        new HashSet<>(olc.getNodesOfClass(DataObjectState.class));
                possibleStatesOfObject.retainAll(enabledStates);
                statesAfterTermination.addAll(possibleStatesOfObject);
            }
        }
    }

    /**
     * Collects the data object states which are currently written.
     * Therefor every enabled node is checked for their data output.
     * This dataoutput will be added to a set and finally returned.
     *
     * @return A set with all data object states created by the enabled
     * activities
     */
    private Set<INode> findDataStatesWrittenByEnabledNodes() {
        Set<INode> statesAfterTermination = new HashSet<>();
        for (INode enabledNode : enabledNodes) {
            for (IEdge outgoingDataFlow :
                    enabledNode.getOutgoingEdgesOfType(DataFlow.class)) {
                statesAfterTermination.add(outgoingDataFlow.getTarget());
            }
        }
        return statesAfterTermination;
    }

    /**
     * This method enables the {@link #possibleEnabledCombinedTransitions}.
     * These combined ransitions describe all transitions which have at least
     * "source state" which is part of the output sets of the current node.
     */
    private void updatePossibleEnabledCombinedTransitions() {
        possibleEnabledCombinedTransitions.clear();
        for (CombinedTransition ct : combinedTransitions) {
            for (StateTransition transition : ct.getTransitions()) {
                if (enabledStates.contains(transition.getSource()) &&
                        !enabledCombinedTransitions.contains(ct)) {
                    possibleEnabledCombinedTransitions.add(ct);
                    break;
                }
            }
        }
    }

    /**
     * This method determines all the {@link #enabledCombinedTransitions}.
     * Therefor it checks for all combined transitions in {@link #combinedTransitions}
     * which contain only state transitions with a source data object state
     * written by the current activity.
     */
    private void updateEnabledCombinedTransitions() {
        enabledCombinedTransitions.clear();
        for (CombinedTransition ct : combinedTransitions) {
            boolean notEnabled = false;
            for (StateTransition transition : ct.getTransitions()) {
                if (notEnabled = !enabledStates.contains(transition.getSource())) {
                    break;
                }
            }
            if (!notEnabled) {
                enabledCombinedTransitions.add(ct);
            }
        }
    }

    private void updateEnabledStates() {
        // TODO implement
    }

    private void init() {
        initCombinedTransitions();
        initSets();
        initActivityCentricProcessModel();
        initEnabledStates();
    }

    private void initEnabledStates() {
        currentNode = model.getStartNode();
        enabledStates = new HashSet<>();
        for (IModel olc : synchronizedObjectLifeCycle.getOLCs()) {
            enabledStates.add((DataObjectState) olc.getStartNode());
        }
    }

    private void initSets() {
        uncheckedNodes = new HashSet<>();
        checkedNodes = new HashSet<>();
        enabledNodes = new HashSet<>();
        combinedTransitions = new ArrayList<>();
        createdNodes = new ArrayList<>();
        nopLists = new HashMap<>();
        combinedTransitionPerActivity = new HashMap<>();
    }

    private void initActivityCentricProcessModel() {
        model = new ActivityCentricProcessModel();
        Event startEvent = new Event();
        startEvent.setType(Event.Type.START);
        model.addNode(startEvent);
        model.setStartNode(startEvent);
        uncheckedNodes.add(startEvent);
    }

    // TODO: Refactor
    private void initCombinedTransitions() {
        for (ObjectLifeCycle olc : synchronizedObjectLifeCycle.getOLCs()) {
            for (IEdge transition : olc.getEdgeOfType(StateTransition.class)) {
                boolean hasBeenAdded = false;
                for (CombinedTransition combinedTransition : combinedTransitions) {
                    if (hasBeenAdded = combinedTransition.isCombinedTransition((StateTransition) transition)) {
                        combinedTransition.addTransitionAndOLC((StateTransition) transition, olc);
                        break;
                    }
                }
                if (!hasBeenAdded) {
                    combinedTransitions.add(new CombinedTransition((StateTransition) transition, olc));
                }
            }
        }
    }

    @Override
    public <T extends IModel> T convert(IModel model, Class T) {
        // TODO implement
        return null;
    }
}
