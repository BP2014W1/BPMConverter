package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.*;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.CombinedTransition;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize.SynchronizedObjectLifeCycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;


/**
 * This class implements the Builder pattern for Activities.
 * It used to convert a {@link SynchronizedObjectLifeCycle} into
 * an {@link ActivityCentricProcessModel}.
 * It enables you to define a {@link Activity} and to relate it
 * to other ones.
 * In the end you may receive that activity and establish incoming
 * and outgoing data flow. In addition you can generate gateways and
 * control flow preceding or succeeding the activity.
 */
public class ActivityBuilder {
    /**
     * The flyweight object, which holds the data shared among all
     * builder objects of one Converter.
     */
    private OLCConversionFlyweight flyweight;
    /**
     * This {@link CombinedTransition} represents the Transition
     * executed by the activity.
     */
    private final CombinedTransition ctExecuted;
    /**
     * The Activity which is created and initialized by the builder.
     * Outgoing and incoming control flow will only be set during the
     * {@link #build() method.
     */
    private Activity activity;
    /**
     * The incoming data flow of the Activity. It will automatically
     * connected to the activity.
     */
    private Collection<DataFlow> incomingDataFlow;
    /**
     * The outgoing data flow of the Activity. It will automatically
     * connected to the activity.
     */
    private Collection<DataFlow> outgoingDataFlow;
    /**
     * This Collection hold ActivityBuilder objects for each successing
     * Activity.
     */
    private Collection<ActivityBuilder> successorActivities;
    /**
     * Saves the concurrent combined transition, to determine
     * the states after termination.
     */
    private Collection<CombinedTransition> concurrentCTs;
    private Collection<ActivityBuilder> predecessors;

    public Collection<CombinedTransition> getPets() {
        return pets;
    }

    public Collection<Activity> getNopActivities() {
        return nopActivities;
    }

    public Collection<CombinedTransition> getEnabledCTs() {
        return enabledCTs;
    }

    /**
     * The incoming control flow of the Activity.
     * It will established/ connected to the activity during the execution
     * of the build method.

     */
    private Collection<ControlFlow> incomingControlFlow;
    /**
     * The outgoing control flow of the Activity.
     * It will established/ connected to the activity during the execution
     * of the build method.
     */
    private Collection<ControlFlow> outgoingControlFlow;
    /**
     * The list of enabled combined transitions.
     * This list contains all combined transitions which can be executed based
     * on the data objects and data object states written during the execution
     * of the activity represented/ created by the builder
     * {@link #outgoingDataFlow}.
     */
    private Collection<CombinedTransition> enabledCTs;
    /**
     * A collection of all states available before executing this task.
     * The {@link DataObjectState} Objects are needed in order to determine
     * the enabled combined Transition. Some of them may be used as input
     * to the activity and will be written in another state.
     */
    private Collection<DataObjectState> availableStates;

    /**
     * Indicates weather or not the node has been checked.
     */
    private boolean isChecked = false;
    private Collection<Activity> nopActivities;
    private Collection<CombinedTransition> pets;

    /**
     * Creates a new ActivityBuilder object for a given context represented
     * by the given {@link OLCConversionFlyweight} Object and a given
     * {@link CombinedTransition}. In addition we provide a set of available
     * {@link DataObjectState} Objects, that represent the Data Objects and
     * available before the execution of the Task.
     *
     * @param flyweight       The flyweight which holds shared resources.
     *                        The resources are necessary for the creation.
     *                        State changes of the flyweight object may affect
     *                        changes to the activity.
     * @param availableStates A Collection of DataObjectStates which represent
     *                        the Data Objects available before the execution of
     *                        the Activity represented by this Activity Builder.
     *                        To reduce the risk of external mainpulation they
     *                        will aggregated into a new set.
     * @param ctExecuted      The CombinedTransition executed by this activity.
     *                        Every {@link Activity} is represented by one
     *                        {@link CombinedTransition} and vice versa.
     *                        <p/>
     * Pre: * Both parameters must not be null.
     */
    public ActivityBuilder(
            OLCConversionFlyweight<ActivityCentricProcessModel> flyweight,
            Collection<DataObjectState> availableStates,
            CombinedTransition ctExecuted) {
        assert flyweight != null : "The flyweight must not be null";
        assert ctExecuted != null : "The combined transition must not be null";
        assert availableStates != null : "The collection of available states must not be null";
        this.flyweight = flyweight;
        this.ctExecuted = ctExecuted;
        this.availableStates = new HashSet<>(availableStates);
        initialize();
    }

    /**
     * This method initializes attributes.
     * All initializations must not base on any other parameters than
     * the ones given to the constructor, or by elements initalized before.
     */
    private void initialize() {
        initializeActivity();
        initializeDataFlow();
        initializeNOPActivities();
        predecessors = new HashSet<>();
    }


    /**
     * Creates the activity.
     * The name will be initialized using a string accumulated
     * from the different transitions being part of the combined transition
     * executed by this activity.
     */
    private void initializeActivity() {
        String name = "";
        for (StateTransition transition : ctExecuted.getTransitions()) {
            if (!name.contains(transition.getLabel())) {
                name = name + transition.getLabel() + ", ";
            }
        }
        name = name.substring(0, name.length() - 2);
        activity = new Activity(name);
    }

    /**
     * Initializes the data flow of the activity created by this builder.
     * Therefor it checks all transitions aggregated by the combined transition.
     * For each transition an input and Data Object, with the state according to
     * the label and state of the Data Object State which is source/target of the
     * data flow will be added.
     */
    private void initializeDataFlow() {
        assert activity != null : "The activity has to be initialized first";
        incomingDataFlow = new HashSet<>();
        outgoingDataFlow = new HashSet<>();
        for (StateTransition transition : ctExecuted.getTransitions()) {
            addIncomingDataFlow((DataObjectState) transition.getSource());
            addOutgoingDataFlow((DataObjectState) transition.getTarget());
        }
    }

    /**
     * Adds a new {@link DataFlow} with the activity as source
     * and a {@link DataObject} with the specified state as target.
     *
     * @param target The {@link DataObjectState} for the {@link DataObject}.
     *               <p/>
     *               Pre: {@link #incomingDataFlow} must be initalized
     */
    private void addOutgoingDataFlow(DataObjectState target) {
        DataFlow dataFlow = new DataFlow(
                activity,
                flyweight.getDataobjectForState(target));
        flyweight.getDataobjectForState(target).addIncomingEdge(dataFlow);
        outgoingDataFlow.add(dataFlow);
        activity.addOutgoingEdge(dataFlow);
    }


    /**
     * Adds a new {@link DataFlow} with a {@link DataObject} as source
     * and the {@link #activity} as target.
     *
     * @param source The {@link DataObjectState} for the {@link DataObject}.
     *               <p/>
     *               Pre: {@link #outgoingDataFlow} must be initalized
     */
    private void addIncomingDataFlow(DataObjectState source) {
        DataFlow dataFlow = new DataFlow(
                flyweight.getDataobjectForState(source),
                activity);
        flyweight.getDataobjectForState(source).addOutgoingEdge(dataFlow);
        incomingDataFlow.add(dataFlow);
        activity.addIncomingEdge(dataFlow);
    }

    /**
     * This method initialized the enabled combined transition.
     *
     * @return The current {@link ActivityBuilder} object will be returned
     * to use method chaining.
     * <p/>
     * Pre: * The flyweight Object must be set.
     * * The outgoing Data Flow must be set.
     */
    public ActivityBuilder findEnabledCombinedTransitions(
            Collection<DataObjectState> availableStates) {
        // FIXME: fix the ct type issue
        // Pre-Condition: Variables have to be set
        assert flyweight != null : "The flyweight has not been set";
        assert outgoingDataFlow != null : "The DataFlow has to be set";
        this.availableStates = new HashSet<>(availableStates);
        for (Object element : flyweight.getCombinedTransitions()) {
            CombinedTransition ct = (CombinedTransition)element;
            if (activityEnablesCombinedTransition(ct)) {
                enabledCTs.add(ct);
            }
            enabledCTs.add(ct);
        }
        return this;
    }

    /**
     * This method initialized the enabled combined transition.
     *
     * @return The current {@link ActivityBuilder} object will be returned
     * to use method chaining.
     * <p/>
     * Pre: * The flyweight Object must be set.
     * * The outgoing Data Flow must be set.
     */
    public ActivityBuilder findEnabledCombinedTransitions() {
        // FIXME: fix the ct type issue
        // Pre-Condition: Variables have to be set
        assert flyweight != null : "The flyweight has not been set";
        assert outgoingDataFlow != null : "The DataFlow has to be set";
        assert availableStates != null : "availableStates must be set";
        enabledCTs = new HashSet<>();
        for (Object element : flyweight.getCombinedTransitions()) {
            CombinedTransition ct = (CombinedTransition)element;
            if (activityEnablesCombinedTransition(ct)) {
                enabledCTs.add(ct);
            }
        }
        return this;
    }



    /**
     * Initializes {@link #pets}.
     * Combined transitions which depend in parts on Data Objects and
     * states written by the activity represented by this builder.
     *
     * ALso if the concurrentCTs have been set beforehand, the parameter
     * will be ignored.
     *
     * The set will be disjoint to {@link #enabledCTs}.
     * Afterwards the collection has to be reduced in order to determine
     * which combined transitions will be enabled.
     * Afterwards they will be reduced.
     *
     * @param concurrentCTs A Collection of Combined Transitions indicating
     *                      activities which are concurrent to this activity.
     *
     * Pre: * {@link #flyweight} must have been initialized.
     *      * {@link #enabledCTs} must have been initialized.
     */
    public ActivityBuilder findPossibleEnabledCombinedTransitions(
            Collection<CombinedTransition> concurrentCTs) {
        assert flyweight != null : "The flyweight must be initialized";
        assert enabledCTs != null : "The enabledCTs must be initialized";
        if (this.concurrentCTs == null) {
            assert concurrentCTs != null : "The parameter must not be null.";
            concurrentCTs.remove(this);
            this.concurrentCTs = new HashSet<>(concurrentCTs);
        }
        Collection<DataObjectState> statesAfterTermination =
                getStatesAfterConcurrentActivities(concurrentCTs);
        pets = new HashSet<>();
        for (Object element : flyweight.getCombinedTransitions()) {
            CombinedTransition ct = (CombinedTransition)element;
            if (isPossibleEnabledTransition(ct)) {
                pets.add(ct);
            }
        }
        Collection<CombinedTransition> ctsToBeRemoved = new HashSet<>();
        for (CombinedTransition pet : pets) {
            if (!pet.isEnabledForStates(statesAfterTermination)) {
                ctsToBeRemoved.add(pet);
            }
        }
        pets.removeAll(ctsToBeRemoved);
        markAsChecked();
        enabledCTs.removeAll(this.concurrentCTs);
        pets.removeAll(this.concurrentCTs);
        return this;
    }

    /**
     * A combined transition is a possible combined transition if and only if
     * it is not an enabled transition and if at least one transition which is
     * part of the combined transition will be enabled by termination this
     * activity.
     *
     * @param ct The combined Transition which will be chekced weather or not
     *           it is a a possible enabled combined transition.
     * @return True if the CombinedTransition ct is a possible enabled combined
     * transition else false.
     *
     * TODO: This method may be optimized: Iterate over the OLCs - the condition must hold for every shared OLC
     */
    private boolean isPossibleEnabledTransition(CombinedTransition ct) {
        if (enabledCTs.contains(ct)) {
            return false;
        }
        for (StateTransition transition : ct.getTransitions()) {
            for (StateTransition transition1 : ctExecuted.getTransitions()) {
                if (transition.getSource().equals(transition1.getTarget())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This methods determines all data states which available
     * after the termination of this and all concurrent activities.
     * They are needed to reduce the possible enabled Combined Transitions.
     * @param concurrentCTs The Combined Transitions which represent activities
     *                      concurrent to this one.
     * @return A Collection of all states concurrent to this one.
     */
    private Collection<DataObjectState> getStatesAfterConcurrentActivities(
            Collection<CombinedTransition> concurrentCTs) {
        this.concurrentCTs = new HashSet<>(concurrentCTs);
        Collection<DataObjectState> statesAfter =
                new HashSet<>(statesAvailableAfterTermination());
        for (CombinedTransition ct : concurrentCTs) {
            for (StateTransition transition : ct.getTransitions()) {
                statesAfter.remove(transition.getSource());
                statesAfter.add((DataObjectState) transition.getTarget());
            }
        }
        return statesAfter;
    }

    private Collection<DataObjectState> getStatesAfterConcurrentActivities() {
        Collection<DataObjectState> statesAfter =
                new HashSet<>(statesAvailableAfterTermination());
        for (CombinedTransition ct : concurrentCTs) {
            for (StateTransition transition : ct.getTransitions()) {
                statesAfter.remove(transition.getSource());
                statesAfter.add((DataObjectState) transition.getTarget());
            }
        }
        return statesAfter;
    }

    /**
     * This method determines if a combined transition will enabled by the
     * activity.
     * This means all {@link DataObjectState} which have to be available at
     * the start of the combined transition are written by the activity
     * represented by this activity builder.
     *
     * @param ct The combined transition to be checked.
     * @return True if it will be enabled else false.
     * <p/>
     * Pre: {@link #ctExecuted} has to be enabled.
     */
    private boolean activityEnablesCombinedTransition(CombinedTransition ct) {
        Collection<DataObjectState> statesAfterTermination =
                statesAvailableAfterTermination();
        for (StateTransition transition : ct.getTransitions()) {
            if (!statesAfterTermination.contains(transition.getSource())) {
                return false;
            }
        }
        return true;
    }

    private Collection<StateTransition> getEnabledTransitions(
                Collection<DataObjectState> enabledStates) {
            Collection<StateTransition> enabledTransitions = new LinkedList<>();
            for (DataObjectState state : enabledStates) {
                for (IEdge transition :
                        state.getIncomingEdgesOfType(StateTransition.class)) {
                    enabledTransitions.add((StateTransition) transition);
                }
            }
        return enabledTransitions;
    }

    /**
     * Returns a collection of {@link DataObjectState}.
     * Those states represent the {@link DataObject} available after
     * termination of the activity represented by this Builder.
     *
     * @return A Collection containing all States available after termination.
     */
    private Collection<DataObjectState> statesAvailableAfterTermination() {
        Collection<DataObjectState> states = new HashSet<>(availableStates);
        for (StateTransition transition : ctExecuted.getTransitions()) {
            states.remove(transition.getSource());
            states.add((DataObjectState) transition.getTarget());
        }
        return states;
    }

    /**
     * ializes the No Operation activities for the activity.
     * The NOP Activities will automatically marked as checked.
     * Also an empty combined transition will be added.
     * All NOP Activities are saved centralized inside the flyweight
     * to prevent multiple NOP Activities for the same DataObjectState.
     */
    private void initializeNOPActivities() {
        nopActivities = new HashSet<>();
        if (outgoingControlFlow == null) {
            outgoingControlFlow = new HashSet<>();
        }
        for (StateTransition transition : ctExecuted.getTransitions()) {
            Activity nopActivity = flyweight
                    .getNOPActivityForState(
                            (DataObjectState) transition.getTarget());
            if (null != nopActivity) {
                nopActivities.add(nopActivity);
//                ControlFlow cf = new ControlFlow(activity, nopActivity);
//               flyweight.addIncomingEdgeFor(nopActivity, cf);
//                outgoingControlFlow.add(cf);
                //flyweight.addIncomingEdgeFor(nopActivity, cf);
            }
        }
    }

    /**
     * This method determines if the output sets of different
     * {@link ActivityBuilder} are disjoint.
     * Therefore it compares the {@link de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle}
     * of each Output Data Object.
     *
     * @param otherActivity The ActivityBuilder Object to Compare this Object with.
     *
     * @return true if both ActivityBuilders have only disjoint outputsets.
     */
    public boolean outputSetsAreDisjoint(ActivityBuilder otherActivity) {
        for (DataFlow dataFlow : outgoingDataFlow) {
            for (DataFlow otherFlow : otherActivity.outgoingDataFlow) {
                if (((DataObject)dataFlow.getTarget()).getOlc()
                        .equals(((DataObject) otherFlow.getTarget()).getOlc())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This method determines if the input sets of different
     * {@link ActivityBuilder} are disjoint.
     * Therefore it will compare the {@link de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle}
     * of each Input Data Object.
     *
     * @param otherActivity The ActivityBuilder Object to Compare this Object with.
     *
     * @return true if both ActivityBuilders have only disjoint input sets.
     */
    public boolean inputSetsAreDisjoint(ActivityBuilder otherActivity) {
        for (DataFlow dataFlow : incomingDataFlow) {
            for (DataFlow otherFlow : otherActivity.incomingDataFlow) {
                if (((DataObject)dataFlow.getSource()).getOlc()
                        .equals(((DataObject) otherFlow.getSource()).getOlc())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This method builds the activity, represented by this builder.
     * ControlFlow will be established.
     *
     * @return The newly created and initialized activity.
     */
    public Activity build() {
        assert 1 == incomingControlFlow.size() :
                "Each activity should have exatly one incoming edge";
        assert outgoingControlFlow == null || 1 >= outgoingControlFlow.size() :
                "Each activity should have no more than one incoming edge";
        if (outgoingControlFlow == null) {
            outgoingControlFlow = new HashSet<>();
        }
        for (ControlFlow flow : incomingControlFlow) {
            activity.addIncomingEdge(flow);
        }
        for (ControlFlow flow : outgoingControlFlow) {
            activity.addOutgoingEdge(flow);
        }
        return activity;
    }

    public ControlFlow addPredecessor(Event startEvent) {
        ControlFlow incoming = new ControlFlow(startEvent, activity);
        if (incomingControlFlow == null) {
            incomingControlFlow = new HashSet<>();
        }
        incomingControlFlow.add(incoming);
        startEvent.addOutgoingEdge(incoming);
        return incoming;
    }

    public ControlFlow addPredecessor(Gateway gateway) {
        ControlFlow incoming = new ControlFlow(gateway, activity);
        if (incomingControlFlow == null) {
            incomingControlFlow = new ArrayList<>();
        }
        incomingControlFlow.add(incoming);
        gateway.addOutgoingEdge(incoming);
        INode predecessor = gateway;
        do {
            predecessor = predecessor.getIncomingEdges().get(0).getSource();
        } while (!(predecessor instanceof Activity ||
                predecessor instanceof Event));
        if (predecessor instanceof Activity) {
            predecessors.add(getActivityBuilderForActivity((Activity)predecessor));
        }
        return incoming;
    }

    /**
     * Returns the ActivityBuilder of an activity by accessing the flyweight object.
     * @param predecessor The Activity
     * @return The received Activity Builder.
     */
    private ActivityBuilder getActivityBuilderForActivity(Activity predecessor) {
        for (Object builder : flyweight.getActivityBuilders()) {
            if (((ActivityBuilder)builder).activity.equals(predecessor)) {
                return (ActivityBuilder)builder;
            }
        }
        return null;
    }

    public ControlFlow addPredecessor(ActivityBuilder builder) {
        ControlFlow incoming = new ControlFlow(builder.activity, this.activity);
        if (incomingControlFlow == null) {
            incomingControlFlow = new ArrayList<>();
        }
        incomingControlFlow.add(incoming);
        predecessors.add(builder);
        if (builder.outgoingControlFlow == null) {
            builder.outgoingControlFlow = new HashSet<>();
        }
        builder.outgoingControlFlow.add(incoming);
        return incoming;
    }

    // # _____GETTER & SETTER & TOGGLE_____

    /**
     * Indicates weather or not the node has been checked.
     *
     * @return The state of the activity.
     */
    public boolean isChecked() {
        return isChecked;
    }

    /**
     * Marks the state of the activity as checked.
     * This method can not be undone.
     */
    public void markAsChecked() {
        this.isChecked = true;
    }

    public CombinedTransition getCtExecuted() {
        return ctExecuted;
    }

    /**
     * Returns a collection of all ActivityBuilder instances which
     * represent a successor to the system.
     *
     * @return A new Collection. Means Changes to the collection will
     * not affect the state of the activity builder.
     */
    public Collection<ActivityBuilder> getSuccessorActivities() {
        if (successorActivities == null) {
            initSuccessorActivities();
        }
        return new HashSet<>(successorActivities);
    }

    /**
     * This method initializes the list of successor Activities.
     *
     * Pre: {@link #pets} and {@link #enabledCTs} must be initialized.
     */
    private void initSuccessorActivities() {
        assert pets != null :  "pets have to be initialized";
        assert enabledCTs != null : "enabledCTs have to be initialized";
        Collection<DataObjectState> states = statesAvailableAfterTermination();
        successorActivities = new HashSet<>();
        for (CombinedTransition ct : enabledCTs) {
            successorActivities.add(flyweight.getActivityBuilderFor(ct, states));
        }
        states = getStatesAfterConcurrentActivities();
        for (CombinedTransition ct : pets) {
            successorActivities.add(flyweight.getActivityBuilderFor(ct, states));
        }
    }

    /**
     * Determine outgoing control flow.
     *
     * Pre: The Successors have to be initialized.
     */
    public void establishOutgoingControlFlow() {
        assert successorActivities != null :
                "The successorActivities have to be initialized";
        if (outgoingControlFlow == null) {
            outgoingControlFlow = new HashSet<>();
        }
        if (nopActivities.size() + successorActivities.size() == 1) {
            if (nopActivities.isEmpty()) {
                outgoingControlFlow.add(
                        successorActivities.iterator().next().addPredecessor(this));
            } else {
                ControlFlow cf = new ControlFlow(activity,
                        nopActivities.iterator().next());
                flyweight.addIncomingEdgeFor(
                        nopActivities.iterator().next(), cf);
                outgoingControlFlow.add(cf);
            }
        } else if (nopActivities.isEmpty() && successorsAreDisjoint()) {
            Gateway and = new Gateway();
            and.setType(Gateway.Type.AND);
            ControlFlow outgoing = new ControlFlow(activity, and);
            outgoingControlFlow.add(outgoing);
            and.addIncomingEdge(outgoing);
            for (ActivityBuilder successor : successorActivities) {
                ControlFlow cf = new ControlFlow(and, successor.activity);
                successor.incomingControlFlow.add(cf);
                and.addOutgoingEdge(cf);
            }
            flyweight.getModelUnderConstruction().addNode(and);
        } else {
            Gateway xor = new Gateway();
            xor.setType(Gateway.Type.XOR);
            ControlFlow outgoing = new ControlFlow(activity, xor);
            xor.addIncomingEdge(outgoing);
            outgoingControlFlow.clear();
            outgoingControlFlow.add(outgoing);
            for (Activity nopActivity : nopActivities) {
                ControlFlow cf = new ControlFlow(xor, nopActivity);
                // FIXME: Maybe the list has to be cleared before
                flyweight.addIncomingEdgeFor(nopActivity, cf);
                xor.addOutgoingEdge(cf);
            }
            for (ActivityBuilder successor : successorActivities) {
                xor.addOutgoingEdge(successor.addPredecessor(xor));
            }
            flyweight.getModelUnderConstruction().addNode(xor);
        }
    }


    /**
     * Checks weather or not the transitions of the ActivityBuilder Objects,
     * representing the successors are disjoint or not.
     *
     * @return true if they are disjoint else if not.
     */
    private boolean successorsAreDisjoint() {
        for (ActivityBuilder node1 : successorActivities) {
            for (ActivityBuilder node2 : successorActivities) {
                if (!node1.inputSetsAreDisjoint(node2)) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * This method establishs the incoming control flow.
     *
     * Means if this activity has more than one incoming edge
     * an gateway will be established.
     */
    public void establishIncomingControlFlow() {
        if (incomingControlFlow.size() > 1) {
            Collection<ControlFlow> newFlow = new HashSet<>();
            Gateway gateway = new Gateway();
            if (incomingControlFlowHasDisjointDataOutput()) {
                gateway.setType(Gateway.Type.AND);
            } else {
                gateway.setType(Gateway.Type.XOR);
            }
            for (ControlFlow flow : incomingControlFlow) {
                flow.setTarget(gateway);
                gateway.addIncomingEdge(flow);
            }
            ControlFlow newFlowObject = new ControlFlow(gateway, activity);
            gateway.addOutgoingEdge(newFlowObject);
            newFlow.add(newFlowObject);
            flyweight.getModelUnderConstruction().addNode(gateway);
            incomingControlFlow = newFlow;
        }
    }

    /**
     * Determines if the DataOutputSets of the incoming dataFlow are
     * disjoint.
     *
     * @return Returns true if the preceding activities have disjoint
     * data outputs, false else.
     */
    private boolean incomingControlFlowHasDisjointDataOutput() {
        for (ActivityBuilder predecessor : predecessors) {
            for (ActivityBuilder predecessor2 : predecessors) {
                if (!predecessor.equals(predecessor2) &&
                        !predecessor.outputSetsAreDisjoint(predecessor2)) {
                    return false;
                }
            }
        }
        return true;
    }
}