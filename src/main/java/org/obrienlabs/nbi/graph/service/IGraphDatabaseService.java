/*
 * Michael O'Brien (2017)  Overly Enthusiastic - Science | Software | Hardware | Experimentation
 * michael at obrienlabs.org
 * https://github.com/obrienlabs
 * https://twitter.com/_mikeobrien
 * http://eclipsejpa.blogspot.ca/
 */
package org.obrienlabs.nbi.graph.service;

import org.neo4j.graphdb.Node;

public interface IGraphDatabaseService {
	
	Node createNode();
	Node getNodeById( long id);
	//void shutdown();

}
