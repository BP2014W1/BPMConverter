package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.IModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.*;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize.CombinedTransition;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize.SynchronizedObjectLifeCycle;

import java.util.*;

/**
 * This is a Flyweight for objects involved in the transformation of sOLCs.
 * It holds shared objects. For example the (@link #combinedTransitions),
 * model currently under construction, created nodes and more.
 * Be aware that this resources are not thread safe.
 * To reduce manipulation by other classes, collections are implemented
 * as compositions instead of aggregation. Means every time a collection is
 * requested a new one will be created, containing the same objects as the
 * internal one.
 * In addition all Elements will be initialized calling the Constructor.
 *
 * @param <T> Describes the type of the model to be created.
 *            It must be a concrete implementation of IModel.
 *            Abstract classes and Interfaces are not allowed.
 */
public class OLCConversionFlyweight<T extends IModel> {
    /**
     * The model currently under Construction.
     * You may request the model in order to add Nodes.
     */
    private T modelUnderConstruction;
    /**
     * A Collection containing all combined Transition of the
     * synchronized object life cycle. It will be initialized
     * using the synchronized object life cycle provided to the
     * constructor.
     */
    private Collection<CombinedTransition> combinedTransitions;

    /**
     * This method is the synchronized Object Life Cycle to be transformed.
     * It is necessary in order to generate resources e.g.
     * {@link #combinedTransitions} see {@link #initCombinedTransitions()}
     * for an example.
     * It may also be used for the transformation.
     */
    private SynchronizedObjectLifeCycle sOLC;

    /**
     * This map saves {@link DataObject}[s] for each {@link DataObjectState}.
     * Therefore it is impossible two have two identical Data Objects.
     */
    private Map<DataObjectState, DataObject> dataObjectsPerState;

    /**
     * This Map saves for each final data state NOP activities.
     * They will be represented by a {@link Activity} Object.
     */
    private Map<DataObjectState, Activity> nopActivitiesForFinalStates;
    /**
     * NOP activities will only be created once for each final state.
     * Thus, they can have more than one incoming edge.
     * Because {@link Activity} does not allow to have multiple edges
     * they will be added to this map to determine preceeding joins/merges.
     */
    private Map<Activity, Collection<ControlFlow>> incomingEdgesOfNOP;
    /**
     * For each Combined transition of a Synchronized Object Life Cycle
     * an Activity Builder will be created. Those are unique. Hence,
     * they will be saved as a part of the *global* state.
     */
    private Map<CombinedTransition, ActivityBuilder> builderPerCombinedTransition;

    /**
     * Constructs a new Flyweight object for the given
     * {@link SynchronizedObjectLifeCycle}.
     * A new model described by the second parameter will be created.
     * Be aware that the class must not be an interface or an abstract
     * class. Hence you may use only concrete implementations.
     * Both parameters must not be null.
     *
     * @param sOLC       This is the synchronized Object Life Cycle.
     *                   It must not be null. If the object is changed
     *                   at a later point of time, this changes will
     *                   put the Flyweight into a corrupt state.
     * @param modelClass This is the class of the model to be constructed.
     *                   This parameter must not be null and not an abstract
     *                   class or an interface.
     * @throws IllegalAccessException Constructor can not be called from here.
     * @throws InstantiationException ModelClass could not be instantiated.
     *                                <p/>
     *                                Pre: * Both parameter must not be null.
     *                                * The modelClass parameter must be a concrete Implementation.
     */
    public OLCConversionFlyweight(SynchronizedObjectLifeCycle sOLC,
                                  Class<T> modelClass)
            throws IllegalAccessException, InstantiationException {
        // Pre-Conditions: Not null
        assert null != sOLC : "The synchronized OLC must not be null";
        assert null != modelClass : "The given class must not be null";

        modelUnderConstruction = modelClass.newInstance();
        builderPerCombinedTransition = new HashMap<>();
        this.sOLC = sOLC;
        init();
    }

    /**
     * This method initializes some fields.
     * All attributes which can be determined based on the synchronized
     * Object Life Cycle and the model Class will be initialized.
     * Except for the {@link #modelUnderConstruction} which will be set
     * from within he constructor.
     */
    private void init() {
        initCombinedTransitions();
        initDataObjects();
        initNOPActivities();
    }

    /**
     * This methods initializes {@link #nopActivitiesForFinalStates}.
     * All final states of all OLC being part of the synchronized OLC
     * will extracted and an Activity will be created.
     */
    private void initNOPActivities() {
        nopActivitiesForFinalStates = new HashMap<>();
        incomingEdgesOfNOP = new HashMap<>();
        for (ObjectLifeCycle olc : sOLC.getOLCs()) {
            for (INode state :
                    olc.getFinalNodesOfClass(DataObjectState.class)) {
                addNOPActivityForState((DataObjectState) state);
            }
        }
    }

    /**
     * Creates a NOP Activity for a final state.
     * The label will be "NOP : state".
     * The activity will added to the map {@link #nopActivitiesForFinalStates}.
     *
     * @param state The final state for the NOP.
     */
    private void addNOPActivityForState(DataObjectState state) {
        Activity nop = new Activity("NOP : " + state.getName());
        nopActivitiesForFinalStates.put(state, nop);
        incomingEdgesOfNOP.put(nop, new ArrayList<ControlFlow>());
    }

    /**
     * This method initializes the {@link DataObject}.
     * They will saved inside a Map, which combines each
     * data object with its {@link DataObjectState}
     * The method iterates over all OLCs inside the synchronized
     * OLCs and creates a new entry for each state.
     */
    private void initDataObjects() {
        dataObjectsPerState = new HashMap<>();
        for (ObjectLifeCycle olc : sOLC.getOLCs()) {
            for (INode state : olc.getNodesOfClass(DataObjectState.class)) {
                DataObject newObject = new DataObject(olc.getLabel(),
                        (DataObjectState) state);
                newObject.setOlc(olc);
                dataObjectsPerState.put((DataObjectState) state, newObject);
            }
        }
    }

    /**
     * This method initialize the combined transitions.
     * Therefor all Object Life Cycles which are part of the sOLC will
     * be examined. Transitions of different OLC with the same label
     * will be group.
     * In future versions the synchronized Edges should be used.
     * TODO: Use synchronized transitions instead
     */
    private void initCombinedTransitions() {
        combinedTransitions = new ArrayList<>();
        for (ObjectLifeCycle olc : sOLC.getOLCs()) {
            for (IEdge transition : olc.getEdgeOfType(StateTransition.class)) {
                boolean hasBeenAdded = false;
                for (CombinedTransition ct : combinedTransitions) {
                    if (hasBeenAdded =
                            ct.isCombinedTransition((StateTransition) transition)) {
                        ct.addTransitionAndOLC((StateTransition) transition,
                                olc);
                        break;
                    }
                }
                if (!hasBeenAdded) {
                    combinedTransitions.add(
                            new CombinedTransition((StateTransition) transition,
                                    olc));
                }
            }
        }
    }

    /**
     * This method finalizes the Model.
     * This means an end Event will be added and the Gateways before
     * and After the NOP Activities will be established as well as Activities
     * for each ActivityBuilder.
     * In the end we may add a Gateway preceedign the end event.
     */
    public void finalizeModel() {
        Event endEvent = new Event();
        endEvent.setType(Event.Type.END);
        modelUnderConstruction.addNode(endEvent);
        modelUnderConstruction.addFinalNode(endEvent);
        Collection<Activity> finalActivities = new HashSet<>();
        for (Map.Entry<Activity, Collection<ControlFlow>> entry :
                incomingEdgesOfNOP.entrySet()) {
            if (entry.getValue().size() == 1) {
                entry.getKey().addIncomingEdge(
                        entry.getValue().iterator().next());
            } else {
                Gateway xor = new Gateway();
                xor.setType(Gateway.Type.XOR);
                for (ControlFlow flow : entry.getValue()) {
                    flow.setTarget(xor);
                    xor.addIncomingEdge(flow);
                }
                ControlFlow incoming = new ControlFlow(xor, entry.getKey());
                Collection<ControlFlow> incomingFlow = new HashSet<>();
                xor.addOutgoingEdge(incoming);
                incomingFlow.add(incoming);
                entry.setValue(incomingFlow);
                entry.getKey().addIncomingEdge(incoming);
                modelUnderConstruction.addNode(xor);
            }
        }
        for (ActivityBuilder activityBuilder : builderPerCombinedTransition.values()) {
            Activity activity = activityBuilder.build();
            finalActivities.addAll(activityBuilder.getNopActivities());
            if (activity.getOutgoingEdgesOfType(ControlFlow.class).isEmpty()) {
                finalActivities.add(activity);
            }
            modelUnderConstruction.addNode(
                    activity
            );
        }
        if (finalActivities.size() == 1) {
            Activity finalActivity = finalActivities.iterator().next();
            if (nopActivitiesForFinalStates.values().contains(finalActivity)) {
                IEdge cf = finalActivity.getIncomingEdgesOfType(ControlFlow.class)
                        .iterator().next();
                cf.setTarget(endEvent);
                endEvent.addIncomingEdge(cf);
            } else {
                ControlFlow cf = new ControlFlow(finalActivity, endEvent);
                finalActivity.addOutgoingEdge(cf);
                endEvent.addIncomingEdge(cf);
            }
        } else {
            Gateway xor = new Gateway();
            xor.setType(Gateway.Type.XOR);
            for (Activity finalActivity : finalActivities) {
                if (nopActivitiesForFinalStates.values().contains(finalActivity)) {
                    IEdge cf = finalActivity.getIncomingEdgesOfType(ControlFlow.class)
                            .iterator().next();
                    cf.setTarget(xor);
                    xor.addIncomingEdge(cf);
                } else {
                    ControlFlow cf = new ControlFlow(finalActivity, xor);
                    finalActivity.addOutgoingEdge(cf);
                    xor.addIncomingEdge(cf);
                }
            }
            ControlFlow cf = new ControlFlow(xor, endEvent);
            xor.addOutgoingEdge(cf);
            endEvent.addIncomingEdge(cf);
            modelUnderConstruction.addNode(xor);
        }
        for (DataObject dataObject : dataObjectsPerState.values()) {
            modelUnderConstruction.addNode(dataObject);
        }
    }

    // # ____GETTERS____

    /**
     * Returns the model which is currently under construction.
     * Changes to the modell will affect the state of the flywight,
     * hence they will be available to all objects using the it.
     *
     * @return The model being constructed.
     */
    public T getModelUnderConstruction() {
        return modelUnderConstruction;
    }

    /**
     * Returns a Collection of combined transitions.
     * The collection will be constructed for every call of the getter,
     * but the content will be same as the one of the flyweight.
     * Hence changes to the collection will not affect the flyweight but
     * changes to the elements will.
     *
     * @return A new collection with all combined transitions.
     */
    public Collection<CombinedTransition> getCombinedTransitions() {
        return new ArrayList<CombinedTransition>(combinedTransitions);
    }

    /**
     * Returns the synchronizedObjectLifeCycle which will be transformed.
     * Changes will affect the state of the flyweight and can result
     * in a corrupt state.
     *
     * @return The synchronized Object Life Cycle to be transformed.
     */
    public SynchronizedObjectLifeCycle getsOLC() {
        return sOLC;
    }

    /**
     * Returns a {@link DataObject} for a given {@link DataObjectState}.
     * This methods asserts that we will not create more than one DataObject
     * for each DataObjectState.
     * The {@link #dataObjectsPerState} have to be initialized first.
     *
     * @param state The State of the data object.
     *              <p/>
     *              Pre-Condition: {@link #dataObjectsPerState} must be initialized.
     */
    public DataObject getDataobjectForState(DataObjectState state) {
        assert dataObjectsPerState != null : "The data objects have to initialized";
        return dataObjectsPerState.get(state);
    }

    /**
     * Returns the NOP Activity for a given state
     *
     * @return The Activity representing the NOP null state is not final.
     */
    public Activity getNOPActivityForState(DataObjectState state) {
        return nopActivitiesForFinalStates.get(state);
    }

    /**
     * Returns a ActivityBuilder instance for a given combined transition.
     * Use this method to make sure not to create more than one ActivityBuilder
     * Object for each combined transition.
     *
     * @param ct     The combined transition which specifies the activityBuilder.
     * @param states The states available before the execution of the activity.
     * @return The ActivityBuilder instance which can be either new or reused.
     */
    public ActivityBuilder getActivityBuilderFor(
            CombinedTransition ct,
            Collection<DataObjectState> states) {
        if (builderPerCombinedTransition == null) {
            builderPerCombinedTransition = new HashMap<>();
        }
        if (builderPerCombinedTransition.get(ct) == null) {
            ActivityBuilder newActivity = new ActivityBuilder(
                    (OLCConversionFlyweight<ActivityCentricProcessModel>) this,
                    states,
                    ct);
            builderPerCombinedTransition.put(ct, newActivity);
        }
        return builderPerCombinedTransition.get(ct);
    }

    public void addIncomingEdgeFor(Activity nop, ControlFlow cf) {
        incomingEdgesOfNOP.get(nop).add(cf);
    }

    public Collection<ActivityBuilder> getActivityBuilders() {
        return builderPerCombinedTransition.values();
    }
}