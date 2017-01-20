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
import org.neo4j.server.NeoServer;
import org.neo4j.server.database.Database;
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
                    GraphDatabaseService graph = null;
                    // convert all config (spring, conf, code) to vararg Pairs
                	List<Pair<String, String>> pairs = new ArrayList<>();
                	for(Entry<String, String> entry : config.entrySet()) {
                		pairs.add(Pair.of(entry.getKey(), entry.getValue()));
                	} 	
                    Pair<String, String> pairArray[] = new Pair[pairs.size()];
                    
                    // will resolve to /dir/data/databases/graph.db 
                    int state = neoServer.start(storeDir, Optional.empty(), pairs.toArray(pairArray)); 
                    // state is 0 for success, 1 will mean a null server
                    if(state > 0) {
                    	log.error("return sate of NeoServer.start(); is 1 - no GraphDatabase available - check config settings");
                    }
                    // will be null if state == 1
                    NeoServer server = neoServer.getServer();
                    if(null != server) {
                    	Database database = server.getDatabase();
                    	if(null != database) {
                    		graph = database.getGraph(); 
                    		// set the paxos HA listener only when dbms.mode=HA
                    		// Note: initial TO_MASTER callback during server.start above is missed
                    		if(graph instanceof HighlyAvailableGraphDatabase) {
                    			HighlyAvailableGraphDatabase haGraph = (HighlyAvailableGraphDatabase) graph;
                    			haMonitor.setDb(haGraph);
                    			HighAvailabilityMemberStateMachine memberStateMachine = 
                    					(haGraph).getDependencyResolver()
                    					.resolveDependency(HighAvailabilityMemberStateMachine.class);
                    			if ( memberStateMachine != null ) {
                    				memberStateMachine.addHighAvailabilityMemberListener(haMonitor);
                    				System.out.println("register: " +  haMonitor);
                    				// rethrow isMaster callback from start
                    				//haMonitor.getMasterListenerManager().masterChanged(haGraph.isMaster());
                    				System.out.println("isMaster: " + haGraph.isMaster());
                    			}
                    		}
                    	} else {
                    		log.error("database null : check your http configuration settings");
                    	}
                    } else {
                    	log.error("server null : check your http configuration settings");
                    }
                    return graph;
                } };
    }
    
    public ExtendedHighlyAvailableGraphDatabaseFactory(HaMonitor aInHaMonitor)  {
        haMonitor = aInHaMonitor;
    }
}
