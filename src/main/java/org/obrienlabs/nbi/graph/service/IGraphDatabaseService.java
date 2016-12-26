package org.obrienlabs.nbi.graph.service;

import org.neo4j.graphdb.Node;

public interface IGraphDatabaseService {
	
	Node createNode();
	Node getNodeById( long id);
	//void shutdown();

}
