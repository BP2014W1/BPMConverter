package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.IModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.*;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.*;

import javax.naming.ldap.Control;
import java.util.*;

public class SynchronizedOLCToActivityCentric implements IConverter {
    private SynchronizedObjectLifeCycle synchronizedObjectLifeCycle;
    private List<CombinedTransition> combinedTransitions;
    private List<INode> nodesToBeChecked;
    private List<INode> nodesChecked;
    private Set<INode> enabledNodes;
    private Map<INode, CombinedTransition> nodesAndTheirTransition;
    private Set<DataObjectState> enabledStates;
    private List<CombinedTransition> enabledCombinedTransitions;
    private List<CombinedTransition> possibleEnabledCombinedTransitions;
    private ActivityCentricProcessModel model;
    private Event startEvent;
    private Map<INode, List<IEdge>> incomingEdgesPerNode;

    public SynchronizedOLCToActivityCentric() {
        nodesChecked = new ArrayList<>();
        nodesToBeChecked = new LinkedList<>();
        enabledNodes = new HashSet<>();
        nodesAndTheirTransition = new HashMap<>();
        incomingEdgesPerNode = new HashMap<>();
    }

    public ActivityCentricProcessModel convert(SynchronizedObjectLifeCycle synchronizedOLC) {
        List<INode> nodesCreated = new LinkedList<>();
        synchronizedObjectLifeCycle = synchronizedOLC;
        init();
        do {
            INode node = nodesToBeChecked.get(0);
            updateEnabledStatesFor(node);
            initializeEnabledCombinedTransitions();
            initializePossibleEnabledCombinedTransitions();
            enabledCombinedTransitions.addAll(possibleEnabledCombinedTransitions);
            //nodesCreated.addAll(getAndRemoveExistingNodesFromEnabledOnes(node));
            for (CombinedTransition combinedTransition : enabledCombinedTransitions) {
                if (!nodesAndTheirTransition.containsValue(combinedTransition)) {
                    INode activity = createNodeFor(combinedTransition);
                    addDataNodesForActivityToModel(activity);
                    nodesCreated.add(activity);
                    checkForFinalState(activity);
                    nodesAndTheirTransition.put(activity, combinedTransition);
                } else {
                    for (Map.Entry<INode, CombinedTransition> entry : nodesAndTheirTransition.entrySet()) {
                        if (combinedTransition.equals(entry.getValue())
                                && !enabledNodes.contains(entry.getKey())
                                && !nodesCreated.contains(entry.getKey())) {
                            nodesCreated.add(entry.getKey());
                            break;
                        }
                    }
                }
            }
            nodesToBeChecked.remove(node);
            nodesChecked.add(node);
            enabledNodes.addAll(nodesCreated);
            addControlFlow(node, nodesCreated);
            enabledNodes.removeAll(nodesCreated);
            if (nodesToBeChecked.isEmpty()) {
                enabledNodes.clear();
                nodesToBeChecked.addAll(nodesCreated);
                nodesToBeChecked.removeAll(nodesChecked);
                enabledNodes.addAll(nodesCreated);
                nodesCreated.clear();
            }
        } while (!nodesToBeChecked.isEmpty());
        establishJoinsAndMerges();
        addEndEvent();
        establishJoinsAndMerges();
        return model;
    }

    private Collection getAndRemoveExistingNodesFromEnabledOnes(INode node) {
        Collection<INode> existingNodes = new HashSet<>();
        for (Map.Entry<INode, CombinedTransition> entry : nodesAndTheirTransition.entrySet()) {
            if (enabledCombinedTransitions.contains(entry.getValue())) {
                existingNodes.add(entry.getKey());
                enabledCombinedTransitions.remove(entry.getValue());
            }
        }
        return existingNodes;
    }

    private void addEndEvent() {
        Set<INode> finalActivities = new HashSet<>();
        for (INode node : model.getNodesOfClass(Activity.class)) {
            if (node.getOutgoingEdgesOfType(ControlFlow.class).isEmpty()) {
                finalActivities.add(node);
            }
        }
        Event endEvent = new Event();
        endEvent.setType(Event.Type.END);
        model.addNode(endEvent);
        model.addFinalNode(endEvent);
        List<IEdge> incomingEdges = new LinkedList<>();
        if (finalActivities.isEmpty()) {
            ControlFlow flow = new ControlFlow(startEvent, endEvent);
            startEvent.addOutgoingEdge(flow);
            incomingEdges.add(flow);
        }
        for (INode node : finalActivities) {
            ControlFlow flow = new ControlFlow(node, endEvent);
            node.addOutgoingEdge(flow);
            incomingEdges.add(flow);
        }
        incomingEdgesPerNode.put(endEvent, incomingEdges);
    }

    private void establishJoinsAndMerges() {
        for (Map.Entry<INode, List<IEdge>> entry : incomingEdgesPerNode.entrySet()) {
            if (1 == entry.getValue().size()) {
                    if (entry.getKey().getIncomingEdgesOfType(ControlFlow.class).isEmpty()) {
                        entry.getKey().addIncomingEdge(entry.getValue().get(0));
                    }
            } else {
                    // TODO: make pretty
                    boolean isJoin = false;
                    for (IEdge edge : entry.getValue()) {
                        for (IEdge edge2 : entry.getValue()) {
                            if (!edge.equals(edge2)) {
                                if (!Collections.disjoint(nodesAndTheirTransition.get(
                                                edge.getSource())
                                                .getTransitionsAndOLCs()
                                                .values(),
                                        nodesAndTheirTransition.get(edge2.getSource()).getTransitionsAndOLCs().values())) {
                                    isJoin = true;
                                    break;
                                }
                            }
                        }
                        if (isJoin) {
                            break;
                        }
                    }
                    if (isJoin) {
                        createJoin(entry.getKey());
                    } else {
                        createMerge(entry.getKey());
                    }
                    incomingEdgesPerNode.put(entry.getKey(), new LinkedList<>(entry.getKey().getIncomingEdgesOfType(ControlFlow.class)));
            }
        }
    }

    private void createJoin(INode key) {
        createGatewayJoin(key, Gateway.Type.XOR);
    }

    private void createMerge(INode key) {
        createGatewayJoin(key, Gateway.Type.AND);
    }

    private void createGatewayJoin(INode target, Gateway.Type type) {
        Gateway join = new Gateway();
        join.setType(type);
        for (IEdge edge : incomingEdgesPerNode.get(target)) {
            join.addIncomingEdge(edge);
            edge.setTarget(join);
        }
        ControlFlow flow = new ControlFlow(join, target);
        target.addIncomingEdge(flow);
        join.addOutgoingEdge(flow);
        model.addNode(join);
    }

    private void checkForFinalState(INode activity) {

    }

    private void addControlFlow(INode source) {
        if (1 >= enabledNodes.size()) {
            for (INode target : enabledNodes) {
                ControlFlow flow = new ControlFlow(source, target);
                source.addOutgoingEdge(flow);
                if (!incomingEdgesPerNode.containsKey(target)) {
                    incomingEdgesPerNode.put(target, new ArrayList<IEdge>());
                }
                incomingEdgesPerNode.get(target).add(flow);
            }
            return;
        }
        for (INode target : enabledNodes) {
            for (INode target2 : enabledNodes) {
                if (!target.equals(target2)) {
                    if (!Collections.disjoint(
                            nodesAndTheirTransition.get(target).getTransitionsAndOLCs().keySet(),
                            nodesAndTheirTransition.get(target2).getTransitionsAndOLCs().keySet())) {
                        addXorConstruct(source);
                        return;
                    }
                }
            }
        }
        addAndConstruct(source);
    }

    private void addControlFlow(INode source, Collection<INode> nodesCreated) {
        if (1 >= nodesCreated.size()) {
            for (INode target : nodesCreated) {
                ControlFlow flow = new ControlFlow(source, target);
                source.addOutgoingEdge(flow);
                if (!incomingEdgesPerNode.containsKey(target)) {
                    incomingEdgesPerNode.put(target, new ArrayList<IEdge>());
                }
                incomingEdgesPerNode.get(target).add(flow);
            }
            return;
        }
        for (INode target : nodesCreated) {
            for (INode target2 : nodesCreated) {
                if (!target.equals(target2)) {
                    if (!Collections.disjoint(
                            nodesAndTheirTransition.get(target).getTransitionsAndOLCs().values(),
                            nodesAndTheirTransition.get(target2).getTransitionsAndOLCs().values())) {
                        addXorConstruct(source);
                        return;
                    }
                }
            }
        }
        addAndConstruct(source);
    }

    private void addXorConstruct(INode source) {
        addSplitFork(source, Gateway.Type.XOR);
    }

    private void addSplitFork(INode source, Gateway.Type type) {
        Gateway gateway = new Gateway();
        gateway.setType(type);
        ControlFlow flow = new ControlFlow(source, gateway);
        gateway.addIncomingEdge(flow);
        source.addOutgoingEdge(flow);
        for (INode target : enabledNodes) {
            flow = new ControlFlow(gateway, target);
            gateway.addOutgoingEdge(flow);
            if (!incomingEdgesPerNode.containsKey(target)) {
                incomingEdgesPerNode.put(target, new ArrayList<IEdge>());
            }
            incomingEdgesPerNode.get(target).add(flow);
        }
        model.addNode(gateway);
        nodesChecked.add(gateway);
    }

    private void addAndConstruct(INode source) {
        addSplitFork(source, Gateway.Type.AND);
    }

    // TODO: Reuse existing nodes
    private void addDataNodesForActivityToModel(INode activity) {
        for (IEdge edge : activity.getOutgoingEdgesOfType(DataFlow.class)) {
            model.addNode(edge.getTarget());
        }
        for (IEdge edge : activity.getIncomingEdgesOfType(DataFlow.class)) {
            model.addNode(edge.getTarget());
        }
        model.addNode(activity);
    }

    private void updateEnabledStatesFor(INode node) {
        if (node instanceof Event) {
            return;
        }
        for (IEdge dataFlow : node.getOutgoingEdgesOfType(DataFlow.class)) {
            DataObjectState currentState = ((DataObject) dataFlow.getTarget()).getState();
            List<DataObjectState> disabledStates = new LinkedList<>();
            for (DataObjectState oldState : enabledStates) {
                for (ObjectLifeCycle olc : synchronizedObjectLifeCycle.getOLCs()) {
                    if (olc.getNodes().contains(oldState) && olc.getNodes().contains(currentState)) {
                        disabledStates.add(oldState);
                    }
                }
            }
            enabledStates.removeAll(disabledStates);
            enabledStates.add(currentState);
        }
    }

    private void init() {
        initializeModelElements();
        initializeCombinedTransitions();
        initializeEnabledStates();
        initializeEnabledCombinedTransitions();
    }

    private void initializeModelElements() {
        model = new ActivityCentricProcessModel();
        startEvent = new Event();
        startEvent.setType(Event.Type.START);
        model.setStartNode(startEvent);
        model.addNode(startEvent);
        nodesToBeChecked.add(startEvent);
    }


    private void initializeCombinedTransitions() {
        combinedTransitions = new ArrayList<>();
        for (ObjectLifeCycle olc : synchronizedObjectLifeCycle.getOLCs()) {
            for (IEdge transition : olc.getEdgeOfType(StateTransition.class)) {
                boolean hasBeenAdded = false;
                for (CombinedTransition combinedTransition : combinedTransitions) {
                    if (combinedTransition.isCombinedTransition((StateTransition) transition)) {
                        combinedTransition.addTransitionAndOLC((StateTransition) transition, olc);
                        hasBeenAdded = true;
                    }
                }
                if (!hasBeenAdded) {
                    combinedTransitions.add(new CombinedTransition((StateTransition) transition, olc));
                }
            }
        }
    }

    private void initializeEnabledCombinedTransitions() {
        enabledCombinedTransitions = new ArrayList<>();
        for (CombinedTransition combinedTransition : combinedTransitions) {
            boolean hasToBeEnabled = true;
            for (StateTransition transition :
                    combinedTransition.getTransitionsAndOLCs().keySet()) {
                hasToBeEnabled = enabledStates.contains(transition.getSource());
            }
            if (hasToBeEnabled) {
                enabledCombinedTransitions.add(combinedTransition);
            }
        }
    }

    private void initializePossibleEnabledCombinedTransitions() {
        possibleEnabledCombinedTransitions = new LinkedList<>();
        for (CombinedTransition combinedTransition : combinedTransitions) {
            for (StateTransition transition :
                    combinedTransition.getTransitionsAndOLCs().keySet()) {
                if (enabledStates.contains(transition.getSource()) &&
                        !enabledCombinedTransitions.contains(combinedTransition)) {
                    possibleEnabledCombinedTransitions.add(combinedTransition);
                    break;
                }
            }
        }
        reducePossibleEnabledCombinedTransitions();
    }

    private void reducePossibleEnabledCombinedTransitions() {
        List<DataObjectState> statesToBeReached = new ArrayList<>();
        List<CombinedTransition> combinedTransitionsToBeRemoved = new ArrayList<>();
        for (INode node : enabledNodes) {
            for (IEdge edge : node.getOutgoingEdgesOfType(DataFlow.class)) {
                statesToBeReached.add(((DataObject) edge.getTarget()).getState());
            }
        }
        for (CombinedTransition combinedTransition :
                possibleEnabledCombinedTransitions) {
            boolean hasToBeRemoved = false;
            for (StateTransition transition :
                    combinedTransition.getTransitionsAndOLCs().keySet()) {
                hasToBeRemoved = !statesToBeReached.contains(transition.getSource());
                if (hasToBeRemoved) {
                    combinedTransitionsToBeRemoved.add(combinedTransition);
                    break;
                }
            }
        }
        possibleEnabledCombinedTransitions.removeAll(combinedTransitionsToBeRemoved);
    }

    private void initializeEnabledStates() {
        enabledStates = new HashSet<>();
        for (ObjectLifeCycle olc : synchronizedObjectLifeCycle.getOLCs()) {
            enabledStates.add((DataObjectState) olc.getStartNode());
        }
    }

    private INode createNodeFor(CombinedTransition combinedTransition) {
        Activity newNode = new Activity();
        model.addNode(newNode);
        nodesAndTheirTransition.put(newNode, combinedTransition);
        if (null == combinedTransition) {
            newNode.setName("");
            return newNode;
        }
        for (Map.Entry<StateTransition, ObjectLifeCycle> entry :
                combinedTransition.getTransitionsAndOLCs().entrySet()) {
            if (null == newNode.getName() || newNode.getName().isEmpty()) {
                newNode.setName(entry.getKey().getLabel());
            }
            DataObject input = new DataObject(entry.getValue().getLabel(),
                    (DataObjectState) entry.getKey().getSource());
            DataObject output = new DataObject(entry.getValue().getLabel(),
                    (DataObjectState) entry.getKey().getTarget());
            newNode.addIncomingEdge(new DataFlow(input, newNode));
            newNode.addOutgoingEdge(new DataFlow(newNode, output));
        }
        return newNode;
    }

    @Override
    public <T extends IModel> T convert(IModel model, Class T) {
        if (T != ActivityCentricProcessModel.class ||
               !(model instanceof  SynchronizedObjectLifeCycle)) {
            return null;
        }
        return (T)convert((SynchronizedObjectLifeCycle)model);
    }
}
