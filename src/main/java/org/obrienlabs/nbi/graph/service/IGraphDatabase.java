package org.obrienlabs.nbi.graph.service;

import org.neo4j.graphdb.GraphDatabaseService;

public interface IGraphDatabase {
	GraphDatabaseService getDB();
	//void start();
	//void stop();

}
