package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.*;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.CombinedTransition;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize.SynchronizedObjectLifeCycle;
import javafx.animation.Transition;

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
     * exectued by the activity.
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
     * Indicates weather or not the node has been chcked.
     */
    private boolean isChecked = false;
    private HashSet<Activity> nopActivities;

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
        this.availableStates = new HashSet<>(availableStates);
        this.ctExecuted = ctExecuted;
        initalize();
    }

    /**
     * This method initalizes attributes.
     * All initalizations must not base on any other parameters than
     * the ones given to the constructor, or by elements initalized before.
     */
    private void initalize() {
        initalizeActivity();
        initalizeDataFlow();
    }


    /**
     * Creates the activity.
     * The name will be initalized using a string accumulated
     * from the different transitions being part of the combined transition
     * executed by this activity.
     */
    private void initalizeActivity() {
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
     * Initalizes the data flow of the activity created by this builder.
     * Therefor it checks all transitions aggregated by the combined transition.
     * For each transition an input and Data Object, with the state according to
     * the label and state of the Data Object State which is source/target of the
     * data flow will be added.
     */
    private void initalizeDataFlow() {
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
     * and a {@link DataObject} with th specifiedstate as target.
     *
     * @param target The {@link DataObjectState} for the {@link DataObject}.
     *               <p/>
     *               Pre: {@link #incomingDataFlow} must be initalized
     */
    private void addOutgoingDataFlow(DataObjectState target) {
        DataFlow dataFlow = new DataFlow(activity,
                flyweight.getDataobjectForState(target));
        outgoingDataFlow.add(dataFlow);
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
        DataFlow dataFlow = new DataFlow(flyweight.getDataobjectForState(source),
                activity);
        incomingDataFlow.add(dataFlow);
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
        // Pre-Condition: Variables have to be set
        assert flyweight != null : "The flyweight has not been set";
        assert outgoingDataFlow != null : "The DataFlow has to be set";
        assert availableStates != null : "The availableStates have to be set";
        for (CombinedTransition ct : flyweight.getCombinedTransitions()) {
            if (activityEnablesCombinedTransition(ct)) {
                enabledCTs.add(ct);
            }
            enabledCTs.add(ct);
        }
        return this;
    }

    /**
     * This method determines if a combined transition will enabled by the
     * activity.
     * This means all {@link DataObjectState} which have to be available at
     * the start of the combined transition are written by the activity
     * represented by this activity builder.
     * <p/>
     * Therefore it determines all possible successors of the combinedTransition
     * which will be
     *
     * @param ct The combined transition to be checked.
     * @return True if it will be enabled else false.
     * <p/>
     * Pre: {@link #ctExecuted} has to be enabled.
     */
    private boolean activityEnablesCombinedTransition(CombinedTransition ct) {
        Collection<StateTransition> enabledTransitions = getEnabledTransitions(
                statesAvailableAfterTermination());
        for (StateTransition transition : enabledTransitions) {
            Collection<StateTransition> followingTransition =
                    ctExecuted.getTransitions();
            followingTransition.retainAll(transition
                    .getSource()
                    .getOutgoingEdgesOfType(StateTransition.class));
            if (followingTransition.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private Collection<StateTransition> getEnabledTransitions(
            Collection<DataObjectState> enabledStates) {
        Collection<StateTransition> enabledTransitions = new LinkedList<>();
        for (DataObjectState state : enabledStates) {
            enabledTransitions.addAll((Collection<? extends StateTransition>)
                    state.getIncomingEdgesOfType(StateTransition.class));
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
     * Initalizes the No Operation activities for the activity.
     * The NOP Activities will automatically marked as checked.
     * Also an empty combined transition will be added.
     * All NOP Activities are saved centralized inside the flyweight
     * to prevent multiple NOP Activities for the same DataObjectState.
     */
    private void initalizeNOPActivities() {
        nopActivities = new HashSet<>();
        for (StateTransition transition : ctExecuted.getTransitions()) {
            if (null != flyweight
                    .getNOPActivityForState(
                            (DataObjectState) transition.getTarget())) {
                nopActivities.add(flyweight
                        .getNOPActivityForState(
                                (DataObjectState) transition.getTarget()));
            }
        }
    }

    /**
     * This method builds the activity, represented by this builder.
     * ControlFlow will be established.
     *
     * @return The newly created and initialized activity.
     */
    public Activity build() {
        return null;
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
}