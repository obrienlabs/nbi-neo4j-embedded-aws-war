package org.obrienlabs.nbi.graph.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional; 

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactoryState;
import org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory;
import org.neo4j.helpers.collection.Pair;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;
import org.neo4j.kernel.ha.cluster.HighAvailabilityMemberStateMachine;
import org.neo4j.server.enterprise.EnterpriseBootstrapper;

// http://localhost:8080/nbi-neo4j/FrontController?action=graph
//@Named("WrappedEnterpriseBoootstrapper")
public class ExtendedHighlyAvailableGraphDatabaseFactory extends HighlyAvailableGraphDatabaseFactory {
	private Log log = LogFactory.getLog(ExtendedHighlyAvailableGraphDatabaseFactory.class);
    private HaMonitor haMonitor;

    @Override
     protected GraphDatabaseBuilder.DatabaseCreator createDatabaseCreator(
                final File storeDir, final GraphDatabaseFactoryState state)  {
            return new GraphDatabaseBuilder.DatabaseCreator() {
                @Override
                public GraphDatabaseService newDatabase( final Map<String, String> config ) {
                    EnterpriseBootstrapper neoServer = new EnterpriseBootstrapper();
                    // convert all config (spring, conf, code) to vararg Pairs
                	List<Pair<String, String>> pairs = new ArrayList<>();
                	for(Entry<String, String> entry : config.entrySet()) {
                		pairs.add(Pair.of(entry.getKey(), entry.getValue()));
                	} 	
                    Pair<String, String> pairArray[] = new Pair[pairs.size()];
                    // will resolve to /dir/data/databases/graph.db 
                    neoServer.start(storeDir, Optional.empty(), pairs.toArray(pairArray));
                    GraphDatabaseService graph = neoServer.getServer().getDatabase().getGraph(); 
                    // set the paxos HA listener only when dbms.mode=HA
                    if(graph instanceof HighlyAvailableGraphDatabase) {
                    	haMonitor.setDb((HighlyAvailableGraphDatabase) graph);
                    	HighAvailabilityMemberStateMachine memberStateMachine = 
                    			((HighlyAvailableGraphDatabase)graph).getDependencyResolver()
                    				.resolveDependency(HighAvailabilityMemberStateMachine.class);
                    	if ( memberStateMachine != null ) {
                    		memberStateMachine.addHighAvailabilityMemberListener(haMonitor);
                    		log.info("register: " +  haMonitor);
                    	}
                    }
                    return graph;
                } };
    }
    
    public ExtendedHighlyAvailableGraphDatabaseFactory(HaMonitor aInHaMonitor)  {
        haMonitor = aInHaMonitor;
    }
}
