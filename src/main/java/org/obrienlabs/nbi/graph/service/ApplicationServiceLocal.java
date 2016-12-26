package org.obrienlabs.nbi.graph.service;

import java.util.List;

public interface ApplicationServiceLocal {
	
	// neo4j 3.1.0
	String getGraph();
	
	Boolean health();
}
