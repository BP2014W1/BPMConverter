package de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IEdge;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.IModel;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.INode;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.activity_centric.DataObject;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.converter.CombinedTransition;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.DataObjectState;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.ObjectLifeCycle;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.StateTransition;
import de.uni_potsdam.hpi.bpt.bp2014.conversion.olc.synchronize.SynchronizedObjectLifeCycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
     *
     * Pre: * Both parameter must not be null.
     *      * The modelClass parameter must be a concrete Implementation.
     */
    public OLCConversionFlyweight(SynchronizedObjectLifeCycle sOLC,
                                   Class<T> modelClass)
            throws IllegalAccessException, InstantiationException {
        // Pre-Conditions: Not null
        assert null != sOLC : "The synchronized OLC must not be null";
        assert null != modelClass : "The given class must not be null";

        modelUnderConstruction = modelClass.newInstance();
        this.sOLC = sOLC;
        init();
    }

    /**
     * This method initalizes some fields.
     * All attributes which can be determined based on the synchronized
     * Object Life Cycle and the model Class will be initalized.
     * Except for the {@link #modelUnderConstruction} which will be set
     * from within he constructor.
     */
    private void init() {
        initCombinedTransitions();
        initDataObjects();
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
                        (DataObjectState)state);
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
        return new ArrayList<>(combinedTransitions);
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
     *
     * Pre-Condition: {@link #dataObjectsPerState} must be initialized.
     */
    public DataObject getDataobjectForState(DataObjectState state) {
        assert dataObjectsPerState != null : "The data objects have to initialized";
        return dataObjectsPerState.get(state);
    }
}