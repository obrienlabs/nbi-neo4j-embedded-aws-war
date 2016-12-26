package org.obrienlabs.nbi.graph.service;

import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;
import org.neo4j.kernel.ha.cluster.HighAvailabilityMemberChangeEvent;
import org.neo4j.kernel.ha.cluster.HighAvailabilityMemberListener;

public class HaMonitor implements HighAvailabilityMemberListener {

    protected HighlyAvailableGraphDatabase db;
    
    public void setDb(HighlyAvailableGraphDatabase aInDb) {
        db = aInDb;
    }
    
	@Override
	public void masterIsElected(HighAvailabilityMemberChangeEvent event) {
		System.out.println("MasterIsElected");
	}

	@Override
	public void masterIsAvailable(HighAvailabilityMemberChangeEvent event) {
		System.out.println("masterIsAvailable");
	}

	@Override
	public void slaveIsAvailable(HighAvailabilityMemberChangeEvent event) {
		System.out.println("slaveIsAvailable");
	}

	@Override
	public void instanceStops(HighAvailabilityMemberChangeEvent event) {
		System.out.println("instanceStops");
	}

	@Override
	public void instanceDetached(HighAvailabilityMemberChangeEvent event) {
		System.out.println("instanceDetached");
	}
}
