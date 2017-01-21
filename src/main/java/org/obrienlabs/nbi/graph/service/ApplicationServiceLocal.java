/*
 * Michael O'Brien (2017)  Overly Enthusiastic - Science | Software | Hardware | Experimentation
 * michael at obrienlabs.org
 * https://github.com/obrienlabs
 * https://twitter.com/_mikeobrien
 * http://eclipsejpa.blogspot.ca/
 */
package org.obrienlabs.nbi.graph.service;

import java.util.List;

public interface ApplicationServiceLocal {
	
	// neo4j 3.1.0
	String getGraph();
	
	Boolean health();
}
