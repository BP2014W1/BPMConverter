package de.uni_potsdam.hpi.bpt.bp2014.conversion.olc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Stpehan on 01.04.2015.
 */
public class SynchronizedObjectLifeCycle {
    private List<ObjectLifeCycle> objectLifeCycles;
    private Map<StateTransition, StateTransition> synchronisationEdges;

    public SynchronizedObjectLifeCycle() {
        objectLifeCycles = new ArrayList<>();
        synchronisationEdges = new HashMap<>();
    }


    public List<ObjectLifeCycle> getOLCs() {
        return objectLifeCycles;
    }
}
