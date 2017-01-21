/*
 * Michael O'Brien (2017)  Overly Enthusiastic - Science | Software | Hardware | Experimentation
 * michael at obrienlabs.org
 * https://github.com/obrienlabs
 * https://twitter.com/_mikeobrien
 * http://eclipsejpa.blogspot.ca/
 */
package org.obrienlabs.nbi.graph.service;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.graphdb.Node; 
import org.springframework.stereotype.Service;

//@TransactionConfiguration( transactionManager = "transactionManager", defaultRollback = true)
@Service("daoFacade")
//@Repository(value="daoFacade")
//@Component
//@Transactional // will get a javax.persistence.TransactionRequiredException: No transactional EntityManager available without it
public class ApplicationService implements ApplicationServiceLocal {
	
	//private @Value("${server}") String server; // aws only
	private Log log = LogFactory.getLog(ApplicationService.class);

	@Inject
	private IGraphDatabaseService graphDatabaseService;

	// url
	// http://localhost:8080/biometric-nbi/FrontController?action=graph
	@Override
	public String getGraph() {
			Node node  = graphDatabaseService.createNode();
		    return node.toString() + "+" + node.getId() + ":" + node.hashCode();
	}
	
    @Override
    public Boolean health() {
    	Boolean health = true;
    	// TODO: check database
    	return health;
    }

}
